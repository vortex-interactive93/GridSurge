package com.example.gridsurge.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.gridsurge.R
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StudioSplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val logoScale = remember { Animatable(0.90f) }
    val logoAlpha = remember { Animatable(0.0f) }
    val screenAlpha = remember { Animatable(1.0f) }

    LaunchedEffect(Unit) {
        // Play startup atmospheric swell
        SfxManager.playSfx(SfxType.MODAL_WHOOSH)

        // 1. Smooth entrance scale & fade
        launch {
            logoAlpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
        launch {
            logoScale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // 2. Hold presentation on pure black (700ms)
        delay(1200L)

        // 3. Smooth exit fade into Main Menu
        screenAlpha.animateTo(
            targetValue = 0.0f,
            animationSpec = tween(durationMillis = 300, easing = FastOutLinearInEasing)
        )

        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(screenAlpha.value)
            .background(Color.Black), // Pure True Black (#000000)
        contentAlignment = Alignment.Center
    ) {
        // Studio Emblem & Typography
        Image(
            painter = painterResource(id = R.drawable.logo_vortex_interactive),
            contentDescription = "Vortex Interactive",
            modifier = Modifier
                .fillMaxWidth(1.00f) // Constrain width to prevent over-scaling
                .scale(logoScale.value)
                .alpha(logoAlpha.value),
            contentScale = ContentScale.Fit
        )
    }
}
