package com.example.gridsurge.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.R
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.ui.CyberChamferShape

enum class AchievementTier(
    val title: String,
    val levelCode: String,
    val color: Color,
    val iconRes: Int,
    val requiredScore: Int
) {
    TIER_01("INITIATE", "PROTO // 01", Color(0xFFCD7F32), R.drawable.ic_medal_crest_bronze, 0),
    TIER_02("OPERATIVE", "CYBER // 02", Color(0xFF00E5FF), R.drawable.ic_medal_tier_silver, 300),
    TIER_03("SURGE MASTER", "OVERLOAD // 03", Color(0xFFFFD700), R.drawable.ic_medal_star_gold, 800),
    TIER_04("SINGULARITY ARCHITECT", "TRANSCEND // 04", Color(0xFFFF0055), R.drawable.ic_medal_platinum, 1500)
}

data class TierAchievement(
    val id: String,
    val tier: AchievementTier,
    val title: String,
    val directive: String,
    val currentProgress: Int,
    val target: Int,
    val starReward: Int,
    val isClaimed: Boolean
) {
    val isCompleted: Boolean get() = currentProgress >= target
}

@Composable
fun TierAchievementsScreen(
    currentStarBalance: Int,
    totalAchievementScore: Int,
    achievements: List<TierAchievement>,
    onClaimReward: (String) -> Unit,
    onBackToHub: () -> Unit
) {
    var selectedTier by remember { mutableStateOf(AchievementTier.TIER_01) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF03060E))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x2200E5FF))
                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(6.dp))
                        .clickable {
                            SfxManager.playSfx(SfxType.MODAL_WHOOSH)
                            onBackToHub()
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("< HUB", color = Color(0xFF00E5FF), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "NEURAL ACHIEVEMENTS",
                    color = Color(0xFF5C8599),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .background(Color(0x22162238), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF263859), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("★ $currentStarBalance", color = Color(0xFFFFD700), fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active Tier Hero Banner with Medal Emblem
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xF00A1424), selectedTier.color.copy(alpha = 0.15f))
                        ),
                        shape = CyberChamferShape
                    )
                    .border(1.dp, selectedTier.color.copy(alpha = 0.6f), CyberChamferShape)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = selectedTier.iconRes),
                        contentDescription = selectedTier.title,
                        modifier = Modifier.size(68.dp),
                        contentScale = ContentScale.Fit
                    )
                    Column {
                        Text(
                            text = selectedTier.levelCode,
                            color = selectedTier.color,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedTier.title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tier Selector Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AchievementTier.entries.forEach { tier ->
                    val isSelected = tier == selectedTier
                    val isUnlocked = totalAchievementScore >= tier.requiredScore

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .background(
                                color = if (isSelected) tier.color.copy(alpha = 0.20f) else Color(0x160B1220),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) tier.color else if (isUnlocked) Color(0xFF1E2D4A) else Color(0x33FF0055),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                SfxManager.playSfx(SfxType.SNAP_TICK)
                                selectedTier = tier
                            }
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = tier.levelCode.split("//").last().trim(),
                                color = if (isSelected) tier.color else Color(0xFF5C8599),
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isUnlocked) tier.title.split(" ").first() else "LOCKED",
                                color = if (isSelected) Color.White else if (isUnlocked) Color(0xFF90A4AE) else Color(0x88FF0055),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Achievement Cards List
            val filteredList = achievements.filter { it.tier == selectedTier }
            var processingClaimIds by remember { mutableStateOf(setOf<String>()) }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.id }) { ach ->
                    val progressPct = (ach.currentProgress.toFloat() / ach.target.coerceAtLeast(1)).coerceIn(0f, 1f)
                    val animatedProgress by animateFloatAsState(targetValue = progressPct, label = "achProgress")
                    val isClaimingThis = processingClaimIds.contains(ach.id)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(listOf(Color(0xF00A1424), Color(0xDD040812))),
                                shape = CyberChamferShape
                            )
                            .border(
                                width = 1.dp,
                                color = if (ach.isCompleted && !ach.isClaimed) selectedTier.color else Color(0x441E2D4A),
                                shape = CyberChamferShape
                            )
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ach.title,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = ach.directive,
                                        color = Color(0xFF78909C),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                if (ach.isClaimed || isClaimingThis) {
                                    Box(
                                        modifier = Modifier
                                            .height(32.dp)
                                            .background(Color(0x16162238), RoundedCornerShape(4.dp))
                                            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isClaimingThis) "CLAIMING..." else "CLAIMED",
                                            color = Color(0xFF5C8599),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else if (ach.isCompleted) {
                                    Box(
                                        modifier = Modifier
                                            .height(32.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(selectedTier.color)
                                            .clickable {
                                                if (!processingClaimIds.contains(ach.id)) {
                                                    processingClaimIds = processingClaimIds + ach.id
                                                    SfxManager.playSfx(SfxType.LEVEL_COMPLETE)
                                                    onClaimReward(ach.id)
                                                }
                                            }
                                            .padding(horizontal = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("CLAIM +${ach.starReward} ★", color = Color(0xFF03060E), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .height(32.dp)
                                            .background(Color(0x22162238), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+${ach.starReward} ★", color = Color(0xFFFFD700), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(5.dp)
                                        .background(Color(0xFF0E1A2E), RoundedCornerShape(2.5.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(animatedProgress)
                                            .fillMaxHeight()
                                            .background(
                                                brush = Brush.horizontalGradient(listOf(selectedTier.color.copy(alpha = 0.6f), selectedTier.color)),
                                                shape = RoundedCornerShape(2.5.dp)
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${ach.currentProgress} / ${ach.target}",
                                    color = Color(0xFF90A4AE),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}
