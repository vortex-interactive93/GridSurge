package com.example.gridsurge.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight

@Composable
fun NeuralSyncProgressBar(
    currentProgress: Float, // 0.0f .. 1.0f
    progressText: String,   // e.g., "6 / 12 TILES" or "750 / 1500 PTS"
    targetIconRes: Int? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val animatedProgress by animateFloatAsState(
        targetValue = currentProgress.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "sync_progress"
    )

    val barColor by animateColorAsState(
        targetValue = if (animatedProgress >= 1f) Color(0xFFFFD700) else Color(0xFF00E5FF),
        label = "bar_color"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // High-Contrast Cyber Outer Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(13.dp), spotColor = barColor)
                .clip(RoundedCornerShape(13.dp))
                .background(Color(0xFF060D1A))
                .border(1.5.dp, barColor.copy(alpha = 0.85f), RoundedCornerShape(13.dp))
        ) {
            // Glowing Gradient Fill
            if (animatedProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(13.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = if (animatedProgress >= 1f) {
                                    listOf(Color(0xFFFFB300), Color(0xFFFFEA00))
                                } else {
                                    listOf(Color(0xFF0077FF), Color(0xFF00E5FF), Color(0xFF00FF88))
                                }
                            )
                        )
                )
            }

            // High-Contrast Center Text (Auto-scaling Canvas approach)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                
                val hudTextPaint = Paint().apply {
                    color = if (animatedProgress > 0.45f) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    isAntiAlias = true
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                    letterSpacing = 0.1f
                }

                var targetTextSize = canvasHeight * 0.55f
                hudTextPaint.textSize = targetTextSize

                val maxTextWidth = canvasWidth - (canvasHeight * 2f)
                val textWidth = hudTextPaint.measureText(progressText)
                if (textWidth > maxTextWidth) {
                    hudTextPaint.textSize = targetTextSize * (maxTextWidth / textWidth)
                }

                val textY = (canvasHeight / 2f) - ((hudTextPaint.descent() + hudTextPaint.ascent()) / 2f)
                drawContext.canvas.nativeCanvas.drawText(
                    progressText,
                    canvasWidth / 2f,
                    textY,
                    hudTextPaint
                )
            }
        }

        // Endcap Goal Icon Badge
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF081426))
                .border(2.dp, barColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (targetIconRes != null) {
                Image(
                    painter = painterResource(id = targetIconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = "★",
                    color = barColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
