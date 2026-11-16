package com.example.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.RecordingChannel
import com.example.model.MainTab
import com.example.ui.DiktorViewModel
import com.example.ui.DiktorViewModelFactory
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioRose
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

@Composable
fun DiktorMainScreen(
    currentUserId: String?,
    userDisplayName: String?,
    isSigningIn: Boolean,
    authError: String?,
    onGoogleSignInClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: DiktorViewModel = viewModel(
        key = currentUserId ?: "local_guest",
        factory = DiktorViewModelFactory(application, currentUserId)
    )

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isHelpOpen by viewModel.isHelpOpen.collectAsStateWithLifecycle()
    val isMuted by viewModel.isMuted.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()

    val activeModuleFilter by viewModel.activeModuleFilter.collectAsStateWithLifecycle()
    val activeProgramState by viewModel.activeProgramState.collectAsStateWithLifecycle()
    val activeExerciseModal by viewModel.activeExerciseModal.collectAsStateWithLifecycle()

    val customScripts by viewModel.customScripts.collectAsStateWithLifecycle()
    val allScripts by viewModel.allScripts.collectAsStateWithLifecycle()
    val activeStudioGenre by viewModel.activeStudioGenre.collectAsStateWithLifecycle()
    val activeScriptId by viewModel.activeScriptId.collectAsStateWithLifecycle()
    val prompterFontSizeSp by viewModel.prompterFontSizeSp.collectAsStateWithLifecycle()
    val isPrompterScrolling by viewModel.isPrompterScrolling.collectAsStateWithLifecycle()
    val prompterSpeed by viewModel.prompterSpeed.collectAsStateWithLifecycle()
    val aiUiState by viewModel.aiUiState.collectAsStateWithLifecycle()

    val dictionBpm by viewModel.dictionBpm.collectAsStateWithLifecycle()
    val isMetroRunning by viewModel.isDictionMetroRunning.collectAsStateWithLifecycle()
    val beatIndex by viewModel.dictionBeatIndex.collectAsStateWithLifecycle()
    val syllableIndex by viewModel.dictionSyllableIndex.collectAsStateWithLifecycle()
    val activeTableKey by viewModel.activeDictionTableKey.collectAsStateWithLifecycle()
    val activeTable by viewModel.activeDictionTable.collectAsStateWithLifecycle()
    val customTables by viewModel.customDictionTables.collectAsStateWithLifecycle()
    val allTwisters by viewModel.allTwisters.collectAsStateWithLifecycle()
    val customTwisters by viewModel.customTwisters.collectAsStateWithLifecycle()
    val activeTwisterFilter by viewModel.activeTwisterFilter.collectAsStateWithLifecycle()
    val activeTwisterId by viewModel.activeTwisterId.collectAsStateWithLifecycle()

    val aiHistory by viewModel.aiHistory.collectAsStateWithLifecycle()

    val activeRecChannel by viewModel.audioRecorder.activeChannel.collectAsStateWithLifecycle()
    val candleLiveRms by viewModel.audioRecorder.liveRms.collectAsStateWithLifecycle()
    val waveformSamples by viewModel.audioRecorder.waveformSamples.collectAsStateWithLifecycle()
    val studioElapsedSec by viewModel.audioRecorder.studioElapsedSec.collectAsStateWithLifecycle()
    val quickElapsedSec by viewModel.audioRecorder.quickElapsedSec.collectAsStateWithLifecycle()
    val studioWavBytes by viewModel.audioRecorder.studioWavBytes.collectAsStateWithLifecycle()
    val hasQuickRecording by viewModel.audioRecorder.hasQuickRecording.collectAsStateWithLifecycle()
    val playingTarget by viewModel.audioRecorder.playingTarget.collectAsStateWithLifecycle()
    val playbackProgress by viewModel.audioRecorder.playbackProgress.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.toastMessages.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    val isRecordingAny = activeRecChannel != RecordingChannel.NONE
    val todayIso = remember { viewModel.getTodayIsoDate() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBg)
    ) {
        val useNavRail = maxWidth >= 720.dp

        Scaffold(
            containerColor = StudioBg,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                DiktorTopHeader(
                    streakDays = progress.streakDays.toInt(),
                    isMuted = isMuted,
                    isRecordingAny = isRecordingAny,
                    isSignedIn = currentUserId != null,
                    userDisplayName = userDisplayName,
                    onOpenHelp = { viewModel.openHelp() },
                    onToggleMute = { viewModel.toggleSoundMute() },
                    onGoogleSignIn = onGoogleSignInClick,
                    onSignOut = onSignOutClick
                )
            },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        containerColor = StudioBg,
                        tonalElevation = 0.dp
                    ) {
                        MainTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.switchTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = tabIcon(tab),
                                        contentDescription = tab.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = StudioAmber,
                                    selectedTextColor = StudioAmber,
                                    indicatorColor = StudioAmber.copy(alpha = 0.14f),
                                    unselectedIconColor = StudioTextSecondary,
                                    unselectedTextColor = StudioTextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = StudioCard,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        MainTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.switchTab(tab) },
                                icon = { Icon(tabIcon(tab), contentDescription = tab.title) },
                                label = { Text(tab.title, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = StudioAmber,
                                    selectedTextColor = StudioAmber,
                                    indicatorColor = StudioAmber.copy(alpha = 0.14f),
                                    unselectedIconColor = StudioTextSecondary,
                                    unselectedTextColor = StudioTextSecondary
                                ),
                                modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (currentTab) {
                        MainTab.TRAININGS -> TrainingsTab(
                            progress = progress,
                            todayIsoDate = todayIso,
                            activeModuleFilter = activeModuleFilter,
                            onSelectModuleFilter = { viewModel.selectModuleFilter(it) },
                            onStartDailyCourse = { viewModel.startDailyCourseWorkout() },
                            onAdvanceCourseEarly = { viewModel.advanceToNextCourseDayEarly() },
                            onStartExpressProgram = { viewModel.startExpressProgram() },
                            onOpenExercise = { viewModel.openExerciseModal(it) }
                        )
                        MainTab.STUDIO -> StudioTab(
                            allScripts = allScripts,
                            customScriptsCount = customScripts.size,
                            activeGenre = activeStudioGenre,
                            activeScriptId = activeScriptId,
                            prompterFontSizeSp = prompterFontSizeSp,
                            isPrompterScrolling = isPrompterScrolling,
                            prompterSpeed = prompterSpeed,
                            isRecording = activeRecChannel == RecordingChannel.STUDIO,
                            recordingElapsedSec = studioElapsedSec,
                            waveformSamples = waveformSamples,
                            hasRecordedTake = (studioWavBytes?.size ?: 0) > 44,
                            isPlayingTake = playingTarget == RecordingChannel.STUDIO,
                            playbackProgress = if (playingTarget == RecordingChannel.STUDIO) playbackProgress else 0f,
                            aiUiState = aiUiState,
                            onSelectGenre = { viewModel.selectStudioGenre(it) },
                            onSelectScript = { viewModel.selectScript(it) },
                            onAddCustomScript = { title, genre, text -> viewModel.addCustomScript(title, genre, text) },
                            onDeleteCustomScript = { viewModel.deleteCustomScript(it) },
                            onAdjustFont = { viewModel.adjustPrompterFont(it) },
                            onSelectSpeed = { viewModel.setPrompterSpeed(it) },
                            onTogglePrompterScroll = { viewModel.togglePrompterScrolling() },
                            onStopPrompterScroll = { viewModel.stopPrompterScrolling() },
                            onToggleRecording = { granted, request -> viewModel.toggleStudioRecording(request, granted) },
                            onMicPermissionGranted = { viewModel.startStudioRecordingNow() },
                            onMicPermissionDenied = { viewModel.showToast("Для записи дубля необходимо разрешение на микрофон") },
                            onTogglePlayback = { viewModel.audioRecorder.togglePlayback(RecordingChannel.STUDIO) { viewModel.showToast(it) } },
                            onAnalyzeGemini = { viewModel.evaluateTakeWithGemini() },
                            onAnalyzeAcoustic = { viewModel.evaluateTakeAcousticLocal() },
                            onOpenExercise = { viewModel.openExerciseModal(it) }
                        )
                        MainTab.DICTION -> DictionTab(
                            bpm = dictionBpm,
                            isMetroRunning = isMetroRunning,
                            beatIndex = beatIndex,
                            syllableIndex = syllableIndex,
                            activeTableKey = activeTableKey,
                            activeTable = activeTable,
                            customTables = customTables,
                            allTwisters = allTwisters,
                            customTwistersCount = customTwisters.size,
                            activeTwisterFilter = activeTwisterFilter,
                            activeTwisterId = activeTwisterId,
                            isQuickRecording = activeRecChannel == RecordingChannel.QUICK_TWISTER,
                            quickElapsedSec = quickElapsedSec,
                            hasQuickRecording = hasQuickRecording,
                            isQuickPlaying = playingTarget == RecordingChannel.QUICK_TWISTER,
                            quickPlayProgress = if (playingTarget == RecordingChannel.QUICK_TWISTER) playbackProgress else 0f,
                            onUpdateBpm = { viewModel.updateDictionBpm(it) },
                            onToggleMetro = { viewModel.toggleDictionMetronome() },
                            onSelectTable = { viewModel.selectDictionTable(it) },
                            onSelectCustomTable = { viewModel.selectCustomDictionTable(it) },
                            onAddCustomTable = { title, clusters -> viewModel.addCustomDictionTable(title, clusters) },
                            onDeleteCustomTable = { viewModel.deleteCustomDictionTable(it) },
                            onSelectTwisterFilter = { viewModel.selectTwisterFilter(it) },
                            onSelectTwister = { viewModel.selectActiveTwister(it) },
                            onCycleNextTwister = { viewModel.cycleNextTwister() },
                            onAddCustomTwister = { tag, diff, txt -> viewModel.addCustomTwister(tag, diff, txt) },
                            onDeleteCustomTwister = { viewModel.deleteCustomTwister(it) },
                            onToggleQuickRec = { granted, req -> viewModel.toggleQuickTwisterRecording(req, granted) },
                            onQuickMicGranted = { viewModel.startQuickTwisterRecordingNow() },
                            onQuickMicDenied = { viewModel.showToast("Для записи скороговорки нужен доступ к микрофону") },
                            onToggleQuickPlayback = { viewModel.audioRecorder.togglePlayback(RecordingChannel.QUICK_TWISTER) { viewModel.showToast(it) } }
                        )
                        MainTab.PROGRESS -> ProgressTab(
                            progress = progress,
                            todayIsoDate = todayIso,
                            customScriptsCount = customScripts.size,
                            customTwistersCount = customTwisters.size,
                            customTablesCount = customTables.size,
                            aiHistory = aiHistory,
                            isSignedIn = currentUserId != null,
                            userDisplayName = userDisplayName,
                            isSigningIn = isSigningIn,
                            authError = authError,
                            onGoogleSignInClick = onGoogleSignInClick,
                            onSignOutClick = onSignOutClick,
                            onOpenExercise = { viewModel.openExerciseModal(it) },
                            onClearHistory = { viewModel.clearAiHistory() }
                        )
                    }
                }
            }
        }
    }

    val modalEx = activeExerciseModal
    if (modalEx != null) {
        ExerciseModalSheet(
            exercise = modalEx.exercise,
            durationSec = modalEx.durationSec,
            programState = activeProgramState,
            egorkiBestCount = progress.egorkiBestCount.toInt(),
            soundEngine = viewModel.soundEngine,
            candleLiveRms = candleLiveRms,
            isCandleMicActive = activeRecChannel == RecordingChannel.CANDLE_MONITOR,
            onStartCandleMic = { viewModel.startCandleMicMonitor() },
            onStopCandleMic = { viewModel.stopCandleMicMonitor() },
            onRecordEgorkiAttempt = { viewModel.recordEgorkiAttempt(it) },
            onShowToast = { viewModel.showToast(it) },
            onCompleteExercise = { jump -> viewModel.completeCurrentExercise(jump) },
            onClose = { viewModel.closeExerciseModal() }
        )
    }

    if (isHelpOpen) {
        HelpModalSheet(
            onClose = { viewModel.closeHelp() },
            onNavigateTab = { viewModel.switchTab(it) }
        )
    }
}

@Composable
private fun DiktorTopHeader(
    streakDays: Int,
    isMuted: Boolean,
    isRecordingAny: Boolean,
    isSignedIn: Boolean,
    userDisplayName: String?,
    onOpenHelp: () -> Unit,
    onToggleMute: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioBg)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(StudioAmber.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                        .border(1.dp, StudioAmber, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = null,
                        tint = StudioAmber,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "Диктор PRO",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = StudioTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioCard, RoundedCornerShape(8.dp))
                        .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                        .clickable { onOpenHelp() }
                        .testTag("open_help_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "Справка",
                        tint = StudioCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StudioAmber.copy(alpha = 0.14f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioAmber.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .height(30.dp)
                        .defaultMinSize(minWidth = 44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Серия дней",
                            tint = StudioAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.size(2.dp))
                        Text(
                            text = "${streakDays}д",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = StudioAmber,
                            maxLines = 1
                        )
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioCard, RoundedCornerShape(8.dp))
                        .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                        .clickable { onToggleMute() }
                        .testTag("sound_mute_button")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isMuted) "Включить звук" else "Выключить звук",
                        tint = if (isMuted) StudioRose else StudioCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSignedIn) StudioEmerald.copy(alpha = 0.14f) else StudioCard,
                                CircleShape
                            )
                            .clickable { menuExpanded = true }
                            .testTag("studio_status_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = if (isSignedIn) "Профиль Google" else "Войти в аккаунт",
                            tint = if (isSignedIn) StudioEmerald else StudioAmber,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    if (isRecordingAny) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(8.dp)
                                .background(StudioRose, CircleShape)
                                .border(1.dp, StudioBg, CircleShape)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor = StudioCard
                    ) {
                        if (isSignedIn) {
                            if (!userDisplayName.isNullOrBlank()) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Облако: $userDisplayName",
                                            color = StudioTextSecondary,
                                            fontSize = 12.sp
                                        )
                                    },
                                    onClick = { menuExpanded = false }
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Выйти из аккаунта",
                                        color = StudioRose,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = null,
                                        tint = StudioRose
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onSignOut()
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Локальный режим",
                                        color = StudioTextSecondary,
                                        fontSize = 12.sp
                                    )
                                },
                                onClick = { menuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Войти через Google",
                                        color = StudioAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Login,
                                        contentDescription = null,
                                        tint = StudioAmber
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onGoogleSignIn()
                                }
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = StudioBorder)
    }
}

private fun tabIcon(tab: MainTab) = when (tab) {
    MainTab.TRAININGS -> Icons.AutoMirrored.Filled.FormatListBulleted
    MainTab.STUDIO -> Icons.Filled.Mic
    MainTab.DICTION -> Icons.Filled.Bolt
    MainTab.PROGRESS -> Icons.Filled.BarChart
}
