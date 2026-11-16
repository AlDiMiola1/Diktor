package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.catalog.DiktorCatalog
import com.example.model.CourseProgram
import com.example.model.Exercise
import com.example.model.ExerciseModule
import com.example.model.UserProgressDoc
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrainingsTab(
    progress: UserProgressDoc,
    todayIsoDate: String,
    activeModuleFilter: ExerciseModule,
    onSelectModuleFilter: (ExerciseModule) -> Unit,
    onStartDailyCourse: () -> Unit,
    onAdvanceCourseEarly: () -> Unit,
    onStartExpressProgram: () -> Unit,
    onOpenExercise: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayNumber = max(1, progress.courseDayNumber.toInt())
    val courseProgram = remember(dayNumber) { DiktorCatalog.getCourseProgramForDay(dayNumber) }
    val doneToday = progress.lastCompletedCourseDate == todayIsoDate

    val filteredExercises = remember(activeModuleFilter) {
        if (activeModuleFilter == ExerciseModule.ALL) {
            DiktorCatalog.EXERCISES
        } else {
            DiktorCatalog.EXERCISES.filter { it.module == activeModuleFilter }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .testTag("trainings_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "daily_hero_card") {
                DailyCourseHeroCard(
                    program = courseProgram,
                    doneToday = doneToday,
                    completedDates = progress.completedDates,
                    todayIsoDate = todayIsoDate,
                    onStartCourse = onStartDailyCourse,
                    onAdvanceEarly = onAdvanceCourseEarly
                )
            }

            item(key = "express_card") {
                ExpressWarmupCard(onStartExpressProgram = onStartExpressProgram)
            }

            item(key = "catalog_header") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "КАТАЛОГ ТРЕНАЖЕРОВ",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = StudioTextSecondary
                        )
                        Text(
                            text = "${filteredExercises.size} упр.",
                            style = MaterialTheme.typography.labelSmall,
                            color = StudioCyan
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ExerciseModule.entries.forEach { mod ->
                            val selected = mod == activeModuleFilter
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) StudioAmber else StudioCard,
                                border = if (selected) null else BorderStroke(1.dp, StudioBorder),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelectModuleFilter(mod) }
                                    .testTag("module_filter_${mod.key}")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = mod.filterLabel,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (selected) Color(0xFF0B0F19) else StudioTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            items(
                items = filteredExercises,
                key = { it.id }
            ) { ex ->
                val showHeader = activeModuleFilter == ExerciseModule.ALL &&
                    (filteredExercises.indexOf(ex) == 0 ||
                        filteredExercises[filteredExercises.indexOf(ex) - 1].module != ex.module)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (showHeader) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = ex.module.displayName.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = StudioCyan
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = StudioBorder.copy(alpha = 0.7f)
                            )
                        }
                    }

                    ExerciseCatalogRow(
                        exercise = ex,
                        isDone = progress.completedExercises.contains(ex.id),
                        onClick = { onOpenExercise(ex.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun DailyCourseHeroCard(
    program: CourseProgram,
    doneToday: Boolean,
    completedDates: List<String>,
    todayIsoDate: String,
    onStartCourse: () -> Unit,
    onAdvanceEarly: () -> Unit
) {
    val borderColor = if (doneToday) StudioEmerald.copy(alpha = 0.55f) else StudioAmber.copy(alpha = 0.55f)
    val completedSet = remember(completedDates) { completedDates.toSet() }
    val weekDays = remember(completedDates, todayIsoDate) { buildCurrentWeekStrip(completedSet, todayIsoDate) }
    val weekDoneCount = weekDays.count { it.isDone }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioCard, RoundedCornerShape(18.dp))
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = StudioAmber.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ДЕНЬ ${program.dayNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = StudioAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        color = StudioSurface,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "~${program.approxMin} МИН",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = StudioCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Цикл ${program.cycleNum} • День ${program.dayInCycle}/${program.totalInCycle}",
                    style = MaterialTheme.typography.labelSmall,
                    color = StudioTextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (doneToday) StudioEmerald.copy(alpha = 0.15f) else StudioAmber.copy(alpha = 0.15f),
                border = BorderStroke(
                    1.dp,
                    if (doneToday) StudioEmerald.copy(alpha = 0.45f) else StudioAmber.copy(alpha = 0.45f)
                )
            ) {
                Text(
                    text = if (doneToday) "✅ Пройдено сегодня" else "⏳ Не пройдено",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = if (doneToday) StudioEmerald else StudioAmber,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = program.shortTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = StudioTextPrimary
            )
            Text(
                text = program.focus,
                style = MaterialTheme.typography.bodySmall,
                color = StudioTextSecondary
            )
        }

        // 7-day Mon-Sun strip
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioBg.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ТЕКУЩАЯ НЕДЕЛЯ (ПН–ВС)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = StudioTextSecondary
                )
                Text(
                    text = "$weekDoneCount / 7 дн.",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = StudioEmerald
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                weekDays.forEach { cell ->
                    val bg = when {
                        cell.isDone -> StudioEmerald.copy(alpha = 0.2f)
                        cell.isToday -> StudioAmber.copy(alpha = 0.15f)
                        else -> StudioCard
                    }
                    val brd = when {
                        cell.isDone -> StudioEmerald.copy(alpha = 0.6f)
                        cell.isToday -> StudioAmber
                        else -> StudioBorder
                    }
                    val txtColor = when {
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
                            text = cell.dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = txtColor
                        )
                        Text(
                            text = if (cell.isDone) "✓" else cell.dayNumberLabel,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = txtColor
                        )
                    }
                }
            }
        }

        // Steps preview
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            program.steps.forEachIndexed { idx, step ->
                val isStudioStep = step.isGoStudioAction
                val ex = DiktorCatalog.findExercise(step.exId)
                val stepTitle = if (isStudioStep) "Контрольный дубль в Студии и ИИ" else (ex?.title ?: "")
                val stepDur = if (isStudioStep) "Эфир" else "${step.durationSec}с"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioBg.copy(alpha = 0.85f), RoundedCornerShape(10.dp))
                        .border(
                            1.dp,
                            if (isStudioStep) StudioCyan.copy(alpha = 0.35f) else StudioBorder.copy(alpha = 0.85f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    if (isStudioStep) StudioCyan.copy(alpha = 0.2f) else StudioAmber.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isStudioStep) StudioCyan else StudioAmber
                            )
                        }
                        Text(
                            text = stepTitle,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isStudioStep) StudioCyan else StudioTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = stepDur,
                        style = MaterialTheme.typography.labelSmall,
                        color = StudioTextSecondary
                    )
                }
            }
        }

        if (doneToday) {
            OutlinedButton(
                onClick = onStartCourse,
                border = BorderStroke(1.dp, StudioEmerald.copy(alpha = 0.55f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = StudioEmerald.copy(alpha = 0.16f),
                    contentColor = StudioEmerald
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("start_daily_course_button")
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "Повторить День ${program.dayNumber} (следующий откроется завтра)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
            }

            OutlinedButton(
                onClick = onAdvanceEarly,
                border = BorderStroke(1.dp, StudioBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = StudioBg,
                    contentColor = StudioCyan
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("unlock_next_day_early_button")
            ) {
                Text(
                    text = "Пройти День ${program.dayNumber + 1} досрочно →",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        } else {
            Button(
                onClick = onStartCourse,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioAmber,
                    contentColor = Color(0xFF0B0F19)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("start_daily_course_button")
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "Начать тренировку Дня ${program.dayNumber}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                )
            }
        }
    }
}

@Composable
private fun ExpressWarmupCard(
    onStartExpressProgram: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioCard, RoundedCornerShape(16.dp))
            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
            .clickable { onStartExpressProgram() }
            .padding(14.dp)
            .testTag("express_warmup_card"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(StudioCyan.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                    .border(1.dp, StudioCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = "Экспресс-разогрев",
                    tint = StudioCyan
                )
            }
            Column {
                Text(
                    text = "Экспресс-разогрев перед эфиром (4 мин)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = StudioTextPrimary
                )
                Text(
                    text = "Лошадка → Штробас-бас → Пробка → Телесуфлер",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioTextSecondary
                )
            }
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .background(StudioBg, RoundedCornerShape(10.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Запустить экспресс-разогрев",
                tint = StudioCyan,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ExerciseCatalogRow(
    exercise: Exercise,
    isDone: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(StudioCard)
            .border(
                1.dp,
                if (isDone) StudioEmerald.copy(alpha = 0.45f) else StudioBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("exercise_card_${exercise.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${exercise.num}.",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = StudioTextMuted
                )
                Text(
                    text = exercise.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = StudioTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (exercise.isFry) {
                    Surface(
                        color = StudioAmber.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Штробас",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = StudioAmber,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                if (isDone) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Выполнено",
                        tint = StudioEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = exercise.shortDesc,
                style = MaterialTheme.typography.bodySmall,
                color = StudioTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = exercise.durationLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = StudioTextSecondary
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(StudioBg, RoundedCornerShape(9.dp))
                    .border(1.dp, StudioBorder, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Открыть ${exercise.title}",
                    tint = StudioAmber,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private data class WeekDayCell(
    val dayLabel: String,
    val dayNumberLabel: String,
    val isDone: Boolean,
    val isToday: Boolean
)

private fun buildCurrentWeekStrip(completedSet: Set<String>, todayIso: String): List<WeekDayCell> {
    val dayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val cal = Calendar.getInstance()
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val mondayShift = (dayOfWeek - Calendar.MONDAY + 7) % 7
    cal.add(Calendar.DAY_OF_YEAR, -mondayShift)
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    return dayNames.map { name ->
        val iso = fmt.format(cal.time)
        val dayNum = cal.get(Calendar.DAY_OF_MONTH).toString()
        val cell = WeekDayCell(
            dayLabel = name,
            dayNumberLabel = dayNum,
            isDone = completedSet.contains(iso),
            isToday = iso == todayIso
        )
        cal.add(Calendar.DAY_OF_YEAR, 1)
        cell
    }
}
