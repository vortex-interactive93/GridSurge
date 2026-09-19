package com.example.gridsurge.ui.monetization

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily

@Composable
fun TacticalAdMultiplierCard(
    bonusStarAmount: Int,
    isNoAdsVip: Boolean,
    isAdReady: Boolean,
    onClaimMultiplier: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "adCardScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "adGlow")
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "sheen"
    )

    val primaryColor = if (isNoAdsVip) Color(0xFF00FF66) else Color(0xFFFFD600)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.horizontalGradient(
                    if (isNoAdsVip) listOf(Color(0xFF003816), Color(0xFF001F0D))
                    else listOf(Color(0xFF332600), Color(0xFF1A1400))
                )
            )
            .border(1.2.dp, primaryColor.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isAdReady || isNoAdsVip
            ) {
                SfxManager.playSfx(SfxType.UI_CONFIRM)
                onClaimMultiplier()
            },
        contentAlignment = Alignment.Center
    ) {
        // Specular Sheen Pass
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val sheenX = w * sheenProgress
            val sheenWidth = w * 0.35f

            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.20f), Color.Transparent),
                    start = Offset(sheenX, 0f),
                    end = Offset(sheenX + sheenWidth, h)
                ),
                size = Size(w, h)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(primaryColor.copy(alpha = 0.2f))
                        .border(1.dp, primaryColor, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isNoAdsVip) Icons.Default.Star else Icons.Default.Tv,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = if (isNoAdsVip) "VIP PRIVILEGE // NO ADS" else "NEURAL TRANSMISSION // AD",
                        color = Color(0xFF8FA3BF),
                        fontSize = 8.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "DOUBLER: +$bonusStarAmount ★ STARS",
                        color = primaryColor,
                        fontSize = 12.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(primaryColor)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isNoAdsVip) "INSTANT" else if (isAdReady) "WATCH" else "LOADING",
                    color = Color(0xFF03070E),
                    fontSize = 10.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
