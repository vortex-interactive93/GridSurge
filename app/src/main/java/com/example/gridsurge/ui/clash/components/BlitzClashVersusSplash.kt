package com.example.gridsurge.ui.clash.components

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import kotlin.math.roundToInt

@Composable
fun BlitzClashVersusSplash(
    playerCard: OperatorCardData,
    rivalCard: OperatorCardData,
    countdownRemainingSec: Int,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }

    // Physical card offsets for collision
    val playerOffsetX = remember { Animatable(-screenWidthPx) }
    val rivalOffsetX = remember { Animatable(screenWidthPx) }
    val vsScale = remember { Animatable(0f) }
    val screenShake = remember { Animatable(0f) }

    // Run the choreographed collision on enter
    LaunchedEffect(Unit) {
        if (countdownRemainingSec <= 2) {
            playerOffsetX.snapTo(0f)
            rivalOffsetX.snapTo(0f)
            vsScale.snapTo(1f)
        } else {
            // 1. Player card flies in from left
            SfxManager.playSfx(SfxType.THRUSTER_BURST)
            playerOffsetX.animateTo(
                targetValue = 0f,
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            )
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            // 2. Rival card rockets in from right and slams into player card
            SfxManager.playSfx(SfxType.THRUSTER_BURST, overridePitch = 1.2f)
            rivalOffsetX.animateTo(
                targetValue = -30f, // Overshoot: slams directly into player's side
                animationSpec = tween(350, easing = LinearOutSlowInEasing)
            )

            // 3. Impact Haptics, Card Slam SFX & Screen Shake
            SfxManager.playSfx(SfxType.CARD_SLAM)
            val hapticConfirm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedbackConstants.CONFIRM
            } else {
                HapticFeedbackConstants.KEYBOARD_TAP
            }
            view.performHapticFeedback(hapticConfirm)
            screenShake.snapTo(16f)
            screenShake.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessHigh))

            // 4. Rebound: Player pushes rival back and both lock into home position
            rivalOffsetX.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )

            // 5. VS Badge slams into center
            vsScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        }
    }

    // Dynamic scale pulse for the remaining seconds
    val pulseAnim = remember { Animatable(1.4f) }
    LaunchedEffect(countdownRemainingSec) {
        pulseAnim.snapTo(1.45f)
        pulseAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(0, screenShake.value.roundToInt()) }
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF030712),
                        Color(0xFF0B101D),
                        Color(0xFF030712)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Directive
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text(
                    text = "TARGET ACQUIRED // 1V1 DUEL",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "SYNCHRONIZING PRNG SEED & DOCKS",
                    color = Color.DarkGray,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Player Card (Slides from left, holds ground)
            TacticalOperatorCard(
                data = playerCard,
                modifier = Modifier.offset { IntOffset(playerOffsetX.value.roundToInt(), 0) }
            )

            // Center Clash "VS" Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .scale(vsScale.value)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(1.dp)
                            .width(64.dp)
                            .background(Brush.horizontalGradient(listOf(Color.Transparent, Color(0xFF00E5FF))))
                    )

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0B101D),
                        border = BorderStroke(2.dp, Color(0xFFFFD600)),
                        modifier = Modifier.size(52.dp),
                        shadowElevation = 10.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "VS",
                                color = Color(0xFFFFD600),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(1.dp)
                            .width(64.dp)
                            .background(Brush.horizontalGradient(listOf(Color(0xFFFF0055), Color.Transparent)))
                    )
                }
            }

            // Rival Card (Rockets in from right, slams & rebounds)
            TacticalOperatorCard(
                data = rivalCard,
                modifier = Modifier.offset { IntOffset(rivalOffsetX.value.roundToInt(), 0) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5-Second Countdown
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = if (countdownRemainingSec > 0) "ENGAGEMENT IN" else "COMMENCE COMBAT",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (countdownRemainingSec > 0) "$countdownRemainingSec" else "GO!",
                    color = if (countdownRemainingSec > 0) Color(0xFFFFD600) else Color(0xFF00FF66),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.scale(pulseAnim.value)
                )
            }
        }
    }
}
