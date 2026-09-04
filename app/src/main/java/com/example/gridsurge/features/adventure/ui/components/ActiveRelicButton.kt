package com.example.gridsurge.features.adventure.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import com.example.gridsurge.features.adventure.model.RelicCyberWareState
import com.example.gridsurge.audio.HapticType
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType

/**
 * Redesigned "Overdrive Reactor Wing" component.
 * Features a hexagonal high-tech aesthetic, rotating core, and improved ergonomics.
 */
@Composable
fun ActiveRelicButton(
    relicState: RelicCyberWareState,
    onActivate: () -> Unit,
    onRelicDragStart: (Float, Float) -> Unit = { _, _ -> },
    onRelicDrag: (Float, Float) -> Unit = { _, _ -> },
    onRelicDragEnd: () -> Unit = {},
    onRelicDragCancel: () -> Unit = {},
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    compact: Boolean = false
) {
    val ability = relicState.abilityType
    val accentColor = Color(ability.colorHex)
    val isReady = relicState.isReady && isEnabled && relicState.isUnlocked

    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "reactor_anim")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "core_rotation"
    )
    
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "aura_alpha"
    )

    val reactorScale by animateFloatAsState(
        targetValue = if (isReady) 1.12f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "reactor_scale"
    )

    val interactionSource = remember { MutableInteractionSource() }
    var buttonPosInWindow by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .size(if (compact) 38.dp else 84.dp)
            .scale(reactorScale)
            .onGloballyPositioned { coords -> buttonPosInWindow = coords.positionInWindow() }
            .pointerInput(isReady) {
                if (!isReady) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        SfxManager.playSfx(SfxType.OVERDRIVE_ACTIVATE)
                        SfxManager.triggerHaptic(HapticType.DOUBLE_CRACK)
                        onRelicDragStart(buttonPosInWindow.x + offset.x, buttonPosInWindow.y + offset.y)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        onRelicDrag(buttonPosInWindow.x + change.position.x, buttonPosInWindow.y + change.position.y)
                    },
                    onDragEnd = onRelicDragEnd,
                    onDragCancel = onRelicDragCancel
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // 1. High-Tech Hexagonal Outer Shell
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                val side = size.width / 2.2f
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                for (i in 0..5) {
                    val angle = Math.toRadians(60.0 * i - 30.0)
                    val x = (centerX + side * Math.cos(angle)).toFloat()
                    val y = (centerY + side * Math.sin(angle)).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }

            // Outer Glow Aura
            if (isReady) {
                drawPath(
                    path = path,
                    color = accentColor.copy(alpha = auraAlpha * 0.4f),
                    style = Stroke(width = 6.dp.toPx(), join = StrokeJoin.Round)
                )
            }

            // Metallic Shell
            drawPath(
                path = path,
                brush = Brush.verticalGradient(listOf(Color(0xFF1E2D4A), Color(0xFF0A1322))),
                style = Fill
            )
            drawPath(
                path = path,
                color = if (isReady) accentColor else Color(0xFF324A6E),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // 2. Rotating Energy Core
        Box(
            modifier = Modifier
                .size(if (compact) 26.dp else 52.dp)
                .graphicsLayer { rotationZ = rotation },
            contentAlignment = Alignment.Center
        ) {
            // Radial Progress Sweep
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = (if (compact) 2.dp else 2.5.dp).toPx()
                val radius = (size.minDimension - strokeW) / 2f
                val sweepAngle = 360f * relicState.chargeProgress
                
                // Track
                drawCircle(
                    color = Color(0x3300E5FF),
                    radius = radius,
                    style = Stroke(width = 1.dp.toPx())
                )
                
                // Active Sweep
                drawArc(
                    color = accentColor.copy(alpha = 0.9f),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }
        }

        // 3. Central Ability Icon
        Box(
            modifier = Modifier
                .size(if (compact) 20.dp else 38.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = isReady
                ) {
                    SfxManager.playSfx(SfxType.OVERDRIVE_ACTIVATE)
                    SfxManager.triggerHaptic(HapticType.DOUBLE_CRACK)
                    onActivate()
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = ability.iconRes),
                contentDescription = ability.title,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = if (isReady) 1.0f else 0.45f },
                contentScale = ContentScale.Fit
            )
            
            // Percentage Label
            if (!isReady && relicState.isUnlocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = "${(relicState.chargeProgress * 100).toInt()}%",
                        color = Color.White,
                        fontSize = if (compact) 7.sp else 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // 4. "OVR" Indicator Badge (full mode only)
        if (!compact) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .background(if (isReady) accentColor else Color(0xFF1E2D4A), shape = RoundedCornerShape(2.dp))
                    .padding(horizontal = 4.dp, vertical = 0.5.dp)
            ) {
                Text(
                    text = "OVR",
                    color = if (isReady) Color.Black else Color.White,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        
        if (isReady) {
            Text(
                text = "READY",
                color = accentColor,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.BottomCenter).offset(y = 12.dp)
            )
        }
    }
}
