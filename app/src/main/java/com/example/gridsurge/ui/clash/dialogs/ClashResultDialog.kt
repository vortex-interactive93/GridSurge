package com.example.gridsurge.ui.clash.dialogs

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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.game.clash.model.ClashMmrSettlement
import com.example.gridsurge.ui.CyberActionButton
import com.example.gridsurge.ui.CyberChamferShape
import com.example.gridsurge.ui.modifiers.cyberBorderGlow
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import java.util.Locale

@Composable
fun ClashResultDialog(
    settlement: ClashMmrSettlement,
    onRematch: () -> Unit,
    onReturnToHub: () -> Unit
) {
    val isWin = settlement.isVictory
    val themeColor = if (isWin) Color(0xFF00FF66) else Color(0xFFFF0055)

    // Animated Rating Ticker
    val animatedMmrProgress = remember { Animatable(0f) }
    LaunchedEffect(settlement.finalMmr) {
        animatedMmrProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )
    }

    val displayMmr = (settlement.startMmr + (settlement.mmrDelta * animatedMmrProgress.value)).toInt()
    val tierProgress = ((displayMmr - settlement.prevTierMmr).toFloat() /
            (settlement.nextTierMmr - settlement.prevTierMmr).toFloat()).coerceIn(0f, 1f)

    Dialog(
        onDismissRequest = onReturnToHub,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF202050A))
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .cyberBorderGlow(listOf(themeColor, themeColor.copy(alpha = 0.35f)), 16.dp)
                    .clip(CyberChamferShape)
                    .background(Color(0xFF070D18))
                    .border(1.5.dp, themeColor, CyberChamferShape)
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isWin) "ARENA SETTLEMENT // DOMINATED" else "ARENA SETTLEMENT // DEFEATED",
                        color = themeColor,
                        fontSize = 9.5.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = if (isWin) "VICTORY" else "DEFEAT",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )

                    // Head-to-Head Comparison Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC0E182A))
                            .border(1.dp, Color(0xFF1E3250), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("YOU", color = Color(0xFF00E5FF), fontSize = 9.sp, fontFamily = ChakraPetchFontFamily, fontWeight = FontWeight.Bold)
                            Text(String.format(Locale.US, "%,d", settlement.finalPlayerScore), color = Color.White, fontSize = 20.sp, fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Black)
                        }

                        Text("VS", color = Color(0xFF556980), fontSize = 12.sp, fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Black)

                        Column(horizontalAlignment = Alignment.End) {
                            Text(settlement.rival.callsign, color = Color(0xFFFF0055), fontSize = 9.sp, fontFamily = ChakraPetchFontFamily, fontWeight = FontWeight.Bold)
                            Text(String.format(Locale.US, "%,d", settlement.finalRivalScore), color = Color(0xFFC4D2E6), fontSize = 20.sp, fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Black)
                        }
                    }

                    // Dynamic Ranked MMR Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0B1424))
                            .border(0.8.dp, Color(0xFF1E304C), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${settlement.currentTierName} // $displayMmr MMR",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isWin) "+${settlement.mmrDelta} MMR" else "${settlement.mmrDelta} MMR",
                                    color = themeColor,
                                    fontSize = 10.sp,
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            // ELO Progress Track
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                            ) {
                                val w = size.width
                                val h = size.height

                                drawRoundRect(
                                    color = Color(0xFF050A12),
                                    size = Size(w, h),
                                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                                )

                                drawRoundRect(
                                    brush = Brush.horizontalGradient(
                                        listOf(themeColor.copy(alpha = 0.6f), themeColor)
                                    ),
                                    size = Size(w * tierProgress, h),
                                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                                )
                            }

                            Text(
                                text = "NEXT PROMOTION: ${settlement.nextTierName} (${settlement.nextTierMmr - displayMmr} PTS)",
                                color = Color(0xFF556980),
                                fontSize = 7.5.sp,
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Action 1: Rematch / Next Match
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    if (isWin) listOf(Color(0xFF00FF66), Color(0xFF009944))
                                    else listOf(Color(0xFF00E5FF), Color(0xFF007799))
                                )
                            )
                            .clickable {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onRematch()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "FIND NEXT GLADIATOR // 90S",
                            color = Color(0xFF040810),
                            fontSize = 12.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

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
