package com.example.gridsurge.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TacticalButton(
    label: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val cut = 10.dp.toPx()
            val w = size.width
            val h = size.height

            val path = Path().apply {
                moveTo(cut, 0f)
                lineTo(w - cut, 0f)
                lineTo(w, cut)
                lineTo(w, h - cut)
                lineTo(w - cut, h)
                lineTo(cut, h)
                lineTo(0f, h - cut)
                lineTo(0f, cut)
                close()
            }

            if (enabled) {
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = 0.2f), color.copy(alpha = 0.05f))
                    )
                )
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            } else {
                drawPath(
                    path = path,
                    color = Color(0x33162238)
                )
                drawPath(
                    path = path,
                    color = Color(0xFF263859),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (enabled) color else Color(0xFF5C8599),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
