package com.example.gridsurge.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import java.util.Locale
import kotlin.math.roundToLong

@Composable
fun TacticalRollingCounter(
    targetValue: Long,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    fontSize: TextUnit,
    color: Color = Color.White,
    durationMs: Int = 1100,
    fontFamily: FontFamily = OrbitronFontFamily
) {
    val countAnim = remember { Animatable(0f) }
    var lastTickBoundary by remember { mutableLongStateOf(0L) }

    LaunchedEffect(targetValue) {
        countAnim.snapTo(0f)
        countAnim.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing)
        )
    }

    val currentValue = countAnim.value.roundToLong()

    // Trigger subtle acoustic detents at major milestone increments
    LaunchedEffect(currentValue) {
        val step = (targetValue / 12).coerceAtLeast(1)
        if (currentValue / step != lastTickBoundary) {
            lastTickBoundary = currentValue / step
            val pitch = 0.95f + ((currentValue.toFloat() / targetValue.coerceAtLeast(1)) * 0.35f)
            SfxManager.playSfx(SfxType.CAROUSEL_SNAP, volume = 0.15f, pitchRate = pitch)
        }
    }

    val formattedNumber = remember(currentValue) {
        String.format(Locale.US, "%,d", currentValue)
    }

    Text(
        text = "$prefix$formattedNumber$suffix",
        color = color,
        fontSize = fontSize,
        fontFamily = fontFamily,
        fontWeight = FontWeight.Black,
        modifier = modifier
    )
}
