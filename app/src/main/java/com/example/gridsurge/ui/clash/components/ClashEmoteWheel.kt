package com.example.gridsurge.ui.clash.components

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.R
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class ClashEmote(
    val id: String,
    @DrawableRes val drawableRes: Int,
    val glowColor: Color,
    val angleDeg: Float // Fanned inward away from right screen edge
) {
    SURGE("SURGE", R.drawable.ic_emote_glitchbot_surge, Color(0xFFFFD600), 270f),       // UP
    TAUNT("TAUNT", R.drawable.ic_emote_glitchbot_taunt, Color(0xFF00FF66), 220f),       // UP-LEFT
    DOOMED("DOOMED", R.drawable.ic_emote_glitchbot_doomed, Color(0xFFFF0055), 180f),    // LEFT
    GREETING("GREETING", R.drawable.ic_emote_glitchbot_greeting, Color(0xFF00E5FF), 130f) // DOWN-LEFT
}

@Composable
fun ClashEmoteController(
    modifier: Modifier = Modifier,
    onSendEmote: (ClashEmote) -> Unit
) {
    val view = LocalView.current
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var selectedEmote by remember { mutableStateOf<ClashEmote?>(null) }
    var cooldownRemainingMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(cooldownRemainingMs) {
        if (cooldownRemainingMs > 0) {
            delay(100L)
            cooldownRemainingMs = (cooldownRemainingMs - 100L).coerceAtLeast(0L)
        }
    }

    val isCooldown = cooldownRemainingMs > 0L

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Inward Arc Selection Nodes (Text-Free, Jumbo 68dp Stickers)
        if (isDragging && !isCooldown) {
            ClashEmote.entries.forEach { emote ->
                val angleRad = (emote.angleDeg * PI / 180f).toFloat()
                // Radius fanned inward toward the matrix
                val targetOffsetX = (cos(angleRad) * 94).dp
                val targetOffsetY = (sin(angleRad) * 94).dp
                val isTargeted = selectedEmote == emote

                val nodeScale by animateFloatAsState(
                    targetValue = if (isTargeted) 1.35f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "nodeScale"
                )

                Box(
                    modifier = Modifier
                        .offset(x = targetOffsetX, y = targetOffsetY)
                        .scale(nodeScale)
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xF00A0E17))
                        .border(
                            width = if (isTargeted) 3.dp else 1.5.dp,
                            color = if (isTargeted) emote.glowColor else Color(0x3300E5FF),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = emote.drawableRes),
                        contentDescription = emote.id,
                        modifier = Modifier
                            .fillMaxSize(0.90f)
                            .clip(CircleShape)
                            .graphicsLayer {
                                blendMode = BlendMode.Screen
                            }
                    )
                }
            }
        }

        // Central Trigger Dial
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCooldown -> Color(0x33FFFFFF)
                        isDragging -> Color(0xCC00E5FF)
                        else -> Color(0xD90A0E17)
                    }
                )
                .border(
                    width = 2.dp,
                    color = if (isCooldown) Color.Gray else Color(0xFF00E5FF),
                    shape = CircleShape
                )
                .pointerInput(isCooldown) {
                    if (isCooldown) return@pointerInput
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            dragOffset = Offset.Zero
                            selectedEmote = null
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        },
                        onDragEnd = {
                            selectedEmote?.let { emote ->
                                onSendEmote(emote)
                                cooldownRemainingMs = 2500L
                                val hapticConfirm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    HapticFeedbackConstants.CONFIRM
                                } else {
                                    HapticFeedbackConstants.KEYBOARD_TAP
                                }
                                view.performHapticFeedback(hapticConfirm)
                                SfxManager.playSfx(SfxType.GESTURE_RELEASE)
                            }
                            isDragging = false
                            dragOffset = Offset.Zero
                            selectedEmote = null
                        },
                        onDragCancel = {
                            isDragging = false
                            dragOffset = Offset.Zero
                            selectedEmote = null
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffset += dragAmount

                            val dist = sqrt(dragOffset.x * dragOffset.x + dragOffset.y * dragOffset.y)
                            if (dist > 35f) {
                                val rad = atan2(dragOffset.y, dragOffset.x)
                                var deg = (rad * 180f / PI).toFloat()
                                if (deg < 0) deg += 360f

                                // Mapped strictly to the inward arc (Right side is dead zone)
                                val newSelection = when (deg) {
                                    in 100f..155f -> ClashEmote.GREETING // DOWN-LEFT
                                    in 156f..200f -> ClashEmote.DOOMED   // LEFT
                                    in 201f..245f -> ClashEmote.TAUNT    // UP-LEFT
                                    in 246f..310f -> ClashEmote.SURGE    // UP
                                    else -> null
                                }

                                if (selectedEmote != newSelection) {
                                    selectedEmote = newSelection
                                    if (newSelection != null) {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        SfxManager.playSfx(SfxType.EMOTE_HOVER)
                                    }
                                }
                            } else {
                                selectedEmote = null
                            }
                        }
                    )
                }
        ) {
            if (isCooldown) {
                Text(
                    text = String.format(Locale.US, "%.1f", cooldownRemainingMs / 1000f),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = Icons.Default.EmojiEmotions,
                    contentDescription = "Hold and flick emote",
                    tint = if (isDragging) Color.Black else Color(0xFF00E5FF),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun FloatingRivalEmoteBadge(
    activeEmote: ClashEmote?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = activeEmote != null,
        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
        exit = scaleOut(tween(220, easing = FastOutLinearInEasing)) + fadeOut(),
        modifier = modifier
    ) {
        if (activeEmote != null) {
            // 1. Continuous Physics Loop: Hover bobbing, vibration, and sway
            val infiniteTransition = rememberInfiniteTransition(label = "emotePhysics")

            // Hover Elevation (Sine Wave)
            val hoverOffset by infiniteTransition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = if (activeEmote == ClashEmote.SURGE) 180 else 1200,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "hoverOffset"
            )

            // Angular Drone Tilt (Roll Sway)
            val droneTiltDeg by infiniteTransition.animateFloat(
                initialValue = when (activeEmote) {
                    ClashEmote.TAUNT -> -14f
                    ClashEmote.DOOMED -> 22f
                    ClashEmote.SURGE -> -3f
                    else -> -6f
                },
                targetValue = when (activeEmote) {
                    ClashEmote.TAUNT -> -8f
                    ClashEmote.DOOMED -> 16f
                    ClashEmote.SURGE -> 3f
                    else -> 6f
                },
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = if (activeEmote == ClashEmote.SURGE) 100 else 1400,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "droneTilt"
            )

            // High-voltage Jitter (Overdrive shudder for SURGE / DOOMED)
            val jitterX = if (activeEmote == ClashEmote.SURGE) {
                (sin(System.currentTimeMillis() / 25.0) * 2.5f).toFloat()
            } else if (activeEmote == ClashEmote.DOOMED) {
                (sin(System.currentTimeMillis() / 45.0) * 1.5f).toFloat()
            } else 0f

            // 2. Thruster Flame Core & Shockwave Ring
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .offset(x = jitterX.dp, y = hoverOffset.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Procedural Particle & Thruster Exhaust Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f

                    // Ground Energy Shockwave Ring
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(activeEmote.glowColor.copy(alpha = 0.45f), Color.Transparent),
                            center = Offset(centerX, size.height - 4.dp.toPx()),
                            radius = 24.dp.toPx()
                        ),
                        center = Offset(centerX, size.height - 4.dp.toPx()),
                        radius = 24.dp.toPx()
                    )

                    // Procedural Flame/Spark Nozzle Plumes
                    val thrusterBaseY = centerY + 18.dp.toPx()
                    val nozzleOffsets = listOf(-16.dp.toPx(), 0f, 16.dp.toPx())

                    nozzleOffsets.forEach { nx ->
                        val flameLength = if (activeEmote == ClashEmote.SURGE) {
                            Random.nextFloat() * 22f + 16f
                        } else if (activeEmote == ClashEmote.DOOMED) {
                            Random.nextFloat() * 8f + 3f
                        } else {
                            Random.nextFloat() * 12f + 8f
                        }

                        // Outer Glow Jet
                        drawLine(
                            color = activeEmote.glowColor.copy(alpha = 0.6f),
                            start = Offset(centerX + nx, thrusterBaseY),
                            end = Offset(centerX + nx, thrusterBaseY + flameLength),
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )

                        // Inner Plasma Core
                        drawLine(
                            color = Color.White,
                            start = Offset(centerX + nx, thrusterBaseY),
                            end = Offset(centerX + nx, thrusterBaseY + flameLength * 0.5f),
                            strokeWidth = 2.5f,
                            cap = StrokeCap.Round
                        )
                    }

                    // DOOMED Smoke Rings (puffing up from antenna)
                    if (activeEmote == ClashEmote.DOOMED) {
                        val smokeY = centerY - 28.dp.toPx() - ((System.currentTimeMillis() % 800) / 800f * 14.dp.toPx())
                        val smokeAlpha = (1f - ((System.currentTimeMillis() % 800) / 800f)).coerceIn(0f, 1f)
                        drawCircle(
                            color = Color.DarkGray.copy(alpha = smokeAlpha * 0.7f),
                            radius = 4.5.dp.toPx(),
                            center = Offset(centerX + 6.dp.toPx(), smokeY)
                        )
                    }
                }

                // 3. The Chibi Glitch-Bot Drone Entity
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .rotate(droneTiltDeg)
                        .shadow(
                            elevation = 14.dp,
                            shape = CircleShape,
                            ambientColor = activeEmote.glowColor,
                            spotColor = activeEmote.glowColor
                        )
                        .clip(CircleShape)
                        .background(Color(0xE6080C14))
                        .border(
                            width = 2.dp,
                            brush = Brush.radialGradient(
                                listOf(activeEmote.glowColor, activeEmote.glowColor.copy(alpha = 0.25f))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = activeEmote.drawableRes),
                        contentDescription = activeEmote.id,
                        modifier = Modifier
                            .fillMaxSize(0.92f)
                            .clip(CircleShape)
                            .graphicsLayer {
                                blendMode = BlendMode.Screen
                            }
                    )
                }
            }
        }
    }
}
