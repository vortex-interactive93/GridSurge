package com.example.gridsurge.ui.honors.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.honors.model.OperativeBadge
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import java.util.Locale

@Composable
fun AccoladeMilestoneCard(
    badge: OperativeBadge,
    isEquipped: Boolean,
    onEquip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && badge.isUnlocked) 0.97f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "cardScale"
    )

    val tierColor = badge.tier.primaryColor

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xDD080E1A))
            .border(
                width = if (isEquipped) 1.5.dp else 1.dp,
                color = when {
                    isEquipped -> Color(0xFF00FF66)
                    badge.isUnlocked -> tierColor.copy(alpha = 0.65f)
                    else -> Color(0xFF162338)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Top Row: Category + Star Bounty
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(tierColor.copy(alpha = 0.2f))
                            .border(0.5.dp, tierColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge.tier.title,
                            color = tierColor,
                            fontSize = 8.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Text(
                        text = badge.category.label,
                        color = Color(0xFF556980),
                        fontSize = 8.sp,
                        fontFamily = ChakraPetchFontFamily
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "+${badge.rewardStars} ★",
                        color = Color(0xFFFFB300),
                        fontSize = 11.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Middle Row: Emblem Preview & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (badge.isUnlocked) Color(0xFF0E1626) else Color(0xFF0A0F1A))
                        .border(1.dp, if (badge.isUnlocked) tierColor else Color(0xFF1E2D44), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (badge.isUnlocked) {
                        Image(
                            painter = painterResource(id = badge.iconRes),
                            contentDescription = badge.title,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFF435368),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = badge.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = badge.subtitle,
                        color = Color(0xFF8FA3BF),
                        fontSize = 9.sp,
                        fontFamily = ChakraPetchFontFamily
                    )
                }
            }

            // Bottom Row: Segmented Progress Bar & Equip CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (badge.isUnlocked) "ACCREDITATION SECURED" else "TELEMETRY PROGRESS",
                            color = Color(0xFF556980),
                            fontSize = 8.sp,
                            fontFamily = ChakraPetchFontFamily
                        )
                        Text(
                            text = "${String.format(Locale.US, "%,d", badge.currentProgress.coerceAtMost(badge.targetThreshold))} / ${String.format(
                                Locale.US, "%,d", badge.targetThreshold)}",
                            color = if (badge.isUnlocked) Color(0xFF00FF66) else tierColor,
                            fontSize = 9.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val ratio = (badge.currentProgress.toFloat() / badge.targetThreshold).coerceIn(0f, 1f)

                        drawRoundRect(
                            color = Color(0xFF121B2B),
                            size = Size(w, h),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                        drawRoundRect(
                            color = if (badge.isUnlocked) Color(0xFF00FF66) else tierColor,
                            size = Size(w * ratio, h),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }

                // Equip Action Button
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                isEquipped -> Color(0xFF0E2419)
                                badge.isUnlocked -> tierColor
                                else -> Color(0xFF131D2E)
                            }
                        )
                        .border(
                            0.8.dp,
                            if (isEquipped) Color(0xFF00FF66) else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable(
                            enabled = badge.isUnlocked && !isEquipped,
                            interactionSource = interactionSource,
                            indication = null
                        ) { onEquip() }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            isEquipped -> "EQUIPPED"
                            badge.isUnlocked -> "▶ EQUIP"
                            else -> "LOCKED"
                        },
                        color = when {
                            isEquipped -> Color(0xFF00FF66)
                            badge.isUnlocked -> Color(0xFF040711)
                            else -> Color(0xFF556980)
                        },
                        fontSize = 10.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
