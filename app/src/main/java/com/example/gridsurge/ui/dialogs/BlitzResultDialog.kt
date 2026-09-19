package com.example.gridsurge.ui.dialogs

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.ads.AdManager
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.game.blitz.model.BlitzScorecardDebrief
import com.example.gridsurge.ui.CyberActionButton
import com.example.gridsurge.ui.CyberChamferShape
import com.example.gridsurge.ui.modifiers.cyberBorderGlow
import com.example.gridsurge.ui.monetization.TacticalAdMultiplierCard
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import java.util.Locale

@Composable
fun BlitzResultDialog(
    debrief: BlitzScorecardDebrief,
    isNoAdsVip: Boolean = false,
    isDoublerClaimed: Boolean = false,
    onClaimDoubler: () -> Unit = {},
    onPlayAgain: () -> Unit,
    onReturnToHub: () -> Unit
) {
    val animatedScoreProgress = remember { Animatable(0f) }
    var isScoreRollComplete by remember { mutableStateOf(false) }
    val animatedStarCount = remember { Animatable(0f) }

    LaunchedEffect(debrief.finalScore) {
        // 1. Roll score first (1.1 seconds)
        animatedScoreProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
        )
        isScoreRollComplete = true

        // 2. Play tactile completion tick
        SfxManager.playSfx(SfxType.LEVEL_COMPLETE, overridePitch = 1.2f)

        // 3. Roll star bounty immediately after
        animatedStarCount.animateTo(
            targetValue = debrief.starReward.toFloat(),
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
        )
    }

    val rollingScore = (debrief.finalScore * animatedScoreProgress.value).toLong()
    val rollingStars = animatedStarCount.value.toInt()

    val infiniteTransition = rememberInfiniteTransition(label = "blitzResultLoop")
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen"
    )

    Dialog(
        onDismissRequest = onReturnToHub,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF002050A))
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .cyberBorderGlow(
                        colors = listOf(Color(0xFFFFD600), Color(0xFFFF1744)),
                        cornerRadius = 16.dp
                    )
                    .clip(CyberChamferShape)
                    .background(Color(0xFF070D18))
                    .border(1.5.dp, Color(0xFFFFD600), CyberChamferShape)
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "CHRONO OPERATION // DEBRIEF",
                        color = Color(0xFFFFD600),
                        fontSize = 10.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = "TIME EXPIRED",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )

                    // Final Score Hero Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC0E182A))
                            .border(1.dp, Color(0xFF1E3250), RoundedCornerShape(8.dp))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "FINAL BLITZ SCORE",
                                color = Color(0xFF556980),
                                fontSize = 8.5.sp,
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(Locale.US, "%,d", rollingScore),
                                color = Color(0xFFFFD600),
                                fontSize = 28.sp,
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // 4-Quadrant Stats Matrix
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BlitzMetricTile(
                            label = "CADENCE",
                            value = "${debrief.piecesPerMinute} PPM",
                            accentColor = Color(0xFF00E5FF),
                            isPersonalBest = debrief.isNewPpmPb,
                            modifier = Modifier.weight(1f)
                        )
                        BlitzMetricTile(
                            label = "MAX COMBO",
                            value = "${debrief.maxCombo}x",
                            accentColor = Color(0xFF00FF66),
                            isPersonalBest = debrief.isNewComboPb,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BlitzMetricTile(
                            label = "FEVER UPTIME",
                            value = "${debrief.feverUptimePercent}%",
                            accentColor = Color(0xFFFF1744),
                            modifier = Modifier.weight(1f)
                        )
                        BlitzMetricTile(
                            label = "TIME EARNED",
                            value = "+${debrief.chronoRefundTotalSec}s",
                            accentColor = Color(0xFFFFD600),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Reward Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0A121F))
                            .border(0.8.dp, Color(0xFF1C2C45), RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STARS ACCREDITED:",
                            color = Color(0xFF8FA3BF),
                            fontSize = 9.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "+$rollingStars ★ STARS",
                            color = if (isScoreRollComplete) Color(0xFFFFD600) else Color(0xFF8FA3BF),
                            fontSize = 12.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Star Reward Doubler Banner
                    if (isScoreRollComplete && !isDoublerClaimed) {
                        val context = LocalContext.current
                        val activity = context as? Activity
                        TacticalAdMultiplierCard(
                            bonusStarAmount = debrief.starReward,
                            isNoAdsVip = isNoAdsVip,
                            isAdReady = true,
                            onClaimMultiplier = {
                                if (activity != null) {
                                    AdManager.showRewardedAd(
                                        activity = activity,
                                        isNoAdsPurchased = isNoAdsVip,
                                        onRewardEarned = { onClaimDoubler() }
                                    )
                                } else {
                                    onClaimDoubler()
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Action 1: Play Again
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFFD600), Color(0xFFFF6D00))
                                )
                            )
                            .clickable {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onPlayAgain()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val w = size.width
                            val h = size.height
                            val sheenX = w * sheenProgress
                            val sheenWidth = w * 0.35f

                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.32f), Color.Transparent),
                                    start = Offset(sheenX, 0f),
                                    end = Offset(sheenX + sheenWidth, h)
                                ),
                                size = Size(w, h)
                            )
                        }

                        Text(
                            text = "DEPLOY AGAIN // 90S",
                            color = Color(0xFF040810),
                            fontSize = 12.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    }

                    // Action 2: Return to Hub
                    CyberActionButton(
                        text = "RETURN TO COMMAND HUB",
                        primaryColor = Color(0xFF8A99AD),
                        isPrimary = false,
                        onClick = {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            onReturnToHub()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BlitzMetricTile(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isPersonalBest: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x990A111E))
            .border(0.8.dp, Color(0xFF172436), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = Color(0xFF556980),
                    fontSize = 8.sp,
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold
                )

                if (isPersonalBest) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFFFFD600))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "NEW PB!",
                            color = Color(0xFF040810),
                            fontSize = 6.5.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Text(
                text = value,
                color = accentColor,
                fontSize = 15.sp,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Black
            )
        }
    }
}
