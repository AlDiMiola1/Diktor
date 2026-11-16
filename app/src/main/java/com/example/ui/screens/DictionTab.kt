package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.catalog.DiktorCatalog
import com.example.model.DictionTable
import com.example.model.TongueTwister
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DictionTab(
    bpm: Int,
    isMetroRunning: Boolean,
    beatIndex: Int,
    syllableIndex: Int,
    activeTableKey: String,
    activeTable: DictionTable,
    customTables: List<DictionTable>,
    allTwisters: List<TongueTwister>,
    customTwistersCount: Int,
    activeTwisterFilter: String,
    activeTwisterId: String,
    isQuickRecording: Boolean,
    quickElapsedSec: Int,
    hasQuickRecording: Boolean,
    isQuickPlaying: Boolean,
    quickPlayProgress: Float,
    onUpdateBpm: (Int) -> Unit,
    onToggleMetro: () -> Unit,
    onSelectTable: (String) -> Unit,
    onSelectCustomTable: (String) -> Unit,
    onAddCustomTable: (String, String) -> Unit,
    onDeleteCustomTable: (String) -> Unit,
    onSelectTwisterFilter: (String) -> Unit,
    onSelectTwister: (String) -> Unit,
    onCycleNextTwister: () -> Unit,
    onAddCustomTwister: (String, String, String) -> Unit,
    onDeleteCustomTwister: (String) -> Unit,
    onToggleQuickRec: (Boolean, () -> Unit) -> Unit,
    onQuickMicGranted: () -> Unit,
    onQuickMicDenied: () -> Unit,
    onToggleQuickPlayback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val quickMicLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onQuickMicGranted() else onQuickMicDenied()
    }

    var showCustomTableForm by remember { mutableStateOf(false) }
    var newTableTitle by remember { mutableStateOf("") }
    var newTableClusters by remember { mutableStateOf("") }
    val previewSyllables = remember(newTableClusters) {
        DiktorCatalog.parseClustersToSyllables(newTableClusters)
    }

    var showNewTwisterForm by remember { mutableStateOf(false) }
    var selectedSoundTag by remember { mutableStateOf("Р / Л") }
    var customSoundTag by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("Базовая") }
    var newTwisterText by remember { mutableStateOf("") }

    val filteredTwisters = remember(allTwisters, activeTwisterFilter) {
        when (activeTwisterFilter) {
            "all" -> allTwisters
            "my" -> allTwisters.filter { it.isCustom }
            else -> allTwisters.filter { it.soundTag == activeTwisterFilter }
        }
    }
    val activeTwister = remember(allTwisters, filteredTwisters, activeTwisterId) {
        allTwisters.find { it.id == activeTwisterId }
            ?: filteredTwisters.firstOrNull()
            ?: allTwisters.firstOrNull()
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
            // Metronome Card
            item(key = "metronome_card") {
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
                                text = "СТУДИЙНЫЙ МЕТРОНОМ",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                color = StudioAmber
                            )
                            Text(
                                text = "Чеканка согласных и контроль темпоритма",
                                style = MaterialTheme.typography.labelSmall,
                                color = StudioTextSecondary
                            )
                        }
                        Text(
                            text = "$bpm BPM",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = StudioCyan
                        )
                    }

                    // 4 Beat LEDs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (i in 0 until 4) {
                            val active = isMetroRunning && beatIndex == i
                            val ledColor = when {
                                active && i == 0 -> StudioAmber
                                active -> StudioCyan
                                else -> StudioBg
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(10.dp)
                                    .background(ledColor, CircleShape)
                                    .border(1.dp, if (active) ledColor else StudioBorder, CircleShape)
                            )
                        }
                    }

                    Slider(
                        value = bpm.toFloat(),
                        onValueChange = { onUpdateBpm(it.toInt()) },
                        valueRange = 50f..140f,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioAmber,
                            activeTrackColor = StudioAmber,
                            inactiveTrackColor = StudioSurface
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(60, 90, 120).forEach { preset ->
                                val isSel = bpm == preset
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) StudioAmber else StudioBg,
                                    border = if (isSel) null else BorderStroke(1.dp, StudioBorder),
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onUpdateBpm(preset) }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "$preset",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSel) Color(0xFF0B0F19) else StudioTextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = onToggleMetro,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMetroRunning) StudioRose else StudioEmerald,
                                contentColor = if (isMetroRunning) Color.White else Color(0xFF0B0F19)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("diction_metronome_button")
                        ) {
                            Icon(
                                imageVector = if (isMetroRunning) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = if (isMetroRunning) "Стоп" else "Старт",
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Syllable Matrices Card
            item(key = "syllable_matrices_card") {
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
                        Text(
                            text = "ТАБЛИЦЫ СОГЛАСНЫХ",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                            color = StudioTextPrimary
                        )
                        OutlinedButton(
                            onClick = { showCustomTableForm = !showCustomTableForm },
                            border = BorderStroke(1.dp, StudioAmber.copy(alpha = 0.55f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = StudioAmber.copy(alpha = 0.14f),
                                contentColor = StudioAmber
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 40.dp)
                                .testTag("toggle_custom_table_form")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("Своя таблица", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    if (showCustomTableForm) {
                        val fieldColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = StudioTextPrimary,
                            unfocusedTextColor = StudioTextPrimary,
                            focusedContainerColor = StudioBg,
                            unfocusedContainerColor = StudioBg
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StudioSurface.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                                .border(1.dp, StudioAmber.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newTableTitle,
                                onValueChange = { newTableTitle = it },
                                label = { Text("Название набора") },
                                singleLine = true,
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newTableClusters,
                                onValueChange = { newTableClusters = it },
                                label = { Text("Сочетания согласных (напр: КПТ, ЗДР, ВСТР)") },
                                minLines = 2,
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Предпросмотр: ${previewSyllables.size} слогов",
                                style = MaterialTheme.typography.labelSmall,
                                color = StudioCyan
                            )
                            if (previewSyllables.isNotEmpty()) {
                                Text(
                                    text = previewSyllables.take(18).joinToString(" • "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StudioTextSecondary
                                )
                            }
                            Button(
                                onClick = {
                                    if (previewSyllables.isNotEmpty()) {
                                        onAddCustomTable(newTableTitle, newTableClusters)
                                        newTableTitle = ""
                                        newTableClusters = ""
                                        showCustomTableForm = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StudioAmber,
                                    contentColor = Color(0xFF0B0F19)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 46.dp)
                                    .testTag("save_custom_table_button")
                            ) {
                                Text("Сохранить таблицу", fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    // Table level tabs
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val tabs = listOf(
                            "lvl1" to "1. База (18)",
                            "lvl2" to "2. Средний (30)",
                            "lvl3" to "3. Хардкор (42)",
                            "custom" to "Мои (${customTables.size})"
                        )
                        tabs.forEach { (key, label) ->
                            val sel = activeTableKey == key
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (sel) StudioAmber else StudioBg,
                                border = if (sel) null else BorderStroke(1.dp, StudioBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelectTable(key) }
                                    .testTag("diction_table_$key")
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (sel) Color(0xFF0B0F19) else StudioTextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    if (activeTableKey == "custom" && customTables.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            customTables.forEach { tbl ->
                                val isSel = tbl.id == activeTable.id
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) StudioCyan.copy(alpha = 0.2f) else StudioBg,
                                    border = BorderStroke(1.dp, if (isSel) StudioCyan else StudioBorder)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clickable { onSelectCustomTable(tbl.id) }
                                            .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                                    ) {
                                        Text(
                                            text = tbl.title,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) StudioCyan else StudioTextPrimary
                                        )
                                        IconButton(
                                            onClick = { onDeleteCustomTable(tbl.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Close,
                                                contentDescription = "Удалить таблицу",
                                                tint = StudioRose,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = activeTable.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = StudioTextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        val sylCount = activeTable.syllables.size
                        val currentNum = if (sylCount > 0) (syllableIndex % sylCount) + 1 else 0
                        Text(
                            text = "Слог: $currentNum / $sylCount",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = StudioAmber
                        )
                    }

                    // 6-column syllable grid
                    if (activeTable.syllables.isEmpty()) {
                        Text(
                            text = "У вас пока нет своих таблиц согласных. Нажмите «+ Своя таблица» выше.",
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StudioBg, RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            activeTable.syllables.chunked(6).forEachIndexed { rowIdx, row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    for (colIdx in 0 until 6) {
                                        val flatIdx = rowIdx * 6 + colIdx
                                        val syl = row.getOrNull(colIdx)
                                        if (syl == null) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        } else {
                                            val isHighlighted = isMetroRunning && (syllableIndex % activeTable.syllables.size) == flatIdx
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(
                                                        if (isHighlighted) StudioAmber else StudioBg,
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (isHighlighted) StudioAmber else StudioBorder,
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(vertical = 7.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = syl,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isHighlighted) FontWeight.Black else FontWeight.Bold,
                                                    color = if (isHighlighted) Color(0xFF0B0F19) else StudioTextPrimary,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tongue Twisters Card
            item(key = "twisters_header_card") {
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
                        Text(
                            text = "ДИКТОРСКИЕ СКОРОГОВОРКИ",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                            color = StudioTextPrimary
                        )
                        OutlinedButton(
                            onClick = { showNewTwisterForm = !showNewTwisterForm },
                            border = BorderStroke(1.dp, StudioAmber.copy(alpha = 0.55f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = StudioAmber.copy(alpha = 0.14f),
                                contentColor = StudioAmber
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 40.dp)
                                .testTag("toggle_new_twister_form")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("Добавить", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    if (showNewTwisterForm) {
                        val fieldColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = StudioTextPrimary,
                            unfocusedTextColor = StudioTextPrimary,
                            focusedContainerColor = StudioBg,
                            unfocusedContainerColor = StudioBg
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StudioSurface.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                                .border(1.dp, StudioAmber.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Целевые звуки:", style = MaterialTheme.typography.labelSmall, color = StudioTextSecondary)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Р / Л", "С / З / Ш", "П / Т / К", "Шипящие", "Свой вариант").forEach { tag ->
                                    val sel = selectedSoundTag == tag
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (sel) StudioCyan else StudioBg,
                                        border = if (sel) null else BorderStroke(1.dp, StudioBorder),
                                        modifier = Modifier.clickable { selectedSoundTag = tag }
                                    ) {
                                        Text(
                                            text = tag,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sel) Color(0xFF0B0F19) else StudioTextPrimary,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                            if (selectedSoundTag == "Свой вариант") {
                                OutlinedTextField(
                                    value = customSoundTag,
                                    onValueChange = { customSoundTag = it },
                                    label = { Text("Укажите звуки (напр. Б / В)") },
                                    singleLine = true,
                                    colors = fieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Text("Сложность:", style = MaterialTheme.typography.labelSmall, color = StudioTextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Базовая", "Сложная", "Хардкор").forEach { diff ->
                                    val sel = selectedDifficulty == diff
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (sel) StudioEmerald else StudioBg,
                                        border = if (sel) null else BorderStroke(1.dp, StudioBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedDifficulty = diff }
                                    ) {
                                        Text(
                                            text = diff,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sel) Color(0xFF0B0F19) else StudioTextPrimary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = newTwisterText,
                                onValueChange = { newTwisterText = it },
                                label = { Text("Текст скороговорки") },
                                minLines = 2,
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    val finalTag = if (selectedSoundTag == "Свой вариант") {
                                        customSoundTag.trim().ifEmpty { "Свои звуки" }
                                    } else {
                                        selectedSoundTag
                                    }
                                    if (newTwisterText.isNotBlank()) {
                                        onAddCustomTwister(finalTag, selectedDifficulty, newTwisterText)
                                        newTwisterText = ""
                                        customSoundTag = ""
                                        showNewTwisterForm = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StudioAmber,
                                    contentColor = Color(0xFF0B0F19)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 46.dp)
                                    .testTag("save_new_twister_button")
                            ) {
                                Text("Сохранить скороговорку", fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    // Active Featured Twister + Quick Dictation Recorder
                    if (activeTwister != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StudioBg, RoundedCornerShape(14.dp))
                                .border(1.dp, StudioAmber.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(color = StudioCyan.copy(alpha = 0.16f), shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            text = activeTwister.soundTag,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StudioCyan,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Surface(color = StudioSurface, shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            text = activeTwister.difficulty,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = StudioTextSecondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (activeTwister.isCustom) {
                                        Surface(color = StudioAmber, shape = RoundedCornerShape(6.dp)) {
                                            Text(
                                                text = "Моя",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF0B0F19),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                OutlinedButton(
                                    onClick = onCycleNextTwister,
                                    border = BorderStroke(1.dp, StudioBorder),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                                ) {
                                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.size(4.dp))
                                    Text("Следующая", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Text(
                                text = activeTwister.text,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 22.sp
                                ),
                                color = StudioTextPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val granted = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED
                                        onToggleQuickRec(granted) {
                                            quickMicLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    },
                                    border = BorderStroke(1.dp, StudioRose.copy(alpha = 0.65f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isQuickRecording) StudioRose else StudioCard,
                                        contentColor = if (isQuickRecording) Color.White else StudioRose
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = 46.dp)
                                        .testTag("quick_twister_record_button")
                                ) {
                                    Icon(
                                        imageVector = if (isQuickRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.size(6.dp))
                                    Text(
                                        text = if (isQuickRecording) "Стоп (${formatTimerMMSS(quickElapsedSec)})" else "Записать скороговорку",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                }

                                if (hasQuickRecording && !isQuickRecording) {
                                    IconButton(
                                        onClick = onToggleQuickPlayback,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .background(StudioCyan.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
                                            .border(1.dp, StudioCyan.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                            .testTag("quick_twister_play_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isQuickPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                            contentDescription = "Прослушать скороговорку",
                                            tint = StudioCyan
                                        )
                                    }
                                }
                            }

                            if (isQuickPlaying) {
                                LinearProgressIndicator(
                                    progress = { quickPlayProgress },
                                    color = StudioCyan,
                                    trackColor = StudioSurface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }

                    // Twister Filter Chips
                    val filterOptions = listOf(
                        "all" to "Все",
                        "Р / Л" to "Р / Л",
                        "С / З / Ш" to "С / З / Ш",
                        "П / Т / К" to "П / Т / К",
                        "Шипящие" to "Шипящие",
                        "my" to "Мои ($customTwistersCount)"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filterOptions.forEach { (key, label) ->
                            val sel = activeTwisterFilter == key
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (sel) StudioAmber else StudioBg,
                                border = if (sel) null else BorderStroke(1.dp, StudioBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelectTwisterFilter(key) }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sel) Color(0xFF0B0F19) else StudioTextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (filteredTwisters.isEmpty()) {
                item(key = "twisters_empty") {
                    Text(
                        text = "В этой категории пока нет скороговорок. Нажмите «+ Добавить».",
                        style = MaterialTheme.typography.bodySmall,
                        color = StudioTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StudioCard, RoundedCornerShape(14.dp))
                            .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    )
                }
            } else {
                items(items = filteredTwisters, key = { it.id }) { tw ->
                    val isSel = tw.id == activeTwister?.id
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSel) StudioAmber.copy(alpha = 0.12f) else StudioCard)
                            .border(
                                1.dp,
                                if (isSel) StudioAmber.copy(alpha = 0.7f) else StudioBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectTwister(tw.id) }
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(color = StudioCyan.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                                    Text(
                                        text = tw.soundTag,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioCyan,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = tw.difficulty,
                                    fontSize = 10.sp,
                                    color = StudioTextSecondary
                                )
                                if (tw.isCustom) {
                                    Surface(color = StudioAmber, shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            text = "Моя",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0B0F19),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            if (tw.isCustom) {
                                Text(
                                    text = "Удалить",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioRose,
                                    modifier = Modifier
                                        .clickable { onDeleteCustomTwister(tw.id) }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = tw.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioTextPrimary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
