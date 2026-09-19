package com.example.gridsurge.ui.honors.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.honors.model.OperativeBadge
import com.example.gridsurge.honors.model.PlayerPrestigeProfile
import com.example.gridsurge.ui.modifiers.cyberBorderGlow
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import java.util.Locale

@Composable
fun OperativeCallingCard(
    profile: PlayerPrestigeProfile,
    equippedBadge: OperativeBadge,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bannerFx")
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen"
    )

    val primaryColor = equippedBadge.tier.primaryColor

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(108.dp)
            .cyberBorderGlow(
                colors = listOf(primaryColor, equippedBadge.tier.secondaryColor),
                cornerRadius = 14.dp
            )
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF070C16))
            .border(1.5.dp, primaryColor.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
    ) {
        // 1. Calling Card Background Art Layer
        Image(
            painter = painterResource(id = equippedBadge.bannerBackgroundRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.35f },
            contentScale = ContentScale.Crop
        )

        // 2. Hardware Specular Sheen Pass
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val sheenX = w * sheenProgress
            val sheenWidth = w * 0.30f

            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.20f), Color.Transparent),
                    start = Offset(sheenX, 0f),
                    end = Offset(sheenX + sheenWidth, h)
                ),
                size = Size(w, h)
            )
        }

        // 3. Operational Telemetry Layout
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Holographic Emblem Node
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xDD090F1C))
                        .border(1.5.dp, primaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = equippedBadge.iconRes),
                        contentDescription = equippedBadge.title,
                        modifier = Modifier.size(34.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(primaryColor.copy(alpha = 0.2f))
                            .border(0.5.dp, primaryColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = equippedBadge.tier.title,
                            color = primaryColor,
                            fontSize = 7.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Text(
                        text = profile.callsign,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = equippedBadge.title,
                        color = primaryColor,
                        fontSize = 9.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Right: Global Standing Metric
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "GLOBAL TELEMETRY",
                    color = Color(0xFF556980),
                    fontSize = 8.sp,
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "RANK #${profile.globalLeaderboardRank}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${String.format(Locale.US, "%,d", profile.lifetimeBlocksDestroyed)} BLOCKS PURGED",
                    color = primaryColor,
                    fontSize = 8.sp,
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
