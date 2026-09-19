package com.example.gridsurge.ui.quests

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.audio.BgmManager
import com.example.gridsurge.audio.BgmTrack
import com.example.gridsurge.audio.HapticType
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.operations.data.OperationsCatalog
import com.example.gridsurge.operations.model.DailyOperationsState
import com.example.gridsurge.operations.model.DirectiveStatus
import com.example.gridsurge.ui.components.StarVaultPill
import com.example.gridsurge.ui.operations.components.DirectiveCard
import com.example.gridsurge.ui.operations.components.OperationMilestoneTrack
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import kotlinx.coroutines.launch

@Composable
fun DailyMissionsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStore: () -> Unit,
    profileManager: PlayerProfileManager,
    viewModel: DailyMissionsViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val stars by profileManager.starCurrency.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        BgmManager.playTrack(context, BgmTrack.MAIN_HUB)
    }

    val opsState by viewModel.operationsState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF040711))
    ) {
        // Perspective Tactical Background Grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val lines = 14
            for (i in 0..lines) {
                val y = (h / lines) * i
                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = 0.03f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp)
        ) {
            // ==================== 1. TOP TELEMETRY ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC0D1424))
                        .border(1.dp, Color(0xFF1E2D44), RoundedCornerShape(8.dp))
                        .clickable {
                            SfxManager.playSfx(SfxType.UI_BACK)
                            onNavigateBack()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "HUB",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "DAILY OPERATIONS",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "DIRECTIVES & MILESTONE REWARDS",
                        color = Color(0xFF00E5FF),
                        fontSize = 8.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                StarVaultPill(
                    stars = stars,
                    onClick = {
                        SfxManager.playSfx(SfxType.UI_CONFIRM)
                        onNavigateToStore()
                    }
                )
            }

            // ==================== 2. GLOBAL RESET COUNTDOWN TERMINAL ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x990C121E))
                    .border(0.8.dp, Color(0xFF1B283A), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00FF66))
                    )
                    Text(
                        text = "TACTICAL SECTOR UPLINK // SYNCHRONIZED",
                        color = Color(0xFF8FA3BF),
                        fontSize = 8.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "CYCLE IN: ${opsState.resetCountdownFormatted}",
                    color = Color(0xFF00E5FF),
                    fontSize = 9.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }

            // ==================== 3. OPERATIONS STREAM ====================
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Operation Milestone Energy Conduit
                item {
                    OperationMilestoneTrack(
                        currentPoints = opsState.currentOpsPoints,
                        maxPoints = opsState.maxOpsPoints,
                        milestones = opsState.milestoneStates,
                        onClaimMilestone = { tier ->
                            SfxManager.playSfx(SfxType.LEVEL_COMPLETE)
                            SfxManager.triggerHaptic(HapticType.SURGE_EXPLOSION)
                            viewModel.claimMilestoneConduit(tier)
                        }
                    )
                }

                // Header Divider
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE SECTOR DIRECTIVES",
                            color = Color(0xFF00E5FF),
                            fontSize = 9.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "${opsState.directives.count { it.status == DirectiveStatus.CLAIMED }}/${opsState.directives.size} COMPLETED",
                            color = Color(0xFF556980),
                            fontSize = 8.sp,
                            fontFamily = ChakraPetchFontFamily
                        )
                    }
                }

                // Directives List
                items(opsState.directives, key = { it.id }) { directive ->
                    DirectiveCard(
                        directive = directive,
                        onDeploy = {
                            SfxManager.playSfx(SfxType.MODE_LOCK_IN)
                            onNavigateBack()
                        },
                        onClaim = {
                            SfxManager.playSfx(SfxType.UI_CONFIRM, overridePitch = 1.1f)
                            SfxManager.triggerHaptic(HapticType.CLICK)
                            viewModel.claimOperationDirective(directive.id)
                        }
                    )
                }
            }
        }
    }
}
