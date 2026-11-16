package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.audio.StudioSoundEngine
import com.example.model.Exercise
import com.example.model.ExerciseVizType
import com.example.ui.ActiveProgramState
import com.example.ui.components.buildTeleprompterAnnotatedString
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioRose
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ExerciseModalSheet(
    exercise: Exercise,
    durationSec: Int,
    programState: ActiveProgramState?,
    egorkiBestCount: Int,
    soundEngine: StudioSoundEngine,
    candleLiveRms: Float,
    isCandleMicActive: Boolean,
    onStartCandleMic: () -> Unit,
    onStopCandleMic: () -> Unit,
    onRecordEgorkiAttempt: (Int) -> Unit,
    onShowToast: (String) -> Unit,
    onCompleteExercise: (jumpToStudio: Boolean) -> Unit,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler { onClose() }

        DisposableEffect(exercise.id) {
            onDispose { onStopCandleMic() }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StudioBg)
                .systemBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            key(exercise.id, programState?.stepIndex ?: -1) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (programState != null) {
                        val total = programState.program.steps.size
                        val curr = programState.stepIndex + 1
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StudioCard, RoundedCornerShape(14.dp))
                                .border(1.dp, StudioAmber.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = programState.program.title,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = StudioAmber
                                )
                                Text(
                                    text = "Шаг $curr из $total",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = StudioTextPrimary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { (curr.toFloat() / total.coerceAtLeast(1)).coerceIn(0f, 1f) },
                                color = StudioAmber,
                                trackColor = StudioBg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }

                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Упражнение #${exercise.num} • ${exercise.module.displayName}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = StudioCyan
                            )
                            Text(
                                text = exercise.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = StudioTextPrimary
                            )
                        }
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(48.dp)
                                .background(StudioCard, RoundedCornerShape(12.dp))
                                .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                                .testTag("close_exercise_modal_button")
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Закрыть тренажер", tint = StudioTextPrimary)
                        }
                    }

                    // Interactive Visual Stage
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 250.dp)
                            .background(StudioCard, RoundedCornerShape(18.dp))
                            .border(1.dp, StudioBorder, RoundedCornerShape(18.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ExerciseVisualStage(
                            exercise = exercise,
                            durationSec = durationSec,
                            egorkiBestCount = egorkiBestCount,
                            soundEngine = soundEngine,
                            candleLiveRms = candleLiveRms,
                            isCandleMicActive = isCandleMicActive,
                            onStartCandleMic = onStartCandleMic,
                            onRecordEgorkiAttempt = onRecordEgorkiAttempt,
                            onShowToast = onShowToast,
                            onJumpToStudio = { onCompleteExercise(true) }
                        )
                    }

                    // Instructions Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StudioCard, RoundedCornerShape(16.dp))
                            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ТЕХНИКА ВЫПОЛНЕНИЯ",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = StudioTextSecondary
                        )
                        exercise.steps.forEachIndexed { idx, stepText ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(StudioAmber.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = StudioAmber
                                    )
                                }
                                Text(
                                    text = stepText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StudioTextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    val completeText = when {
                        programState == null -> "✓ Засчитать выполнение"
                        programState.stepIndex + 1 < programState.program.steps.size -> "Следующий шаг →"
                        else -> "✓ Завершить программу"
                    }

                    Button(
                        onClick = { onCompleteExercise(false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioEmerald,
                            contentColor = Color(0xFF0B0F19)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 52.dp)
                            .testTag("complete_exercise_button")
                    ) {
                        Text(completeText, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseVisualStage(
    exercise: Exercise,
    durationSec: Int,
    egorkiBestCount: Int,
    soundEngine: StudioSoundEngine,
    candleLiveRms: Float,
    isCandleMicActive: Boolean,
    onStartCandleMic: () -> Unit,
    onRecordEgorkiAttempt: (Int) -> Unit,
    onShowToast: (String) -> Unit,
    onJumpToStudio: () -> Unit
) {
    when (exercise.vizType) {
        ExerciseVizType.HIDDEN_YAWN -> HiddenYawnStage(durationSec, soundEngine)
        ExerciseVizType.HORSE_LIPS -> HorseLipsStage(durationSec, soundEngine)
        ExerciseVizType.JAW_RELEASE -> JawReleaseStage(soundEngine)
        ExerciseVizType.NECK_PENDULUM -> NeckPendulumStage(soundEngine, onShowToast)
        ExerciseVizType.CANDLE_BREATH -> CandleBreathStage(
            candleLiveRms = candleLiveRms,
            isCandleMicActive = isCandleMicActive,
            onStartCandleMic = onStartCandleMic,
            onShowToast = onShowToast
        )
        ExerciseVizType.PUMP_STRELNIKOVA -> PumpStrelnikovaStage(soundEngine)
        ExerciseVizType.DOG_BREATH -> DogBreathStage(durationSec, soundEngine)
        ExerciseVizType.EGORKI_33 -> EgorkiStage(egorkiBestCount, soundEngine, onRecordEgorkiAttempt)
        ExerciseVizType.TARZAN_CHEST -> TarzanChestStage(durationSec, soundEngine)
        ExerciseVizType.CRY_SIGH -> CrySighStage(durationSec, soundEngine)
        ExerciseVizType.MASK_FOCUS -> MaskFocusStage(soundEngine)
        ExerciseVizType.VOCAL_FRY -> VocalFryStage(soundEngine)
        ExerciseVizType.CORK_TRAINING -> CorkTrainingStage(soundEngine)
        ExerciseVizType.TONGUE_CLEANING -> TongueCleaningStage(soundEngine)
        ExerciseVizType.LIPS_TRUMPET_SMILE -> LipsPoseStage(soundEngine)
        ExerciseVizType.SYLLABLE_MATRICES -> SyllableMatrixModalStage(soundEngine)
        ExerciseVizType.INTONATION_SWING -> IntonationSwingStage(soundEngine)
        ExerciseVizType.SEMANTIC_MAGNET -> SemanticMagnetStage(soundEngine)
        ExerciseVizType.MARKUP_READING -> MarkupReadingStage(onJumpToStudio)
        ExerciseVizType.TONGUE_ROOT_DROP -> TongueRootDropStage(durationSec, soundEngine)
        ExerciseVizType.RAGDOLL_SHOULDERS -> RagdollShouldersStage(durationSec, soundEngine)
        ExerciseVizType.BOX_BREATH -> BoxBreathStage(durationSec, soundEngine)
        ExerciseVizType.STEPPED_EXHALE -> SteppedExhaleStage(durationSec, soundEngine)
        ExerciseVizType.VOCAL_FRY_MORNING -> VocalFryMorningStage(durationSec, soundEngine)
        ExerciseVizType.VOCAL_FRY_GLISSANDO -> VocalFryGlissandoStage(durationSec, soundEngine)
        ExerciseVizType.VOCAL_FRY_SYLLABLES -> VocalFrySyllablesStage(durationSec, soundEngine)
        ExerciseVizType.VOCAL_FRY_PHRASES -> VocalFryPhrasesStage(soundEngine)
        ExerciseVizType.TONGUE_CLICKS, ExerciseVizType.EXPLOSIVE_CONSONANTS -> ArticulationCueStage(
            cues = if (exercise.vizType == ExerciseVizType.TONGUE_CLICKS) {
                listOf("ЦОК (Улыбка)", "ЦОК (Трубочка)", "ЦОК (Рупор)")
            } else {
                listOf("П-Т-К", "Б-Д-Г", "П-Б / Т-Д / К-Г")
            },
            durationSec = durationSec,
            soundEngine = soundEngine
        )
        ExerciseVizType.SENTENCE_DROP -> MarkedQuoteTimerStage(
            markupQuote = "«Проект полностью готов к запуску в *эфир* ↘»",
            durationSec = durationSec,
            soundEngine = soundEngine
        )
        ExerciseVizType.TEMPO_CONTRAST -> MarkedQuoteTimerStage(
            markupQuote = "«Мы проанализировали сотни показателей за секунду, || но главный *вывод* | оказался *простым* ↘»",
            durationSec = durationSec,
            soundEngine = soundEngine
        )
        ExerciseVizType.DRAMATIC_PAUSE -> MarkedQuoteTimerStage(
            markupQuote = "«Победителем становится... || (2 сек тишины) *ваш голос* ↘»",
            durationSec = durationSec,
            soundEngine = soundEngine
        )
    }
}

@Composable
private fun SimpleCountdownRow(
    totalSec: Int,
    soundEngine: StudioSoundEngine,
    buttonLabelStart: String = "▶ Старт"
) {
    var remaining by remember(totalSec) { mutableIntStateOf(totalSec) }
    var running by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        while (running && remaining > 0) {
            delay(1000L)
            remaining -= 1
            if (remaining <= 0) {
                running = false
                finished = true
                soundEngine.playChime(isComplete = true)
            }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (finished) "Готово!" else formatTimerMMSS(remaining),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
            color = if (finished) StudioEmerald else StudioAmber
        )
        Button(
            onClick = {
                if (running) {
                    running = false
                } else {
                    if (remaining <= 0) {
                        remaining = totalSec
                        finished = false
                    }
                    running = true
                    soundEngine.playChime(isComplete = false)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = StudioAmber,
                contentColor = Color(0xFF0B0F19)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.defaultMinSize(minHeight = 44.dp)
        ) {
            Text(
                text = when {
                    running -> "⏸ Пауза"
                    finished -> "↻ Повторить"
                    remaining < totalSec -> "▶ Продолжить"
                    else -> buttonLabelStart
                },
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun HiddenYawnStage(durationSec: Int, soundEngine: StudioSoundEngine) {
    val transition = rememberInfiniteTransition(label = "yawn")
    val anim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
        label = "yawn_cycle"
    )
    var phaseIdx by remember { mutableIntStateOf(0) }
    val phases = listOf(
        "Вдох носом с зевком (4с)" to StudioCyan,
        "Удержание объема глотки (3с)" to StudioEmerald,
        "Мягкий расслабленный выдох (5с)" to StudioAmber
    )
    LaunchedEffect(Unit) {
        val secs = listOf(4, 3, 5)
        while (true) {
            delay(secs[phaseIdx] * 1000L)
            phaseIdx = (phaseIdx + 1) % phases.size
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Canvas(modifier = Modifier.size(190.dp, 125.dp)) {
            val w = size.width
            val h = size.height
            drawRoundRect(
                color = StudioSurface,
                topLeft = Offset(w * 0.25f, h * 0.1f),
                size = Size(w * 0.5f, h * 0.8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(32f, 32f),
                style = Stroke(width = 4f)
            )
            // Rising soft palate
            val palateY = h * (0.42f - anim * 0.08f)
            val palatePath = Path().apply {
                moveTo(w * 0.34f, palateY + 12f)
                quadraticTo(w * 0.5f, palateY - 18f, w * 0.66f, palateY + 12f)
            }
            drawPath(palatePath, color = StudioEmerald, style = Stroke(width = 8f, cap = StrokeCap.Round))
            // Lowering larynx
            val larynxY = h * (0.62f + anim * 0.12f)
            drawRoundRect(
                color = StudioAmber,
                topLeft = Offset(w * 0.4f, larynxY),
                size = Size(w * 0.2f, 18f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(9f, 9f)
            )
        }
        Text(
            text = "Фаза: ${phases[phaseIdx].first}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = phases[phaseIdx].second
        )
        SimpleCountdownRow(durationSec, soundEngine)
    }
}

@Composable
private fun HorseLipsStage(durationSec: Int, soundEngine: StudioSoundEngine) {
    val transition = rememberInfiniteTransition(label = "horse")
    val flutter by transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(90, easing = LinearEasing), RepeatMode.Reverse),
        label = "flutter"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Canvas(modifier = Modifier.size(190.dp, 110.dp)) {
            val midY = size.height * 0.48f + flutter
            drawOval(
                color = StudioRose,
                topLeft = Offset(size.width * 0.24f, midY - 18f),
                size = Size(size.width * 0.52f, 22f)
            )
            drawOval(
                color = Color(0xFFFB7185),
                topLeft = Offset(size.width * 0.26f, midY + 4f),
                size = Size(size.width * 0.48f, 20f)
            )
            drawLine(
                color = StudioCyan,
                start = Offset(size.width * 0.15f, size.height * 0.85f),
                end = Offset(size.width * 0.85f, size.height * 0.92f),
                strokeWidth = 5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
            )
        }
        Text(
            text = "Сброс давления: ТПР-Р-Р ↘",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = StudioCyan
        )
        SimpleCountdownRow(durationSec, soundEngine)
    }
}

@Composable
private fun JawReleaseStage(soundEngine: StudioSoundEngine) {
    var left by remember { mutableIntStateOf(50) }
    var running by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        while (running && left > 0) {
            delay(1000L)
            left -= 1
            if (left == 35 || left == 20) soundEngine.playChime(false)
            if (left <= 0) {
                running = false
                soundEngine.playChime(true)
            }
        }
    }

    val stageIdx = when {
        left <= 0 -> 3
        left <= 20 -> 2
        left <= 35 -> 1
        else -> 0
    }
    val stageText = when (stageIdx) {
        0 -> "Этап 1 (15с): Массаж левой скулы"
        1 -> "Этап 2 (15с): Массаж правой скулы"
        2 -> "Этап 3 (20с): Мертвая челюсть + «МА-А-А»"
        else -> "Челюсть полностью расслаблена!"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Canvas(modifier = Modifier.size(160.dp, 120.dp)) {
            drawOval(
                color = StudioBorder,
                topLeft = Offset(size.width * 0.22f, size.height * 0.08f),
                size = Size(size.width * 0.56f, size.height * 0.84f),
                style = Stroke(width = 4f)
            )
            drawCircle(
                color = StudioAmber.copy(alpha = if (stageIdx == 0) 0.95f else 0.25f),
                radius = 18f,
                center = Offset(size.width * 0.28f, size.height * 0.52f)
            )
            drawCircle(
                color = StudioCyan.copy(alpha = if (stageIdx == 1) 0.95f else 0.25f),
                radius = 18f,
                center = Offset(size.width * 0.72f, size.height * 0.52f)
            )
            drawArc(
                color = StudioEmerald.copy(alpha = if (stageIdx >= 2) 1f else 0.3f),
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(size.width * 0.33f, size.height * 0.58f),
                size = Size(size.width * 0.34f, size.height * 0.28f),
                style = Stroke(width = 7f, cap = StrokeCap.Round)
            )
        }
        Text(text = stageText, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = StudioAmber)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = formatTimerMMSS(left), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black), color = StudioTextPrimary)
            Button(
                onClick = {
                    if (!running) {
                        if (left <= 0) left = 50
                        running = true
                        soundEngine.playChime(false)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = StudioAmber, contentColor = Color(0xFF0B0F19))
            ) {
                Text(if (running) "Идет разминка..." else "▶ Запустить 50с", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun NeckPendulumStage(soundEngine: StudioSoundEngine, onShowToast: (String) -> Unit) {
    var reps by remember { mutableIntStateOf(0) }
    val transition = rememberInfiniteTransition(label = "pendulum")
    val angle by transition.animateFloat(
        initialValue = -28f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "swing"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Canvas(modifier = Modifier.size(130.dp, 110.dp)) {
            rotate(degrees = angle, pivot = Offset(size.width / 2f, size.height * 0.25f)) {
                drawCircle(
                    color = StudioCyan,
                    radius = 36f,
                    center = Offset(size.width / 2f, size.height * 0.55f),
                    style = Stroke(width = 5f)
                )
                drawCircle(
                    color = StudioAmber,
                    radius = 8f,
                    center = Offset(size.width / 2f, size.height * 0.85f)
                )
            }
        }
        Text("$reps / 8 повторов", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = StudioTextPrimary)
        Button(
            onClick = {
                reps += 1
                soundEngine.playClick(reps % 4 == 0)
                if (reps == 8) {
                    soundEngine.playChime(true)
                    onShowToast("8 перекатов выполнено!")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = StudioCyan, contentColor = Color(0xFF0B0F19)),
            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
        ) {
            Text("+1 Перекат подбородка", fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun CandleBreathStage(
    candleLiveRms: Float,
    isCandleMicActive: Boolean,
    onStartCandleMic: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) onStartCandleMic() else onShowToast("Нет доступа к микрофону — используйте ползунок ниже")
    }
    var manualDeg by remember { mutableFloatStateOf(0f) }
    val activeDeg = if (isCandleMicActive) min(85f, candleLiveRms * 380f) else manualDeg

    val statusText: String
    val statusColor: Color
    when {
        activeDeg > 76f -> {
            statusText = "Слишком резко! Вы задули свечу — смягчите выдох"
            statusColor = StudioRose
        }
        activeDeg in 32f..62f -> {
            statusText = "Идеально (${activeDeg.toInt()}°): ровная опора на диафрагму!"
            statusColor = StudioEmerald
        }
        else -> {
            statusText = "Угол пламени: ${activeDeg.toInt()}° (цель: 35°–60°)"
            statusColor = StudioTextSecondary
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Canvas(modifier = Modifier.size(190.dp, 125.dp)) {
            val baseX = size.width * 0.45f
            val wickY = size.height * 0.58f
            // Target 45° green arc zone
            drawArc(
                color = StudioEmerald.copy(alpha = 0.22f),
                startAngle = -55f,
                sweepAngle = 35f,
                useCenter = true,
                topLeft = Offset(baseX - 95f, wickY - 95f),
                size = Size(190f, 190f)
            )
            // Candle body
            drawRoundRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(baseX - 18f, wickY + 8f),
                size = Size(36f, size.height - wickY - 12f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
            drawLine(color = StudioTextSecondary, start = Offset(baseX, wickY - 4f), end = Offset(baseX, wickY + 8f), strokeWidth = 4f)
            // Rotating Flame
            val flameAlpha = if (activeDeg > 76f) 0.18f else 1f
            rotate(degrees = activeDeg, pivot = Offset(baseX, wickY)) {
                drawOval(
                    color = StudioAmber.copy(alpha = flameAlpha),
                    topLeft = Offset(baseX - 13f, wickY - 48f),
                    size = Size(26f, 46f)
                )
                drawOval(
                    color = Color(0xFFFEF08A).copy(alpha = flameAlpha),
                    topLeft = Offset(baseX - 7f, wickY - 34f),
                    size = Size(14f, 30f)
                )
            }
        }

        Text(
            text = statusText,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = statusColor,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = {
                if (isCandleMicActive) return@Button
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                if (granted) onStartCandleMic() else launcher.launch(Manifest.permission.RECORD_AUDIO)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCandleMicActive) StudioEmerald else StudioAmber,
                contentColor = Color(0xFF0B0F19)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 46.dp)
                .testTag("candle_mic_button")
        ) {
            Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = if (isCandleMicActive) "Микрофон активен — дуйте «С-с-с»" else "Включить микрофон",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Ручная проверка потока «С-с-с»:", style = MaterialTheme.typography.labelSmall, color = StudioTextSecondary)
                Text("${manualDeg.toInt()}°", style = MaterialTheme.typography.labelSmall, color = StudioAmber)
            }
            Slider(
                value = manualDeg,
                onValueChange = { manualDeg = it },
                valueRange = 0f..85f,
                colors = SliderDefaults.colors(thumbColor = StudioAmber, activeTrackColor = StudioAmber)
            )
        }
    }
}

@Composable
private fun PumpStrelnikovaStage(soundEngine: StudioSoundEngine) {
    var running by remember { mutableStateOf(false) }
    var breath by remember { mutableIntStateOf(0) }
    var setNum by remember { mutableIntStateOf(1) }
    var done by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        while (running) {
            delay(1000L)
            breath += 1
            if (breath > 8) {
                breath = 1
                setNum += 1
            }
            if (setNum > 4) {
                running = false
                done = true
                soundEngine.playChime(true)
            } else {
                soundEngine.playClick(breath == 1)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(if (running && breath > 0) 1.08f else 1f)
                .background(StudioCyan.copy(alpha = 0.18f), CircleShape)
                .border(2.dp, StudioCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$breath/8", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = StudioTextPrimary)
                Text("Вдох носом", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StudioCyan)
            }
        }
        Text(
            text = if (done) "Все 4 серии по 8 вдохов выполнены!" else "Серия ${setNum.coerceAtMost(4)} из 4 • Темп: 1 вдох/сек",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = if (done) StudioEmerald else StudioTextSecondary
        )
        Button(
            onClick = {
                if (!running) {
                    breath = 0
                    setNum = 1
                    done = false
                    running = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = StudioAmber, contentColor = Color(0xFF0B0F19))
        ) {
            Text(
                text = when {
                    running -> "Идет серия..."
                    done -> "↻ Повторить"
                    else -> "▶ Запустить насос (4 × 8)"
                },
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DogBreathStage(durationSec: Int, soundEngine: StudioSoundEngine) {
    val transition = rememberInfiniteTransition(label = "dog")
    val scaleX by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(350), RepeatMode.Reverse),
        label = "pulse"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(110.dp, 88.dp)
                .scale(scaleX = scaleX, scaleY = 1f)
                .background(StudioBg, RoundedCornerShape(16.dp))
                .border(2.dp, StudioAmber, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ХА!", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = StudioAmber)
                Text("Толчок животом", fontSize = 10.sp, color = StudioTextSecondary)
            }
        }
        SimpleCountdownRow(durationSec, soundEngine)
    }
}

@Composable
private fun EgorkiStage(
    egorkiBestCount: Int,
    soundEngine: StudioSoundEngine,
    onRecordAttempt: (Int) -> Unit
) {
    var count by remember { mutableIntStateOf(0) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            color = StudioEmerald.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, StudioEmerald.copy(alpha = 0.45f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "Личный рекорд: $egorkiBestCount",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = StudioEmerald,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }
        Text(
            text = "$count",
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
            color = StudioAmber
        )
        Text(
            text = "«Как на горке, на пригорке стоят 33 Егорки: раз Егорка, два Егорка...»",
            style = MaterialTheme.typography.bodySmall,
            color = StudioTextSecondary,
            textAlign = TextAlign.Center
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    count += 1
                    soundEngine.playClick(count % 5 == 0)
                    onRecordAttempt(count)
                },
                colors = ButtonDefaults.buttonColors(containerColor = StudioAmber, contentColor = Color(0xFF0B0F19)),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("egorki_tap_button")
            ) {
                Text("+1 Егорка", fontWeight = FontWeight.Black)
            }
            OutlinedButton(
                onClick = { count = 0 },
                border = BorderStroke(1.dp, StudioBorder),
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Text("Сброс", color = StudioTextSecondary)
            }
        }
    }
}

@Composable
private fun TarzanChestStage(durationSec: Int, soundEngine: StudioSoundEngine) {
    val transition = rememberInfiniteTransition(label = "tarzan")
    val ring by transition.animateFloat(
        initialValue = 18f,
        targetValue = 52f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart),
        label = "ring"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(160.dp, 120.dp)) {
                val c = Offset(size.width / 2f, size.height / 2f)
                drawCircle(color = StudioAmber.copy(alpha = 0.7f), radius = ring, center = c, style = Stroke(width = 4f))
                drawCircle(color = StudioCyan.copy(alpha = 0.45f), radius = (ring * 0.65f), center = c, style = Stroke(width = 3f))
            }
            Text("М-М-М...", fontWeight = FontWeight.Black, color = StudioTextPrimary)
        }
        SimpleCountdownRow(durationSec, soundEngine)
    }
}

@Composable
private fun CrySighStage(durationSec: Int, soundEngine: StudioSoundEngine) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(modifier = Modifier.size(180.dp, 110.dp)) {
            drawCircle(color = StudioCyan, radius = 18f, center = Offset(size.width * 0.25f, size.height * 0.3f), style = Stroke(4f))
            val arc = Path().apply {
                moveTo(size.width * 0.32f, size.height * 0.3f)
                quadraticTo(size.width * 0.78f, size.height * 0.28f, size.width * 0.38f, size.height * 0.8f)
            }
            drawPath(arc, color = StudioAmber, style = Stroke(width = 6f, cap = StrokeCap.Round))
            drawRoundRect(
                color = StudioEmerald,
                topLeft = Offset(size.width * 0.16f, size.height * 0.68f),
                size = Size(54f, 28f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                style = Stroke(width = 4f)
            )
        }
        Text("Посыл звука по дуге вперед и в грудь: «Ма-а-а-м»", style = MaterialTheme.typography.bodySmall, color = StudioTextPrimary)
        SimpleCountdownRow(durationSec, soundEngine)
    }
}

@Composable
private fun MaskFocusStage(soundEngine: StudioSoundEngine) {
    val zones = listOf(
        "1. Переносица: «Н-н-н» (вибрация в носу)" to StudioCyan,
        "2. Губы и зубы: «М-м-м» (передняя маска)" to StudioAmber,
        "3. Грудь: «Ми-мэ-ма-мо-му» (глубокий объем)" to StudioEmerald
    )
    var activeIdx by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000L)
            activeIdx = (activeIdx + 1) % zones.size
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        zones.forEachIndexed { idx, (label, clr) ->
            val active = idx == activeIdx
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (active) StudioSurface else StudioBg, RoundedCornerShape(12.dp))
                    .border(1.dp, if (active) clr else StudioBorder, RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = if (active) clr else StudioTextSecondary)
            }
        }
        SimpleCountdownRow(54, soundEngine)
    }
}

@Composable
private fun VocalFryStage(soundEngine: StudioSoundEngine) {
    var sec by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            sec = (sec + 1) % 15
        }
    }
    val isPhase1 = sec < 5
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background((if (isPhase1) StudioAmber else StudioEmerald).copy(alpha = 0.14f), RoundedCornerShape(14.dp))
                .border(1.dp, if (isPhase1) StudioAmber else StudioEmerald, RoundedCornerShape(14.dp))
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isPhase1) "ФАЗА 1 (5С): ШТРОБАС" else "ФАЗА 2 (10С): ВЫХОД В ГРУДНОЙ ТОН",
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = if (isPhase1) StudioAmber else StudioEmerald
            )
            Text(
                text = if (isPhase1) "Минимальный выдох — тихий треск связок без давления" else "Плавно раскройте треск в ровный низкий «А-а-а»",
                fontSize = 12.sp,
                color = StudioTextPrimary,
                textAlign = TextAlign.Center
            )
        }
        SimpleCountdownRow(45, soundEngine)
    }
}

@Composable
private fun CorkTrainingStage(soundEngine: StudioSoundEngine) {
    var left by remember { mutableIntStateOf(90) }
    var running by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        while (running && left > 0) {
            delay(1000L)
            left -= 1
            if (left == 30) soundEngine.playChime(false)
            if (left <= 0) {
                running = false
                soundEngine.playChime(true)
            }
        }
    }

    val stageTwo = left in 1..30
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = (if (stageTwo) StudioEmerald else StudioAmber).copy(alpha = 0.16f),
            border = BorderStroke(1.dp, if (stageTwo) StudioEmerald else StudioAmber),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = when {
                    left <= 0 -> "Артикуляция активирована!"
                    stageTwo -> "Этап 2 (30с): Уберите пробку — читайте чисто!"
                    else -> "Этап 1 (60с): Чтение с пробкой в зубах"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (stageTwo || left <= 0) StudioEmerald else StudioAmber,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        Text(
            text = "«Бык тупогуб, тупогубенький бычок, у быка бела губа была тупа. Король-орёл, орёл-король!»",
            style = MaterialTheme.typography.bodySmall,
            color = StudioTextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioBg, RoundedCornerShape(12.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(formatTimerMMSS(left), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black), color = StudioTextPrimary)
            Button(
                onClick = {
                    if (!running) {
                        if (left <= 0) left = 90
                        running = true
                        soundEngine.playChime(false)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = StudioAmber, contentColor = Color(0xFF0B0F19))
            ) {
                Text(if (running) "Идет тренировка..." else "▶ Старт 90с", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TongueCleaningStage(soundEngine: StudioSoundEngine) {
    val transition = rememberInfiniteTransition(label = "tongue_orbit")
    val deg by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "orbit"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(modifier = Modifier.size(130.dp, 100.dp)) {
            drawOval(color = StudioBorder, topLeft = Offset(size.width * 0.18f, size.height * 0.16f), size = Size(size.width * 0.64f, size.height * 0.68f), style = Stroke(6f))
            val rad = deg * PI / 180.0
            val cx = size.width / 2f + (cos(rad) * size.width * 0.25).toFloat()
            val cy = size.height / 2f + (sin(rad) * size.height * 0.22).toFloat()
            drawCircle(color = StudioRose, radius = 12f, center = Offset(cx, cy))
        }
        Text("Круговая чистка десен языком при сомкнутых губах", fontSize = 12.sp, color = StudioTextSecondary)
        SimpleCountdownRow(40, soundEngine)
    }
}

@Composable
private fun LipsPoseStage(soundEngine: StudioSoundEngine) {
    val poses = listOf(
        Triple("1. ТРУБОЧКА («У»)", 36f, 36f),
        Triple("2. УЛЫБКА («И»)", 110f, 22f),
        Triple("3. РУПОР («О»)", 64f, 68f)
    )
    var idx by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000L)
            idx = (idx + 1) % poses.size
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(modifier = Modifier.size(140.dp, 90.dp)) {
            val w = poses[idx].second
            val h = poses[idx].third
            drawOval(
                color = StudioRose,
                topLeft = Offset((size.width - w) / 2f, (size.height - h) / 2f),
                size = Size(w, h),
                style = Stroke(width = 8f)
            )
        }
        Text(poses[idx].first, fontWeight = FontWeight.Black, color = StudioCyan)
        SimpleCountdownRow(45, soundEngine)
    }
}

@Composable
private fun SyllableMatrixModalStage(soundEngine: StudioSoundEngine) {
    val syls = listOf("БТКА", "БТКО", "БТКУ", "ПТКА", "ПТКО", "ПТКУ", "ГДКА", "ГДКО", "ГДКУ")
    var idx by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        while (running) {
            soundEngine.playClick(idx % 3 == 0)
            delay(650L)
            idx = (idx + 1) % syls.size
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            syls.chunked(3).forEachIndexed { r, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEachIndexed { c, s ->
                        val flat = r * 3 + c
                        val active = flat == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (active) StudioAmber else StudioBg, RoundedCornerShape(10.dp))
                                .border(1.dp, if (active) StudioAmber else StudioBorder, RoundedCornerShape(10.dp))
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(s, fontWeight = FontWeight.Black, color = if (active) Color(0xFF0B0F19) else StudioTextPrimary)
                        }
                    }
                }
            }
        }
        Button(
            onClick = { running = !running },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (running) StudioRose else StudioEmerald,
                contentColor = if (running) Color.White else Color(0xFF0B0F19)
            )
        ) {
            Text(if (running) "■ Стоп" else "▶ Запустить метроном слогов", fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun IntonationSwingStage(soundEngine: StudioSoundEngine) {
    val levels = listOf(
        "3. Верхний регистр (заинтересованно) ↗",
        "2. Деловой средний регистр (ровно) →",
        "1. Грудной низкий бас (авторитетно) ↘"
    )
    var lvl by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000L)
            lvl = (lvl + 1) % 3
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        levels.forEachIndexed { i, txt ->
            val active = i == lvl
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (active) StudioAmber.copy(alpha = 0.18f) else StudioBg, RoundedCornerShape(12.dp))
                    .border(1.dp, if (active) StudioAmber else StudioBorder, RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Text(txt, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = if (active) StudioAmber else StudioTextSecondary)
            }
        }
        Text("«Сегодня вечером мы подведем главные итоги недели»", fontSize = 12.sp, color = StudioTextPrimary, textAlign = TextAlign.Center)
        SimpleCountdownRow(45, soundEngine)
    }
}

@Composable
private fun SemanticMagnetStage(soundEngine: StudioSoundEngine) {
    var take by remember { mutableIntStateOf(0) }
    val takes = listOf(
        "«*Я* | не говорил, что он ушел» (именно я)",
        "«Я | *не говорил*, что он ушел» (отрицание)",
        "«Я не говорил, что | *он* ушел» (кто-то другой)",
        "«Я не говорил, что он | *ушел*» (он остался)"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (i in 0..3) {
                val sel = i == take
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (sel) StudioAmber else StudioBg,
                    border = if (sel) null else BorderStroke(1.dp, StudioBorder),
                    modifier = Modifier.clickable {
                        take = i
                        soundEngine.playClick(true)
                    }
                ) {
                    Text(
                        text = "Дубль ${i + 1}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (sel) Color(0xFF0B0F19) else StudioTextPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioBg, RoundedCornerShape(12.dp))
                .border(1.dp, StudioCyan.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = buildTeleprompterAnnotatedString(takes[take]),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MarkupReadingStage(onJumpToStudio: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("🎙 Студийный телесуфлер готов", fontWeight = FontWeight.Black, color = StudioTextPrimary)
        Text(
            text = "Откройте вкладку «Студия и ИИ», прочитайте текст с разметкой и получите разбор голоса.",
            fontSize = 12.sp,
            color = StudioTextSecondary,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onJumpToStudio,
            colors = ButtonDefaults.buttonColors(containerColor = StudioCyan, contentColor = Color(0xFF0B0F19))
        ) {
            Text("Перейти в Студию и ИИ →", fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun TongueRootDropStage(durationSec: Int, soundEngine: StudioSoundEngine) {
    val transition = rememberInfiniteTransition(label = "tongue_drop")
    val dy by transition.animateFloat(0f, 12f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "dy")
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(modifier = Modifier.size(160.dp, 100.dp)) {
            drawArc(color = StudioBorder, startAngle = -80f, sweepAngle = 160f, useCenter = false, topLeft = Offset(30f, 15f), size = Size(90f, 70f), style = Stroke(5f))
            drawOval(color = StudioRose.copy(alpha = 0.85f), topLeft = Offset(55f, 45f + dy), size = Size(75f, 22f))
        }
        Text("Корень языка расслаблен ↓", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StudioCyan)
        SimpleCountdownRow(durationSec, soundEngine)
    }
}

@Composable
private fun RagdollShouldersStage(durationSec: Int, soundEngine: StudioSoundEngine) {
    var sec by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            sec = (sec + 1) % 6
        }
    }
    val tense = sec < 2
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            color = (if (tense) StudioCyan else StudioAmber).copy(alpha = 0.16f),
            border = BorderStroke(1.dp, if (tense) StudioCyan else StudioAmber),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (tense) "Фаза: Вдох + плечи вверх к ушам (2с)" else "Фаза: Резкий сброс плеч вниз + «ХА-А-А» (4с)",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (tense) StudioCyan else StudioAmber,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
        SimpleCountdownRow(durationSec, soundEngine)
    }
}

@Composable
private fun BoxBreathStage(durationSec: Int, soundEngine: StudioSoundEngine) {
    var stepSec by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            stepSec = (stepSec + 1) % 16
        }
    }
    val phases = listOf("Вдох ребрами", "Задержка", "Выдох «Ф-ф-ф»", "Пауза")
    val pIdx = stepSec / 4
    val rem = 4 - (stepSec % 4)
    val scale = if (pIdx == 0 || pIdx == 1) 1.12f else 0.94f

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .scale(scale)
                .background(StudioCyan.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
                .border(2.dp, StudioCyan, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(phases[pIdx], fontSize = 11.sp, fontWeight = FontWeight.Black, color = StudioTextPrimary, textAlign = TextAlign.Center)
                Text("$rem", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = StudioAmber)
            }
        }
        SimpleCountdownRow(durationSec, soundEngine)
    }
}

@Composable
private fun SteppedExhaleStage(durationSec: Int, soundEngine: StudioSoundEngine) {
    var beat by remember { mutableIntStateOf(1) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(800L)
            beat = (beat + 1) % 5
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            for (i in 1..4) {
                val active = i == beat
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (active) StudioAmber else StudioBg, RoundedCornerShape(10.dp))
                        .border(1.dp, if (active) StudioAmber else StudioBorder, RoundedCornerShape(10.dp))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("С! #$i", fontWeight = FontWeight.Black, color = if (active) Color(0xFF0B0F19) else StudioTextSecondary)
                }
            }
        }
        SimpleCountdownRow(durationSec, soundEngine)
    }
}

@Composable
private fun VocalFryMorningStage(durationSec: Int, soundEngine: StudioSoundEngine) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(modifier = Modifier.size(220.dp, 70.dp)) {
            val mid = size.height / 2f
            val path = Path().apply {
                moveTo(0f, mid)
                for (x in 1..22) {
                    val px = (x / 22f) * size.width
                    val py = if (x % 3 == 0) mid - 20f else if (x % 3 == 1) mid + 20f else mid
                    lineTo(px, py)
                }
            }
            drawPath(path, color = StudioAmber, style = Stroke(width = 4f))
        }
        Text("Губы сомкнуты • Мягкий ровный треск «М-м-м» без зажима", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StudioAmber, textAlign = TextAlign.Center)
        SimpleCountdownRow(durationSec, soundEngine)
    }
}

@Composable
private fun VocalFryGlissandoStage(durationSec: Int, soundEngine: StudioSoundEngine) {
    var sec by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            sec = (sec + 1) % 12
        }
    }
    val active = when {
        sec < 3 -> 0
        sec < 7 -> 1
        else -> 2
    }
    val cards = listOf(
        "1. Треск (3с)" to "Штробас",
        "2. Мычание (4с)" to "Грудь «М-м-м»",
        "3. Бас (5с)" to "«М-А-А-М» ↘"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            cards.forEachIndexed { i, (t, sub) ->
                val isSel = i == active
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isSel) StudioAmber.copy(alpha = 0.18f) else StudioBg, RoundedCornerShape(10.dp))
                        .border(1.dp, if (isSel) StudioAmber else StudioBorder, RoundedCornerShape(10.dp))
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(t, fontSize = 10.sp, fontWeight = FontWeight.Black, color = if (isSel) StudioAmber else StudioTextSecondary)
                    Text(sub, fontSize = 10.sp, color = StudioTextPrimary)
                }
            }
        }
        SimpleCountdownRow(durationSec, soundEngine)
    }
}

@Composable
private fun VocalFrySyllablesStage(durationSec: Int, soundEngine: StudioSoundEngine) {
    val syls = listOf("БРА-А", "БРО-О", "БРУ-У", "МРА-А", "МРО-О", "МРУ-У", "ГРА-А", "ГРО-О", "ГРУ-У")
    var idx by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1400L)
            idx = (idx + 1) % syls.size
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            syls.chunked(3).forEachIndexed { r, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEachIndexed { c, s ->
                        val active = r * 3 + c == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (active) StudioAmber else StudioBg, RoundedCornerShape(10.dp))
                                .border(1.dp, if (active) StudioAmber else StudioBorder, RoundedCornerShape(10.dp))
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(s, fontWeight = FontWeight.Black, fontSize = 12.sp, color = if (active) Color(0xFF0B0F19) else StudioTextPrimary)
                        }
                    }
                }
            }
        }
        Text("Входите в каждый слог через легкий треск внизу", fontSize = 11.sp, color = StudioTextSecondary)
        SimpleCountdownRow(durationSec, soundEngine)
    }
}

@Composable
private fun VocalFryPhrasesStage(soundEngine: StudioSoundEngine) {
    var pIdx by remember { mutableIntStateOf(0) }
    val phrases = listOf(
        "«В мире, | где решает *каждый* звук ↘»",
        "«Остается только | *голос* из глубины ↘»",
        "«Настоящая *мощь* | не требует крика ↘»",
        "«Слушай | *тишину* между словами ↘»"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (i in 0..3) {
                val sel = i == pIdx
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (sel) StudioAmber else StudioBg,
                    border = if (sel) null else BorderStroke(1.dp, StudioBorder),
                    modifier = Modifier.clickable {
                        pIdx = i
                        soundEngine.playClick(true)
                    }
                ) {
                    Text(
                        text = "Фраза ${i + 1}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (sel) Color(0xFF0B0F19) else StudioTextPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioBg, RoundedCornerShape(12.dp))
                .border(1.dp, StudioAmber.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = buildTeleprompterAnnotatedString(phrases[pIdx]),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        Text("Зацепите выделенное слово штробасом ↘", fontSize = 11.sp, color = StudioAmber)
    }
}

@Composable
private fun ArticulationCueStage(cues: List<String>, durationSec: Int, soundEngine: StudioSoundEngine) {
    var idx by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1500L)
            idx = (idx + 1) % cues.size
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .background(StudioBg, RoundedCornerShape(16.dp))
                .border(2.dp, StudioCyan, RoundedCornerShape(16.dp))
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(cues[idx], style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = StudioCyan)
        }
        SimpleCountdownRow(durationSec, soundEngine)
    }
}

@Composable
private fun MarkedQuoteTimerStage(markupQuote: String, durationSec: Int, soundEngine: StudioSoundEngine) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioBg, RoundedCornerShape(12.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = buildTeleprompterAnnotatedString(markupQuote),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        SimpleCountdownRow(durationSec, soundEngine)
    }
}
