package com.example.gridsurge.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun TacticalSegmentedSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueCommit: (Float) -> Unit = {},
    onDetent: () -> Unit = {},
    accentColor: Color = Color(0xFF00E5FF),
    modifier: Modifier = Modifier
) {
    val totalSegments = 24
    var lastNotchIndex by remember { mutableIntStateOf((value * totalSegments).roundToInt()) }

    // Prevent stale parameter capture inside pointerInput
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnValueCommit by rememberUpdatedState(onValueCommit)
    val currentOnDetent by rememberUpdatedState(onDetent)

    // Local state tracking the exact position under the finger
    var currentDragValue by remember(value) { mutableFloatStateOf(value) }

    val dbFormatted = remember(value) {
        if (value <= 0.01f) "-INF dB"
        else {
            val gain = value.toDouble().pow(2.5)
            val db = (20.0 * log10(gain)).coerceIn(-60.0, 0.0)
            "${String.format(Locale.US, "%.1f", db)} dB"
        }
    }

    val percentageInt = (value * 100f).roundToInt()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = dbFormatted,
                    color = Color(0xFF8FA3BF),
                    fontSize = 10.sp,
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "[$percentageInt%]",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xDD070B14))
                .border(1.dp, Color(0xFF182436), RoundedCornerShape(6.dp))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                        val quantized = (ratio * 20).roundToInt() / 20f
                        currentDragValue = quantized
                        currentOnValueChange(quantized)
                        currentOnValueCommit(quantized)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            currentOnValueCommit(currentDragValue)
                        },
                        onDragCancel = {
                            currentOnValueCommit(currentDragValue)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                            val quantized = (ratio * 20).roundToInt() / 20f
                            val currentNotch = (quantized * totalSegments).roundToInt()

                            if (currentNotch != lastNotchIndex) {
                                currentOnDetent()
                                lastNotchIndex = currentNotch
                            }
                            currentDragValue = quantized
                            currentOnValueChange(quantized)
                        }
                    )
                }
                .padding(horizontal = 6.dp, vertical = 5.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val segmentGap = 3.dp.toPx()
                val segmentWidth = (w - (segmentGap * (totalSegments - 1))) / totalSegments
                val activeSegments = (value * totalSegments).roundToInt()

                for (i in 0 until totalSegments) {
                    val x = i * (segmentWidth + segmentGap)
                    val isActive = i < activeSegments
                    val isLeadingEdge = i == activeSegments - 1

                    val baseSegmentColor = when {
                        !isActive -> Color(0xFF101926)
                        i > totalSegments * 0.85f -> Color(0xFFFFB300)
                        else -> accentColor
                    }

                    drawRoundRect(
                        color = if (isLeadingEdge) Color.White else baseSegmentColor,
                        topLeft = Offset(x, 0f),
                        size = Size(segmentWidth, h),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )

                    if (isLeadingEdge) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.White, accentColor),
                                startY = 0f,
                                endY = h
                            ),
                            topLeft = Offset(x, 0f),
                            size = Size(segmentWidth, h),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    } else if (isActive) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent),
                                startY = 0f,
                                endY = h * 0.5f
                            ),
                            topLeft = Offset(x, 0f),
                            size = Size(segmentWidth, h * 0.5f),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
