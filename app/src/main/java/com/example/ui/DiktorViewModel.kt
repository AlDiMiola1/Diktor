package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.RecordingChannel
import com.example.audio.StudioAudioRecorder
import com.example.audio.StudioSoundEngine
import com.example.data.catalog.DiktorCatalog
import com.example.data.firebase.AiCoachRepository
import com.example.data.firebase.DiktorRepository
import com.example.data.local.LocalDiktorStore
import com.example.model.AiEvaluationDoc
import com.example.model.AiVerdict
import com.example.model.CourseProgram
import com.example.model.CustomDictionTableDoc
import com.example.model.CustomScriptDoc
import com.example.model.CustomTwisterDoc
import com.example.model.DictionTable
import com.example.model.Exercise
import com.example.model.ExerciseModule
import com.example.model.MainTab
import com.example.model.TeleprompterScript
import com.example.model.TongueTwister
import com.example.model.UserProgressDoc
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

private const val TAG = "DiktorViewModel"

data class ActiveProgramState(
    val program: CourseProgram,
    val stepIndex: Int
)

data class ActiveExerciseModalState(
    val exercise: Exercise,
    val durationSec: Int
)

sealed interface AiCoachUiState {
    data object Idle : AiCoachUiState
    data class Loading(val label: String) : AiCoachUiState
    data class Success(val verdict: AiVerdict) : AiCoachUiState
    data class Error(val message: String) : AiCoachUiState
}

class DiktorViewModel(
    application: Application,
    val userId: String?,
    private val repository: DiktorRepository? = if (userId != null) DiktorRepository(application) else null,
    private val localStore: LocalDiktorStore = LocalDiktorStore(application),
    private val aiCoachRepository: AiCoachRepository = AiCoachRepository()
) : AndroidViewModel(application) {

    val soundEngine = StudioSoundEngine()
    val audioRecorder = StudioAudioRecorder(application.applicationContext)

    private val _toastMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toastMessages: SharedFlow<String> = _toastMessages.asSharedFlow()

    private val _currentTab = MutableStateFlow(MainTab.TRAININGS)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _isHelpOpen = MutableStateFlow(false)
    val isHelpOpen: StateFlow<Boolean> = _isHelpOpen.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _progressDoc = MutableStateFlow(
        UserProgressDoc(id = userId ?: "local", userId = userId ?: "local")
    )
    val progress: StateFlow<UserProgressDoc> = _progressDoc.asStateFlow()

    private val _localCustomScripts = MutableStateFlow(
        if (userId == null) localStore.loadCustomScripts() else emptyList()
    )
    private val _localCustomTwisters = MutableStateFlow(
        if (userId == null) localStore.loadCustomTwisters() else emptyList()
    )
    private val _localCustomDictionTables = MutableStateFlow(
        if (userId == null) localStore.loadCustomDictionTables() else emptyList()
    )
    private val _localAiHistory = MutableStateFlow(
        if (userId == null) localStore.loadAiHistory() else emptyList()
    )

    val customScripts: StateFlow<List<TeleprompterScript>> = if (userId != null && repository != null) {
        repository.observeCustomScripts(userId)
            .map { list ->
                list.map {
                    TeleprompterScript(
                        id = it.id,
                        genre = it.genre,
                        genreLabel = it.genreLabel,
                        title = it.title,
                        isCustom = true,
                        text = it.text
                    )
                }
            }
            .catch { e -> handleStreamError(e) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    } else {
        _localCustomScripts.asStateFlow()
    }

    val customTwisters: StateFlow<List<TongueTwister>> = if (userId != null && repository != null) {
        repository.observeCustomTwisters(userId)
            .map { list ->
                list.map {
                    TongueTwister(
                        id = it.id,
                        soundTag = it.soundTag,
                        difficulty = it.difficulty,
                        isCustom = true,
                        text = it.text
                    )
                }
            }
            .catch { e -> handleStreamError(e) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    } else {
        _localCustomTwisters.asStateFlow()
    }

    val customDictionTables: StateFlow<List<DictionTable>> = if (userId != null && repository != null) {
        repository.observeCustomDictionTables(userId)
            .map { list ->
                list.map {
                    DictionTable(
                        id = it.id,
                        title = it.title,
                        subtitle = "${it.title} (${it.syllables.size} слогов)",
                        syllables = it.syllables,
                        isCustom = true
                    )
                }
            }
            .catch { e -> handleStreamError(e) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    } else {
        _localCustomDictionTables.asStateFlow()
    }

    val aiHistory: StateFlow<List<AiEvaluationDoc>> = if (userId != null && repository != null) {
        repository.observeAiEvaluations(userId)
            .catch { e -> handleStreamError(e) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    } else {
        _localAiHistory.asStateFlow()
    }

    // --- Tab 1: Trainings & Modal ---
    private val _activeModuleFilter = MutableStateFlow(ExerciseModule.ALL)
    val activeModuleFilter: StateFlow<ExerciseModule> = _activeModuleFilter.asStateFlow()

    private val _activeProgramState = MutableStateFlow<ActiveProgramState?>(null)
    val activeProgramState: StateFlow<ActiveProgramState?> = _activeProgramState.asStateFlow()

    private val _activeExerciseModal = MutableStateFlow<ActiveExerciseModalState?>(null)
    val activeExerciseModal: StateFlow<ActiveExerciseModalState?> = _activeExerciseModal.asStateFlow()

    // --- Tab 2: Studio & AI ---
    private val _activeStudioGenre = MutableStateFlow("all")
    val activeStudioGenre: StateFlow<String> = _activeStudioGenre.asStateFlow()

    private val _activeScriptId = MutableStateFlow(DiktorCatalog.BUILTIN_SCRIPTS.first().id)
    val activeScriptId: StateFlow<String> = _activeScriptId.asStateFlow()

    private val _prompterFontSizeSp = MutableStateFlow(18)
    val prompterFontSizeSp: StateFlow<Int> = _prompterFontSizeSp.asStateFlow()

    private val _isPrompterScrolling = MutableStateFlow(false)
    val isPrompterScrolling: StateFlow<Boolean> = _isPrompterScrolling.asStateFlow()

    private val _prompterSpeed = MutableStateFlow(1.6f)
    val prompterSpeed: StateFlow<Float> = _prompterSpeed.asStateFlow()

    private val _aiUiState = MutableStateFlow<AiCoachUiState>(AiCoachUiState.Idle)
    val aiUiState: StateFlow<AiCoachUiState> = _aiUiState.asStateFlow()

    val allScripts: StateFlow<List<TeleprompterScript>> = customScripts
        .map { custom -> custom + DiktorCatalog.BUILTIN_SCRIPTS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiktorCatalog.BUILTIN_SCRIPTS)

    // --- Tab 3: Diction, Metronome, Twisters ---
    private val _dictionBpm = MutableStateFlow(80)
    val dictionBpm: StateFlow<Int> = _dictionBpm.asStateFlow()

    private val _isDictionMetroRunning = MutableStateFlow(false)
    val isDictionMetroRunning: StateFlow<Boolean> = _isDictionMetroRunning.asStateFlow()

    private val _dictionBeatIndex = MutableStateFlow(-1)
    val dictionBeatIndex: StateFlow<Int> = _dictionBeatIndex.asStateFlow()

    private val _dictionSyllableIndex = MutableStateFlow(0)
    val dictionSyllableIndex: StateFlow<Int> = _dictionSyllableIndex.asStateFlow()

    private val _activeDictionTableKey = MutableStateFlow("lvl1")
    val activeDictionTableKey: StateFlow<String> = _activeDictionTableKey.asStateFlow()

    private val _activeCustomDictionTableId = MutableStateFlow<String?>(null)
    val activeCustomDictionTableId: StateFlow<String?> = _activeCustomDictionTableId.asStateFlow()

    val activeDictionTable: StateFlow<DictionTable> = combine(
        _activeDictionTableKey,
        _activeCustomDictionTableId,
        customDictionTables
    ) { key, customId, customList ->
        if (key == "custom") {
            if (customList.isEmpty()) {
                DictionTable(
                    id = "custom_empty",
                    title = "Мои",
                    subtitle = "Нажмите «+ Своя таблица», чтобы создать свой набор",
                    syllables = emptyList(),
                    isCustom = true
                )
            } else {
                customList.find { it.id == customId } ?: customList.first()
            }
        } else {
            DiktorCatalog.BUILTIN_DICTION_TABLES.find { it.id == key }
                ?: DiktorCatalog.BUILTIN_DICTION_TABLES.first()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiktorCatalog.BUILTIN_DICTION_TABLES.first())

    private val _activeTwisterFilter = MutableStateFlow("all")
    val activeTwisterFilter: StateFlow<String> = _activeTwisterFilter.asStateFlow()

    private val _activeTwisterId = MutableStateFlow(DiktorCatalog.BUILTIN_TWISTERS.first().id)
    val activeTwisterId: StateFlow<String> = _activeTwisterId.asStateFlow()

    val allTwisters: StateFlow<List<TongueTwister>> = customTwisters
        .map { custom -> custom + DiktorCatalog.BUILTIN_TWISTERS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiktorCatalog.BUILTIN_TWISTERS)

    private var metroJob: Job? = null
    private var hasSyncedProgression = false

    init {
        initializeUserSession()
    }

    private fun initializeUserSession() {
        val uid = userId
        val repo = repository
        if (uid == null || repo == null) {
            val loaded = localStore.loadProgress()
            viewModelScope.launch {
                _progressDoc.value = syncDailyProgressionIfNeeded(loaded)
                hasSyncedProgression = true
            }
            return
        }

        viewModelScope.launch {
            try {
                val ensured = repo.ensureProgressDocument()
                _progressDoc.value = syncDailyProgressionIfNeeded(ensured)
                hasSyncedProgression = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize progress document", e)
            }
        }

        viewModelScope.launch {
            repo.observeProgress(uid)
                .catch { e -> handleStreamError(e) }
                .collect { doc ->
                    if (doc != null) {
                        val normalized = if (!hasSyncedProgression) {
                            hasSyncedProgression = true
                            syncDailyProgressionIfNeeded(doc)
                        } else {
                            val recalculatedStreak = calculateCurrentStreak(doc.completedDates).toLong()
                            if (recalculatedStreak != doc.streakDays) doc.copy(streakDays = recalculatedStreak) else doc
                        }
                        _progressDoc.value = normalized
                    }
                }
        }
    }

    private suspend fun syncDailyProgressionIfNeeded(doc: UserProgressDoc): UserProgressDoc {
        val today = getTodayIsoDate()
        var updated = doc
        var changed = false
        if (
            doc.lastCompletedCourseDate.isNotEmpty() &&
            doc.lastCompletedCourseDate < today &&
            doc.lastAdvancedDate != today
        ) {
            updated = updated.copy(
                courseDayNumber = max(1L, doc.courseDayNumber) + 1L,
                lastAdvancedDate = today
            )
            changed = true
        }
        val streak = calculateCurrentStreak(updated.completedDates).toLong()
        if (streak != updated.streakDays) {
            updated = updated.copy(streakDays = streak)
            changed = true
        }
        if (changed) {
            val repo = repository
            if (userId != null && repo != null) {
                try {
                    repo.saveProgress(updated)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed saving synced progression", e)
                }
            } else {
                localStore.saveProgress(updated)
            }
        }
        return updated
    }

    private fun handleStreamError(error: Throwable) {
        val ffe = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (ffe == null && error is CancellationException) return
        Log.e(TAG, "Firestore stream error", ffe ?: error)
    }

    // --- Navigation & Global Actions ---

    fun switchTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun openHelp() {
        _isHelpOpen.value = true
    }

    fun closeHelp() {
        _isHelpOpen.value = false
    }

    fun toggleSoundMute() {
        val next = !_isMuted.value
        _isMuted.value = next
        soundEngine.isMuted = next
        showToast(if (next) "Звуки тренажера выключены" else "Звуки тренажера включены")
    }

    fun showToast(msg: String) {
        _toastMessages.tryEmit(msg)
    }

    // --- Trainings & Programs ---

    fun selectModuleFilter(module: ExerciseModule) {
        _activeModuleFilter.value = module
    }

    fun openExerciseModal(exerciseId: String, overrideDurationSec: Int? = null, fromProgram: Boolean = false) {
        if (!fromProgram) {
            _activeProgramState.value = null
        }
        val ex = DiktorCatalog.findExercise(exerciseId) ?: return
        _activeExerciseModal.value = ActiveExerciseModalState(
            exercise = ex,
            durationSec = overrideDurationSec ?: ex.defaultDurationSec
        )
    }

    fun closeExerciseModal() {
        if (audioRecorder.activeChannel.value == RecordingChannel.CANDLE_MONITOR) {
            audioRecorder.stopAllCapture()
        }
        _activeProgramState.value = null
        _activeExerciseModal.value = null
    }

    fun startDailyCourseWorkout() {
        val dayNum = max(1, _progressDoc.value.courseDayNumber.toInt())
        val prog = DiktorCatalog.getCourseProgramForDay(dayNum)
        _activeProgramState.value = ActiveProgramState(program = prog, stepIndex = 0)
        runCurrentProgramStep()
    }

    fun startExpressProgram() {
        _activeProgramState.value = ActiveProgramState(program = DiktorCatalog.EXPRESS_PROGRAM, stepIndex = 0)
        runCurrentProgramStep()
    }

    private fun runCurrentProgramStep() {
        val pState = _activeProgramState.value ?: return
        val prog = pState.program
        val isDailyCourse = prog.id == "daily_course"
        val step = prog.steps.getOrNull(pState.stepIndex)

        if (step == null) {
            finishActiveProgram(prog, isDailyCourse, switchToStudio = false)
            return
        }

        if (step.isGoStudioAction) {
            finishActiveProgram(prog, isDailyCourse, switchToStudio = true)
            return
        }

        val exId = step.exId ?: return
        openExerciseModal(exId, overrideDurationSec = step.durationSec, fromProgram = true)
    }

    private fun finishActiveProgram(prog: CourseProgram, isDailyCourse: Boolean, switchToStudio: Boolean) {
        val today = getTodayIsoDate()
        val curr = _progressDoc.value
        val newDates = if (isDailyCourse && !curr.completedDates.contains(today)) {
            curr.completedDates + today
        } else {
            curr.completedDates
        }
        val updated = curr.copy(
            completedPrograms = curr.completedPrograms + 1L,
            completedDates = newDates,
            lastCompletedCourseDate = if (isDailyCourse) today else curr.lastCompletedCourseDate,
            streakDays = calculateCurrentStreak(newDates).toLong()
        )
        persistProgress(updated)

        _activeProgramState.value = null
        _activeExerciseModal.value = null
        soundEngine.playChime(isComplete = true)
        if (switchToStudio) {
            _currentTab.value = MainTab.STUDIO
            showToast(
                if (isDailyCourse) "День ${prog.dayNumber} засчитан! Запишите контрольный дубль"
                else "Разогрев завершен! Запишите контрольный дубль"
            )
        } else {
            showToast(
                if (isDailyCourse) "Отлично! День ${prog.dayNumber} пройден. Завтра откроется День ${prog.dayNumber + 1}!"
                else "Программа «${prog.title}» завершена!"
            )
        }
    }

    fun completeCurrentExercise(jumpToStudioAfter: Boolean = false) {
        val currentEx = _activeExerciseModal.value?.exercise
        if (currentEx != null) {
            val curr = _progressDoc.value
            val nextCompleted = if (curr.completedExercises.contains(currentEx.id)) {
                curr.completedExercises
            } else {
                curr.completedExercises + currentEx.id
            }
            val updated = curr.copy(
                completedExercises = nextCompleted,
                totalExerciseRuns = curr.totalExerciseRuns + 1L,
                streakDays = calculateCurrentStreak(curr.completedDates).toLong()
            )
            persistProgress(updated)
        }

        val pState = _activeProgramState.value
        if (pState != null && !jumpToStudioAfter) {
            _activeProgramState.value = pState.copy(stepIndex = pState.stepIndex + 1)
            soundEngine.playChime(isComplete = false)
            runCurrentProgramStep()
        } else {
            soundEngine.playChime(isComplete = true)
            showToast("Упражнение засчитано в прогресс!")
            closeExerciseModal()
            if (jumpToStudioAfter) {
                _currentTab.value = MainTab.STUDIO
            }
        }
    }

    fun advanceToNextCourseDayEarly() {
        val today = getTodayIsoDate()
        val curr = _progressDoc.value
        val nextDay = max(1L, curr.courseDayNumber) + 1L
        val updated = curr.copy(
            courseDayNumber = nextDay,
            lastCompletedCourseDate = "",
            lastAdvancedDate = today
        )
        persistProgress(updated)
        showToast("Открыт День $nextDay автокурса!")
    }

    fun recordEgorkiAttempt(count: Int) {
        val curr = _progressDoc.value
        if (count > curr.egorkiBestCount) {
            val updated = curr.copy(egorkiBestCount = count.toLong())
            persistProgress(updated)
        }
    }

    private fun persistProgress(updated: UserProgressDoc) {
        _progressDoc.value = updated
        val repo = repository
        if (userId == null || repo == null) {
            localStore.saveProgress(updated)
            return
        }
        viewModelScope.launch {
            try {
                repo.saveProgress(updated)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed saving progress", e)
                showToast("Не удалось сохранить прогресс в облако")
            }
        }
    }

    // --- Studio & Teleprompter & AI ---

    fun selectStudioGenre(genre: String) {
        _activeStudioGenre.value = genre
        val all = allScripts.value
        val filtered = when (genre) {
            "all" -> all
            "custom" -> customScripts.value
            else -> all.filter { it.genre == genre }
        }
        if (filtered.isNotEmpty() && filtered.none { it.id == _activeScriptId.value }) {
            _activeScriptId.value = filtered.first().id
        }
    }

    fun selectScript(scriptId: String) {
        _activeScriptId.value = scriptId
    }

    fun adjustPrompterFont(delta: Int) {
        _prompterFontSizeSp.value = (_prompterFontSizeSp.value + delta).coerceIn(14, 30)
    }

    fun setPrompterSpeed(speed: Float) {
        _prompterSpeed.value = speed
    }

    fun togglePrompterScrolling() {
        _isPrompterScrolling.value = !_isPrompterScrolling.value
    }

    fun stopPrompterScrolling() {
        _isPrompterScrolling.value = false
    }

    fun addCustomScript(titleRaw: String, genre: String, textRaw: String) {
        val text = textRaw.trim()
        if (text.isEmpty()) {
            showToast("Введите текст сценария для сохранения")
            return
        }
        val genreLabels = mapOf(
            "custom" to "Мой сценарий",
            "news" to "Новости",
            "trailer" to "Реклама",
            "audiobook" to "Аудиокнига"
        )
        val title = titleRaw.trim().ifEmpty { "Мой текст #${customScripts.value.size + 1}" }
        val genreLabel = genreLabels[genre] ?: "Мой сценарий"

        val repo = repository
        if (userId == null || repo == null) {
            val newId = "local_sc_${UUID.randomUUID()}"
            val newItem = TeleprompterScript(
                id = newId,
                genre = genre,
                genreLabel = genreLabel,
                title = title,
                isCustom = true,
                text = text
            )
            val updated = listOf(newItem) + _localCustomScripts.value
            _localCustomScripts.value = updated
            localStore.saveCustomScripts(updated)
            _activeScriptId.value = newId
            _activeStudioGenre.value = "all"
            showToast("Сценарий сохранен в телесуфлер!")
            return
        }

        viewModelScope.launch {
            try {
                val newId = repo.addCustomScript(title, genre, genreLabel, text)
                _activeScriptId.value = newId
                _activeStudioGenre.value = "all"
                showToast("Сценарий сохранен в телесуфлер!")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showToast("Ошибка сохранения сценария")
            }
        }
    }

    fun deleteCustomScript(scriptId: String) {
        val repo = repository
        if (userId == null || repo == null) {
            val updated = _localCustomScripts.value.filterNot { it.id == scriptId }
            _localCustomScripts.value = updated
            localStore.saveCustomScripts(updated)
            if (_activeScriptId.value == scriptId) {
                _activeScriptId.value = DiktorCatalog.BUILTIN_SCRIPTS.first().id
            }
            showToast("Сценарий удален")
            return
        }
        viewModelScope.launch {
            try {
                repo.deleteCustomScript(scriptId)
                if (_activeScriptId.value == scriptId) {
                    _activeScriptId.value = DiktorCatalog.BUILTIN_SCRIPTS.first().id
                }
                showToast("Сценарий удален")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showToast("Ошибка удаления сценария")
            }
        }
    }

    fun toggleStudioRecording(onRequestMicPermission: () -> Unit, hasPermission: Boolean) {
        if (audioRecorder.activeChannel.value == RecordingChannel.STUDIO) {
            audioRecorder.stopAllCapture()
            showToast("Дубль записан! Нажмите «Разобрать дубль с ИИ»")
            return
        }
        if (!hasPermission) {
            onRequestMicPermission()
            return
        }
        startStudioRecordingNow()
    }

    fun startStudioRecordingNow() {
        _aiUiState.value = AiCoachUiState.Idle
        audioRecorder.startRecording(RecordingChannel.STUDIO) { err ->
            showToast(err)
        }
    }

    fun evaluateTakeWithGemini() {
        val wavBytes = audioRecorder.studioWavBytes.value
        if (wavBytes == null || wavBytes.size <= 44) {
            showToast("Сначала запишите дубль с микрофона")
            return
        }
        val scriptObj = allScripts.value.find { it.id == _activeScriptId.value }
            ?: DiktorCatalog.BUILTIN_SCRIPTS.first()

        _aiUiState.value = AiCoachUiState.Loading("Gemini 3.8 Flash слушает ваш голос...")
        viewModelScope.launch {
            try {
                val verdict = aiCoachRepository.evaluateRecordingWithGemini(
                    audioBytes = wavBytes,
                    scriptTitle = scriptObj.title,
                    scriptText = scriptObj.text
                )
                _aiUiState.value = AiCoachUiState.Success(verdict)
                saveAiEvaluationToCloud(verdict)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val msg = e.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: "Не удалось получить разбор от ИИ-режиссера. Проверьте подключение и попробуйте снова."
                _aiUiState.value = AiCoachUiState.Error(msg)
            }
        }
    }

    fun evaluateTakeAcousticLocal() {
        val stats = audioRecorder.studioAcousticStats.value
        if (stats == null) {
            showToast("Сначала запишите дубль с микрофона")
            return
        }
        val scriptObj = allScripts.value.find { it.id == _activeScriptId.value }
            ?: DiktorCatalog.BUILTIN_SCRIPTS.first()
        _aiUiState.value = AiCoachUiState.Loading("Акустический анализ опоры и пауз...")
        viewModelScope.launch {
            delay(450L)
            val verdict = aiCoachRepository.buildLocalAcousticVerdict(stats, scriptObj.title)
            _aiUiState.value = AiCoachUiState.Success(verdict)
            saveAiEvaluationToCloud(verdict)
        }
    }

    private fun saveAiEvaluationToCloud(verdict: AiVerdict) {
        val doc = AiEvaluationDoc(
            id = "local_ev_${UUID.randomUUID()}",
            userId = userId ?: "local",
            dateLabel = verdict.dateLabel,
            scriptTitle = verdict.scriptTitle,
            modeLabel = verdict.modeLabel,
            headline = verdict.headline,
            totalScore = verdict.totalScore.toLong(),
            chestScore = verdict.chestScore.toLong(),
            supportScore = verdict.supportScore.toLong(),
            pauseScore = verdict.pauseScore.toLong(),
            dictionScore = verdict.dictionScore.toLong(),
            critique = verdict.critique,
            advice = verdict.advice,
            recommendedExId = verdict.recommendedExId
        )
        val repo = repository
        if (userId == null || repo == null) {
            val updated = (listOf(doc) + _localAiHistory.value).take(20)
            _localAiHistory.value = updated
            localStore.saveAiHistory(updated)
            return
        }
        viewModelScope.launch {
            try {
                repo.addAiEvaluation(doc)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Failed persisting AI evaluation", e)
            }
        }
    }

    // --- Diction, Metronome, Twisters ---

    fun updateDictionBpm(bpm: Int) {
        val clamped = bpm.coerceIn(50, 140)
        _dictionBpm.value = clamped
        if (_isDictionMetroRunning.value) {
            startDictionMetronomeLoop()
        }
    }

    fun toggleDictionMetronome() {
        if (_isDictionMetroRunning.value) {
            stopDictionMetronome()
        } else {
            _isDictionMetroRunning.value = true
            _dictionBeatIndex.value = 0
            startDictionMetronomeLoop()
        }
    }

    fun stopDictionMetronome() {
        _isDictionMetroRunning.value = false
        metroJob?.cancel()
        metroJob = null
        _dictionBeatIndex.value = -1
    }

    private fun startDictionMetronomeLoop() {
        metroJob?.cancel()
        metroJob = viewModelScope.launch {
            var beat = 0
            while (isActive && _isDictionMetroRunning.value) {
                val intervalMs = (60_000L / _dictionBpm.value).coerceAtLeast(350L)
                val isAccent = (beat == 0)
                soundEngine.playClick(isAccent = isAccent)
                _dictionBeatIndex.value = beat

                val syllables = activeDictionTable.value.syllables
                if (syllables.isNotEmpty()) {
                    val current = _dictionSyllableIndex.value % syllables.size
                    _dictionSyllableIndex.value = current
                    delay(intervalMs)
                    if (!isActive || !_isDictionMetroRunning.value) break
                    _dictionSyllableIndex.value = (current + 1) % syllables.size
                } else {
                    delay(intervalMs)
                }
                beat = (beat + 1) % 4
            }
        }
    }

    fun selectDictionTable(key: String) {
        _activeDictionTableKey.value = key
        _dictionSyllableIndex.value = 0
    }

    fun selectCustomDictionTable(id: String) {
        _activeDictionTableKey.value = "custom"
        _activeCustomDictionTableId.value = id
        _dictionSyllableIndex.value = 0
    }

    fun addCustomDictionTable(titleRaw: String, clustersRaw: String) {
        val syllables = DiktorCatalog.parseClustersToSyllables(clustersRaw)
        if (syllables.isEmpty()) {
            showToast("Введите хотя бы одно сочетание согласных или слог")
            return
        }
        val title = titleRaw.trim().ifEmpty { "Моя таблица #${customDictionTables.value.size + 1}" }
        val repo = repository
        if (userId == null || repo == null) {
            val newId = "local_tb_${UUID.randomUUID()}"
            val table = DictionTable(
                id = newId,
                title = title,
                subtitle = "$title (${syllables.size} слогов)",
                syllables = syllables,
                isCustom = true
            )
            val updated = listOf(table) + _localCustomDictionTables.value
            _localCustomDictionTables.value = updated
            localStore.saveCustomDictionTables(updated)
            _activeCustomDictionTableId.value = newId
            _activeDictionTableKey.value = "custom"
            _dictionSyllableIndex.value = 0
            showToast("Таблица сохранена (${syllables.size} слогов)!")
            return
        }
        viewModelScope.launch {
            try {
                val newId = repo.addCustomDictionTable(title, syllables)
                _activeCustomDictionTableId.value = newId
                _activeDictionTableKey.value = "custom"
                _dictionSyllableIndex.value = 0
                showToast("Таблица сохранена (${syllables.size} слогов)!")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showToast("Ошибка сохранения таблицы")
            }
        }
    }

    fun deleteCustomDictionTable(tableId: String) {
        val repo = repository
        if (userId == null || repo == null) {
            val updated = _localCustomDictionTables.value.filterNot { it.id == tableId }
            _localCustomDictionTables.value = updated
            localStore.saveCustomDictionTables(updated)
            if (_activeCustomDictionTableId.value == tableId) {
                _activeCustomDictionTableId.value = null
            }
            showToast("Таблица согласных удалена")
            return
        }
        viewModelScope.launch {
            try {
                repo.deleteCustomDictionTable(tableId)
                if (_activeCustomDictionTableId.value == tableId) {
                    _activeCustomDictionTableId.value = null
                }
                showToast("Таблица согласных удалена")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showToast("Ошибка удаления таблицы")
            }
        }
    }

    fun selectTwisterFilter(filterKey: String) {
        _activeTwisterFilter.value = filterKey
    }

    fun selectActiveTwister(id: String) {
        _activeTwisterId.value = id
    }

    fun cycleNextTwister() {
        val all = allTwisters.value
        val filtered = when (val f = _activeTwisterFilter.value) {
            "all" -> all
            "my" -> customTwisters.value
            else -> all.filter { it.soundTag == f }
        }
        val pool = filtered.ifEmpty { all }
        if (pool.isEmpty()) return
        val idx = pool.indexOfFirst { it.id == _activeTwisterId.value }
        val nextIdx = if (idx == -1) 0 else (idx + 1) % pool.size
        _activeTwisterId.value = pool[nextIdx].id
        soundEngine.playClick(isAccent = false)
    }

    fun addCustomTwister(soundTag: String, difficulty: String, textRaw: String) {
        val text = textRaw.trim()
        if (text.isEmpty()) {
            showToast("Введите текст скороговорки")
            return
        }
        val repo = repository
        if (userId == null || repo == null) {
            val newId = "local_tw_${UUID.randomUUID()}"
            val tw = TongueTwister(
                id = newId,
                soundTag = soundTag,
                difficulty = difficulty,
                isCustom = true,
                text = text
            )
            val updated = listOf(tw) + _localCustomTwisters.value
            _localCustomTwisters.value = updated
            localStore.saveCustomTwisters(updated)
            _activeTwisterId.value = newId
            _activeTwisterFilter.value = "all"
            showToast("Скороговорка добавлена!")
            return
        }
        viewModelScope.launch {
            try {
                val newId = repo.addCustomTwister(soundTag, difficulty, text)
                _activeTwisterId.value = newId
                _activeTwisterFilter.value = "all"
                showToast("Скороговорка добавлена!")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showToast("Ошибка сохранения скороговорки")
            }
        }
    }

    fun deleteCustomTwister(twisterId: String) {
        val repo = repository
        if (userId == null || repo == null) {
            val updated = _localCustomTwisters.value.filterNot { it.id == twisterId }
            _localCustomTwisters.value = updated
            localStore.saveCustomTwisters(updated)
            if (_activeTwisterId.value == twisterId) {
                _activeTwisterId.value = DiktorCatalog.BUILTIN_TWISTERS.first().id
            }
            showToast("Скороговорка удалена")
            return
        }
        viewModelScope.launch {
            try {
                repo.deleteCustomTwister(twisterId)
                if (_activeTwisterId.value == twisterId) {
                    _activeTwisterId.value = DiktorCatalog.BUILTIN_TWISTERS.first().id
                }
                showToast("Скороговорка удалена")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showToast("Ошибка удаления скороговорки")
            }
        }
    }

    fun toggleQuickTwisterRecording(onRequestMicPermission: () -> Unit, hasPermission: Boolean) {
        if (audioRecorder.activeChannel.value == RecordingChannel.QUICK_TWISTER) {
            audioRecorder.stopAllCapture()
            showToast("Запись скороговорки готова к прослушиванию")
            return
        }
        if (!hasPermission) {
            onRequestMicPermission()
            return
        }
        startQuickTwisterRecordingNow()
    }

    fun startQuickTwisterRecordingNow() {
        audioRecorder.startRecording(RecordingChannel.QUICK_TWISTER) { err ->
            showToast(err)
        }
    }

    // --- Progress History ---

    fun clearAiHistory() {
        val repo = repository
        if (userId == null || repo == null) {
            if (_localAiHistory.value.isEmpty()) return
            _localAiHistory.value = emptyList()
            localStore.saveAiHistory(emptyList())
            showToast("История оценок очищена")
            return
        }
        val ids = aiHistory.value.map { it.id }.filter { it.isNotEmpty() }
        if (ids.isEmpty()) return
        viewModelScope.launch {
            try {
                repo.clearAiEvaluations(ids)
                showToast("История оценок очищена")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showToast("Не удалось очистить историю")
            }
        }
    }

    // --- Candle Mic Helper ---
    fun startCandleMicMonitor() {
        audioRecorder.startRecording(RecordingChannel.CANDLE_MONITOR) { err ->
            showToast(err)
        }
    }

    fun stopCandleMicMonitor() {
        if (audioRecorder.activeChannel.value == RecordingChannel.CANDLE_MONITOR) {
            audioRecorder.stopAllCapture()
        }
    }

    override fun onCleared() {
        super.onCleared()
        metroJob?.cancel()
        audioRecorder.release()
    }

    // --- Date & Streak Helpers ---

    fun getTodayIsoDate(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return fmt.format(Date())
    }

    fun getOffsetIsoDate(daysOffset: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    fun calculateCurrentStreak(completedDates: List<String>): Int {
        val set = completedDates.toSet()
        if (set.isEmpty()) return 0
        val today = getTodayIsoDate()
        val yesterday = getOffsetIsoDate(-1)
        var offset = when {
            set.contains(today) -> 0
            set.contains(yesterday) -> -1
            else -> return 0
        }
        var streak = 0
        while (set.contains(getOffsetIsoDate(offset))) {
            streak += 1
            offset -= 1
        }
        return streak
    }
}

class DiktorViewModelFactory(
    private val application: Application,
    private val userId: String?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DiktorViewModel(application = application, userId = userId) as T
    }
}
