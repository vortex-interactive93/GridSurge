package com.example.gridsurge.ui.clash.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

@Composable
fun BlitzClashTacticalHud(
    playerCard: OperatorCardData,
    rivalCard: OperatorCardData,
    playerScore: Long,
    rivalScore: Long,
    secondsRemaining: Int,
    boardOccupancy: Float = 0f,
    rivalActionTickerText: String? = null,
    activeRivalEmote: ClashEmote? = null,
    modifier: Modifier = Modifier
) {
    val cyanTheme = Color(0xFF00E5FF)
    val crimsonTheme = Color(0xFFFF0055)

    // Odometer score rolling animations
    val animPlayerScore by animateIntAsState(
        targetValue = playerScore.toInt(),
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "pScore"
    )
    val animRivalScore by animateIntAsState(
        targetValue = rivalScore.toInt(),
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "rScore"
    )

    // Tug-of-War Spring Bias (Score Delta Deflection: 4,000 pts = 100% max deflection)
    val maxLeadThreshold = 4000f
    val scoreDelta = (playerScore - rivalScore).toFloat()
    val targetNormalizedLead = (scoreDelta / maxLeadThreshold).coerceIn(-1f, 1f)

    val animatedLead by animateFloatAsState(
        targetValue = targetNormalizedLead,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "nodeSpring"
    )

    // Foil sheen sweep
    val infiniteTransition = rememberInfiniteTransition(label = "hudSheen")
    val sweepOffset by infiniteTransition.animateFloat(
        initialValue = -150f,
        targetValue = 750f,
        animationSpec = infiniteRepeatable(animation = tween(2800, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "sheenOffset"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .displayCutoutPadding() // Respects camera notches and cutouts
            .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- TOP ROW: MINI OPERATOR CARDS ONLY (Leaves center completely clear for camera cutout) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Local Player Card (Left)
            OperatorMiniCard(
                data = playerCard,
                score = animPlayerScore,
                sweepOffset = sweepOffset,
                isLocalPlayer = true,
                boardOccupancy = boardOccupancy,
                modifier = Modifier.weight(1f)
            )

            // Center Camera Cutout Buffer (Guarantees at least 36dp clearance between cards)
            Spacer(modifier = Modifier.width(36.dp))

            // Rival Card Container (Right)
            Box(modifier = Modifier.weight(1f)) {
                OperatorMiniCard(
                    data = rivalCard,
                    score = animRivalScore,
                    sweepOffset = sweepOffset,
                    isLocalPlayer = false,
                    hasActiveTicker = rivalActionTickerText != null,
                    modifier = Modifier.fillMaxWidth()
                )

                if (activeRivalEmote != null) {
                    FloatingRivalEmoteBadge(
                        activeEmote = activeRivalEmote,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = (-8).dp, y = 38.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- MIDDLE ANCHOR: CENTRAL CHRONO POD & METRICS (Sitting below camera hardware) ---
        val mm = (secondsRemaining / 60).toString().padStart(2, '0')
        val ss = (secondsRemaining % 60).toString().padStart(2, '0')
        val delta = playerScore - rivalScore
        val absDelta = abs(delta)
        val deltaText = when {
            delta > 0 -> String.format(Locale.US, "+%,d LEAD", absDelta)
            delta < 0 -> String.format(Locale.US, "-%,d DEFICIT", absDelta)
            else -> "TIED // 0"
        }
        val deltaColor = when {
            delta > 0 -> cyanTheme
            delta < 0 -> crimsonTheme
            else -> Color.Gray
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            // Chrono Clock
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xD9080D1A),
                border = BorderStroke(
                    1.dp,
                    if (secondsRemaining <= 10) crimsonTheme else Color(0x4D00E5FF)
                )
            ) {
                Text(
                    text = "$mm:$ss",
                    color = if (secondsRemaining <= 10) crimsonTheme else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            // Lead/Deficit Readout
            Text(
                text = deltaText,
                color = deltaColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // --- BOTTOM ROW: HIGH-OCTANE OVERCHARGE TUG-O-WAR RAIL ---
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
        ) {
            val w = size.width
            val barY = size.height / 2f
            val centerZeroX = w / 2f
            val maxTravel = (w / 2f) - 18f
            val nodeX = centerZeroX + (animatedLead * maxTravel)

            // Track Rail Background
            drawLine(
                color = Color(0x331E293B),
                start = Offset(0f, barY),
                end = Offset(w, barY),
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )

            // Cyan Beam
            drawLine(
                color = cyanTheme.copy(alpha = 0.35f),
                start = Offset(0f, barY),
                end = Offset(nodeX, barY),
                strokeWidth = 9f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = cyanTheme,
                start = Offset(0f, barY),
                end = Offset(nodeX, barY),
                strokeWidth = 3.5f,
                cap = StrokeCap.Round
            )

            // Crimson Beam
            drawLine(
                color = crimsonTheme.copy(alpha = 0.35f),
                start = Offset(nodeX, barY),
                end = Offset(w, barY),
                strokeWidth = 9f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = crimsonTheme,
                start = Offset(nodeX, barY),
                end = Offset(w, barY),
                strokeWidth = 3.5f,
                cap = StrokeCap.Round
            )

            // Center Momentum Clash Diamond
            val diamondRadius = 10f
            val path = Path().apply {
                moveTo(nodeX, barY - diamondRadius)
                lineTo(nodeX + diamondRadius, barY)
                lineTo(nodeX, barY + diamondRadius)
                lineTo(nodeX - diamondRadius, barY)
                close()
            }

            drawPath(path = path, color = if (delta >= 0) cyanTheme else crimsonTheme)
            drawPath(path = path, color = Color.White, style = Fill)
        }
    }
}

@Composable
private fun OperatorMiniCard(
    data: OperatorCardData,
    score: Int,
    sweepOffset: Float,
    isLocalPlayer: Boolean,
    boardOccupancy: Float = 0f,
    hasActiveTicker: Boolean = false,
    modifier: Modifier = Modifier
) {
    val themeColor = if (isLocalPlayer) Color(0xFF00E5FF) else Color(0xFFFF0055)
    val cardBg = if (isLocalPlayer) Color(0xF2091522) else Color(0xF21D0912)

    val isHazard = isLocalPlayer && boardOccupancy >= 0.85f
    val infiniteTransition = rememberInfiniteTransition(label = "cardBorder")
    val hazardAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hazardAlpha"
    )

    val borderColor = when {
        isHazard -> Color(0xFFFF1744).copy(alpha = hazardAlpha)
        hasActiveTicker -> Color(0xFFFFD600)
        else -> themeColor.copy(alpha = 0.6f)
    }

    Surface(
        shape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
        color = cardBg,
        border = BorderStroke(1.2.dp, borderColor),
        modifier = modifier.height(56.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, themeColor.copy(alpha = 0.15f), Color.White.copy(alpha = 0.25f), Color.Transparent),
                        start = Offset(sweepOffset, 0f),
                        end = Offset(sweepOffset + 50f, size.height)
                    ),
                    start = Offset(sweepOffset, 0f),
                    end = Offset(sweepOffset + 50f, size.height),
                    strokeWidth = 32f
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLocalPlayer) {
                    // 24dp Avatar Box
                    Surface(
                        shape = CutCornerShape(4.dp),
                        color = Color(0xFF080D1A),
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Image(
                            painter = painterResource(id = data.avatarResId),
                            contentDescription = data.callsign,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = data.callsign.uppercase(),
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                            MiniLevelPill(data.level, themeColor)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MiniBadgeRow(data.equippedBadges)
                            Text(
                                text = String.format(Locale.US, "%,d", score),
                                color = themeColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            MiniLevelPill(data.level, themeColor)
                            Text(
                                text = data.callsign.uppercase(),
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format(Locale.US, "%,d", score),
                                color = themeColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            MiniBadgeRow(data.equippedBadges)
                        }
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    // 24dp Avatar Box
                    Surface(
                        shape = CutCornerShape(4.dp),
                        color = Color(0xFF080D1A),
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Image(
                            painter = painterResource(id = data.avatarResId),
                            contentDescription = data.callsign,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniLevelPill(level: Int, themeColor: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(Color(0xFF080D1A))
            .border(1.dp, themeColor, CircleShape)
    ) {
        Text(
            text = "$level",
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}

@Composable
private fun MiniBadgeRow(badges: List<OperatorFeatBadge>) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        badges.take(3).forEach { badge ->
            MiniHudFeatBadge(badge = badge)
        }
    }
}

@Composable
private fun MiniHudFeatBadge(
    badge: OperatorFeatBadge,
    modifier: Modifier = Modifier
) {
    val shape = when (badge) {
        OperatorFeatBadge.DECA_SURGE -> CutCornerShape(3.dp)
        OperatorFeatBadge.CLUTCH_HERO -> CutCornerShape(topStart = 3.dp, bottomEnd = 3.dp)
        else -> CutCornerShape(2.dp)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(15.dp)
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = badge.badgeColor,
                spotColor = badge.badgeColor
            )
            .background(Color(0xFF060A12), shape)
            .border(0.8.dp, badge.badgeColor, shape)
    ) {
        FeatSigilIcon(
            badge = badge,
            modifier = Modifier.size(9.dp)
        )
    }
}
