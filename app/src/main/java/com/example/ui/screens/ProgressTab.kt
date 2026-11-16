package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.catalog.DiktorCatalog
import com.example.model.AiEvaluationDoc
import com.example.model.UserProgressDoc
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioRose
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

@Composable
fun ProgressTab(
    progress: UserProgressDoc,
    todayIsoDate: String,
    customScriptsCount: Int,
    customTwistersCount: Int,
    customTablesCount: Int,
    aiHistory: List<AiEvaluationDoc>,
    isSignedIn: Boolean,
    userDisplayName: String?,
    isSigningIn: Boolean,
    authError: String?,
    onGoogleSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onOpenExercise: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedSet = remember(progress.completedDates) { progress.completedDates.toSet() }
    val doneToday = progress.lastCompletedCourseDate == todayIsoDate
    val currDay = max(1, progress.courseDayNumber.toInt())
    val last14Days = remember(progress.completedDates, todayIsoDate) {
        buildLast14DaysCells(completedSet, todayIsoDate)
    }
    val totalCustomAssets = customScriptsCount + customTwistersCount + customTablesCount

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .testTag("progress_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "regularity_diary") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioCard, RoundedCornerShape(18.dp))
                        .border(1.dp, StudioBorder, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ДНЕВНИК РЕГУЛЯРНОСТИ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = StudioTextSecondary
                            )
                            Text(
                                text = if (doneToday) {
                                    "День $currDay выполнен • Завтра откроется День ${currDay + 1}"
                                } else {
                                    "Активный этап: День $currDay автокурса"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                color = StudioTextPrimary
                            )
                        }
                        Surface(
                            color = if (doneToday) StudioEmerald.copy(alpha = 0.15f) else StudioAmber.copy(alpha = 0.15f),
                            border = BorderStroke(
                                1.dp,
                                if (doneToday) StudioEmerald.copy(alpha = 0.45f) else StudioAmber.copy(alpha = 0.45f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (doneToday) "✅ Пройдено" else "⏳ Ожидает",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (doneToday) StudioEmerald else StudioAmber,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Серия подряд: ${progress.streakDays} дн.",
                            style = MaterialTheme.typography.labelSmall,
                            color = StudioAmber
                        )
                        Text(
                            text = "Активных дней: ${completedSet.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = StudioCyan
                        )
                    }

                    // 14-day grid (2 rows of 7)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        last14Days.chunked(7).forEach { rowCells ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowCells.forEach { cell ->
                                    val bg = when {
                                        cell.isDone -> StudioEmerald.copy(alpha = 0.2f)
                                        cell.isToday -> StudioAmber.copy(alpha = 0.15f)
                                        else -> StudioBg
                                    }
                                    val brd = when {
                                        cell.isDone -> StudioEmerald.copy(alpha = 0.6f)
                                        cell.isToday -> StudioAmber
                                        else -> StudioBorder
                                    }
                                    val clr = when {
                                        cell.isDone -> StudioEmerald
                                        cell.isToday -> StudioAmber
                                        else -> StudioTextMuted
                                    }
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(bg, RoundedCornerShape(8.dp))
                                            .border(1.dp, brd, RoundedCornerShape(8.dp))
                                            .padding(vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = cell.dateLabel,
                                            fontSize = 9.sp,
                                            color = clr
                                        )
                                        Text(
                                            text = if (cell.isDone) "✓" else if (cell.isToday) "•" else "—",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = clr
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item(key = "stat_cards") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatTile(
                            value = progress.totalExerciseRuns.toString(),
                            label = "Упражнений выполнено",
                            accent = StudioAmber,
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            value = progress.completedPrograms.toString(),
                            label = "Программ завершено",
                            accent = StudioCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatTile(
                            value = progress.egorkiBestCount.toString(),
                            label = "Рекорд «33 Егорки»",
                            accent = StudioEmerald,
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            value = totalCustomAssets.toString(),
                            label = "Своих материалов",
                            accent = StudioPurple,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item(key = "personal_library_card") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioCard, RoundedCornerShape(16.dp))
                        .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Личная база диктора",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = StudioTextPrimary
                        )
                        Text(
                            text = "$customScriptsCount текст. • $customTwistersCount скор. • $customTablesCount табл.",
                            style = MaterialTheme.typography.labelSmall,
                            color = StudioCyan
                        )
                    }
                    HorizontalDivider(color = StudioBorder)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (progress.egorkiBestCount > 0L) {
                                "Ваш лучший результат: ${progress.egorkiBestCount} из 33 Егорок на одном выдохе!"
                            } else {
                                "Рекорд «33 Егорки»: пока не установлен."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioTextSecondary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 10.dp)
                        )
                        OutlinedButton(
                            onClick = { onOpenExercise("egorki_33") },
                            border = BorderStroke(1.dp, StudioEmerald.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = StudioEmerald.copy(alpha = 0.14f),
                                contentColor = StudioEmerald
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("beat_egorki_button")
                        ) {
                            Text("Побить", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            item(key = "cloud_sync_card") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioCard, RoundedCornerShape(16.dp))
                        .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isSignedIn) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = if (isSignedIn) StudioEmerald else StudioAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isSignedIn) {
                                "Облачная синхронизация активна"
                            } else {
                                "Локальное сохранение (офлайн)"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = StudioTextPrimary
                        )
                    }
                    Text(
                        text = if (isSignedIn) {
                            "Аккаунт: ${userDisplayName ?: "Google"}. Прогресс, сценарии, скороговорки и разборы синхронизируются с облаком."
                        } else {
                            "Ваш прогресс и тексты сохраняются на устройстве. Войдите через Google при желании синхронизировать базу между устройствами."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = StudioTextSecondary
                    )
                    if (!authError.isNullOrBlank()) {
                        Text(
                            text = authError,
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioRose
                        )
                    }
                    if (isSignedIn) {
                        OutlinedButton(
                            onClick = onSignOutClick,
                            border = BorderStroke(1.dp, StudioRose.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioRose),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("progress_sign_out_button")
                        ) {
                            Text("Выйти из аккаунта Google", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onGoogleSignInClick,
                            enabled = !isSigningIn,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StudioAmber,
                                contentColor = Color(0xFF0B0F19)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("progress_google_sign_in_button")
                        ) {
                            if (isSigningIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF0B0F19)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Подключение...", fontWeight = FontWeight.Black)
                            } else {
                                Text("Войти через Google", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            item(key = "history_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ИСТОРИЯ ОЦЕНОК ИИ-РЕЖИССЕРА",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = StudioTextSecondary
                    )
                    if (aiHistory.isNotEmpty()) {
                        TextButton(
                            onClick = onClearHistory,
                            modifier = Modifier.testTag("clear_ai_history_button")
                        ) {
                            Text(
                                text = "Очистить статистику",
                                style = MaterialTheme.typography.labelSmall,
                                color = StudioRose
                            )
                        }
                    }
                }
            }

            if (aiHistory.isEmpty()) {
                item(key = "empty_history") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StudioCard, RoundedCornerShape(16.dp))
                            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Пока нет сохраненных разборов из Студии",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = StudioTextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Запишите дубль во вкладке «Студия и ИИ» и нажмите «Разобрать дубль с ИИ», чтобы отслеживать динамику голоса.",
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(items = aiHistory, key = { it.id.ifEmpty { "${it.dateLabel}_${it.headline}" } }) { eval ->
                    val recEx = DiktorCatalog.findExercise(eval.recommendedExId)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StudioCard, RoundedCornerShape(16.dp))
                            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${eval.dateLabel} • ${eval.scriptTitle}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StudioTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = eval.headline,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = StudioTextPrimary
                                )
                            }
                            Surface(
                                color = StudioAmber.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, StudioAmber.copy(alpha = 0.45f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "${eval.totalScore}/10",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = StudioAmber,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Text(
                            text = eval.critique,
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioTextSecondary
                        )

                        if (recEx != null) {
                            Text(
                                text = "Упражнение: «${recEx.title}» →",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = StudioCyan,
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 36.dp)
                                    .clickable { onOpenExercise(recEx.id) }
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatTile(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(StudioCard, RoundedCornerShape(16.dp))
            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
            color = accent
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = StudioTextSecondary
        )
    }
}

private data class DayGridCell(
    val dateLabel: String,
    val isDone: Boolean,
    val isToday: Boolean
)

private fun buildLast14DaysCells(completedSet: Set<String>, todayIso: String): List<DayGridCell> {
    val isoFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val dispFmt = SimpleDateFormat("dd.MM", Locale.US)
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -13)
    return (0 until 14).map {
        val iso = isoFmt.format(cal.time)
        val cell = DayGridCell(
            dateLabel = dispFmt.format(cal.time),
            isDone = completedSet.contains(iso),
            isToday = iso == todayIso
        )
        cal.add(Calendar.DAY_OF_YEAR, 1)
        cell
    }
}
