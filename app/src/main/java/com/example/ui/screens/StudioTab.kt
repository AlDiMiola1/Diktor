package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.RecordingChannel
import com.example.data.catalog.DiktorCatalog
import com.example.model.AiVerdict
import com.example.model.TeleprompterScript
import com.example.ui.AiCoachUiState
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudioTab(
    allScripts: List<TeleprompterScript>,
    customScriptsCount: Int,
    activeGenre: String,
    activeScriptId: String,
    prompterFontSizeSp: Int,
    isPrompterScrolling: Boolean,
    prompterSpeed: Float,
    isRecording: Boolean,
    recordingElapsedSec: Int,
    waveformSamples: List<Float>,
    hasRecordedTake: Boolean,
    isPlayingTake: Boolean,
    playbackProgress: Float,
    aiUiState: AiCoachUiState,
    onSelectGenre: (String) -> Unit,
    onSelectScript: (String) -> Unit,
    onAddCustomScript: (String, String, String) -> Unit,
    onDeleteCustomScript: (String) -> Unit,
    onAdjustFont: (Int) -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onTogglePrompterScroll: () -> Unit,
    onStopPrompterScroll: () -> Unit,
    onToggleRecording: (Boolean, () -> Unit) -> Unit,
    onMicPermissionGranted: () -> Unit,
    onMicPermissionDenied: () -> Unit,
    onTogglePlayback: () -> Unit,
    onAnalyzeGemini: () -> Unit,
    onAnalyzeAcoustic: () -> Unit,
    onOpenExercise: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onMicPermissionGranted() else onMicPermissionDenied()
    }

    var showCustomForm by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newGenre by remember { mutableStateOf("custom") }
    var newBody by remember { mutableStateOf(TextFieldValue("")) }

    val filteredScripts = remember(allScripts, activeGenre) {
        when (activeGenre) {
            "all" -> allScripts
            "custom" -> allScripts.filter { it.isCustom }
            else -> allScripts.filter { it.genre == activeGenre }
        }
    }
    val activeScript = remember(allScripts, filteredScripts, activeScriptId) {
        allScripts.find { it.id == activeScriptId }
            ?: filteredScripts.firstOrNull()
            ?: allScripts.firstOrNull()
    }

    val prompterScrollState = rememberScrollState()
    LaunchedEffect(activeScript?.id) {
        prompterScrollState.scrollTo(0)
        onStopPrompterScroll()
    }
    LaunchedEffect(isPrompterScrolling, prompterSpeed, activeScript?.id) {
        while (isPrompterScrolling) {
            delay(32L)
            val max = prompterScrollState.maxValue
            if (max <= 0 || prompterScrollState.value >= max) {
                onStopPrompterScroll()
                break
            }
            val delta = (prompterSpeed * 1.8f).toInt().coerceAtLeast(1)
            prompterScrollState.scrollTo((prompterScrollState.value + delta).coerceAtMost(max))
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Teleprompter Card
            item(key = "teleprompter_card") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioCard, RoundedCornerShape(18.dp))
                        .border(1.dp, StudioBorder, RoundedCornerShape(18.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "СТУДИЙНЫЙ ТЕЛЕСУФЛЕР",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                color = StudioAmber
                            )
                            Text(
                                text = "Дикторская разметка пауз и акцентов",
                                style = MaterialTheme.typography.labelSmall,
                                color = StudioTextSecondary
                            )
                        }
                        OutlinedButton(
                            onClick = { showCustomForm = !showCustomForm },
                            border = BorderStroke(1.dp, StudioAmber.copy(alpha = 0.55f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = StudioAmber.copy(alpha = 0.14f),
                                contentColor = StudioAmber
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 40.dp)
                                .testTag("toggle_custom_script_form")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("Свой текст", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold))
                        }
                    }

                    if (showCustomForm) {
                        CustomScriptEditorBox(
                            title = newTitle,
                            onTitleChange = { newTitle = it },
                            genre = newGenre,
                            onGenreChange = { newGenre = it },
                            body = newBody,
                            onBodyChange = { newBody = it },
                            onSave = {
                                if (newBody.text.isNotBlank()) {
                                    onAddCustomScript(newTitle, newGenre, newBody.text)
                                    newTitle = ""
                                    newGenre = "custom"
                                    newBody = TextFieldValue("")
                                    showCustomForm = false
                                }
                            }
                        )
                    }

                    // Genre filters
                    val genres = listOf(
                        "all" to "Все",
                        "news" to "Новости",
                        "trailer" to "Реклама",
                        "audiobook" to "Аудиокнига",
                        "custom" to "Мои ($customScriptsCount)"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        genres.forEach { (key, label) ->
                            val selected = activeGenre == key
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) StudioCyan else StudioBg,
                                border = if (selected) null else BorderStroke(1.dp, StudioBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelectGenre(key) }
                                    .testTag("studio_genre_$key")
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (selected) Color(0xFF0B0F19) else StudioTextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    // Script selector carousel
                    if (filteredScripts.isEmpty()) {
                        Text(
                            text = "В этом разделе пока нет сценариев. Нажмите «+ Свой текст».",
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioTextSecondary
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(items = filteredScripts, key = { it.id }) { sc ->
                                val selected = sc.id == activeScript?.id
                                Column(
                                    modifier = Modifier
                                        .widthIn(min = 136.dp, max = 200.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (selected) StudioAmber.copy(alpha = 0.16f) else StudioBg)
                                        .border(
                                            1.dp,
                                            if (selected) StudioAmber else StudioBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onSelectScript(sc.id) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = sc.title,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = StudioTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (sc.isCustom) {
                                            Surface(color = StudioAmber, shape = RoundedCornerShape(4.dp)) {
                                                Text(
                                                    text = "Мой",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF0B0F19),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = sc.genreLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StudioTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Active Teleprompter Viewport
                    if (activeScript != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = activeScript.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = StudioTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (activeScript.isCustom) {
                                    Surface(color = StudioAmber.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            text = "Мой",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StudioAmber,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            if (activeScript.isCustom) {
                                IconButton(
                                    onClick = { onDeleteCustomScript(activeScript.id) },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("delete_custom_script_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Удалить сценарий",
                                        tint = StudioRose,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        val formattedText = remember(activeScript.text) {
                            buildTeleprompterAnnotatedString(activeScript.text)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 135.dp, max = 220.dp)
                                .background(StudioBg, RoundedCornerShape(14.dp))
                                .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
                                .verticalScroll(prompterScrollState)
                                .padding(14.dp)
                        ) {
                            Text(
                                text = formattedText,
                                fontSize = prompterFontSizeSp.sp,
                                lineHeight = (prompterFontSizeSp * 1.48f).sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Teleprompter controls: A- / A+ / Speed / Auto-scroll
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SmallControlPill(text = "A-", onClick = { onAdjustFont(-2) })
                                SmallControlPill(text = "A+", onClick = { onAdjustFont(2) })
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(1.0f to "1.0x", 1.6f to "1.6x", 2.4f to "2.4x").forEach { (spd, lbl) ->
                                    SmallControlPill(
                                        text = lbl,
                                        selected = prompterSpeed == spd,
                                        onClick = { onSelectSpeed(spd) }
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = onTogglePrompterScroll,
                                border = BorderStroke(
                                    1.dp,
                                    if (isPrompterScrolling) StudioAmber else StudioBorder
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isPrompterScrolling) StudioAmber.copy(alpha = 0.14f) else StudioBg,
                                    contentColor = if (isPrompterScrolling) StudioAmber else StudioTextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 40.dp)
                                    .testTag("toggle_prompter_scroll_button")
                            ) {
                                Icon(
                                    imageVector = if (isPrompterScrolling) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text(
                                    text = if (isPrompterScrolling) "Пауза" else "Прокрутка",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Studio Recorder & AI Coach Card
            item(key = "recorder_ai_card") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioCard, RoundedCornerShape(18.dp))
                        .border(1.dp, StudioBorder, RoundedCornerShape(18.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(if (isRecording) StudioRose else StudioEmerald, CircleShape)
                            )
                            Text(
                                text = if (isRecording) "ON AIR • Идет запись" else "СТУДИЙНЫЙ МИКРОФОН",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                color = if (isRecording) StudioRose else StudioTextPrimary
                            )
                        }
                        Text(
                            text = formatTimerMMSS(recordingElapsedSec),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = if (isRecording) StudioRose else StudioAmber
                        )
                    }

                    // Live Oscilloscope Waveform Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .background(StudioBg, RoundedCornerShape(12.dp))
                            .border(1.dp, StudioBorder, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val midY = size.height / 2f
                            if (waveformSamples.isEmpty()) {
                                drawLine(
                                    color = StudioBorder,
                                    start = Offset(0f, midY),
                                    end = Offset(size.width, midY),
                                    strokeWidth = 3f
                                )
                            } else {
                                val stepX = size.width / (waveformSamples.size - 1).coerceAtLeast(1)
                                val path = Path()
                                waveformSamples.forEachIndexed { idx, amp ->
                                    val x = idx * stepX
                                    val sign = if (idx % 2 == 0) 1f else -1f
                                    val y = midY + sign * amp * (size.height * 0.42f)
                                    if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                }
                                drawPath(
                                    path = path,
                                    color = if (isRecording) StudioAmber else StudioCyan,
                                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                                )
                            }
                        }
                        if (!isRecording && waveformSamples.isEmpty()) {
                            Text(
                                text = "Осциллограмма появится при записи дубля",
                                style = MaterialTheme.typography.labelSmall,
                                color = StudioTextSecondary
                            )
                        }
                    }

                    // Record Button
                    Button(
                        onClick = {
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            onToggleRecording(granted) {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) StudioRose else StudioAmber,
                            contentColor = if (isRecording) Color.White else Color(0xFF0B0F19)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 52.dp)
                            .testTag("studio_record_button")
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = if (isRecording) "Остановить запись" else if (hasRecordedTake) "Записать новый дубль" else "Записать дубль",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                        )
                    }

                    // Playback + Analysis Controls when recorded
                    if (hasRecordedTake && !isRecording) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StudioBg, RoundedCornerShape(14.dp))
                                .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                IconButton(
                                    onClick = onTogglePlayback,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(StudioCyan.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                                        .testTag("studio_playback_button")
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingTake) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                        contentDescription = "Прослушать дубль",
                                        tint = StudioCyan
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isPlayingTake) "Воспроизведение дубля..." else "Записанный дубль готов",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = StudioTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { playbackProgress },
                                        color = StudioCyan,
                                        trackColor = StudioSurface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(CircleShape)
                                    )
                                }
                            }

                            Button(
                                onClick = onAnalyzeGemini,
                                enabled = aiUiState !is AiCoachUiState.Loading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StudioEmerald,
                                    contentColor = Color(0xFF0B0F19)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("evaluate_gemini_button")
                            ) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(
                                    text = "Разобрать дубль с ИИ-режиссером",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                                )
                            }

                            OutlinedButton(
                                onClick = onAnalyzeAcoustic,
                                enabled = aiUiState !is AiCoachUiState.Loading,
                                border = BorderStroke(1.dp, StudioBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = StudioCard,
                                    contentColor = StudioCyan
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 44.dp)
                                    .testTag("evaluate_acoustic_button")
                            ) {
                                Icon(Icons.Filled.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(
                                    text = "Акустический экспресс-анализ (офлайн)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    // AI State Output
                    when (aiUiState) {
                        AiCoachUiState.Idle -> Unit
                        is AiCoachUiState.Loading -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(StudioBg, RoundedCornerShape(14.dp))
                                    .border(1.dp, StudioCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = StudioCyan,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = aiUiState.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = StudioCyan
                                )
                            }
                        }
                        is AiCoachUiState.Error -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(StudioRose.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                                    .border(1.dp, StudioRose.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = aiUiState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StudioRose
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = onAnalyzeGemini,
                                        border = BorderStroke(1.dp, StudioAmber),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioAmber)
                                    ) {
                                        Text("Повторить ИИ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = onAnalyzeAcoustic,
                                        border = BorderStroke(1.dp, StudioCyan),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioCyan)
                                    ) {
                                        Text("Акустический разбор", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        is AiCoachUiState.Success -> {
                            AiVerdictCard(
                                verdict = aiUiState.verdict,
                                onPracticeExercise = onOpenExercise
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomScriptEditorBox(
    title: String,
    onTitleChange: (String) -> Unit,
    genre: String,
    onGenreChange: (String) -> Unit,
    body: TextFieldValue,
    onBodyChange: (TextFieldValue) -> Unit,
    onSave: () -> Unit
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = StudioAmber,
        unfocusedBorderColor = StudioBorder,
        focusedTextColor = StudioTextPrimary,
        unfocusedTextColor = StudioTextPrimary,
        focusedContainerColor = StudioBg,
        unfocusedContainerColor = StudioBg
    )

    fun insertAtCursor(snippet: String) {
        val start = body.selection.start.coerceIn(0, body.text.length)
        val end = body.selection.end.coerceIn(start, body.text.length)
        val newText = body.text.substring(0, start) + snippet + body.text.substring(end)
        val cursor = start + snippet.length
        onBodyChange(TextFieldValue(text = newText, selection = TextRange(cursor)))
    }

    fun wrapAccentAtCursor() {
        val start = body.selection.start.coerceIn(0, body.text.length)
        val end = body.selection.end.coerceIn(start, body.text.length)
        val selected = body.text.substring(start, end).ifEmpty { "слово" }
        val wrapped = "*$selected*"
        val newText = body.text.substring(0, start) + wrapped + body.text.substring(end)
        onBodyChange(TextFieldValue(text = newText, selection = TextRange(start + 1, start + 1 + selected.length)))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioSurface.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
            .border(1.dp, StudioAmber.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Название сценария") },
            singleLine = true,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "custom" to "Мой сценарий",
                "news" to "Новости",
                "trailer" to "Реклама",
                "audiobook" to "Аудиокнига"
            ).forEach { (key, label) ->
                val sel = genre == key
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (sel) StudioAmber else StudioBg,
                    border = if (sel) null else BorderStroke(1.dp, StudioBorder),
                    modifier = Modifier.clickable { onGenreChange(key) }
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (sel) Color(0xFF0B0F19) else StudioTextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Markup toolbar
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SmallControlPill(text = "+ |", onClick = { insertAtCursor(" | ") })
            SmallControlPill(text = "+ ||", onClick = { insertAtCursor(" || ") })
            SmallControlPill(text = "+ *акцент*", onClick = { wrapAccentAtCursor() })
            SmallControlPill(text = "+ ↗", onClick = { insertAtCursor(" ↗") })
            SmallControlPill(text = "+ ↘", onClick = { insertAtCursor(" ↘") })
        }

        OutlinedTextField(
            value = body,
            onValueChange = onBodyChange,
            label = { Text("Текст для телесуфлера") },
            minLines = 3,
            maxLines = 6,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSave,
            colors = ButtonDefaults.buttonColors(
                containerColor = StudioAmber,
                contentColor = Color(0xFF0B0F19)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 46.dp)
                .testTag("save_custom_script_button")
        ) {
            Text("Сохранить в телесуфлер", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun AiVerdictCard(
    verdict: AiVerdict,
    onPracticeExercise: (String) -> Unit
) {
    val recEx = DiktorCatalog.findExercise(verdict.recommendedExId) ?: DiktorCatalog.EXERCISES.first()
    val metrics = listOf(
        "Грудной резонатор" to verdict.chestScore,
        "Опора и концовки" to verdict.supportScore,
        "Темп и паузы" to verdict.pauseScore,
        "Четкость дикции" to verdict.dictionScore
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioBg, RoundedCornerShape(16.dp))
            .border(1.dp, StudioEmerald.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = StudioCyan.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = verdict.modeLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = StudioCyan,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = verdict.headline,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                    color = StudioTextPrimary
                )
            }

            Surface(
                color = StudioAmber.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, StudioAmber),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${verdict.totalScore}/10",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = StudioAmber,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            metrics.forEach { (label, score) ->
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall, color = StudioTextSecondary)
                        Text(
                            "$score/10",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = StudioAmber
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (score / 10f).coerceIn(0f, 1f) },
                        color = StudioAmber,
                        trackColor = StudioSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }

        HorizontalDivider(color = StudioBorder)

        Text(
            text = "Зона роста: ${verdict.critique}",
            style = MaterialTheme.typography.bodySmall,
            color = StudioTextPrimary
        )
        Text(
            text = "Совет режиссера: ${verdict.advice}",
            style = MaterialTheme.typography.bodySmall,
            color = StudioEmerald
        )

        Button(
            onClick = { onPracticeExercise(recEx.id) },
            colors = ButtonDefaults.buttonColors(
                containerColor = StudioCyan,
                contentColor = Color(0xFF0B0F19)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 46.dp)
                .testTag("practice_recommended_exercise_button")
        ) {
            Text(
                text = "Отработать: «${recEx.title}»",
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun SmallControlPill(
    text: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) StudioAmber else StudioBg,
        border = if (selected) null else BorderStroke(1.dp, StudioBorder),
        modifier = Modifier
            .defaultMinSize(minHeight = 36.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (selected) Color(0xFF0B0F19) else StudioTextPrimary
            )
        }
    }
}

fun formatTimerMMSS(sec: Int): String {
    val safe = sec.coerceAtLeast(0)
    return "%02d:%02d".format(safe / 60, safe % 60)
}
