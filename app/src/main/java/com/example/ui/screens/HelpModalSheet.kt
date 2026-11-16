package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.MainTab
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
fun HelpModalSheet(
    onClose: () -> Unit,
    onNavigateTab: (MainTab) -> Unit
) {
    var selectedTopic by remember { mutableStateOf("all") }

    val topics = listOf(
        "all" to "Все разделы",
        "schedule" to "1. Режим занятий",
        "studio" to "2. Суфлер и ИИ",
        "custom" to "3. Свои тексты и дикция",
        "safety" to "4. Штробас и связки"
    )

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler { onClose() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StudioBg)
                .systemBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "РУКОВОДСТВО ДИКТОРА",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = StudioCyan
                        )
                        Text(
                            text = "Как пользоваться приложением",
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
                            .testTag("close_help_modal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Закрыть справку",
                            tint = StudioTextPrimary
                        )
                    }
                }

                HorizontalDivider(color = StudioBorder)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    topics.forEach { (key, label) ->
                        val active = selectedTopic == key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (active) StudioAmber else StudioCard,
                            border = if (active) null else BorderStroke(1.dp, StudioBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedTopic = key }
                                .testTag("help_topic_$key")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (active) Color(0xFF0B0F19) else StudioTextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                if (selectedTopic == "all" || selectedTopic == "schedule") {
                    HelpSectionCard(
                        badge = "ШАГ 1",
                        badgeColor = StudioAmber,
                        title = "Автокурс по дням vs Отдельные упражнения"
                    ) {
                        Text(
                            text = "Вкладка «Тренировки» построена по принципу «от расслабления к звуку». Не пытайтесь сразу говорить низко без разминки.",
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioTextSecondary
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StudioBg, RoundedCornerShape(12.dp))
                                .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "• Экспресс-разогрев (4 мин): запускайте перед важными созвонами, записью голосовых или дублем в Студии. Цепочка сама переключит вас на телесуфлер.",
                                style = MaterialTheme.typography.bodySmall,
                                color = StudioTextPrimary
                            )
                            Text(
                                text = "• Автокурс по дням (14-дневный цикл): 1 короткая сбалансированная тренировка в день по всем модулям (Зажимы → Дыхание → Штробас → Грудь → Дикция → Интонация).",
                                style = MaterialTheme.typography.bodySmall,
                                color = StudioTextPrimary
                            )
                            Text(
                                text = "• Отдельные упражнения (32 шт.): используйте для точечной проработки рекомендаций ИИ-режиссера или для нового рекорда в «33 Егорках».",
                                style = MaterialTheme.typography.bodySmall,
                                color = StudioTextPrimary
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StudioSurface.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Рекомендуемый график на неделю:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = StudioTextPrimary
                            )
                            Text(
                                text = "Пн / Ср / Пт — Автокурс дня + 2 дубля в Студии.\nВт / Чт / Сб — Экспресс-разогрев (4 мин) + 5 минут скороговорок под метроном.\nВс — отдых для голосовых связок.",
                                style = MaterialTheme.typography.bodySmall,
                                color = StudioTextSecondary
                            )
                        }
                    }
                }

                if (selectedTopic == "all" || selectedTopic == "studio") {
                    HelpSectionCard(
                        badge = "ШАГ 2",
                        badgeColor = StudioCyan,
                        title = "Как читать суфлер, записывать и включать ИИ"
                    ) {
                        Text(
                            text = "Во вкладке «Студия и ИИ» тексты размечены специальными дикторскими знаками:",
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioTextSecondary
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StudioBg, RoundedCornerShape(12.dp))
                                .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("| — короткая смысловая пауза (0.3–0.5 сек, без вдоха)", style = MaterialTheme.typography.bodySmall, color = StudioCyan)
                            Text("|| — глубокая пауза и бесшумный добор воздуха ребрами", style = MaterialTheme.typography.bodySmall, color = StudioAmber)
                            Text("*слово* — главный смысловой акцент (подать грудью)", style = MaterialTheme.typography.bodySmall, color = StudioAmber)
                            Text("↗ и ↘ — повышение интонации (интрига) и посадка вниз (уверенная точка)", style = MaterialTheme.typography.bodySmall, color = StudioEmerald)
                        }
                        Text(
                            text = "Нажмите «Записать новый дубль», прочитайте текст и остановите запись. После прослушивания нажмите «Разобрать дубль с ИИ-режиссером» (нейросеть Gemini прослушает живой тембр) или используйте «Акустический разбор» для быстрой офлайн-оценки.",
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioTextSecondary
                        )
                        OutlinedButton(
                            onClick = {
                                onClose()
                                onNavigateTab(MainTab.STUDIO)
                            },
                            border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.45f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = StudioCyan.copy(alpha = 0.14f),
                                contentColor = StudioCyan
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Перейти в Студию и ИИ →", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                if (selectedTopic == "all" || selectedTopic == "custom") {
                    HelpSectionCard(
                        badge = "ШАГ 3",
                        badgeColor = StudioEmerald,
                        title = "Где добавлять свои тексты, таблицы и скороговорки"
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StudioBg, RoundedCornerShape(12.dp))
                                .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "1. Свои сценарии в телесуфлер: откройте «Студия и ИИ» → нажмите «+ Свой текст» и расставьте маркеры + |, + || и + *акцент*.",
                                style = MaterialTheme.typography.bodySmall,
                                color = StudioTextPrimary
                            )
                            HorizontalDivider(color = StudioBorder)
                            Text(
                                text = "2. Свои скороговорки и диктофон: во вкладке «Дикция и темп» пролистайте до блока «Дикторские скороговорки» → «+ Добавить».",
                                style = MaterialTheme.typography.bodySmall,
                                color = StudioTextPrimary
                            )
                            HorizontalDivider(color = StudioBorder)
                            Text(
                                text = "3. Таблицы согласных: выберите 18, 30 или 42 слога, либо создайте свою таблицу кнопкой «+ Своя таблица» — сочетания согласных сами разложатся по гласным А-О-У-Э-И-Ы.",
                                style = MaterialTheme.typography.bodySmall,
                                color = StudioTextPrimary
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                onClose()
                                onNavigateTab(MainTab.DICTION)
                            },
                            border = BorderStroke(1.dp, StudioAmber.copy(alpha = 0.45f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = StudioAmber.copy(alpha = 0.14f),
                                contentColor = StudioAmber
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Открыть Дикцию и скороговорки →", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                if (selectedTopic == "all" || selectedTopic == "safety") {
                    HelpSectionCard(
                        badge = "ВАЖНО",
                        badgeColor = StudioRose,
                        title = "Техника безопасности: Штробас и грудной бас",
                        borderColor = StudioRose.copy(alpha = 0.45f)
                    ) {
                        Text(
                            text = "1. Штробас делается на минимуме воздуха: это легкий расслабленный треск связок (как сонный утренний голос), а не хрип с натугой.",
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioTextPrimary
                        )
                        Text(
                            text = "2. Не давите кадык вниз силой: низкий дикторский голос рождается за счет резонанса в груди («Тарзан» и «Скрытый зевок»).",
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioTextPrimary
                        )
                        Text(
                            text = "3. Вода и паузы: держите рядом стакан теплой воды. При появлении осиплости или усталости остановите тренировку до следующего дня.",
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioTextPrimary
                        )
                    }
                }

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StudioAmber,
                        contentColor = Color(0xFF0B0F19)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
                        .testTag("help_done_button")
                ) {
                    Text(
                        text = "Понятно, вернуться к тренировкам",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun HelpSectionCard(
    badge: String,
    badgeColor: Color,
    title: String,
    borderColor: Color = StudioBorder,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioCard, RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = badgeColor.copy(alpha = 0.16f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = StudioTextPrimary
            )
        }
        content()
    }
}
