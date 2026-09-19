package com.example.gridsurge.game.render

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class ClearedLineEvent(
    val index: Int,          // Row index (0..7) or Column index (0..7)
    val isRow: Boolean,      // True for horizontal row, false for vertical column
    val colorLong: Long = 0xFF00E5FFL, // Cyan (#00E5FF) for player clears, Crimson (#FF0055) for rival attacks
    val progress: Float      // Animated from 0f to 1f over 150ms
) {
    val color: Color get() = Color(colorLong)
}

@Composable
fun VectorLineClearOverlay(
    activeClears: List<ClearedLineEvent>,
    modifier: Modifier = Modifier
) {
    if (activeClears.isEmpty()) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val cellSize = size.width / 8f

        activeClears.forEach { clear ->
            val progress = clear.progress
            val alpha = (1f - progress).coerceIn(0f, 1f)
            val strokeBloom = (8.dp.toPx() * (1f - progress * 0.5f))
            val strokeCore = 2.5.dp.toPx()

            if (clear.isRow) {
                val y = (clear.index + 0.5f) * cellSize
                val sweepX = size.width * (progress * 1.35f).coerceAtMost(1f)

                // 1. Wide Neon Bloom Vector Rail
                drawLine(
                    color = clear.color.copy(alpha = alpha * 0.45f),
                    start = Offset(0f, y),
                    end = Offset(sweepX, y),
                    strokeWidth = strokeBloom,
                    cap = StrokeCap.Round
                )

                // 2. High-Voltage Razor Core
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(0f, y),
                    end = Offset(sweepX, y),
                    strokeWidth = strokeCore,
                    cap = StrokeCap.Square
                )

                // 3. Perimeter Boundary Reticles
                val bracketSize = 7.dp.toPx()
                drawLine(
                    color = clear.color.copy(alpha = alpha),
                    start = Offset(0f, y - bracketSize),
                    end = Offset(0f, y + bracketSize),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = clear.color.copy(alpha = alpha),
                    start = Offset(size.width, y - bracketSize),
                    end = Offset(size.width, y + bracketSize),
                    strokeWidth = 2.dp.toPx()
                )
            } else {
                val x = (clear.index + 0.5f) * cellSize
                val sweepY = size.height * (progress * 1.35f).coerceAtMost(1f)

                // 1. Wide Neon Bloom Vector Rail
                drawLine(
                    color = clear.color.copy(alpha = alpha * 0.45f),
                    start = Offset(x, 0f),
                    end = Offset(x, sweepY),
                    strokeWidth = strokeBloom,
                    cap = StrokeCap.Round
                )

                // 2. High-Voltage Razor Core
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(x, 0f),
                    end = Offset(x, sweepY),
                    strokeWidth = strokeCore,
                    cap = StrokeCap.Square
                )

                // 3. Perimeter Boundary Reticles
                val bracketSize = 7.dp.toPx()
                drawLine(
                    color = clear.color.copy(alpha = alpha),
                    start = Offset(x - bracketSize, 0f),
                    end = Offset(x + bracketSize, 0f),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = clear.color.copy(alpha = alpha),
                    start = Offset(x - bracketSize, size.height),
                    end = Offset(x + bracketSize, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Cross-Clear Intersection Shockwaves (Spawns at row + column intersections)
        val rows = activeClears.filter { it.isRow }
        val cols = activeClears.filter { !it.isRow }

        if (rows.isNotEmpty() && cols.isNotEmpty()) {
            rows.forEach { r ->
                cols.forEach { c ->
                    val ix = (c.index + 0.5f) * cellSize
                    val iy = (r.index + 0.5f) * cellSize
                    val maxProgress = maxOf(r.progress, c.progress)
                    val diamondRadius = 18.dp.toPx() * maxProgress
                    val diamondAlpha = (1f - maxProgress).coerceIn(0f, 1f)

                    val diamondPath = Path().apply {
                        moveTo(ix, iy - diamondRadius)
                        lineTo(ix + diamondRadius, iy)
                        lineTo(ix, iy + diamondRadius)
                        lineTo(ix - diamondRadius, iy)
                        close()
                    }

                    drawPath(
                        path = diamondPath,
                        color = r.color.copy(alpha = diamondAlpha * 0.6f),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = diamondAlpha),
                        radius = diamondRadius * 0.35f,
                        center = Offset(ix, iy)
                    )
                }
            }
        }
    }
}
