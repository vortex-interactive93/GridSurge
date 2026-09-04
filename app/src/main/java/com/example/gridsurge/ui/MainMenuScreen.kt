package com.example.gridsurge.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.R
import com.example.gridsurge.audio.*
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.meta.ThemeCatalog
import com.example.gridsurge.meta.data.DailyLoginRepository
import com.example.gridsurge.settings.SettingsManager
import com.example.gridsurge.armory.data.ArmoryDataStoreRepository
import com.example.gridsurge.ui.components.GridSurgeHeroLogo
import com.example.gridsurge.ui.components.StarVaultPill
import com.example.gridsurge.ui.settings.SettingsDialog
import com.example.gridsurge.game.glitch.*
import com.example.gridsurge.ui.glitch.DailyGlitchEntryDialog
import com.example.gridsurge.ui.screens.DailyLoginDialog
import com.example.gridsurge.ui.screens.UPLINK_REWARDS
import com.example.gridsurge.ui.dialogs.CyberAvatarRegistry
import com.example.gridsurge.ui.dialogs.CyberProfileSetupDialog
import com.example.gridsurge.ui.dialogs.ModeSelectionCatalog
import com.example.gridsurge.ui.dialogs.ModeSelectionDrawer
import com.example.gridsurge.ui.modifiers.cyberBorderGlow
import kotlinx.coroutines.launch

@Composable
fun MainMenuScreen(
    profileManager: PlayerProfileManager,
    armoryRepository: ArmoryDataStoreRepository,
    dailyLoginRepository: DailyLoginRepository,
    onNavigate: (Screen) -> Unit
) {
    val scope = rememberCoroutineScope()
    val dailyLoginState by dailyLoginRepository.loginStateFlow.collectAsState(initial = null)
    var showDailyLoginDialog by remember { mutableStateOf(false) }

    LaunchedEffect(dailyLoginState) {
        if (dailyLoginState?.canClaimToday == true) {
            showDailyLoginDialog = true
        }
    }

    val stars by profileManager.starCurrency.collectAsState()
    val rawEquippedSkinId by profileManager.equippedBlockSkinId.collectAsState()
    val equippedTheme = remember(rawEquippedSkinId) { ThemeCatalog.getThemeById(rawEquippedSkinId) }

    val callsign by profileManager.callsign.collectAsState()
    val avatarKey by profileManager.avatarKey.collectAsState()
    val activeAvatar = CyberAvatarRegistry.getPresetById(avatarKey)

    var selectedModeIndex by remember { mutableIntStateOf(0) }
    val currentModeSpec = ModeSelectionCatalog.ALL_MODES[selectedModeIndex]

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDailyGlitchDialog by remember { mutableStateOf(false) }
    var showProfileEditModal by remember { mutableStateOf(false) }
    var showModeSelectionDrawer by remember { mutableStateOf(false) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        BgmManager.playTrack(context, BgmTrack.MAIN_HUB)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0C14))
    ) {
        // Ambient Neon Grid Horizon (Animated Perspective Grid)
        val infiniteTransition = rememberInfiniteTransition(label = "gridScroll")
        val gridOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "offset"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val horizonY = h * 0.45f
            
            val lines = 12
            for (i in 0..lines) {
                val startX = w / 2f
                val startY = horizonY
                val endX = (w / lines) * i
                val endY = h
                drawLine(
                    color = Color(0x1A00E5FF),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val horizontalLines = 8
            for (i in 0..horizontalLines) {
                val t = (i.toFloat() / horizontalLines + gridOffset) % 1f
                val lineY = horizonY + (h - horizonY) * (t * t)
                drawLine(
                    color = Color(0x1A00E5FF),
                    start = Offset(0f, lineY),
                    end = Offset(w, lineY),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        Image(
            painter = painterResource(id = R.drawable.bg_main_hub),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x660A0C14))
        )

        // Main UI Layout (The Cyber Command Deck)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Zone 1: Status Header Bar (Icon-Only Actions, No Text Wrapping)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Operative Profile Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xCC141926))
                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(10.dp))
                        .clickable {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            showProfileEditModal = true
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Image(
                        painter = painterResource(id = activeAvatar.iconRes),
                        contentDescription = callsign,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(callsign, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }

                // Center: Star Vault Pill
                StarVaultPill(
                    stars = stars,
                    onClick = { 
                        SfxManager.playSfx(SfxType.UI_CONFIRM)
                        onNavigate(Screen.STORE) 
                    }
                )

                // Right: Clean 40x40 Icon Buttons (Quests & Settings)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Quests Icon Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xCC141926))
                            .border(1.dp, Color(0xFF26334D), RoundedCornerShape(10.dp))
                            .clickable {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onNavigate(Screen.QUESTS)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = "Quests",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFB300))
                        )
                    }

                    // Settings Icon Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xCC141926))
                            .border(1.dp, Color(0xFF26334D), RoundedCornerShape(10.dp))
                            .clickable {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                showSettingsDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF8A99AD),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Zone 2: Action & Hero Centerpiece
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Hero Logo (Moved higher)
                GridSurgeHeroLogo(
                    modifier = Modifier.offset(y = (-24).dp)
                )

                // Mode Hero Card (With Inline < > Cycle Arrows & Tap To Select)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cyberBorderGlow(
                            colors = listOf(currentModeSpec.accentColor, Color.Transparent),
                            cornerRadius = 18.dp
                        )
                        .clip(CyberChamferShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xF00D1526), Color(0xFE060A14))
                            )
                        )
                        .border(1.5.dp, currentModeSpec.accentColor, CyberChamferShape)
                        .clickable {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            showModeSelectionDrawer = true
                        }
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left Arrow <
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x33141926))
                                .border(1.dp, currentModeSpec.accentColor.copy(alpha = 0.5f), CircleShape)
                                .clickable {
                                    SfxManager.playSfx(SfxType.SNAP_TICK)
                                    selectedModeIndex = if (selectedModeIndex > 0) selectedModeIndex - 1 else ModeSelectionCatalog.ALL_MODES.size - 1
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("‹", color = currentModeSpec.accentColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }

                        // Center Mode Info
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = currentModeSpec.category,
                                color = currentModeSpec.accentColor,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = currentModeSpec.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = currentModeSpec.subtitle,
                                color = Color(0xFF90A4AE),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x2200E5FF))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "[ TAP TO CHANGE MODE ]",
                                    color = Color(0xFF00E5FF),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Right Arrow >
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x33141926))
                                .border(1.dp, currentModeSpec.accentColor.copy(alpha = 0.5f), CircleShape)
                                .clickable {
                                    SfxManager.playSfx(SfxType.SNAP_TICK)
                                    selectedModeIndex = (selectedModeIndex + 1) % ModeSelectionCatalog.ALL_MODES.size
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("›", color = currentModeSpec.accentColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live-Ops Event Marquee Ribbon
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0x3300E676), Color(0x3300E5FF))
                            )
                        )
                        .border(1.dp, Color(0x8800E676), RoundedCornerShape(10.dp))
                        .clickable {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            selectedModeIndex = 2 // Daily Glitch
                            showDailyGlitchDialog = true
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡ LIVE: DAILY GLITCH (Resets in 18h) • 500★ REWARD",
                        color = Color(0xFF00E676),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dominant PLAY NOW Action Anchor (Matching active mode theme color)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .cyberBorderGlow(
                            colors = listOf(currentModeSpec.accentColor, currentModeSpec.accentColor.copy(alpha = 0.5f)),
                            cornerRadius = 14.dp
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(currentModeSpec.accentColor, currentModeSpec.accentColor.copy(alpha = 0.75f))
                            )
                        )
                        .clickable {
                            SfxManager.playSfx(SfxType.LEVEL_COMPLETE)
                            if (currentModeSpec.screen == Screen.DAILY_GLITCH) {
                                showDailyGlitchDialog = true
                            } else {
                                onNavigate(currentModeSpec.screen)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶ START MATCH",
                        color = Color(0xFF040812),
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Zone 3: Persistent 3-Tab Meta Navigation Dock (Armory | Achievements | Leaderboards)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Dock Tab 1: Armory
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xCC141926))
                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(12.dp))
                        .clickable {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            onNavigate(Screen.ARMORY)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Image(
                            painter = painterResource(id = equippedTheme.blockSkinRes),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("ARMORY", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }

                // Dock Tab 2: Achievements
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xCC141926))
                        .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                        .clickable {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            onNavigate(Screen.ACHIEVEMENTS)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏆 REWARDS", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }

                // Dock Tab 3: Leaderboards
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xCC141926))
                        .border(1.dp, Color(0xFFFF0055), RoundedCornerShape(12.dp))
                        .clickable {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            onNavigate(Screen.LEADERBOARD)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("📊 RANKS", color = Color(0xFFFF0055), fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Modals & Drawers
        if (showModeSelectionDrawer) {
            ModeSelectionDrawer(
                selectedScreen = currentModeSpec.screen,
                onSelectMode = { spec ->
                    val index = ModeSelectionCatalog.ALL_MODES.indexOfFirst { it.title == spec.title }
                    if (index >= 0) selectedModeIndex = index
                    showModeSelectionDrawer = false
                },
                onDismiss = { showModeSelectionDrawer = false }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                settingsManager = SettingsManager.getInstance(LocalContext.current),
                onDismiss = { showSettingsDialog = false }
            )
        }

        if (showProfileEditModal) {
            CyberProfileSetupDialog(
                profileManager = profileManager,
                isPvpRequiredNotice = false,
                onProfileInitialized = { showProfileEditModal = false },
                onNavigateToAuth = {
                    showProfileEditModal = false
                    onNavigate(Screen.AUTH)
                },
                onDismiss = { showProfileEditModal = false }
            )
        }

        if (showDailyGlitchDialog) {
            val todaySeedDate = DailyGlitchCountdownManager.getSeedHeaderDate()
            val lastGlitchDate by profileManager.lastGlitchSeedDate.collectAsState()
            val hasTicketToday = lastGlitchDate != todaySeedDate

            val dailyUiState = remember(hasTicketToday, todaySeedDate) {
                DailyGlitchUiState(
                    seedDateFormatted = todaySeedDate,
                    timeRemainingMillis = DailyGlitchCountdownManager.getMillisUntilNextUtcMidnight(),
                    formattedTimeRemaining = DailyGlitchCountdownManager.formatDurationHms(DailyGlitchCountdownManager.getMillisUntilNextUtcMidnight()),
                    hasTicketAvailable = hasTicketToday,
                    userPersonalBestScore = 0L,
                    userPersonalBestWaves = 0,
                    userRank = null,
                    retryStarCost = 100,
                    leaderboardPreview = listOf(
                        DailyLeaderboardEntry(1, "CYBER_GHOST", 14200L, 8, DailyGlitchTier.BRONZE),
                        DailyLeaderboardEntry(2, "NEON_VIPER", 12100L, 6, DailyGlitchTier.BRONZE),
                        DailyLeaderboardEntry(3, "VOID_WALKER", 9800L, 5, DailyGlitchTier.BRONZE)
                    ),
                    userEntry = null
                )
            }

            val isNoAdsPurchased by profileManager.isNoAdsPurchased.collectAsState()

            DailyGlitchEntryDialog(
                uiState = dailyUiState,
                isNoAdsPurchased = isNoAdsPurchased,
                onLaunchMission = {
                    profileManager.consumeGlitchTicket(todaySeedDate)
                    showDailyGlitchDialog = false
                    onNavigate(Screen.DAILY_GLITCH)
                },
                onDismiss = { showDailyGlitchDialog = false }
            )
        }

        if (showDailyLoginDialog && dailyLoginState != null) {
            DailyLoginDialog(
                currentDayStreak = dailyLoginState!!.currentStreak,
                canClaimToday = dailyLoginState!!.canClaimToday,
                onClaimDay = { reward ->
                    scope.launch {
                        dailyLoginRepository.claimReward(reward.starAmount)
                        showDailyLoginDialog = false
                    }
                },
                onDismiss = { showDailyLoginDialog = false }
            )
        }
    }
}
