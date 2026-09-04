package com.example.gridsurge.ui.quests

import androidx.compose.animation.core.*
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
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.meta.quests.DailyMission
import com.example.gridsurge.meta.quests.QuestState
import com.example.gridsurge.meta.quests.QuestType
import com.example.gridsurge.ui.components.StarVaultPill
import com.example.gridsurge.audio.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun DailyMissionsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStore: () -> Unit,
    profileManager: PlayerProfileManager,
    viewModel: DailyMissionsViewModel,
    modifier: Modifier = Modifier
) {
    val missions by viewModel.missions.collectAsState()
    val isCrateClaimed by viewModel.isCrateClaimed.collectAsState()
    val stars by profileManager.starCurrency.collectAsState()

    val completedCount = missions.count { it.state == QuestState.CLAIMED }
    val totalMissions = missions.size.coerceAtLeast(5)

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        BgmManager.playTrack(context, BgmTrack.MAIN_HUB)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0C14))
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_main_hub),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC0A0C14))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xCC141926))
                        .border(1.dp, Color(0xFF26334D), RoundedCornerShape(10.dp))
                        .clickable { 
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            onNavigateBack() 
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "‹ HUB",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "DAILY MISSIONS",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )

                StarVaultPill(
                    stars = stars,
                    onClick = {
                        SfxManager.playSfx(SfxType.UI_CONFIRM)
                        onNavigateToStore()
                    }
                )
            }

            // Daily Reset & Milestone Banner
            DailyResetBanner(
                completedCount = completedCount,
                totalMissions = totalMissions,
                isCrateClaimed = isCrateClaimed,
                onClaimCrate = {
                    SfxManager.playSfx(SfxType.UI_CONFIRM)
                    viewModel.claimDailyCrate()
                }
            )

            // Mission Cards List
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(missions, key = { it.id }) { mission ->
                    CyberMissionCard(
                        mission = mission,
                        onClaim = { missionId ->
                            viewModel.claimMission(missionId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyResetBanner(
    completedCount: Int,
    totalMissions: Int,
    isCrateClaimed: Boolean,
    onClaimCrate: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val livePulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "live_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0x3300E5FF), Color(0x33E040FB))
                )
            )
            .border(1.dp, Color(0x6600E5FF), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color(0xFF00E676).copy(alpha = livePulse))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DAILY PROTOCOL RESET",
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "RESETS IN 18H 32M",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (completedCount >= totalMissions && !isCrateClaimed) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF00E676))))
                        .clickable { onClaimCrate() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "CLAIM CRATE (+200★)",
                        color = Color(0xFF0A0C14),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            } else if (isCrateClaimed) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x66141926))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "CRATE CLAIMED ✓",
                        color = Color(0xFF00E676),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x99141926))
                        .border(0.8.dp, Color(0xFFFFB300), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "STATUS: $completedCount / $totalMissions",
                        color = Color(0xFFFFB300),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
