package com.example.gridsurge.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import kotlin.math.roundToInt

@Composable
fun FloatingRewardAnchor(
    rewardText: String,
    triggerKey: Any?,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(triggerKey) {
        if (triggerKey != null) {
            isVisible = true
            animProgress.snapTo(0f)
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
            )
            isVisible = false
        }
    }

    if (isVisible) {
        val yOffset = (-35.dp.value * animProgress.value).dp
        val alpha = (1f - animProgress.value).coerceIn(0f, 1f)
        val scale = 0.8f + (0.4f * (1f - animProgress.value))

        Box(
            modifier = modifier
                .offset { IntOffset(0, yOffset.toPx().roundToInt()) }
                .graphicsLayer {
                    this.alpha = alpha
                    this.scaleX = scale
                    this.scaleY = scale
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rewardText,
                color = Color(0xFFFFB300),
                fontSize = 13.sp,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Black
            )
        }
    }
}
