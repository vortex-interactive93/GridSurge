package com.example.gridsurge.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun HolographicWatermark(
    @DrawableRes watermarkRes: Int,
    modifier: Modifier = Modifier,
    size: Dp = 130.dp,
    alignment: Alignment = Alignment.CenterEnd,
    offsetX: Dp = 16.dp,
    offsetY: Dp = 0.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hologramBreathing")

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val gyroDrift by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gyroDrift"
    )

    Box(
        modifier = modifier,
        contentAlignment = alignment
    ) {
        Image(
            painter = painterResource(id = watermarkRes),
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .offset(x = offsetX, y = offsetY)
                .graphicsLayer {
                    alpha = pulseAlpha
                    rotationZ = gyroDrift
                    blendMode = BlendMode.Screen
                },
            contentScale = ContentScale.Fit
        )
    }
}
