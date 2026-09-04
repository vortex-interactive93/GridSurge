package com.example.gridsurge.ui.quests

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.R
import com.example.gridsurge.meta.quests.DailyMission
import com.example.gridsurge.meta.quests.QuestState
import com.example.gridsurge.meta.quests.QuestType
import com.example.gridsurge.ui.components.SegmentedCyberProgressBar

@Composable
fun CyberMissionCard(
    mission: DailyMission,
    onClaim: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val iconRes = when (mission.type) {
        QuestType.COMBO -> R.drawable.ic_overdrive_x5
        QuestType.LINES -> R.drawable.ic_quest_lines
        QuestType.SURGE_CORE -> R.drawable.sector_1_block
        QuestType.TIME_BLITZ -> R.drawable.ic_quest_lines
        QuestType.BLITZ_CLASH -> R.drawable.ic_quest_combo
    }

    val cardAccent = when (mission.state) {
        QuestState.CLAIMABLE -> Color(0xFF00E5FF)
        QuestState.CLAIMED -> Color(0xFF26334D)
        QuestState.IN_PROGRESS -> Color(0xFF00E5FF)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xEE101522),
                        Color(0xDD0D111A)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        cardAccent.copy(alpha = if (mission.state == QuestState.CLAIMED) 0.2f else 0.6f),
                        Color(0xFFE040FB).copy(alpha = if (mission.state == QuestState.CLAIMED) 0.1f else 0.3f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Vector Circular Icon Bezel
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF141926))
                    .border(1.5.dp, cardAccent.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = if (mission.type == QuestType.COMBO) Modifier.width(50.dp).height(30.dp) else Modifier.size(42.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // 2. Center Info & Dynamic Segmented Gauge
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = mission.title,
                        color = if (mission.state == QuestState.CLAIMED) Color(0xFF8A99AD) else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${mission.currentProgress} / ${mission.targetProgress}",
                        color = cardAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Segmented Progress Bar (Direct math alignment)
                SegmentedCyberProgressBar(
                    progress = mission.progressFraction,
                    totalSegments = 10,
                    activeGradient = listOf(Color(0xFF00E5FF), Color(0xFFE040FB))
                )
            }

            // 3. Dynamic Action Button
            MissionActionButton(
                state = mission.state,
                starReward = mission.starReward,
                onClick = { if (mission.state == QuestState.CLAIMABLE) onClaim(mission.id) }
            )
        }
    }
}

@Composable
private fun MissionActionButton(
    state: QuestState,
    starReward: Int,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "btn_pulse"
    )

    when (state) {
        QuestState.CLAIMABLE -> {
            Box(
                modifier = Modifier
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF00E5FF), Color(0xFF00B0FF))
                        )
                    )
                    .clickable { onClick() }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "CLAIM",
                        color = Color(0xFF0A0C14),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "+$starReward★",
                        color = Color(0xFF0A0C14),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
        QuestState.IN_PROGRESS -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF141926))
                    .border(1.dp, Color(0xFF26334D), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$starReward ★",
                    color = Color(0xFFFFB300),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        QuestState.CLAIMED -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x44141926))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CLAIMED",
                    color = Color(0xFF4A5568),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
