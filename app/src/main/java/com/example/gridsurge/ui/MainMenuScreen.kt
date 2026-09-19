package com.example.gridsurge.ui

import android.app.Activity
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import com.example.gridsurge.ads.AdManager
import com.example.gridsurge.monetization.engine.FirewallJammerManager
import com.example.gridsurge.ui.hub.components.FirewallJammerCard
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.R
import com.example.gridsurge.armory.data.ArmoryDataStoreRepository
import com.example.gridsurge.audio.BgmManager
import com.example.gridsurge.audio.BgmTrack
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.game.glitch.DailyGlitchCountdownManager
import com.example.gridsurge.game.glitch.DailyGlitchTier
import com.example.gridsurge.game.glitch.DailyGlitchUiState
import com.example.gridsurge.game.glitch.DailyLeaderboardEntry
import com.example.gridsurge.hub.model.GameMode
import com.example.gridsurge.hub.model.ModeTelemetry
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.meta.data.DailyLoginRepository
import com.example.gridsurge.meta.data.DailyMissionsRepository
import kotlinx.coroutines.flow.flowOf
import com.example.gridsurge.settings.SettingsManager
import com.example.gridsurge.ui.components.StarVaultPill
import com.example.gridsurge.ui.components.SubspaceStarfield
import com.example.gridsurge.ui.dialogs.CyberAvatarRegistry
import com.example.gridsurge.ui.dialogs.CyberProfileSetupDialog
import com.example.gridsurge.ui.glitch.DailyGlitchEntryDialog
import com.example.gridsurge.ui.modifiers.cyberBorderGlow
import com.example.gridsurge.ui.screens.DailyLoginDialog
import com.example.gridsurge.ui.settings.SettingsDialog
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun MainMenuScreen(
    profileManager: PlayerProfileManager,
    armoryRepository: ArmoryDataStoreRepository,
    dailyLoginRepository: DailyLoginRepository,
    dailyMissionsRepository: DailyMissionsRepository? = null,
    onNavigate: (Screen) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val claimableMissionsCount by remember(dailyMissionsRepository) {
        dailyMissionsRepository?.getClaimableMissionsCount() ?: flowOf(0)
    }.collectAsState(initial = 0)

    val dailyLoginState by dailyLoginRepository.loginStateFlow.collectAsState(initial = null)
    var showDailyLoginDialog by remember { mutableStateOf(false) }

    LaunchedEffect(dailyLoginState) {
        if (dailyLoginState?.canClaimToday == true) {
            showDailyLoginDialog = true
        }
    }

    val stars by profileManager.starCurrency.collectAsState()
    val callsign by profileManager.callsign.collectAsState()
    val avatarKey by profileManager.avatarKey.collectAsState()
    val activeAvatar = CyberAvatarRegistry.getPresetById(avatarKey)

    val favModeKey by profileManager.favoriteMode.collectAsState()
    val lastModeKey by profileManager.lastPlayedMode.collectAsState()
    val startModeKey = favModeKey.ifBlank { lastModeKey }

    val modes = remember { GameMode.entries }
    val startModeIndex = remember(startModeKey) {
        val idx = modes.indexOfFirst { it.name == startModeKey }
        if (idx >= 0) idx else 0
    }

    val totalLoopCount = 10_000
    val initialPage = remember(startModeIndex) {
        (totalLoopCount / 2) * modes.size + startModeIndex
    }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { totalLoopCount * modes.size })

    // Acoustic detent on carousel page change
    var lastSettledPage by remember { mutableIntStateOf(pagerState.settledPage) }
    LaunchedEffect(pagerState.settledPage) {
        if (pagerState.settledPage != lastSettledPage) {
            SfxManager.playSfx(SfxType.CAROUSEL_SWIPE, volume = 0.5f)
            lastSettledPage = pagerState.settledPage
        }
    }

    val userHighScore by profileManager.highScore.collectAsState()
    val userTotalRuns by profileManager.totalRuns.collectAsState()
    val userGlitchScore by profileManager.glitchBestScore.collectAsState()
    val userBlitzScore by profileManager.blitzHighScore.collectAsState()
    val userRatingPoints by profileManager.ratingPoints.collectAsState()

    // Telemetry mapping
    val telemetryMap = remember(userHighScore, userTotalRuns, userGlitchScore, userBlitzScore, userRatingPoints) {
        mapOf(
            GameMode.CLASSIC to ModeTelemetry(userHighScore.toLong(), "SORTIES", userTotalRuns.toString()),
            GameMode.CAMPAIGN to ModeTelemetry(0L, "NODES PURGED", "0/45"),
            GameMode.DAILY_GLITCH to ModeTelemetry(userGlitchScore, "CYCLE RESET", "24H"),
            GameMode.TIME_BLITZ to ModeTelemetry(userBlitzScore, "TOP SCORE", String.format(Locale.US, "%,d", userBlitzScore)),
            GameMode.BLITZ_CLASH to ModeTelemetry(userRatingPoints.toLong(), "PVP RATING", "$userRatingPoints MMR")
        )
    }

    // Dynamic Chromatic Horizon Blending
    val currentPosition = pagerState.currentPage + pagerState.currentPageOffsetFraction
    val activeAccentColor = remember(currentPosition) {
        val baseIndex = Math.floorMod(currentPosition.toInt(), modes.size)
        val nextIndex = Math.floorMod(baseIndex + 1, modes.size)
        val fraction = (currentPosition - currentPosition.toInt()).coerceIn(0f, 1f)
        lerp(modes[baseIndex].primaryColor, modes[nextIndex].primaryColor, fraction)
    }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDailyGlitchDialog by remember { mutableStateOf(false) }
    var showProfileEditModal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        BgmManager.playTrack(context, BgmTrack.MAIN_HUB)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040711))
    ) {
        // 1. Dynamic Chromatic Horizon Glow & Grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        activeAccentColor.copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = Offset(w / 2f, h * 0.42f),
                    radius = w * 0.95f
                ),
                center = Offset(w / 2f, h * 0.42f),
                radius = w * 0.95f
            )

            // Perspective Grid Wireframe
            val lines = 12
            for (i in 0..lines) {
                val y = (h / lines) * i
                drawLine(
                    color = activeAccentColor.copy(alpha = 0.025f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }
        }

        // 2. Subspace Starfield Particle Warp
        SubspaceStarfield(
            pagerOffset = currentPosition,
            accentColor = activeAccentColor
        )

        // 3. Cyber Atmospheric Particle Motes
        AmbientCyberMotes(accentColor = activeAccentColor)

        // Vignette Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x9905070E), Color(0x3305070E), Color(0xF205070E))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ==================== 1. TOP COMMAND TELEMETRY ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Operative Dossier Capsule
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xCC0D1424))
                        .border(1.dp, activeAccentColor.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                        .clickable {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            showProfileEditModal = true
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Image(
                        painter = painterResource(id = activeAvatar.iconRes),
                        contentDescription = callsign,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = callsign,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = OrbitronFontFamily,
                        letterSpacing = 0.5.sp
                    )
                }

                // Currency & Settings Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StarVaultPill(
                        stars = stars,
                        onClick = {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            onNavigate(Screen.STORE)
                        }
                    )

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xCC0D1424))
                            .border(1.dp, Color(0xFF1E2D44), RoundedCornerShape(10.dp))
                            .clickable {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                showSettingsDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF8FA3BF),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // ==================== 2. HERO TITLE & TACTICAL PIPS ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "GRID SURGE",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "QUANTUM PUZZLE PROTOCOL // SELECT VECTOR",
                    color = activeAccentColor,
                    fontSize = 8.sp,
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                // Tactical Segmented Pager Pips
                TacticalPagerPips(
                    pageCount = modes.size,
                    currentPage = Math.floorMod(pagerState.currentPage, modes.size),
                    accentColor = activeAccentColor,
                    onSelectPage = { targetPageIndex ->
                        scope.launch {
                            val currentBase = (pagerState.currentPage / modes.size) * modes.size
                            pagerState.animateScrollToPage(currentBase + targetPageIndex)
                        }
                    },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // ==================== 3. 3D CYLINDRICAL MODE CAROUSEL ====================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val density = LocalDensity.current.density
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 38.dp),
                    pageSpacing = 12.dp,
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->
                    val actualIndex = Math.floorMod(pageIndex, modes.size)
                    val mode = modes[actualIndex]
                    val telemetry = telemetryMap[mode] ?: ModeTelemetry(0L, "--", "--")

                    // Real-Time 3D Projection Math
                    val pageOffset = (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                    val absOffset = abs(pageOffset)

                    val cardScale = (1.0f - (absOffset * 0.12f)).coerceIn(0.88f, 1.0f)
                    val cardAlpha = (1.0f - (absOffset * 0.45f)).coerceIn(0.40f, 1.0f)
                    val cardRotationY = (-pageOffset * 15f).coerceIn(-20f, 20f)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = cardScale
                                scaleY = cardScale
                                alpha = cardAlpha
                                rotationY = cardRotationY
                                cameraDistance = 12f * density
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        TacticalModeCard(
                            mode = mode,
                            telemetry = telemetry,
                            pageOffset = pageOffset,
                            isFavorite = favModeKey == mode.name,
                            onToggleFavorite = {
                                SfxManager.playSfx(SfxType.STAR_TOGGLE)
                                profileManager.setFavoriteMode(mode.name)
                            },
                            onInitiate = {
                                SfxManager.playSfx(SfxType.MODE_LOCK_IN)
                                profileManager.recordLastPlayedMode(mode.name)
                                if (mode == GameMode.DAILY_GLITCH) {
                                    showDailyGlitchDialog = true
                                } else {
                                    onNavigate(mode.targetScreen)
                                }
                            }
                        )
                    }
                }
            }

            // ==================== 3.5. FIREWALL JAMMER SHIELD CARD ====================
            val jammerManager = remember(context) { FirewallJammerManager(context) }
            val jammerState by jammerManager.jammerState.collectAsState()

            FirewallJammerCard(
                state = jammerState,
                onWatchAdToExtend = {
                    val activity = context as? Activity ?: return@FirewallJammerCard
                    AdManager.showRewardedAd(
                        activity = activity,
                        isNoAdsPurchased = profileManager.isNoAdsPurchased.value,
                        onRewardEarned = {
                            jammerManager.stackJammerTime()
                            SfxManager.playSfx(SfxType.LEVEL_COMPLETE, overridePitch = 1.4f)
                        }
                    )
                },
                onVipUpgradeClick = {
                    onNavigate(Screen.STORE)
                },
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // ==================== 4. UNIFIED META NAVIGATION DOCK ====================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xCC0A101C))
                    .border(1.dp, Color(0xFF1B2A40), RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DockTabItem(
                        label = "ARMORY",
                        iconRes = R.drawable.ic_dock_armory,
                        accentColor = Color(0xFF00E5FF),
                        onClick = { onNavigate(Screen.ARMORY) },
                        modifier = Modifier.weight(1f)
                    )

                    DockTabItem(
                        label = "MISSIONS",
                        iconRes = R.drawable.ic_dock_missions,
                        badgeCount = claimableMissionsCount,
                        accentColor = Color(0xFFFFB300),
                        onClick = { onNavigate(Screen.QUESTS) },
                        modifier = Modifier.weight(1f)
                    )

                    DockTabItem(
                        label = "RANKS",
                        iconRes = R.drawable.ic_dock_ranks,
                        accentColor = Color(0xFFFF0055),
                        onClick = { onNavigate(Screen.LEADERBOARD) },
                        modifier = Modifier.weight(1f)
                    )

                    DockTabItem(
                        label = "VAULT",
                        iconRes = R.drawable.ic_dock_vault,
                        accentColor = Color(0xFF00FF66),
                        onClick = { onNavigate(Screen.CAREER) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Drawers & Overlays
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
            val lastExtraRetryDate by profileManager.lastGlitchExtraRetryDate.collectAsState()

            val hasTicketToday = lastGlitchDate != todaySeedDate
            val hasExtraRetryToday = lastExtraRetryDate != todaySeedDate

            val userGlitchScore by profileManager.glitchBestScore.collectAsState()
            val userGlitchWaves by profileManager.glitchBestWaves.collectAsState()

            val dailyUiState = remember(hasTicketToday, hasExtraRetryToday, todaySeedDate, userGlitchScore, userGlitchWaves) {
                DailyGlitchUiState(
                    seedDateFormatted = todaySeedDate,
                    timeRemainingMillis = DailyGlitchCountdownManager.getMillisUntilNextUtcMidnight(),
                    formattedTimeRemaining = DailyGlitchCountdownManager.formatDurationHms(
                        DailyGlitchCountdownManager.getMillisUntilNextUtcMidnight()
                    ),
                    hasTicketAvailable = hasTicketToday,
                    hasExtraRetryAvailable = hasExtraRetryToday,
                    userPersonalBestScore = userGlitchScore,
                    userPersonalBestWaves = userGlitchWaves,
                    userRank = null,
                    retryStarCost = 100,
                    leaderboardPreview = emptyList(),
                    userEntry = null
                )
            }

            val isNoAdsPurchased by profileManager.isNoAdsPurchased.collectAsState()

            DailyGlitchEntryDialog(
                uiState = dailyUiState,
                isNoAdsPurchased = isNoAdsPurchased,
                onLaunchMission = {
                    if (hasTicketToday) {
                        profileManager.consumeGlitchTicket(todaySeedDate)
                    } else {
                        profileManager.consumeGlitchExtraRetry(todaySeedDate)
                    }
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

/**
 * Tactical Mode Card with Specular Sweep & Procedural Radar Graticule.
 */
@Composable
private fun TacticalModeCard(
    mode: GameMode,
    telemetry: ModeTelemetry,
    pageOffset: Float,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onInitiate: () -> Unit
) {
    val midLayerParallax = -pageOffset * 10f
    val deepGlyphParallax = -pageOffset * 26f

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "cardMotion")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corePulse"
    )

    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAngle"
    )

    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheenProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(410.dp)
            .cyberBorderGlow(
                colors = listOf(mode.primaryColor, Color.Transparent),
                cornerRadius = 16.dp
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xF20F1829), Color(0xFD050912))
                )
            )
            .border(1.4.dp, mode.primaryColor.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card Header
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
                            .background(mode.primaryColor.copy(alpha = 0.2f))
                            .border(0.5.dp, mode.primaryColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = mode.badgeLabel,
                            color = mode.primaryColor,
                            fontSize = 8.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite Mode",
                        tint = if (isFavorite) Color(0xFFFFD600) else Color(0x66FFFFFF),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onToggleFavorite() }
                    )
                }

                Text(
                    text = if (isFavorite) "★ FAVORITE" else "SECTOR V4.2",
                    color = if (isFavorite) Color(0xFFFFD600) else Color(0xFF556980),
                    fontSize = 8.sp,
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = if (isFavorite) FontWeight.Bold else FontWeight.Normal
                )
            }

            // Mode Title Block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = mode.title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = mode.subtitle,
                    color = mode.primaryColor,
                    fontSize = 9.sp,
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Mode Core Viewport (Central Holographic Diorama)
            Box(
                modifier = Modifier
                    .size(135.dp)
                    .graphicsLayer {
                        translationX = midLayerParallax
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xCC070C17))
                    .border(1.dp, mode.primaryColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Procedural Circular Reticle Graticule & Radar Sweep
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = size.width * 0.44f

                    drawCircle(
                        color = mode.primaryColor.copy(alpha = 0.15f * pulse),
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(mode.primaryColor.copy(alpha = 0.35f * pulse), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = r
                        ),
                        radius = r,
                        center = Offset(cx, cy)
                    )

                    // Sweeping Radar Line
                    val endX = cx + r * cos(radarAngle)
                    val endY = cy + r * sin(radarAngle)
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(mode.primaryColor.copy(alpha = 0.7f), Color.Transparent),
                            start = Offset(cx, cy),
                            end = Offset(endX, endY)
                        ),
                        start = Offset(cx, cy),
                        end = Offset(endX, endY),
                        strokeWidth = 1.8f
                    )
                }

                Image(
                    painter = painterResource(id = mode.glyphRes),
                    contentDescription = mode.title,
                    modifier = Modifier
                        .size(95.dp)
                        .graphicsLayer {
                            translationX = deepGlyphParallax
                            blendMode = BlendMode.Screen
                        },
                    contentScale = ContentScale.Fit
                )
            }

            // Lore / Description
            Text(
                text = mode.description,
                color = Color(0xFF8FA3BF),
                fontSize = 10.sp,
                fontFamily = ChakraPetchFontFamily,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Telemetry Readout Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x990A101C))
                    .border(0.8.dp, Color(0xFF1B283C), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "APEX RECORD",
                        color = Color(0xFF556980),
                        fontSize = 8.sp,
                        fontFamily = ChakraPetchFontFamily
                    )
                    Text(
                        text = String.format(Locale.US, "%,d", telemetry.apexScore),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = telemetry.secondaryLabel,
                        color = Color(0xFF556980),
                        fontSize = 8.sp,
                        fontFamily = ChakraPetchFontFamily
                    )
                    Text(
                        text = telemetry.secondaryValue,
                        color = mode.primaryColor,
                        fontSize = 13.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Primary Initiation CTA Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .graphicsLayer {
                        scaleX = buttonScale
                        scaleY = buttonScale
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(mode.primaryColor, mode.primaryColor.copy(alpha = 0.85f))
                        )
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onInitiate() },
                contentAlignment = Alignment.Center
            ) {
                // Specular Light Sweep Pass
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val sheenX = w * sheenProgress
                    val sheenWidth = w * 0.35f

                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.28f),
                                Color.Transparent
                            ),
                            start = Offset(sheenX, 0f),
                            end = Offset(sheenX + sheenWidth, h)
                        ),
                        size = Size(w, h)
                    )
                }

                Text(
                    text = mode.ctaLabel,
                    color = Color(0xFF040711),
                    fontSize = 12.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
            }
        }
    }
}

/**
 * Segmented HUD Pagination Pips.
 */
@Composable
private fun TacticalPagerPips(
    pageCount: Int,
    currentPage: Int,
    accentColor: Color,
    onSelectPage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until pageCount) {
            val isSelected = i == currentPage
            val pipWidth by animateDpAsState(
                targetValue = if (isSelected) 26.dp else 10.dp,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "pipWidth"
            )

            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(pipWidth)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isSelected) accentColor else Color(0xFF1E2D44))
                    .clickable { onSelectPage(i) }
            )
        }
    }
}

/**
 * Bottom HUD Dock Navigation Bar.
 */
@Composable
private fun DockTabItem(
    label: String,
    @DrawableRes iconRes: Int,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                SfxManager.playSfx(SfxType.UI_CONFIRM)
                onClick()
            }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            blendMode = BlendMode.Screen
                        }
                )
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-4).dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB300)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$badgeCount",
                            color = Color(0xFF060911),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = ChakraPetchFontFamily
                        )
                    }
                }
            }

            Text(
                text = label,
                color = accentColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ChakraPetchFontFamily,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Ambient floating data motes (Cyber Dust) that provide living depth to the cockpit.
 */
@Composable
private fun AmbientCyberMotes(accentColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "moteCycle")
    val cycleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "motes"
    )

    // Pre-allocated immutable mote coordinates
    val motes = remember {
        List(18) {
            Triple(
                Random.nextFloat(), // X ratio
                Random.nextFloat(), // Base Y ratio
                Random.nextFloat() * 2.5f + 1.5f // Radius
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        motes.forEach { (xRatio, ySeed, radius) ->
            val curY = ((ySeed - cycleProgress + 1f) % 1f) * h
            val curX = (xRatio * w) + (sin((cycleProgress * 6.28f) + (xRatio * 10f)) * 16f)

            drawCircle(
                color = accentColor.copy(alpha = 0.18f),
                radius = radius,
                center = Offset(curX, curY)
            )
        }
    }
}
