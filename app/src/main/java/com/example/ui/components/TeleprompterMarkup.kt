package com.example.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioRose
import com.example.ui.theme.StudioTextPrimary

fun buildTeleprompterAnnotatedString(rawText: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < rawText.length) {
            when {
                rawText[i] == '*' -> {
                    val end = rawText.indexOf('*', i + 1)
                    if (end != -1) {
                        val word = rawText.substring(i + 1, end)
                        withStyle(
                            SpanStyle(
                                color = StudioAmber,
                                fontWeight = FontWeight.Black,
                                background = StudioAmber.copy(alpha = 0.14f)
                            )
                        ) {
                            append(" $word ")
                        }
                        i = end + 1
                    } else {
                        withStyle(SpanStyle(color = StudioTextPrimary)) { append('*') }
                        i++
                    }
                }
                i + 1 < rawText.length && rawText[i] == '|' && rawText[i + 1] == '|' -> {
                    withStyle(
                        SpanStyle(
                            color = StudioAmber,
                            fontWeight = FontWeight.Black,
                            background = StudioAmber.copy(alpha = 0.22f)
                        )
                    ) {
                        append(" || ")
                    }
                    i += 2
                }
                rawText[i] == '|' -> {
                    withStyle(
                        SpanStyle(
                            color = StudioCyan,
                            fontWeight = FontWeight.Black
                        )
                    ) {
                        append(" | ")
                    }
                    i++
                }
                rawText[i] == '↗' -> {
                    withStyle(
                        SpanStyle(
                            color = StudioEmerald,
                            fontWeight = FontWeight.Black
                        )
                    ) {
                        append("↗")
                    }
                    i++
                }
                rawText[i] == '↘' -> {
                    withStyle(
                        SpanStyle(
                            color = StudioRose,
                            fontWeight = FontWeight.Black
                        )
                    ) {
                        append("↘")
                    }
                    i++
                }
                else -> {
                    val nextSpecial = findNextSpecialIndex(rawText, i)
                    withStyle(SpanStyle(color = StudioTextPrimary)) {
                        append(rawText.substring(i, nextSpecial))
                    }
                    i = nextSpecial
                }
            }
        }
    }
}

private fun findNextSpecialIndex(text: String, start: Int): Int {
    for (idx in start until text.length) {
        val ch = text[idx]
        if (ch == '*' || ch == '|' || ch == '↗' || ch == '↘') {
            return idx
        }
    }
    return text.length
}
