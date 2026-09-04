package com.example.gridsurge.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.ui.CyberChamferShape
import kotlinx.coroutines.delay

/**
 * Dynamic High-Tech Vector Overdrive Combo Badge.
 * Renders dynamically for ANY combo streak (x2 up to x99+) using pure vector graphics.
 */
@Composable
fun ComboBadgeOverlay(
    comboStreak: Int,
    sectorId: Int = 1,
    dropPxX: Float = 0f,
    dropPxY: Float = 0f,
    modifier: Modifier = Modifier
) {
    if (comboStreak < 2) return

    val scaleAnim = remember { Animatable(0.4f) }
    val alphaAnim = remember { Animatable(0.0f) }

    LaunchedEffect(comboStreak) {
        scaleAnim.snapTo(0.4f)
        alphaAnim.snapTo(1.0f)

        // Explosive spring pop-in with smooth fade-out
        scaleAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        delay(1100L)
        alphaAnim.animateTo(0.0f, animationSpec = tween(300))
    }

    val primaryColor = when {
        comboStreak >= 10 -> Color(0xFFFF0055) // Singularity Crimson
        comboStreak >= 5 -> Color(0xFFFFD600)  // Overdrive Gold
        else -> Color(0xFF00E5FF)             // Surge Cyan
    }

    val titleText = when {
        comboStreak >= 10 -> "SINGULARITY OVERLOAD"
        comboStreak >= 5 -> "OVERDRIVE MATRIX"
        else -> "SURGE STREAK"
    }

    if (alphaAnim.value > 0f) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .scale(scaleAnim.value)
                .alpha(alphaAnim.value)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFA050A14), Color(0xDD02050A))
                    ),
                    shape = CyberChamferShape
                )
                .border(1.5.dp, primaryColor.copy(alpha = alphaAnim.value), CyberChamferShape)
                .padding(vertical = 8.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val bracketLen = 14.dp.toPx()
                val bracketThickness = 2.dp.toPx()

                // Decorative Corner Tech Brackets
                val p = Path().apply {
                    moveTo(0f, bracketLen); lineTo(0f, 0f); lineTo(bracketLen, 0f)
                    moveTo(w - bracketLen, 0f); lineTo(w, 0f); lineTo(w, bracketLen)
                    moveTo(0f, h - bracketLen); lineTo(0f, h); lineTo(bracketLen, h)
                    moveTo(w - bracketLen, h); lineTo(w, h); lineTo(w, h - bracketLen)
                }
                drawPath(p, primaryColor.copy(alpha = alphaAnim.value), style = Stroke(bracketThickness))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "x$comboStreak $titleText",
                    color = primaryColor.copy(alpha = alphaAnim.value),
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}
