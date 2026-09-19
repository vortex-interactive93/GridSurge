package com.example.gridsurge.ui

import android.app.Activity
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.example.gridsurge.ads.AdManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.gridsurge.R
import com.example.gridsurge.analytics.GridSurgeAnalytics
import com.example.gridsurge.game.ui.ModalOrchestrator
import com.example.gridsurge.game.ui.ModalType
import com.example.gridsurge.features.adventure.engine.NeuralAugmentDraftManager
import com.example.gridsurge.features.adventure.engine.RelicCyberWareManager
import com.example.gridsurge.features.adventure.model.LevelNodeSpec
import com.example.gridsurge.features.adventure.model.NeuralAugment
import com.example.gridsurge.features.adventure.model.ObjectiveType
import com.example.gridsurge.features.adventure.model.RelicAbilityType
import com.example.gridsurge.features.adventure.model.StarEvaluationResult
import com.example.gridsurge.features.adventure.ui.dialogs.AdventureVictoryDialog
import com.example.gridsurge.features.adventure.ui.AdventureViewModel
import com.example.gridsurge.features.adventure.ui.dialogs.NeuralAugmentDraftDialog
import com.example.gridsurge.armory.data.ArmoryDataStoreRepository
import com.example.gridsurge.audio.BgmManager
import com.example.gridsurge.audio.BgmTrack
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.meta.model.ClaimTriggerReason
import com.example.gridsurge.ui.modal.CallsignClaimModal
import com.example.gridsurge.clash.data.ClashReplayRepository
import com.example.gridsurge.game.clash.network.SupabaseClashRepository
import com.example.gridsurge.game.clash.network.model.LiveMatchmakingPhase
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.Brush
import com.example.gridsurge.ui.clash.components.BlitzClashActiveVisor
import com.example.gridsurge.ui.clash.components.BlitzClashTacticalHud
import com.example.gridsurge.ui.clash.components.ClashEmote
import com.example.gridsurge.ui.clash.components.ClashEmoteController
import com.example.gridsurge.ui.clash.components.OperatorCardData
import com.example.gridsurge.ui.clash.components.OperatorFeatBadge
import com.example.gridsurge.ui.clash.components.FloatingRivalEmoteBadge
import com.example.gridsurge.ui.clash.components.LiveClashMatchmakingTerminal
import com.example.gridsurge.ui.clash.components.OverchargeMomentumBar
import com.example.gridsurge.game.GridSurgeGameView
import com.example.gridsurge.features.adventure.data.AdventureSectorRegistry
import com.example.gridsurge.features.adventure.ui.components.SegmentedRelicEnergyBar
import com.example.gridsurge.game.blitz.model.BlitzScorecardDebrief
import com.example.gridsurge.game.clash.engine.RivalMatchmakingRegistry
import com.example.gridsurge.game.clash.model.ClashMmrSettlement
import com.example.gridsurge.game.clash.model.RivalCombatant
import com.example.gridsurge.game.glitch.model.DailyGlitchDebriefState
import com.example.gridsurge.game.glitch.model.DailySeedMetadata
import com.example.gridsurge.game.render.ClearedLineEvent
import com.example.gridsurge.game.render.VectorLineClearOverlay
import com.example.gridsurge.game.replay.MatchReplayData
import com.example.gridsurge.leaderboard.data.GlitchLeaderboardRepository
import com.example.gridsurge.leaderboard.data.LeaderboardRepository
import com.example.gridsurge.leaderboard.model.GameModeType
import com.example.gridsurge.leaderboard.model.MatchReplayEnvelope
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.meta.data.DailyMissionsRepository
import com.example.gridsurge.meta.quests.QuestType
import com.example.gridsurge.settings.SettingsManager
import com.example.gridsurge.theme.BackgroundThemeManager
import com.example.gridsurge.theme.ThemeNormalizer
import com.example.gridsurge.ui.clash.dialogs.ClashResultDialog
import com.example.gridsurge.ui.components.*
import com.example.gridsurge.ui.dialogs.BlitzClashResultDialog
import com.example.gridsurge.ui.dialogs.BlitzResultDialog
import com.example.gridsurge.ui.dialogs.CyberAvatarRegistry
import com.example.gridsurge.ui.dialogs.InGamePauseDialog
import com.example.gridsurge.ui.glitch.DailyGlitchResultDialog
import com.example.gridsurge.ui.pause.TacticalPauseTerminalDialog
import com.example.gridsurge.ui.pause.model.PauseMissionTelemetry
import com.example.gridsurge.ui.replay.ReplayTheaterScreen
import com.example.gridsurge.ui.settings.SettingsDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

@Composable
fun GameScreen(
    profileManager: PlayerProfileManager,
    armoryRepository: ArmoryDataStoreRepository,
    dailyMissionsRepository: DailyMissionsRepository,
    adventureViewModel: AdventureViewModel,
    gameMode: Screen,
    levelNumber: Int = 1,
    adventureLevel: LevelNodeSpec? = null,
    onNavigateBack: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isNoAdsPurchased by profileManager.isNoAdsPurchased.collectAsState()

    val exitToHub: (Boolean) -> Unit = { completed ->
        if (activity != null) {
            AdManager.showInterstitialAd(activity, isNoAdsPurchased)
        }
        onNavigateBack(completed)
    }

    // Resolve true initial level: prioritize passed LevelNodeSpec over default parameter
    val initialResolvedLevel = remember(levelNumber, adventureLevel) {
        adventureLevel?.levelNumber ?: levelNumber
    }

    var currentStageIndex by remember(initialResolvedLevel) {
        mutableIntStateOf(initialResolvedLevel)
    }

    // Dynamic Single-Source Blueprint & Benchmark Resolution
    val activeBlueprint = remember(currentStageIndex, gameMode) {
        if (gameMode == Screen.GAME_ADVENTURE) {
            AdventureSectorRegistry.getLevelBlueprint(currentStageIndex)
        } else null
    }

    val activeBenchmark = remember(currentStageIndex, gameMode) {
        if (gameMode == Screen.GAME_ADVENTURE) {
            AdventureSectorRegistry.getBenchmark(currentStageIndex)
        } else null
    }

    val relicManager = remember { RelicCyberWareManager() }
    val relicState by relicManager.relicState.collectAsState()
    val draftManager = remember { NeuralAugmentDraftManager() }
    val savedAugmentIds by profileManager.activeSectorAugmentIds.collectAsState()

    LaunchedEffect(savedAugmentIds) {
        draftManager.syncFromSavedAugmentIds(savedAugmentIds)
    }

    val draftOptions by draftManager.draftOptions.collectAsState()
    val activeAugments by draftManager.activeAugments.collectAsState()

    val globalHighScore by profileManager.highScore.collectAsState()
    val highestSectorCleared by profileManager.highestSectorCleared.collectAsState(initial = 0)
    val rawEquippedSkinId by profileManager.equippedBlockSkinId.collectAsState()

    val activeThemeToApply = remember(rawEquippedSkinId) {
        ThemeNormalizer.normalize(rawEquippedSkinId)
    }

    val backgroundRes = remember(gameMode, activeBlueprint, activeThemeToApply) {
        if (activeBlueprint != null) {
            when (activeBlueprint.sectorId) {
                1 -> R.drawable.bg_sector_neon_grid
                2 -> R.drawable.bg_sector_solar_flare
                3 -> R.drawable.bg_sector_crimson_breach
                4 -> R.drawable.bg_sector_toxic_surge
                5 -> R.drawable.bg_sector_quantum_singularity
                else -> R.drawable.bg_sector_neon_grid
            }
        } else {
            val internalMode = when (gameMode) {
                Screen.TIME_BLITZ -> GameModeType.TIME_BLITZ
                Screen.DAILY_GLITCH -> GameModeType.DAILY_GLITCH
                else -> GameModeType.CLASSIC_SURGE
            }
            BackgroundThemeManager.getBackgroundForMode(internalMode, equippedThemeKey = activeThemeToApply)
        }
    }

    val advUiState by adventureViewModel.uiState.collectAsState()
    val equippedRelicName by profileManager.equippedRelicAbilityName.collectAsState()

    val isRelicClaimedForSector = remember(activeBlueprint?.sectorId, advUiState.sectorRecords) {
        val sectorIdx = activeBlueprint?.sectorId ?: 1
        advUiState.sectorRecords[sectorIdx]?.isRelicClaimed ?: false
    }

    val activeRelicAbility = remember<RelicAbilityType>(equippedRelicName, isRelicClaimedForSector, activeBlueprint?.sectorId) {
        val sectorIdx = activeBlueprint?.sectorId ?: 1
        val equipped = try {
            RelicAbilityType.valueOf(equippedRelicName)
        } catch (_: Exception) {
            RelicAbilityType.NONE
        }
        if (equipped != RelicAbilityType.NONE) {
            equipped
        } else if (isRelicClaimedForSector) {
            RelicAbilityType.getRelicForSector(sectorIdx)
        } else {
            RelicAbilityType.NONE
        }
    }

    LaunchedEffect(activeRelicAbility) {
        relicManager.configureRelic(activeRelicAbility)
    }

    LaunchedEffect(gameMode, activeBlueprint) {
        val track = when (gameMode) {
            Screen.GAME_CLASSIC -> BgmTrack.CLASSIC_ENDLESS
            Screen.TIME_BLITZ -> BgmTrack.TIME_BLITZ
            Screen.DAILY_GLITCH -> BgmTrack.DAILY_GLITCH
            Screen.BLITZ_CLASH -> BgmTrack.BLITZ_CLASH
            Screen.GAME_ADVENTURE -> BgmTrack.SECTOR_01_NEON
            else -> BgmTrack.CLASSIC_ENDLESS
        }
        BgmManager.playTrack(context, track)
    }

    val activeModal by ModalOrchestrator.currentModal.collectAsState()

    val scope = rememberCoroutineScope()
    val playerMmr by profileManager.ratingPoints.collectAsState()
    val currentCallsign by profileManager.callsign.collectAsState()
    val starsCount by profileManager.perfectStarsCount.collectAsState()
    val userClashWins by profileManager.clashWins.collectAsState()
    val userTotalRuns by profileManager.totalRuns.collectAsState()

    var showHighScoreCallsignClaim by remember { mutableStateOf(false) }
    var score by remember { mutableLongStateOf(0L) }
    var combo by remember { mutableIntStateOf(1) }
    var linesClearedTotal by remember { mutableIntStateOf(0) }
    var coresDestroyedTotal by remember { mutableIntStateOf(0) }
    var movesRemaining by remember { mutableIntStateOf(0) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var activeProgressCount by remember { mutableIntStateOf(0) }
    var boardOccupancy by remember { mutableFloatStateOf(0f) }
    var glitchPurged by remember { mutableIntStateOf(0) }
    var resonanceEnergy by remember { mutableFloatStateOf(0f) }
    var lastDropNormX by remember { mutableFloatStateOf(0.5f) }
    var lastDropNormY by remember { mutableFloatStateOf(0.5f) }
    var glitchDebriefState by remember { mutableStateOf<DailyGlitchDebriefState?>(null) }
    var activeVectorClears by remember { mutableStateOf<List<ClearedLineEvent>>(emptyList()) }

    var rivalScore by remember { mutableLongStateOf(0L) }
    var maxComboInClash by remember { mutableIntStateOf(0) }
    var linesClearedInClash by remember { mutableIntStateOf(0) }
    var playerFlushesInClash by remember { mutableIntStateOf(0) }
    var playerApmInClash by remember { mutableIntStateOf(68) }
    var rivalLinesInClash by remember { mutableIntStateOf(0) }
    var rivalMaxComboInClash by remember { mutableIntStateOf(0) }
    var rivalApmInClash by remember { mutableIntStateOf(52) }
    var rivalFlushesInClash by remember { mutableIntStateOf(0) }
    var matchReplayData by remember { mutableStateOf<MatchReplayData?>(null) }
    var ratingDelta by remember { mutableIntStateOf(0) }
    var finalTimeSec by remember { mutableIntStateOf(0) }
    var gameViewRef by remember { mutableStateOf<GridSurgeGameView?>(null) }
    var victoryEvaluationResult by remember { mutableStateOf<StarEvaluationResult?>(null) }
    var isWinner by remember { mutableStateOf(false) }

    var isBlitzSearching by remember { mutableStateOf(gameMode == Screen.BLITZ_CLASH) }
    var blitzMatchmakingPhase by remember { mutableStateOf(LiveMatchmakingPhase.SEARCHING_QUEUE) }
    var blitzElapsedSec by remember { mutableIntStateOf(0) }
    var blitzSearchWindowMmr by remember { mutableIntStateOf(60) }
    var blitzMatchedRival by remember { mutableStateOf<RivalCombatant?>(null) }
    var blitzCountdownRemainingSec by remember { mutableIntStateOf(3) }
    var activeLiveRoomId by remember { mutableStateOf<String?>(null) }
    var activeRivalEmote by remember { mutableStateOf<ClashEmote?>(null) }
    var rivalActionTickerText by remember { mutableStateOf<String?>(null) }
    var activePlayerCard by remember { mutableStateOf<OperatorCardData?>(null) }
    var activeRivalCard by remember { mutableStateOf<OperatorCardData?>(null) }
    var clashSearchTrigger by remember { mutableIntStateOf(0) }
    val clashRepo = remember { SupabaseClashRepository() }

    DisposableEffect(Unit) {
        onDispose {
            ModalOrchestrator.clearAll()
            scope.launch(Dispatchers.IO) {
                clashRepo.disconnectRoom()
            }
        }
    }

    LaunchedEffect(clashSearchTrigger) {
        if (gameMode == Screen.BLITZ_CLASH) {
            val localUserId = profileManager.callsign.value.ifBlank { "OPERATOR" } + "_" + Build.MODEL.replace(" ", "_")
            val localCallsign = profileManager.callsign.value.ifBlank { "OPERATOR" }
            val localMmr = profileManager.ratingPoints.value.coerceAtLeast(1000)
            val localAvatarKey = profileManager.avatarKey.value.ifBlank { "avatar_caucasian_male" }
            val localEquippedBadgesStr = profileManager.unlockedBadgeIds.value.take(3).joinToString(",")

            // Make sure previous socket session is disconnected cleanly
            clashRepo.disconnectRoom()

            val matchResult = try {
                withTimeout(14_000L) {
                    clashRepo.findMatch(
                        userId = localUserId,
                        callsign = localCallsign,
                        currentMmr = localMmr,
                        avatarKey = localAvatarKey,
                        equippedBadges = localEquippedBadgesStr,
                        onSearching = { window, elapsed ->
                            blitzSearchWindowMmr = window
                            blitzElapsedSec = elapsed
                        }
                    )
                }
            } catch (e: Exception) {
                Log.w("GameScreen", "Match search ended: ${e.message}")
                null
            }

            if (matchResult != null && matchResult.status == "MATCH_FOUND" && matchResult.room_id != null) {
                activeLiveRoomId = matchResult.room_id
                val seed = matchResult.shared_seed ?: 1337L
                val startEpoch = matchResult.start_epoch_ms ?: (System.currentTimeMillis() + 7000L)

                withContext(Dispatchers.Main) {
                    val rivalMmrVal = matchResult.rival_mmr ?: 1200
                    val rivalTier = RivalMatchmakingRegistry.generateOpponent(rivalMmrVal).tierTitle
                    val rivalAvatar = matchResult.rival_avatar_key ?: "avatar_asian_female"
                    val rivalBadgesList = if (!matchResult.rival_equipped_badges.isNullOrBlank()) {
                        matchResult.rival_equipped_badges.split(",").map { it.trim() }
                    } else {
                        listOf("DECA_SURGE", "GRID_NULLIFIER", "FOUNDER")
                    }

                    blitzMatchedRival = RivalCombatant(
                        callsign = matchResult.rival_callsign ?: "RIVAL_OPERATOR",
                        tierTitle = rivalTier,
                        currentMmr = rivalMmrVal,
                        targetScore = 0L,
                        avgPpm = 35,
                        avatarKey = rivalAvatar,
                        equippedBadgeIds = rivalBadgesList
                    )
                    blitzMatchmakingPhase = LiveMatchmakingPhase.STAGING_COUNTDOWN

                    gameViewRef?.prepareLiveClashDuel(sharedSeed = seed, startEpochMs = startEpoch, totalSeconds = 90)

                    clashRepo.onRemoteCombatReceived = { packet ->
                        rivalScore = packet.score
                        gameViewRef?.onRemoteCombatTelemetryReceived(
                            packet.score, packet.lines, packet.combo, packet.fever, packet.tko
                        )

                        rivalLinesInClash += packet.lines
                        rivalMaxComboInClash = maxOf(rivalMaxComboInClash, packet.combo)
                        if (packet.apm > 0) rivalApmInClash = packet.apm
                        if (packet.flushes > 0) rivalFlushesInClash += packet.flushes

                        val actionTag = when {
                            packet.lines >= 4 -> "[ RIVAL // QUAD NULLIFIER ]"
                            packet.lines >= 2 -> "[ RIVAL // DUAL CLEAR ]"
                            packet.combo >= 3 -> "[ RIVAL // OVERDRIVE x${packet.combo} ]"
                            else -> null
                        }

                        if (actionTag != null) {
                            rivalActionTickerText = actionTag
                            scope.launch {
                                delay(1800L)
                                if (rivalActionTickerText == actionTag) {
                                    rivalActionTickerText = null
                                }
                            }
                        }
                    }
                    clashRepo.onRemoteEmoteReceived = { emoteId ->
                        val found = ClashEmote.entries.find { it.id == emoteId }
                        if (found != null) {
                            activeRivalEmote = found
                            SfxManager.playSfx(SfxType.SNAP_TICK, overridePitch = 1.6f)
                            scope.launch {
                                delay(1800L)
                                if (activeRivalEmote == found) {
                                    activeRivalEmote = null
                                }
                            }
                        }
                    }
                    clashRepo.onRemoteHandshakeReceived = { rivalAvatarKey, rivalBadgesStr ->
                        val rivalBadgesList = if (rivalBadgesStr.isNotBlank()) {
                            rivalBadgesStr.split(",").map { it.trim() }
                        } else {
                            listOf("DECA_SURGE", "GRID_NULLIFIER", "FOUNDER")
                        }

                        blitzMatchedRival = blitzMatchedRival?.copy(
                            avatarKey = rivalAvatarKey,
                            equippedBadgeIds = rivalBadgesList
                        )
                    }

                    clashRepo.onOpponentDropped = {
                        gameViewRef?.let { view ->
                            view.isTouchLocked = true
                            view.isEnginePaused = true
                            SfxManager.playSfx(SfxType.LEVEL_COMPLETE, overridePitch = 1.3f)
                            view.ghostDuelEngine.concludeMatchWithVictory(view.currentScore)
                        }
                    }
                }

                val socketJoinJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    clashRepo.joinMatchRoom(matchResult.room_id, localUserId)
                    clashRepo.broadcastHandshake(localAvatarKey, localEquippedBadgesStr)
                }

                withContext(Dispatchers.Main) {
                    SfxManager.playSfx(SfxType.MODE_LOCK_IN)
                    var lastStep = -1
                    while (System.currentTimeMillis() < startEpoch) {
                        val remainingMs = startEpoch - System.currentTimeMillis()
                        // Map the remaining time to a clean 5.. 4.. 3.. 2.. 1.. progression
                        val step = ((remainingMs + 999) / 1000).toInt().coerceIn(1, 5)
                        if (step != lastStep) {
                            lastStep = step
                            blitzCountdownRemainingSec = step
                            SfxManager.playSfx(SfxType.SNAP_TICK)
                        }
                        delay(30L)
                    }
                }

                withTimeoutOrNull(2500L) { socketJoinJob.join() }

                withContext(Dispatchers.Main) {
                    isBlitzSearching = false
                    blitzMatchmakingPhase = LiveMatchmakingPhase.DUEL_ACTIVE
                    gameViewRef?.launchLiveClashDuel(
                        sharedSeed = seed,
                        startEpochMs = startEpoch,
                        totalSeconds = 90
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    isBlitzSearching = false
                    val ghostReplay = ClashReplayRepository.fetchRandomRivalReplay()
                    gameViewRef?.startBlitzClashDuel(ghostReplay)
                }
            }
        }
    }

    LaunchedEffect(highestSectorCleared, gameViewRef) {
        gameViewRef?.highestSectorCleared = highestSectorCleared
    }

    LaunchedEffect(activeAugments, gameViewRef) {
        gameViewRef?.activeAugments = activeAugments
    }

    val starsBalance by profileManager.starCurrency.collectAsState()
    val hasUsedRevive = gameViewRef?.hasUsedReviveThisRun ?: false

    BackHandler(enabled = activeModal == ModalType.NONE || activeModal == ModalType.PAUSE || activeModal == ModalType.SETTINGS) {
        if (activeModal == ModalType.SETTINGS) {
            ModalOrchestrator.dismissModal(ModalType.SETTINGS)
            ModalOrchestrator.showModal(ModalType.PAUSE)
        } else if (activeModal == ModalType.PAUSE) {
            ModalOrchestrator.dismissModal(ModalType.PAUSE)
            gameViewRef?.resumeEngine()
        } else {
            SfxManager.playSfx(SfxType.MODAL_WHOOSH)
            ModalOrchestrator.showModal(ModalType.PAUSE)
            gameViewRef?.pauseEngine()
        }
    }

    val blurEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && activeModal == ModalType.PAUSE) {
        RenderEffect.createBlurEffect(14f, 14f, Shader.TileMode.CLAMP).asComposeRenderEffect()
    } else null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { renderEffect = blurEffect }
    ) {
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(modifier = Modifier.fillMaxSize().background(Color(0x990A0C14)))

        // The Compose-aware clock ticker for Blitz Clash
        var liveDuelSecondsState by remember { mutableIntStateOf(90) }

        LaunchedEffect(blitzMatchmakingPhase, activeLiveRoomId) {
            if (blitzMatchmakingPhase == LiveMatchmakingPhase.DUEL_ACTIVE) {
                while (isActive) {
                    val actualSeconds = gameViewRef?.currentDuelRemainingSeconds ?: 0
                    if (liveDuelSecondsState != actualSeconds) {
                        liveDuelSecondsState = actualSeconds
                    }
                    delay(250L) // Poll rapidly so the UI clock updates every second
                }
            }
        }

        // Critical PvP Deficit Warning Vignette
        if (gameMode == Screen.BLITZ_CLASH && !isBlitzSearching) {
            val currentRivalScore = gameViewRef?.currentRivalScore ?: 0L
            val isCriticalDeficit = (currentRivalScore - score) >= 2000L || liveDuelSecondsState <= 10
            val infiniteTransition = rememberInfiniteTransition(label = "dangerVignette")
            val dangerAlpha by infiniteTransition.animateFloat(
                initialValue = 0.08f,
                targetValue = if (isCriticalDeficit) 0.35f else 0.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dangerAlpha"
            )

            if (isCriticalDeficit) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color.Transparent, Color(0xFFFF0055).copy(alpha = dangerAlpha)),
                                radius = 1400f
                            )
                        )
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            val currentRivalScore = gameViewRef?.currentRivalScore ?: 0L

            if (gameMode == Screen.BLITZ_CLASH && !isBlitzSearching) {
                val userLevel = (starsCount / 3 + 12).coerceIn(12, 99)
                val winPct = if (userTotalRuns > 0) ((userClashWins.toFloat() / userTotalRuns) * 100).toInt().coerceIn(15, 95) else 62

                val playerAvatarKey by profileManager.avatarKey.collectAsState()
                val localAvatarPreset = CyberAvatarRegistry.getPresetById(playerAvatarKey)
                val localCrestResId = when {
                    playerMmr >= 2000 -> R.drawable.ic_rank_crest_gold
                    playerMmr >= 1500 -> R.drawable.ic_rank_crest_silver
                    else -> R.drawable.ic_rank_crest_bronze
                }

                val unlockedBadgesSet by profileManager.unlockedBadgeIds.collectAsState()
                val localBadges = remember(unlockedBadgesSet) {
                    val list = unlockedBadgesSet.map { OperatorFeatBadge.fromId(it) }.distinct()
                    if (list.isNotEmpty()) list.take(3) else listOf(
                        OperatorFeatBadge.DECA_SURGE,
                        OperatorFeatBadge.GRID_NULLIFIER,
                        OperatorFeatBadge.FOUNDER
                    )
                }

                val playerCard = OperatorCardData(
                    callsign = currentCallsign.ifBlank { "OPERATOR" },
                    level = userLevel,
                    tierTitle = RivalMatchmakingRegistry.generateOpponent(playerMmr).tierTitle,
                    mmr = playerMmr,
                    winLossRatio = "$winPct% W/L",
                    equippedBadges = localBadges,
                    isLocalPlayer = true,
                    avatarResId = localAvatarPreset.iconRes,
                    rankCrestResId = localCrestResId
                )
                activePlayerCard = playerCard

                val rivalMmrVal = blitzMatchedRival?.currentMmr ?: playerMmr
                val rivalCallsignStr = blitzMatchedRival?.callsign ?: "RIVAL"
                val rivalLevel = (rivalMmrVal / 38).coerceIn(14, 99)
                val rivalWinPct = (52 + (rivalMmrVal % 18)).coerceIn(48, 89)

                val rivalBadges = remember(blitzMatchedRival?.equippedBadgeIds) {
                    val ids = blitzMatchedRival?.equippedBadgeIds
                    if (!ids.isNullOrEmpty()) {
                        ids.map { OperatorFeatBadge.fromId(it) }.distinct()
                    } else {
                        listOf(OperatorFeatBadge.DECA_SURGE, OperatorFeatBadge.GRID_NULLIFIER, OperatorFeatBadge.FOUNDER)
                    }
                }

                val rivalAvatarPreset = remember(blitzMatchedRival?.avatarKey) {
                    val key = blitzMatchedRival?.avatarKey
                    if (!key.isNullOrBlank()) {
                        CyberAvatarRegistry.getPresetById(key)
                    } else {
                        val presets = CyberAvatarRegistry.PRESETS
                        val index = Math.abs(rivalCallsignStr.hashCode()) % presets.size
                        presets[index]
                    }
                }
                val rivalCrestResId = when {
                    rivalMmrVal >= 2000 -> R.drawable.ic_rank_crest_gold
                    rivalMmrVal >= 1500 -> R.drawable.ic_rank_crest_silver
                    else -> R.drawable.ic_rank_crest_bronze
                }

                val rivalCard = OperatorCardData(
                    callsign = rivalCallsignStr,
                    level = rivalLevel,
                    tierTitle = blitzMatchedRival?.tierTitle ?: "GOLD I",
                    mmr = rivalMmrVal,
                    winLossRatio = "$rivalWinPct% W/L",
                    equippedBadges = rivalBadges,
                    isLocalPlayer = false,
                    avatarResId = rivalAvatarPreset.iconRes,
                    rankCrestResId = rivalCrestResId
                )
                activeRivalCard = rivalCard

                val displaySecondsRemaining = remember(elapsedSeconds, liveDuelSecondsState) {
                    val duelSec = gameViewRef?.currentDuelRemainingSeconds ?: 0
                    if (duelSec in 1..90) duelSec else (90 - elapsedSeconds).coerceAtLeast(0)
                }

                BlitzClashTacticalHud(
                    playerCard = playerCard,
                    rivalCard = rivalCard,
                    playerScore = score,
                    rivalScore = rivalScore,
                    secondsRemaining = displaySecondsRemaining,
                    boardOccupancy = boardOccupancy,
                    rivalActionTickerText = rivalActionTickerText,
                    modifier = Modifier.statusBarsPadding()
                )
            } else if (gameMode != Screen.BLITZ_CLASH) {
                val headerMode = when (gameMode) {
                    Screen.GAME_CLASSIC -> VisorGameMode.CLASSIC
                    Screen.GAME_ADVENTURE -> VisorGameMode.ADVENTURE
                    Screen.TIME_BLITZ -> VisorGameMode.TIME_BLITZ
                    Screen.DAILY_GLITCH -> VisorGameMode.DAILY_GLITCH
                    else -> VisorGameMode.CLASSIC
                }

                val blitz = gameViewRef?.blitzEngine
                val visorTimeRemaining = if (gameMode == Screen.TIME_BLITZ) (blitz?.secondsRemaining ?: 90f) else 0f
                val stageNum = activeBlueprint?.levelNumber ?: currentStageIndex
                val stageDef = AdventureSectorRegistry.getStageByGlobalIndex(stageNum)

                AdaptiveCyberVisorHeader(
                    gameMode = headerMode,
                    score = score,
                    rivalScore = 0,
                    highScore = globalHighScore.toLong(),
                    linesCleared = linesClearedTotal,
                    elapsedSeconds = elapsedSeconds,
                    timeRemainingSec = visorTimeRemaining,
                    targetScore3Star = stageDef.benchmarks.targetScore3Star.toLong(),
                    timeLimit3Star = activeBlueprint?.objective?.star3TimeSec ?: 75,
                    purityIntegrity = gameViewRef?.glitchEngine?.purity ?: 1.0f,
                    activeCores = activeProgressCount,
                    totalCores = activeBlueprint?.objective?.targetAmount ?: 1,
                    catalystsPurged = glitchPurged,
                    totalCatalysts = 35,
                    comboStreak = combo,
                    feverProgress = blitz?.feverMeter ?: 0f,
                    isFeverActive = blitz?.isFeverActive ?: false,
                    objectiveType = activeBlueprint?.objective?.type ?: ObjectiveType.INFECTED_PURGE,
                    movesRemaining = movesRemaining,
                    resonanceEnergy = resonanceEnergy,
                    isWarpReady = resonanceEnergy >= 100f,
                    boardOccupancy = boardOccupancy,
                    activeAugments = activeAugments,
                    relicState = if (relicState.isUnlocked) relicState else null,
                    onRelicActivate = {
                        val ability = relicManager.triggerActivation()
                        ability?.let { gameViewRef?.executeRelicCyberWareAbility(it) }
                    },
                    onRelicDragStart = { x, y -> gameViewRef?.startRelicDrag(x, y) },
                    onRelicDrag = { x, y -> gameViewRef?.updateRelicDrag(x, y) },
                    onRelicDragEnd = { gameViewRef?.endRelicDrag() },
                    onRelicDragCancel = { gameViewRef?.cancelRelicDrag() },
                    onPauseClick = {
                        SfxManager.playSfx(SfxType.MODAL_WHOOSH)
                        ModalOrchestrator.showModal(ModalType.PAUSE)
                        gameViewRef?.pauseEngine()
                    },
                    isEnabled = activeModal == ModalType.NONE,
                    modifier = Modifier.statusBarsPadding()
                )
            }

            if (gameMode == Screen.GAME_ADVENTURE && relicState.isUnlocked) {
                SegmentedRelicEnergyBar(
                    relicState = relicState,
                    onActivate = {
                        val ability = relicManager.triggerActivation()
                        ability?.let { gameViewRef?.executeRelicCyberWareAbility(it) }
                    },
                    onRelicDragStart = { x, y -> gameViewRef?.startRelicDrag(x, y) },
                    onRelicDrag = { x, y -> gameViewRef?.updateRelicDrag(x, y) },
                    onRelicDragEnd = { gameViewRef?.endRelicDrag() },
                    onRelicDragCancel = { gameViewRef?.cancelRelicDrag() },
                    isEnabled = activeModal == ModalType.NONE,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                )
            }

            if (gameMode == Screen.GAME_ADVENTURE && activeAugments.isNotEmpty()) {
                ActiveAugmentsLoadoutRow(
                    activeAugments = activeAugments,
                    modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                )
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val scope = rememberCoroutineScope()

                AndroidView(
                    factory = { ctx ->
                        GridSurgeGameView(ctx).apply {
                            activeThemeKey = activeThemeToApply
                            onMissionEvent = { type, delta ->
                                scope.launch(Dispatchers.IO) {
                                    val missionId = when (type) {
                                        QuestType.COMBO -> "q1"
                                        QuestType.LINES -> "q2"
                                        QuestType.SURGE_CORE -> "q3"
                                        QuestType.TIME_BLITZ -> "q4"
                                        QuestType.BLITZ_CLASH -> "q5"
                                    }
                                    if (type == QuestType.COMBO || type == QuestType.TIME_BLITZ) {
                                        dailyMissionsRepository.updateProgress(missionId, delta)
                                    } else {
                                        dailyMissionsRepository.incrementProgress(missionId, delta)
                                    }
                                }
                            }
                            onDirectiveEvent = { directiveId, delta ->
                                scope.launch(Dispatchers.IO) {
                                    dailyMissionsRepository.incrementProgress(directiveId, delta)
                                }
                            }
                            onVectorLineClear = { events ->
                                scope.launch {
                                    for (frame in 0..10) {
                                        val p = frame / 10f
                                        activeVectorClears = events.map { it.copy(progress = p) }
                                        delay(15L)
                                    }
                                    activeVectorClears = emptyList()
                                }
                            }
                            onAdventureStateUpdated = { remMoves, curScore, lines, nodes, _, _, elapsed, _, gPurged, synth, streak, resonance, occupancy, dropX, dropY ->
                                movesRemaining = remMoves
                                score = curScore
                                linesClearedTotal = lines
                                coresDestroyedTotal = nodes
                                elapsedSeconds = elapsed
                                glitchPurged = gPurged
                                resonanceEnergy = resonance
                                boardOccupancy = occupancy
                                lastDropNormX = dropX
                                lastDropNormY = dropY

                                // Resolve current blueprint from the view's actual active state to avoid closure capture issues
                                val activeBp = gameViewRef?.activeBlueprint ?: AdventureSectorRegistry.getLevelBlueprint(gameViewRef?.currentAdventureLevelNumber ?: currentStageIndex)
                                activeProgressCount = when (activeBp.objective.type) {
                                    ObjectiveType.LINE_CLEANSE -> lines
                                    ObjectiveType.CHROMA_SYNTHESIS -> synth
                                    ObjectiveType.SURGE_STREAK_TARGET -> streak
                                    ObjectiveType.INFECTED_PURGE -> nodes
                                    else -> nodes
                                }
                            }
                            onScoreChanged = { newScore, newCombo ->
                                score = newScore
                                combo = newCombo
                                if (newCombo >= 10) {
                                    profileManager.unlockBadge(OperatorFeatBadge.DECA_SURGE.badgeId)
                                }
                            }
                            onLinesCleared = { clearedCount ->
                                linesClearedTotal += clearedCount
                                profileManager.recordLinesCleared(clearedCount)
                                if (clearedCount >= 4) {
                                    profileManager.unlockBadge(OperatorFeatBadge.GRID_NULLIFIER.badgeId)
                                }
                                if (clearedCount >= 2 && combo >= 3) {
                                    profileManager.unlockBadge(OperatorFeatBadge.KINETIC_BREACH.badgeId)
                                }
                                relicManager.onLinesCleared(clearedCount, combo, boardOccupancy)
                            }
                            onRelicConsumed = {
                                relicManager.resetEnergy()
                            }
                            onGameOver = {
                                if (gameMode == Screen.DAILY_GLITCH) {
                                    val dailySeed = DailySeedMetadata.currentUtc()
                                    val finalPurged = gameViewRef?.glitchEngine?.totalPurgedCount ?: glitchPurged
                                    val stars = (score / 1500).toInt().coerceIn(10, 100)
                                    profileManager.addStarCurrency(stars)

                                    profileManager.recordGlitchResult(score, finalPurged)
                                    profileManager.recordGlitchSeedCompleted()

                                    scope.launch {
                                        val matchEnvelope = MatchReplayEnvelope(
                                            matchId = "GLITCH_${System.currentTimeMillis()}",
                                            userId = profileManager.callsign.value,
                                            callsign = profileManager.callsign.value,
                                            mode = GameModeType.DAILY_GLITCH.storageKey,
                                            seed = System.currentTimeMillis(),
                                            claimedScore = score,
                                            totalLinesCleared = finalPurged,
                                            maxComboReached = combo,
                                            durationMs = elapsedSeconds * 1000L,
                                            moves = emptyList(),
                                            clientSignature = ""
                                        )
                                        LeaderboardRepository().submitVerifiedMatch(matchEnvelope)

                                        val result = GlitchLeaderboardRepository.submitDailyScore(
                                            context = context,
                                            seed = dailySeed,
                                            callsign = profileManager.callsign.value,
                                            score = score,
                                            catalystsPurged = finalPurged,
                                            finalPurity = 1.0f
                                        )

                                        withContext(Dispatchers.Main) {
                                            val assignedRank = result.getOrNull()
                                            val bracketTier = when {
                                                assignedRank != null && assignedRank <= 5 -> "TOP 1% // GRANDMASTER"
                                                assignedRank != null && assignedRank <= 25 -> "TOP 5% // MASTER"
                                                assignedRank != null && assignedRank <= 100 -> "TOP 20% // DIAMOND"
                                                else -> "PARTICIPANT"
                                            }

                                            glitchDebriefState = DailyGlitchDebriefState(
                                                dateKey = dailySeed.dateKey,
                                                finalScore = score,
                                                catalystsPurged = finalPurged,
                                                wavesCompleted = (finalPurged / 5) + 1,
                                                globalRank = assignedRank,
                                                percentileTier = bracketTier,
                                                starsAwarded = stars
                                            )
                                        }
                                    }
                                } else if (activeBlueprint == null) {
                                    SfxManager.playSfx(SfxType.SYSTEM_OFFLINE)
                                    ModalOrchestrator.showModal(ModalType.GAME_OVER)
                                    val stars = when (gameMode) {
                                        Screen.TIME_BLITZ -> (score / 1000).toInt().coerceIn(10, 60)
                                        Screen.DAILY_GLITCH -> (score / 2000).toInt().coerceIn(5, 30)
                                        else -> (score / 1500).toInt().coerceIn(5, 50)
                                    }
                                    val isNewHighScore = profileManager.recordGameResult(score.toInt(), combo, stars)
                                    if (isNewHighScore) {
                                        showHighScoreCallsignClaim = true
                                    }

                                    val storageKey = when (gameMode) {
                                        Screen.TIME_BLITZ -> GameModeType.TIME_BLITZ.storageKey
                                        Screen.DAILY_GLITCH -> GameModeType.DAILY_GLITCH.storageKey
                                        else -> GameModeType.CLASSIC_SURGE.storageKey
                                    }
                                    val matchEnvelope = MatchReplayEnvelope(
                                        matchId = "MATCH_${System.currentTimeMillis()}",
                                        userId = profileManager.callsign.value,
                                        callsign = profileManager.callsign.value,
                                        mode = storageKey,
                                        seed = System.currentTimeMillis(),
                                        claimedScore = score,
                                        totalLinesCleared = linesClearedTotal,
                                        maxComboReached = combo,
                                        durationMs = elapsedSeconds * 1000L,
                                        moves = emptyList(),
                                        clientSignature = ""
                                    )
                                    scope.launch(Dispatchers.IO) {
                                        LeaderboardRepository().submitVerifiedMatch(matchEnvelope)
                                    }

                                    GridSurgeAnalytics.logMatchCompleted(gameMode.name, score, "LOSS")
                                }
                            }
                            onStageVictoryEvaluated = { levelNum, finalScore, evalResult, time ->
                                if (gameMode == Screen.GAME_ADVENTURE) {
                                    score = finalScore
                                    finalTimeSec = time
                                    victoryEvaluationResult = evalResult

                                    adventureViewModel.onLevelCompleted(levelNum, finalScore, evalResult.totalStars, time.toLong())
                                    val stageInSec = ((levelNum - 1) % 9) + 1
                                    if (stageInSec == 3 || stageInSec == 6) {
                                        draftManager.rollAugmentDraft(sectorId = activeBlueprint?.sectorId ?: 1)
                                    }
                                    ModalOrchestrator.showModal(ModalType.VICTORY)

                                    if (evalResult.totalStars >= 3) {
                                        profileManager.recordPerfectStar()
                                    }
                                    profileManager.recordRelicWin()
                                    if (levelNum % 9 == 0) {
                                        profileManager.recordSectorCleared(activeBlueprint?.sectorId ?: 1)
                                        profileManager.clearActiveSectorAugments()
                                        draftManager.resetRun()
                                    }
                                    GridSurgeAnalytics.logMatchCompleted(gameMode.name, finalScore, "VICTORY")
                                }
                            }
                            onClashFinished = { winner, pScore, rScore, stars, mCombo, lines, reboots, replay ->
                                isWinner = winner
                                score = pScore
                                rivalScore = rScore
                                maxComboInClash = mCombo
                                linesClearedInClash = lines
                                playerFlushesInClash = reboots
                                matchReplayData = replay
                                if (winner) {
                                    profileManager.recordClashWin()
                                    val totalWins = profileManager.clashWins.value
                                    if (totalWins >= 10) {
                                        profileManager.unlockBadge(OperatorFeatBadge.APEX_PREDATOR.badgeId)
                                    }
                                    if (liveDuelSecondsState <= 5) {
                                        profileManager.unlockBadge(OperatorFeatBadge.CLUTCH_HERO.badgeId)
                                    }
                                    if (rScore == 0L) {
                                        profileManager.unlockBadge(OperatorFeatBadge.FLAWLESS_VICTORY.badgeId)
                                    }
                                    if (boardOccupancy >= 0.85f) {
                                        profileManager.unlockBadge(OperatorFeatBadge.REDLINE_SURVIVOR.badgeId)
                                    }
                                }

                                val roomId = activeLiveRoomId
                                if (roomId != null) {
                                    val winnerId = if (winner) profileManager.callsign.value else (blitzMatchedRival?.callsign ?: "RIVAL")
                                    scope.launch(Dispatchers.IO) {
                                        clashRepo.concludeMatch(roomId, winnerId, pScore, rScore)
                                        clashRepo.disconnectRoom()
                                    }
                                    activeLiveRoomId = null
                                }

                                val delta = if (winner) 30 else -15
                                ratingDelta = delta
                                ModalOrchestrator.showModal(ModalType.CLASH_RESULT)
                                profileManager.addStarCurrency(stars)
                                profileManager.updateRatingPoints(delta)

                                val matchEnvelope = MatchReplayEnvelope(
                                    matchId = "CLASH_${System.currentTimeMillis()}",
                                    userId = profileManager.callsign.value,
                                    callsign = profileManager.callsign.value,
                                    mode = GameModeType.BLITZ_CLASH.storageKey,
                                    seed = System.currentTimeMillis(),
                                    claimedScore = pScore,
                                    totalLinesCleared = lines,
                                    maxComboReached = mCombo,
                                    durationMs = 75000L,
                                    moves = emptyList(),
                                    clientSignature = ""
                                )

                                scope.launch(Dispatchers.IO) {
                                    LeaderboardRepository().submitVerifiedMatch(matchEnvelope)
                                    ClashReplayRepository.uploadReplay(profileManager.activeTitle.value, replay)
                                }
                            }
                            onStageDefeat = { ModalOrchestrator.showModal(ModalType.GAME_OVER) }
                            onBroadcastLiveMove = { pScore, pLines, pCombo, pFever, pTko ->
                                val calcApm = if (elapsedSeconds > 0) ((linesClearedTotal * 60) / elapsedSeconds).coerceIn(15, 180) else 68
                                playerApmInClash = calcApm
                                clashRepo.broadcastMove(pScore, pLines, pCombo, pFever, pTko, apm = calcApm, flushes = playerFlushesInClash)
                            }
                            gameViewRef = this

                            when (gameMode) {
                                Screen.GAME_CLASSIC -> startClassicMatch()
                                Screen.TIME_BLITZ -> startTimeBlitzMatch()
                                Screen.DAILY_GLITCH -> startGlitchMode()
                                Screen.BLITZ_CLASH -> {
                                    // Handled reactively by LaunchedEffect(clashSearchTrigger)
                                }
                                Screen.GAME_ADVENTURE -> {
                                    if (adventureLevel != null) {
                                        startAdventureMatch(adventureLevel)
                                    } else {
                                        val bp = AdventureSectorRegistry.getLevelBlueprint(currentStageIndex)
                                        startAdventureLevel(bp)
                                    }
                                }
                                else -> startClassicMatch()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { view ->
                        view.highestSectorCleared = highestSectorCleared
                        view.relicManager = relicManager
                        view.activeAugments = activeAugments
                        view.onBroadcastLiveMove = { pScore, pLines, pCombo, pFever, pTko ->
                            val calcApm = if (elapsedSeconds > 0) ((linesClearedTotal * 60) / elapsedSeconds).coerceIn(15, 180) else 68
                            playerApmInClash = calcApm
                            clashRepo.broadcastMove(pScore, pLines, pCombo, pFever, pTko, apm = calcApm, flushes = playerFlushesInClash)
                        }
                        if (gameMode == Screen.GAME_ADVENTURE && view.currentAdventureLevelNumber != currentStageIndex) {
                            if (adventureLevel != null && adventureLevel.levelNumber == currentStageIndex) {
                                view.startAdventureMatch(adventureLevel)
                            } else {
                                val bp = AdventureSectorRegistry.getLevelBlueprint(currentStageIndex)
                                view.startAdventureLevel(bp)
                            }
                        }
                        if (view.activeThemeKey != activeThemeToApply) {
                            view.activeThemeKey = activeThemeToApply
                            view.setTheme(activeThemeToApply)
                            view.invalidate()
                        }
                    }
                )

                VectorLineClearOverlay(
                    activeClears = activeVectorClears,
                    modifier = Modifier.fillMaxSize()
                )

                ComboBadgeOverlay(
                    comboStreak = combo,
                    sectorId = if (gameMode == Screen.GAME_ADVENTURE) (activeBlueprint?.sectorId ?: 1) else 1,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp)
                )

                // Floating Rival Emote Badge Overlay (Zero layout measurement impact on grid)
                if (gameMode == Screen.BLITZ_CLASH && !isBlitzSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 10.dp, end = 16.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        FloatingRivalEmoteBadge(activeEmote = activeRivalEmote)
                    }

                    ClashEmoteController(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 150.dp),
                        onSendEmote = { emote ->
                            clashRepo.broadcastEmote(emote.id)
                        }
                    )
                }
            }
        }

        // Modal Dialogs
        if (activeModal == ModalType.PAUSE) {
            val timerMm = (elapsedSeconds / 60).toString().padStart(2, '0')
            val timerSs = (elapsedSeconds % 60).toString().padStart(2, '0')
            val timeStr = "$timerMm:$timerSs"

            val pauseTelemetry = when (gameMode) {
                Screen.DAILY_GLITCH -> {
                    val seedMeta = DailySeedMetadata.currentUtc()
                    val purityPct = ((gameViewRef?.glitchEngine?.purity ?: 1.0f) * 100).toInt().coerceIn(0, 100)
                    PauseMissionTelemetry(
                        gameModeTitle = "DAILY GLITCH // CONTAINMENT",
                        sectorOrSeedTag = "SEED // ${seedMeta.dateKey}",
                        directive = "Purge 35 Anomaly Catalysts before System Purity reaches 0%",
                        currentScore = score,
                        timeElapsedFormatted = timeStr,
                        primaryMetricLabel = "SYSTEM PURITY",
                        primaryMetricValue = "$purityPct%",
                        primaryMetricColor = if (purityPct > 30) Color(0xFF00FF66) else Color(0xFFFF0055),
                        secondaryMetricLabel = "CATALYSTS PURGED",
                        secondaryMetricValue = "$glitchPurged / 35",
                        isRestartAvailable = false
                    )
                }
                Screen.GAME_ADVENTURE -> {
                    val stageNum = activeBlueprint?.levelNumber ?: currentStageIndex
                    PauseMissionTelemetry(
                        gameModeTitle = "SECTOR 0${activeBlueprint?.sectorId ?: 1} // STAGE 0$stageNum",
                        sectorOrSeedTag = activeBlueprint?.stageName ?: "SECTOR OPERATION",
                        directive = activeBlueprint?.directive ?: "Purge sector cores to secure the matrix.",
                        currentScore = score,
                        timeElapsedFormatted = timeStr,
                        primaryMetricLabel = "MOVES LEFT",
                        primaryMetricValue = "$movesRemaining",
                        primaryMetricColor = Color(0xFF00E5FF),
                        secondaryMetricLabel = "OBJECTIVE",
                        secondaryMetricValue = "$activeProgressCount / ${activeBlueprint?.objective?.targetAmount ?: 1}",
                        isRestartAvailable = true
                    )
                }
                Screen.BLITZ_CLASH -> {
                    val mm = (elapsedSeconds / 60).toString().padStart(2, '0')
                    val ss = (elapsedSeconds % 60).toString().padStart(2, '0')
                    PauseMissionTelemetry(
                        gameModeTitle = "BLITZ CLASH // LIVE DUEL",
                        sectorOrSeedTag = "ROOM // ${activeLiveRoomId?.take(8) ?: "ARENA"}",
                        directive = "Out-score your rival operator before the clock expires.",
                        currentScore = score,
                        timeElapsedFormatted = "$mm:$ss",
                        primaryMetricLabel = "YOUR SCORE",
                        primaryMetricValue = "$score",
                        primaryMetricColor = Color(0xFF00E5FF),
                        secondaryMetricLabel = "RIVAL SCORE",
                        secondaryMetricValue = "$rivalScore",
                        isRestartAvailable = false
                    )
                }
                else -> {
                    PauseMissionTelemetry(
                        gameModeTitle = if (gameMode == Screen.TIME_BLITZ) "TIME BLITZ" else "CLASSIC SURGE",
                        sectorOrSeedTag = "STANDARD MATRIX",
                        directive = "Clear lines, maintain high combos, and maximize total score.",
                        currentScore = score,
                        timeElapsedFormatted = timeStr,
                        primaryMetricLabel = "LINES CLEARED",
                        primaryMetricValue = "$linesClearedTotal",
                        primaryMetricColor = Color(0xFF00FF66),
                        secondaryMetricLabel = "SURGE COMBO",
                        secondaryMetricValue = "${combo}x",
                        isRestartAvailable = true
                    )
                }
            }

            val isClash = gameMode == Screen.BLITZ_CLASH
            TacticalPauseTerminalDialog(
                telemetry = pauseTelemetry,
                isRestartAvailable = !isClash,
                abortActionLabel = if (isClash) "FORFEIT MATCH" else "ABORT RUN",
                onResume = {
                    ModalOrchestrator.dismissModal(ModalType.PAUSE)
                    gameViewRef?.resumeEngine()
                },
                onOpenSettings = {
                    ModalOrchestrator.dismissModal(ModalType.PAUSE)
                    ModalOrchestrator.showModal(ModalType.SETTINGS)
                },
                onRestartMatch = {
                    ModalOrchestrator.dismissModal(ModalType.PAUSE)
                    score = 0L
                    combo = 1
                    linesClearedTotal = 0
                    coresDestroyedTotal = 0
                    activeProgressCount = 0
                    relicManager.resetEnergy()
                    if (gameMode == Screen.GAME_ADVENTURE) {
                        profileManager.clearActiveSectorAugments()
                        val firstStageInSector = (((currentStageIndex - 1) / 9) * 9) + 1
                        currentStageIndex = firstStageInSector
                    }
                    gameViewRef?.quickRestartMatch()
                },
                onAbortToHub = {
                    ModalOrchestrator.dismissModal(ModalType.PAUSE)
                    if (isClash) {
                        clashRepo.broadcastMove(score, 0, 0, false, isTko = true)
                        activeLiveRoomId?.let { roomId ->
                            scope.launch(Dispatchers.IO) {
                                clashRepo.concludeMatch(
                                    roomId = roomId,
                                    winnerId = blitzMatchedRival?.callsign ?: "RIVAL",
                                    pScore = score,
                                    rScore = rivalScore
                                )
                                clashRepo.disconnectRoom()
                            }
                        }
                    }
                    exitToHub(false)
                }
            )
        }

        if (activeModal == ModalType.SETTINGS) {
            SettingsDialog(
                settingsManager = SettingsManager.getInstance(context),
                onDismiss = {
                    ModalOrchestrator.dismissModal(ModalType.SETTINGS)
                    ModalOrchestrator.showModal(ModalType.PAUSE)
                }
            )
        }

        if (activeModal == ModalType.GAME_OVER) {
            if (gameMode == Screen.TIME_BLITZ) {
                val blitzState = gameViewRef?.blitzController?.blitzEngine?.state
                val ppm = blitzState?.piecesPerMinute ?: 0
                val maxCombo = blitzState?.maxComboStreak ?: combo
                val feverUptimePct = blitzState?.feverUptimePercent ?: 0
                val refundedSec = blitzState?.totalTimeRefundedSec ?: 0
                val starReward = (score / 1000L).toInt().coerceIn(10, 150)

                val debrief = BlitzScorecardDebrief(
                    finalScore = score,
                    linesCleared = linesClearedTotal,
                    maxCombo = maxCombo,
                    piecesPerMinute = ppm,
                    feverUptimePercent = feverUptimePct,
                    chronoRefundTotalSec = refundedSec,
                    starReward = starReward
                )

                BlitzResultDialog(
                    debrief = debrief,
                    onPlayAgain = {
                        ModalOrchestrator.dismissModal(ModalType.GAME_OVER)
                        score = 0L
                        combo = 1
                        linesClearedTotal = 0
                        coresDestroyedTotal = 0
                        activeProgressCount = 0
                        relicManager.resetEnergy()
                        gameViewRef?.startTimeBlitzMatch()
                    },
                    onReturnToHub = {
                        ModalOrchestrator.dismissModal(ModalType.GAME_OVER)
                        onNavigateBack(false)
                    }
                )
            } else if (gameMode == Screen.BLITZ_CLASH) {
                val rivalScore = gameViewRef?.currentRivalScore ?: 35000L
                val isWin = score >= rivalScore
                val mmrDelta = if (isWin) 32 else -18
                val startMmr = 1840
                val finalMmr = (startMmr + mmrDelta).coerceAtLeast(1000)

                val rival = RivalMatchmakingRegistry.generateOpponent(startMmr).copy(targetScore = rivalScore)

                val settlement = ClashMmrSettlement(
                    isVictory = isWin,
                    startMmr = startMmr,
                    mmrDelta = mmrDelta,
                    finalMmr = finalMmr,
                    prevTierMmr = rival.prevTierMmr,
                    nextTierMmr = rival.nextTierMmr,
                    currentTierName = rival.tierTitle,
                    nextTierName = "RANK PROMOTION",
                    finalPlayerScore = score,
                    finalRivalScore = rivalScore,
                    starsBounty = if (isWin) 25 else 5,
                    rival = rival
                )

                ClashResultDialog(
                    settlement = settlement,
                    onRematch = {
                        ModalOrchestrator.dismissModal(ModalType.GAME_OVER)
                        score = 0L
                        combo = 1
                        linesClearedTotal = 0
                        gameViewRef?.startBlitzClashDuel()
                    },
                    onReturnToHub = {
                        ModalOrchestrator.dismissModal(ModalType.GAME_OVER)
                        onNavigateBack(false)
                    }
                )
            } else {
                val failureSubtitle = when {
                    gameViewRef?.isAdventureModeActive == true -> {
                        val obj = activeBlueprint?.objective
                        when (obj?.type) {
                            ObjectiveType.SURGE_STREAK_TARGET -> {
                                val currentMaxStreak = gameViewRef?.adventureBoard?.maxStreakReached ?: 0
                                "STREAK FAILED: $currentMaxStreak / ${obj.targetAmount} COMBO"
                            }
                            ObjectiveType.CHROMA_SYNTHESIS -> {
                                val synth = gameViewRef?.adventureBoard?.synthesisCount ?: 0
                                "CIRCUITS SYNTHESIZED: $synth / ${obj.targetAmount}"
                            }
                            ObjectiveType.LINE_CLEANSE -> {
                                val lines = gameViewRef?.adventureBoard?.linesClearedThisStage ?: 0
                                "LINES CLEARED: $lines / ${obj.targetAmount}"
                            }
                            else -> {
                                val purged = coresDestroyedTotal
                                val target = obj?.targetAmount ?: 1
                                val pct = if (target > 0) ((purged.toFloat() / target.toFloat()) * 100f).toInt() else 0
                                "CORES PURGED: $purged / $target ($pct%)"
                            }
                        }
                    }
                    else -> "MATRIX SATURATION // LINES CLEARED: $linesClearedTotal"
                }

                MatrixFailureReviveDialog(
                    finalScore = score,
                    canRevive = !hasUsedRevive,
                    starBalance = starsBalance,
                    objectiveType = activeBlueprint?.objective?.type ?: ObjectiveType.INFECTED_PURGE,
                    adventureCoreProgress = if (activeBlueprint != null) Pair(coresDestroyedTotal, activeBlueprint.objective.targetAmount) else null,
                    failureSubtitle = failureSubtitle,
                    isGlitchMode = (gameMode == Screen.DAILY_GLITCH),
                    onWatchAdToRevive = {
                        val currentActivity = context as? Activity ?: return@MatrixFailureReviveDialog
                        gameViewRef?.isTouchLocked = true
                        gameViewRef?.isEnginePaused = true
                        BgmManager.pause()

                        AdManager.showRewardedAd(
                            activity = currentActivity,
                            isNoAdsPurchased = profileManager.isNoAdsPurchased.value,
                            onRewardEarned = {
                                BgmManager.resume()
                                gameViewRef?.isTouchLocked = false
                                gameViewRef?.isEnginePaused = false
                                gameViewRef?.deployEmpSurgeRevive()
                                relicManager.resetEnergy()
                                ModalOrchestrator.dismissModal(ModalType.GAME_OVER)
                            },
                            onAdNotReady = {
                                BgmManager.resume()
                                gameViewRef?.isTouchLocked = false
                                gameViewRef?.isEnginePaused = false
                                Toast.makeText(context, "Ad loading... Please tap again in a moment.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onDeployEmp = {
                        if (starsBalance >= 50) {
                            profileManager.addStarCurrency(-50)
                            gameViewRef?.deployEmpSurgeRevive()
                            relicManager.resetEnergy()
                            ModalOrchestrator.dismissModal(ModalType.GAME_OVER)
                        }
                    },
                    onReboot = {
                        ModalOrchestrator.dismissModal(ModalType.GAME_OVER)
                        score = 0L
                        combo = 1
                        linesClearedTotal = 0
                        coresDestroyedTotal = 0
                        activeProgressCount = 0
                        relicManager.resetEnergy()
                        gameViewRef?.quickRestartMatch()
                    },
                    onAbort = { exitToHub(false) }
                )
            }
        }

        if (activeModal == ModalType.AUGMENT_DRAFT && draftOptions.isNotEmpty()) {
            NeuralAugmentDraftDialog(
                options = draftOptions,
                onAugmentSelected = { chosenAugment: NeuralAugment ->
                    draftManager.selectAugment(chosenAugment)
                    profileManager.addActiveSectorAugment(chosenAugment.id)
                    gameViewRef?.runState?.installAugment(chosenAugment)
                    ModalOrchestrator.dismissModal(ModalType.AUGMENT_DRAFT)

                    currentStageIndex++
                    score = 0L
                    combo = 1
                    linesClearedTotal = 0
                    coresDestroyedTotal = 0
                    activeProgressCount = 0
                    elapsedSeconds = 0
                    relicManager.resetEnergy()

                    gameViewRef?.advanceToNextAdventureLevel()
                }
            )
        }

        if (activeModal == ModalType.VICTORY && victoryEvaluationResult != null) {
            AdventureVictoryDialog(
                levelNumber = currentStageIndex,
                score = score,
                evaluationResult = victoryEvaluationResult!!,
                finalTimeSeconds = finalTimeSec,
                isSectorBoss = (currentStageIndex % 9 == 0),
                onNextLevel = {
                    val wasBoss = (currentStageIndex % 9 == 0)
                    val stageInSec = ((currentStageIndex - 1) % 9) + 1
                    val isDraftStage = (stageInSec == 3 || stageInSec == 6)

                    ModalOrchestrator.dismissModal(ModalType.VICTORY)

                    if (wasBoss) {
                        onNavigateBack(true)
                    } else if (isDraftStage && draftOptions.isNotEmpty()) {
                        ModalOrchestrator.showModal(ModalType.AUGMENT_DRAFT)
                    } else {
                        currentStageIndex++
                        score = 0L
                        combo = 1
                        linesClearedTotal = 0
                        coresDestroyedTotal = 0
                        activeProgressCount = 0
                        elapsedSeconds = 0
                        relicManager.resetEnergy()

                        gameViewRef?.advanceToNextAdventureLevel()
                    }
                },
                onReplay = {
                    ModalOrchestrator.dismissModal(ModalType.VICTORY)
                    score = 0L
                    combo = 1
                    linesClearedTotal = 0
                    coresDestroyedTotal = 0
                    activeProgressCount = 0
                    relicManager.resetEnergy()
                    gameViewRef?.quickRestartMatch()
                },
                onReturnToMap = {
                    val stageInSec = ((currentStageIndex - 1) % 9) + 1
                    val isDraftStage = (stageInSec == 3 || stageInSec == 6)
                    ModalOrchestrator.dismissModal(ModalType.VICTORY)
                    if (isDraftStage && draftOptions.isNotEmpty()) {
                        ModalOrchestrator.showModal(ModalType.AUGMENT_DRAFT)
                    } else {
                        onNavigateBack(false)
                    }
                }
            )
        }

        if (activeModal == ModalType.CLASH_RESULT) {
            BlitzClashResultDialog(
                isWinner = isWinner,
                playerScore = score,
                rivalScore = rivalScore,
                starsEarned = if (isWinner) 50 else 15,
                ratingDelta = ratingDelta,
                maxCombo = maxComboInClash,
                linesCleared = linesClearedInClash,
                playerFlushes = playerFlushesInClash,
                playerApm = playerApmInClash,
                rivalApm = rivalApmInClash,
                rivalFlushes = rivalFlushesInClash,
                playerCard = activePlayerCard,
                rivalCard = activeRivalCard,
                replayData = matchReplayData,
                onWatchReplay = {
                    ModalOrchestrator.dismissModal(ModalType.CLASH_RESULT)
                    ModalOrchestrator.showModal(ModalType.REPLAY_THEATER)
                },
                onRematch = {
                    ModalOrchestrator.dismissModal(ModalType.CLASH_RESULT)
                    // Reset game metrics & clash telemetry
                    score = 0L
                    rivalScore = 0L
                    combo = 1
                    linesClearedTotal = 0
                    maxComboInClash = 0
                    linesClearedInClash = 0
                    playerFlushesInClash = 0
                    playerApmInClash = 68
                    rivalLinesInClash = 0
                    rivalMaxComboInClash = 0
                    rivalApmInClash = 52
                    rivalFlushesInClash = 0
                    activeLiveRoomId = null
                    blitzMatchedRival = null

                    // Reset search telemetry
                    blitzElapsedSec = 0
                    blitzSearchWindowMmr = 60
                    blitzMatchmakingPhase = LiveMatchmakingPhase.SEARCHING_QUEUE
                    isBlitzSearching = true

                    // Re-trigger the matchmaking coroutine
                    clashSearchTrigger++
                },
                onExit = {
                    ModalOrchestrator.clearAll()
                    onNavigateBack(false)
                }
            )
        }

        if (activeModal == ModalType.REPLAY_THEATER && matchReplayData != null) {
            ReplayTheaterScreen(
                replayData = matchReplayData!!,
                onClose = {
                    ModalOrchestrator.dismissModal(ModalType.REPLAY_THEATER)
                    ModalOrchestrator.showModal(ModalType.CLASH_RESULT)
                }
            )
        }

        if (showHighScoreCallsignClaim) {
            CallsignClaimModal(
                suggestedCallsign = currentCallsign,
                triggerReason = ClaimTriggerReason.FIRST_HIGH_SCORE,
                onCallsignConfirmed = { newCallsign ->
                    profileManager.saveCyberProfile(
                        callsignToSave = newCallsign,
                        avatarKeyToSave = profileManager.avatarKey.value,
                        titleToSave = profileManager.activeTitle.value
                    )
                    showHighScoreCallsignClaim = false
                },
                onDismissRequest = {
                    showHighScoreCallsignClaim = false
                }
            )
        }

        glitchDebriefState?.let { debrief ->
            DailyGlitchResultDialog(
                debrief = debrief,
                onViewLeaderboard = {
                    glitchDebriefState = null
                    onNavigateBack(false)
                },
                onReturnToHub = {
                    glitchDebriefState = null
                    onNavigateBack(false)
                }
            )
        }

        if (gameMode == Screen.BLITZ_CLASH && isBlitzSearching) {
            val userLevel = (starsCount / 3 + 12).coerceIn(12, 99)
            val winPct = if (userTotalRuns > 0) ((userClashWins.toFloat() / userTotalRuns) * 100).toInt().coerceIn(15, 95) else 62

            val playerAvatarKey by profileManager.avatarKey.collectAsState()
            val localAvatarPreset = CyberAvatarRegistry.getPresetById(playerAvatarKey)

            val unlockedBadgesSet by profileManager.unlockedBadgeIds.collectAsState()
            val localBadges = remember(unlockedBadgesSet) {
                val list = unlockedBadgesSet.map { OperatorFeatBadge.fromId(it) }.distinct()
                if (list.isNotEmpty()) list.take(3) else listOf(
                    OperatorFeatBadge.DECA_SURGE,
                    OperatorFeatBadge.GRID_NULLIFIER,
                    OperatorFeatBadge.FOUNDER
                )
            }

            LiveClashMatchmakingTerminal(
                phase = blitzMatchmakingPhase,
                elapsedSeconds = blitzElapsedSec,
                searchMmrWindow = blitzSearchWindowMmr,
                playerMmr = playerMmr,
                matchedRival = blitzMatchedRival,
                countdownRemainingSec = blitzCountdownRemainingSec,
                localCallsign = currentCallsign,
                playerLevel = userLevel,
                playerWinLoss = "$winPct% W/L",
                localAvatarResId = localAvatarPreset.iconRes,
                localEquippedBadges = localBadges,
                onCancelQueue = {
                    scope.launch(Dispatchers.IO) {
                        val clashRepo = SupabaseClashRepository()
                        val localUserId = profileManager.callsign.value + "_" + Build.MODEL.replace(" ", "_")
                        clashRepo.cancelQueue(localUserId)
                    }
                    onNavigateBack(false)
                }
            )
        }
    }
}
