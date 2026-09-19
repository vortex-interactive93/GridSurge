package com.example.gridsurge.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.example.gridsurge.hub.model.SubspaceStarfieldState

@Composable
fun SubspaceStarfield(
    pagerOffset: Float,
    accentColor: Color,
    modifier: Modifier = Modifier,
    particleCount: Int = 40
) {
    val starfieldState = remember { SubspaceStarfieldState(particleCount) }
    var previousOffset by remember { mutableFloatStateOf(pagerOffset) }

    val delta = pagerOffset - previousOffset
    previousOffset = pagerOffset

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        starfieldState.initIfNeeded(w, h)
        starfieldState.update(w, h, delta * 45f)

        // Draw batch with zero allocation
        starfieldState.particles.forEach { p ->
            val particleColor = accentColor.copy(alpha = p.alpha)
            drawRect(
                color = particleColor,
                topLeft = Offset(p.x, p.y),
                size = Size(p.size, p.size)
            )
        }
    }
}
