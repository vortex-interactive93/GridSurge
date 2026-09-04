package com.example.gridsurge.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun SegmentedCyberProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    totalSegments: Int = 10,
    activeGradient: List<Color> = listOf(Color(0xFF00E5FF), Color(0xFFE040FB)),
    emptyTrackColor: Color = Color(0xFF141926),
    emptyBorderColor: Color = Color(0xFF26334D)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "progress_anim"
    )

    val segmentPath = remember { Path() }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp)
    ) {
        val totalWidth = size.width
        val barHeight = size.height
        val spacing = 4.dp.toPx()
        val slantOffset = 6.dp.toPx() // Angled parallelogram skew

        val availableWidth = totalWidth - (spacing * (totalSegments - 1))
        val segmentWidth = availableWidth / totalSegments

        val filledSegmentsCount = (animatedProgress * totalSegments)

        val brush = Brush.linearGradient(
            colors = activeGradient,
            start = Offset(0f, 0f),
            end = Offset(totalWidth, 0f)
        )

        for (i in 0 until totalSegments) {
            val startX = i * (segmentWidth + spacing)
            val endX = startX + segmentWidth

            segmentPath.reset()
            segmentPath.moveTo(startX + slantOffset, 0f)
            segmentPath.lineTo(endX + slantOffset, 0f)
            segmentPath.lineTo(endX, barHeight)
            segmentPath.lineTo(startX, barHeight)
            segmentPath.close()

            val segmentFillFraction = (filledSegmentsCount - i).coerceIn(0f, 1f)

            if (segmentFillFraction > 0f) {
                // Draw Active Neon Filled Segment
                drawPath(
                    path = segmentPath,
                    brush = brush,
                    style = Fill,
                    alpha = if (segmentFillFraction >= 1f) 1f else segmentFillFraction
                )
                drawPath(
                    path = segmentPath,
                    color = Color.White.copy(alpha = 0.4f),
                    style = Stroke(width = 1.dp.toPx())
                )
            } else {
                // Draw Empty Track Segment
                drawPath(
                    path = segmentPath,
                    color = emptyTrackColor,
                    style = Fill
                )
                drawPath(
                    path = segmentPath,
                    color = emptyBorderColor,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }
}
