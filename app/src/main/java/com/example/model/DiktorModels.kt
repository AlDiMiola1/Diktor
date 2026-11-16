package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class MainTab(val title: String) {
    TRAININGS("Тренировки"),
    STUDIO("Студия и ИИ"),
    DICTION("Дикция и темп"),
    PROGRESS("Прогресс")
}

enum class ExerciseModule(val key: String, val displayName: String, val filterLabel: String) {
    ALL("all", "Все (32)", "Все (32)"),
    CLAMPS("clamps", "1. Зажимы", "1. Зажимы (6)"),
    BREATH("breath", "2. Дыхание", "2. Дыхание (6)"),
    RESONATORS("resonators", "3. Резонаторы и штробас", "3. Резонаторы и штробас (8)"),
    ARTICULATION("articulation", "4. Артикуляция", "4. Артикуляция (6)"),
    INTONATION("intonation", "5. Интонация", "5. Интонация (6)")
}

enum class ExerciseVizType {
    HIDDEN_YAWN,
    HORSE_LIPS,
    JAW_RELEASE,
    NECK_PENDULUM,
    TONGUE_ROOT_DROP,
    RAGDOLL_SHOULDERS,
    CANDLE_BREATH,
    PUMP_STRELNIKOVA,
    DOG_BREATH,
    EGORKI_33,
    BOX_BREATH,
    STEPPED_EXHALE,
    TARZAN_CHEST,
    CRY_SIGH,
    MASK_FOCUS,
    VOCAL_FRY,
    VOCAL_FRY_MORNING,
    VOCAL_FRY_GLISSANDO,
    VOCAL_FRY_SYLLABLES,
    VOCAL_FRY_PHRASES,
    CORK_TRAINING,
    TONGUE_CLEANING,
    LIPS_TRUMPET_SMILE,
    SYLLABLE_MATRICES,
    TONGUE_CLICKS,
    EXPLOSIVE_CONSONANTS,
    INTONATION_SWING,
    SEMANTIC_MAGNET,
    MARKUP_READING,
    SENTENCE_DROP,
    TEMPO_CONTRAST,
    DRAMATIC_PAUSE
}

data class Exercise(
    val id: String,
    val num: Int,
    val module: ExerciseModule,
    val title: String,
    val shortDesc: String,
    val durationLabel: String,
    val vizType: ExerciseVizType,
    val defaultDurationSec: Int = 45,
    val isFry: Boolean = false,
    val steps: List<String>
)

data class CourseStep(
    val exId: String? = null,
    val durationSec: Int = 45,
    val isGoStudioAction: Boolean = false
)

data class CourseDayTemplate(
    val title: String,
    val focus: String,
    val approxMin: Int,
    val steps: List<CourseStep>
)

data class CourseProgram(
    val id: String,
    val dayNumber: Int,
    val dayInCycle: Int,
    val totalInCycle: Int,
    val cycleNum: Int,
    val title: String,
    val shortTitle: String,
    val focus: String,
    val approxMin: Int,
    val steps: List<CourseStep>
)

data class TeleprompterScript(
    val id: String,
    val genre: String,
    val genreLabel: String,
    val title: String,
    val isCustom: Boolean,
    val text: String
)

data class TongueTwister(
    val id: String,
    val soundTag: String,
    val difficulty: String,
    val isCustom: Boolean,
    val text: String
)

data class DictionTable(
    val id: String,
    val title: String,
    val subtitle: String,
    val syllables: List<String>,
    val isCustom: Boolean = false
)

data class AcousticStats(
    val durationSec: Double,
    val avgRms: Double,
    val maxRms: Double,
    val pauseRatio: Double,
    val tailRms: Double
)

data class AiVerdict(
    val id: String = "",
    val dateLabel: String = "",
    val scriptTitle: String = "",
    val modeLabel: String = "",
    val headline: String = "",
    val totalScore: Int = 8,
    val chestScore: Int = 8,
    val supportScore: Int = 8,
    val pauseScore: Int = 8,
    val dictionScore: Int = 8,
    val critique: String = "",
    val advice: String = "",
    val recommendedExId: String = "tarzan_chest"
)

// --- Firestore Data Models ---

@IgnoreExtraProperties
data class UserProgressDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val completedExercises: List<String> = emptyList(),
    val totalExerciseRuns: Long = 0L,
    val completedPrograms: Long = 0L,
    val egorkiBestCount: Long = 0L,
    val streakDays: Long = 0L,
    val courseDayNumber: Long = 1L,
    val completedDates: List<String> = emptyList(),
    val lastCompletedCourseDate: String = "",
    val lastAdvancedDate: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CustomScriptDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val genre: String = "custom",
    val genreLabel: String = "Мой сценарий",
    val title: String = "",
    val text: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CustomTwisterDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val soundTag: String = "Р / Л",
    val difficulty: String = "Базовая",
    val text: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CustomDictionTableDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val syllables: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AiEvaluationDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateLabel: String = "",
    val scriptTitle: String = "",
    val modeLabel: String = "",
    val headline: String = "",
    val totalScore: Long = 8L,
    val chestScore: Long = 8L,
    val supportScore: Long = 8L,
    val pauseScore: Long = 8L,
    val dictionScore: Long = 8L,
    val critique: String = "",
    val advice: String = "",
    val recommendedExId: String = "tarzan_chest",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
