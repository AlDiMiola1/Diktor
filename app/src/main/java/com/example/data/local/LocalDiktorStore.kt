package com.example.data.local

import android.content.Context
import com.example.model.AiEvaluationDoc
import com.example.model.DictionTable
import com.example.model.TeleprompterScript
import com.example.model.TongueTwister
import com.example.model.UserProgressDoc
import org.json.JSONArray
import org.json.JSONObject

private const val PREFS_NAME = "diktor_local_prefs"
private const val KEY_PROGRESS = "progress_json"
private const val KEY_SCRIPTS = "scripts_json"
private const val KEY_TWISTERS = "twisters_json"
private const val KEY_TABLES = "tables_json"
private const val KEY_AI_HISTORY = "ai_history_json"

class LocalDiktorStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadProgress(): UserProgressDoc {
        val raw = prefs.getString(KEY_PROGRESS, null) ?: return UserProgressDoc(id = "local", userId = "local")
        return try {
            val obj = JSONObject(raw)
            UserProgressDoc(
                id = "local",
                userId = "local",
                completedExercises = obj.optJSONArray("completedExercises").toStringList(),
                totalExerciseRuns = obj.optLong("totalExerciseRuns", 0L),
                completedPrograms = obj.optLong("completedPrograms", 0L),
                egorkiBestCount = obj.optLong("egorkiBestCount", 0L),
                streakDays = obj.optLong("streakDays", 0L),
                courseDayNumber = obj.optLong("courseDayNumber", 1L),
                completedDates = obj.optJSONArray("completedDates").toStringList(),
                lastCompletedCourseDate = obj.optString("lastCompletedCourseDate", ""),
                lastAdvancedDate = obj.optString("lastAdvancedDate", "")
            )
        } catch (_: Exception) {
            UserProgressDoc(id = "local", userId = "local")
        }
    }

    fun saveProgress(doc: UserProgressDoc) {
        val obj = JSONObject().apply {
            put("completedExercises", JSONArray(doc.completedExercises))
            put("totalExerciseRuns", doc.totalExerciseRuns)
            put("completedPrograms", doc.completedPrograms)
            put("egorkiBestCount", doc.egorkiBestCount)
            put("streakDays", doc.streakDays)
            put("courseDayNumber", doc.courseDayNumber)
            put("completedDates", JSONArray(doc.completedDates))
            put("lastCompletedCourseDate", doc.lastCompletedCourseDate)
            put("lastAdvancedDate", doc.lastAdvancedDate)
        }
        prefs.edit().putString(KEY_PROGRESS, obj.toString()).apply()
    }

    fun loadCustomScripts(): List<TeleprompterScript> {
        val raw = prefs.getString(KEY_SCRIPTS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.optJSONObject(i) ?: return@mapNotNull null
                TeleprompterScript(
                    id = obj.optString("id"),
                    genre = obj.optString("genre", "custom"),
                    genreLabel = obj.optString("genreLabel", "Мой сценарий"),
                    title = obj.optString("title"),
                    isCustom = true,
                    text = obj.optString("text")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveCustomScripts(list: List<TeleprompterScript>) {
        val arr = JSONArray()
        list.forEach { sc ->
            arr.put(JSONObject().apply {
                put("id", sc.id)
                put("genre", sc.genre)
                put("genreLabel", sc.genreLabel)
                put("title", sc.title)
                put("text", sc.text)
            })
        }
        prefs.edit().putString(KEY_SCRIPTS, arr.toString()).apply()
    }

    fun loadCustomTwisters(): List<TongueTwister> {
        val raw = prefs.getString(KEY_TWISTERS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.optJSONObject(i) ?: return@mapNotNull null
                TongueTwister(
                    id = obj.optString("id"),
                    soundTag = obj.optString("soundTag", "Р / Л"),
                    difficulty = obj.optString("difficulty", "Базовая"),
                    isCustom = true,
                    text = obj.optString("text")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveCustomTwisters(list: List<TongueTwister>) {
        val arr = JSONArray()
        list.forEach { tw ->
            arr.put(JSONObject().apply {
                put("id", tw.id)
                put("soundTag", tw.soundTag)
                put("difficulty", tw.difficulty)
                put("text", tw.text)
            })
        }
        prefs.edit().putString(KEY_TWISTERS, arr.toString()).apply()
    }

    fun loadCustomDictionTables(): List<DictionTable> {
        val raw = prefs.getString(KEY_TABLES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.optJSONObject(i) ?: return@mapNotNull null
                val title = obj.optString("title")
                val syllables = obj.optJSONArray("syllables").toStringList()
                DictionTable(
                    id = obj.optString("id"),
                    title = title,
                    subtitle = "$title (${syllables.size} слогов)",
                    syllables = syllables,
                    isCustom = true
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveCustomDictionTables(list: List<DictionTable>) {
        val arr = JSONArray()
        list.forEach { tb ->
            arr.put(JSONObject().apply {
                put("id", tb.id)
                put("title", tb.title)
                put("syllables", JSONArray(tb.syllables))
            })
        }
        prefs.edit().putString(KEY_TABLES, arr.toString()).apply()
    }

    fun loadAiHistory(): List<AiEvaluationDoc> {
        val raw = prefs.getString(KEY_AI_HISTORY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.optJSONObject(i) ?: return@mapNotNull null
                AiEvaluationDoc(
                    id = obj.optString("id"),
                    userId = "local",
                    dateLabel = obj.optString("dateLabel"),
                    scriptTitle = obj.optString("scriptTitle"),
                    modeLabel = obj.optString("modeLabel"),
                    headline = obj.optString("headline"),
                    totalScore = obj.optLong("totalScore", 8L),
                    chestScore = obj.optLong("chestScore", 8L),
                    supportScore = obj.optLong("supportScore", 8L),
                    pauseScore = obj.optLong("pauseScore", 8L),
                    dictionScore = obj.optLong("dictionScore", 8L),
                    critique = obj.optString("critique"),
                    advice = obj.optString("advice"),
                    recommendedExId = obj.optString("recommendedExId", "tarzan_chest")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveAiHistory(list: List<AiEvaluationDoc>) {
        val arr = JSONArray()
        list.take(20).forEach { ev ->
            arr.put(JSONObject().apply {
                put("id", ev.id)
                put("dateLabel", ev.dateLabel)
                put("scriptTitle", ev.scriptTitle)
                put("modeLabel", ev.modeLabel)
                put("headline", ev.headline)
                put("totalScore", ev.totalScore)
                put("chestScore", ev.chestScore)
                put("supportScore", ev.supportScore)
                put("pauseScore", ev.pauseScore)
                put("dictionScore", ev.dictionScore)
                put("critique", ev.critique)
                put("advice", ev.advice)
                put("recommendedExId", ev.recommendedExId)
            })
        }
        prefs.edit().putString(KEY_AI_HISTORY, arr.toString()).apply()
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).map { optString(it) }.filter { it.isNotEmpty() }
    }
}
