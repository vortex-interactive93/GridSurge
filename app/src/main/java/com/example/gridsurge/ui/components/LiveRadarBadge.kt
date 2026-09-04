package com.example.gridsurge.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LiveRadarBadge(
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF00FF66)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val radius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        Box(modifier = Modifier.size(12.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Static center dot
                drawCircle(color = accentColor, radius = 3.dp.toPx())
                
                // Expanding ring
                drawCircle(
                    color = accentColor.copy(alpha = alpha),
                    radius = (6.dp.toPx() * radius),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
        Text(
            text = "LIVE",
            color = accentColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}
