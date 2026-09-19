package com.example.gridsurge.ui.campaign.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.game.campaign.model.SectorMilestoneTier
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily

@Composable
fun SectorMilestoneVaultStrip(
    totalStarsEarned: Int,
    maxStars: Int,
    milestones: List<SectorMilestoneTier>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xDD070E1A))
            .border(1.dp, Color(0xFF14243B), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(16.dp))
                    Text(
                        text = "SECTOR STAR BOUNTY",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "$totalStarsEarned / $maxStars ★",
                    color = Color(0xFFFFD600),
                    fontSize = 11.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black
                )
            }

            // Milestone Caches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                milestones.forEach { tier ->
                    val isUnlocked = totalStarsEarned >= tier.requiredStars
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isUnlocked) Color(0x3300FF66) else Color(0xFF0D1826))
                            .border(0.6.dp, if (isUnlocked) Color(0xFF00FF66) else Color(0xFF1E3048), RoundedCornerShape(4.dp))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${tier.requiredStars}★: ${tier.rewardLabel}",
                            color = if (isUnlocked) Color(0xFF00FF66) else Color(0xFF6B829E),
                            fontSize = 7.5.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
