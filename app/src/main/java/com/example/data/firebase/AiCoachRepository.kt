package com.example.data.firebase

import android.util.Log
import com.example.data.catalog.DiktorCatalog
import com.example.model.AcousticStats
import com.example.model.AiVerdict
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private const val TAG = "AiCoachRepository"

class AiCoachRepository {

    private val evaluationSchema = Schema.obj(
        properties = mapOf(
            "headline" to Schema.string(description = "Короткий заголовок вердикта до 5 слов на русском языке"),
            "totalScore" to Schema.integer(description = "Общая оценка от 1 до 10"),
            "chestScore" to Schema.integer(description = "Грудной резонатор от 1 до 10"),
            "supportScore" to Schema.integer(description = "Опора дыхания и концовки фраз от 1 до 10"),
            "pauseScore" to Schema.integer(description = "Темп и смысловые паузы от 1 до 10"),
            "dictionScore" to Schema.integer(description = "Четкость дикции от 1 до 10"),
            "critique" to Schema.string(description = "Главная зона роста, 1-2 конкретных предложения на русском языке"),
            "advice" to Schema.string(description = "Практическая рекомендация на следующий дубль, 1-2 предложения на русском языке"),
            "recommendedExId" to Schema.enumeration(
                values = listOf(
                    "hidden_yawn",
                    "horse_lips",
                    "candle_breath",
                    "tarzan_chest",
                    "mask_focus",
                    "cork_training",
                    "syllable_matrices",
                    "intonation_swing",
                    "semantic_magnet",
                    "sentence_drop"
                ),
                description = "ID рекомендованного упражнения из списка"
            )
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = evaluationSchema
            }
        )
    }

    suspend fun evaluateRecordingWithGemini(
        audioBytes: ByteArray,
        scriptTitle: String,
        scriptText: String
    ): AiVerdict {
        val dateLabel = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date())
        val promptText = """
            Ты опытный режиссер дубляжа и педагог по сценической речи.
            Прослушай эту аудиозапись диктора, который читает текст: "$scriptText".
            Оцени голос строго как дикторскую подачу (без упоминания децибел и герц) на русском языке.
        """.trimIndent()

        try {
            val response = withTimeout(60_000L) {
                model.generateContent(
                    content {
                        inlineData(audioBytes, "audio/wav")
                        text(promptText)
                    }
                )
            }
            val jsonString = response.text
                ?: throw IllegalStateException("ИИ-режиссер вернул пустой ответ.")

            val json = JSONObject(jsonString)
            val recId = json.optString("recommendedExId", "tarzan_chest")
            val validRecId = if (DiktorCatalog.EXERCISES.any { it.id == recId }) recId else "tarzan_chest"

            return AiVerdict(
                dateLabel = dateLabel,
                scriptTitle = scriptTitle,
                modeLabel = "Gemini AI Режиссер",
                headline = json.optString("headline", "Студийный разбор дубля").take(120),
                totalScore = json.optInt("totalScore", 8).coerceIn(1, 10),
                chestScore = json.optInt("chestScore", 8).coerceIn(1, 10),
                supportScore = json.optInt("supportScore", 7).coerceIn(1, 10),
                pauseScore = json.optInt("pauseScore", 8).coerceIn(1, 10),
                dictionScore = json.optInt("dictionScore", 8).coerceIn(1, 10),
                critique = json.optString("critique", "Обратите внимание на глубину грудного резонатора в начале фраз.").take(1000),
                advice = json.optString("advice", "Сделайте скрытый зевок перед записью и не зажимайте нижнюю челюсть.").take(1000),
                recommendedExId = validRecId
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini audio evaluation timed out", e)
            throw IllegalStateException("Превышено время ожидания ИИ-режиссера (60с). Проверьте сеть и попробуйте снова.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI audio evaluation failed", e)
            throw e
        }
    }

    fun buildLocalAcousticVerdict(stats: AcousticStats, scriptTitle: String): AiVerdict {
        val dateLabel = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date())
        val chestScore = (6 + stats.avgRms * 28.0).roundToInt().coerceIn(5, 10)
        val supportScore = if (stats.tailRms < stats.avgRms * 0.55) 6 else 8
        val pauseScore = if (stats.pauseRatio in 0.14..0.42) 9 else 6
        val dictionScore = if (stats.maxRms > 0.12) 8 else 7
        val total = ((chestScore + supportScore + pauseScore + dictionScore) / 4.0).roundToInt().coerceIn(1, 10)

        var critique = "В концовках длинных предложений немного падает воздушная опора — последние слова звучат тише середины фразы."
        var advice = "Перед каждой глубокой паузой || добирайте воздух нижними ребрами и дотягивайте последнее ударное слово грудью ↘."
        var recExId = "tarzan_chest"

        if (stats.pauseRatio < 0.14) {
            critique = "Слишком плотный темп: короткие паузы | и глубокие вдохи || пролетают без остановки."
            advice = "Делайте честную остановку на 0.5–1 секунду на знаках | и ||, чтобы слушатель успевал осмыслить фразу."
            recExId = "semantic_magnet"
        } else if (stats.avgRms < 0.04) {
            critique = "Звук идет преимущественно на связках без активного включения грудного резонатора."
            advice = "Опустите гортань скрытым зевком и разогрейте грудную клетку мычанием перед следующим дублем."
            recExId = "hidden_yawn"
        } else if (supportScore <= 6) {
            recExId = "candle_breath"
        }

        return AiVerdict(
            dateLabel = dateLabel,
            scriptTitle = scriptTitle,
            modeLabel = "Акустический разбор",
            headline = if (total >= 8) "Уверенная студийная подача" else "Хорошая база — усиливаем опору",
            totalScore = total,
            chestScore = chestScore,
            supportScore = supportScore,
            pauseScore = pauseScore,
            dictionScore = dictionScore,
            critique = critique,
            advice = advice,
            recommendedExId = recExId
        )
    }
}
