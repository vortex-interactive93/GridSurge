package com.example.gridsurge.features.adventure.ui.dialogs

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.R
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.ui.CyberActionButton
import com.example.gridsurge.ui.CyberChamferShape

@Composable
fun RelicUnlockedSuccessDialog(
    relic: RelicSpec,
    onDismiss: () -> Unit
) {
    // Pulsing entrance and continuous background aura animations
    val infiniteTransition = rememberInfiniteTransition(label = "relicCelebration")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.82f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF09172A), Color(0xFF040812))
                        ),
                        shape = CyberChamferShape
                    )
                    .border(1.5.dp, Color(0xFF00E5FF), CyberChamferShape)
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Protocol Badge
                Text(
                    text = "SECTOR 0${relic.sectorNumber} COMPLETE",
                    color = Color(0xFF5C8599),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "OPERATOR DOSSIER UNLOCKED",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Hero Preview with Rotating Holographic Particle Ring
                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Background Particle / Aura Ring
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(pulseGlow)
                    ) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val radius = size.width * 0.44f

                        // Outer Cyan Glow
                        drawCircle(
                            color = Color(0x3300E5FF),
                            radius = radius * 1.08f,
                            style = Stroke(width = 4.dp.toPx())
                        )

                        // Rotating Segmented Gyro Ring
                        rotate(rotationAngle) {
                            val step = 30f
                            for (angle in 0 until 360 step 60) {
                                drawArc(
                                    color = Color(0xFF00E5FF),
                                    startAngle = angle.toFloat(),
                                    sweepAngle = step,
                                    useCenter = false,
                                    topLeft = Offset(cx - radius, cy - radius),
                                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        }
                    }

                    // Relic Medal Badge Preview
                    Image(
                        painter = painterResource(id = relic.rewardBadgeRes),
                        contentDescription = "Sector Relic Badge",
                        modifier = Modifier.size(90.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rewards itemized breakdown
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x2200E5FF), RoundedCornerShape(4.dp))
                        .border(0.5.dp, Color(0x6600E5FF), RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "TITLE: [${relic.rewardTitle}]",
                            color = Color(0xFF00E5FF),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "+${relic.rewardStars} ★ STAR VAULT GRANT",
                            color = Color(0xFFFFD700),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF00E5FF))
                        .clickable {
                            SfxManager.playSfx(SfxType.BONUS_UNLOCKED)
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CLAIM & EQUIP TITLE ►",
                        color = Color(0xFF03060E),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
