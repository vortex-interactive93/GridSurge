package com.example.gridsurge.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.example.gridsurge.clash.data.ClashReplayRepository
import com.example.gridsurge.game.GridSurgeGameView
import com.example.gridsurge.features.adventure.data.AdventureSectorRegistry
import com.example.gridsurge.features.adventure.ui.components.SegmentedRelicEnergyBar
import com.example.gridsurge.game.blitz.BlitzState
import com.example.gridsurge.game.replay.MatchReplayData
import com.example.gridsurge.leaderboard.model.GameModeType
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.meta.data.DailyMissionsRepository
import com.example.gridsurge.meta.quests.QuestType
import com.example.gridsurge.settings.SettingsManager
import com.example.gridsurge.theme.BackgroundThemeManager
import com.example.gridsurge.theme.ThemeNormalizer
import com.example.gridsurge.ui.components.*
import com.example.gridsurge.ui.dialogs.BlitzClashResultDialog
import com.example.gridsurge.ui.dialogs.InGamePauseDialog
import com.example.gridsurge.ui.replay.ReplayTheaterScreen
import com.example.gridsurge.ui.settings.SettingsDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    var rivalScore by remember { mutableLongStateOf(0L) }
    var maxComboInClash by remember { mutableIntStateOf(0) }
    var linesClearedInClash by remember { mutableIntStateOf(0) }
    var matchReplayData by remember { mutableStateOf<MatchReplayData?>(null) }
    var ratingDelta by remember { mutableIntStateOf(0) }
    var finalTimeSec by remember { mutableIntStateOf(0) }
    var gameViewRef by remember { mutableStateOf<GridSurgeGameView?>(null) }
    var victoryEvaluationResult by remember { mutableStateOf<StarEvaluationResult?>(null) }
    var isWinner by remember { mutableStateOf(false) }

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

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(modifier = Modifier.fillMaxSize().background(Color(0x990A0C14)))

        Column(modifier = Modifier.fillMaxSize()) {
            val headerMode = when (gameMode) {
                Screen.GAME_CLASSIC -> VisorGameMode.CLASSIC
                Screen.GAME_ADVENTURE -> VisorGameMode.ADVENTURE
                Screen.TIME_BLITZ -> VisorGameMode.TIME_BLITZ
                Screen.DAILY_GLITCH -> VisorGameMode.DAILY_GLITCH
                Screen.BLITZ_CLASH -> VisorGameMode.BLITZ_CLASH
                else -> VisorGameMode.CLASSIC
            }

            val blitz = gameViewRef?.blitzEngine
            val feverActive = blitz?.state == BlitzState.FEVER_ACTIVE
            val fProgress = blitz?.feverMeter ?: 0f
            val currentRivalScore = gameViewRef?.currentRivalScore ?: 0L
            val clashRemainingSec = gameViewRef?.currentDuelRemainingSeconds?.toFloat() ?: 75f

            AdaptiveCyberVisorHeader(
                gameMode = headerMode,
                score = score,
                rivalScore = currentRivalScore,
                highScore = globalHighScore.toLong(),
                linesCleared = linesClearedTotal,
                elapsedSeconds = elapsedSeconds,
                timeRemainingSec = if (gameMode == Screen.BLITZ_CLASH) clashRemainingSec else (blitz?.secondsRemaining ?: 90f),
                activeCores = activeProgressCount,
                totalCores = activeBlueprint?.objective?.targetAmount ?: 1,
                catalystsPurged = glitchPurged,
                totalCatalysts = 20,
                comboStreak = combo,
                feverProgress = fProgress,
                isFeverActive = feverActive,
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
                                scope.launch {
                                    val missionId = when (type) {
                                        QuestType.COMBO -> "q1"
                                        QuestType.LINES -> "q2"
                                        QuestType.SURGE_CORE -> "q3"
                                        QuestType.TIME_BLITZ -> "q4"
                                        QuestType.BLITZ_CLASH -> "q5"
                                    }
                                    if (type == QuestType.COMBO) dailyMissionsRepository.updateProgress(missionId, delta)
                                    else dailyMissionsRepository.incrementProgress(missionId, delta)
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
                            }
                            onLinesCleared = { clearedCount ->
                                profileManager.recordLinesCleared(clearedCount)
                            }
                            onRelicConsumed = {
                                relicManager.resetEnergy()
                            }
                            onLinesCleared = { lines: Int ->
                                linesClearedTotal += lines
                                relicManager.onLinesCleared(lines, combo, boardOccupancy)
                            }
                            onGameOver = {
                                if (activeBlueprint == null) {
                                    SfxManager.playSfx(SfxType.SYSTEM_OFFLINE)
                                    ModalOrchestrator.showModal(ModalType.GAME_OVER)
                                    val stars = when (gameMode) {
                                        Screen.TIME_BLITZ -> (score / 1000).toInt().coerceIn(10, 60)
                                        Screen.DAILY_GLITCH -> (score / 2000).toInt().coerceIn(5, 30)
                                        else -> (score / 1500).toInt().coerceIn(5, 50)
                                    }
                                    profileManager.recordGameResult(score.toInt(), combo, stars)
                                    GridSurgeAnalytics.logMatchCompleted(gameMode.name, score, "LOSS")
                                }
                            }
                            onStageVictoryEvaluated = { levelNum, finalScore, evalResult, time ->
                                score = finalScore
                                finalTimeSec = time
                                victoryEvaluationResult = evalResult

                                adventureViewModel.onLevelCompleted(levelNum, finalScore, evalResult.totalStars, time.toLong())
                                val stageInSec = ((levelNum - 1) % 9) + 1
                                if (stageInSec == 3 || stageInSec == 6) {
                                    draftManager.rollAugmentDraft(sectorId = activeBlueprint?.sectorId ?: 1)
                                }
                                ModalOrchestrator.showModal(ModalType.VICTORY)

                                if (levelNum % 9 == 0) {
                                    profileManager.recordSectorCleared(activeBlueprint?.sectorId ?: 1)
                                    profileManager.clearActiveSectorAugments()
                                    draftManager.resetRun()
                                }
                                GridSurgeAnalytics.logMatchCompleted(gameMode.name, finalScore, "VICTORY")
                            }
                            onClashFinished = { winner, pScore, rScore, stars, mCombo, lines, replay ->
                                isWinner = winner
                                score = pScore
                                rivalScore = rScore
                                maxComboInClash = mCombo
                                linesClearedInClash = lines
                                matchReplayData = replay
                                val delta = if (winner) 30 else -15
                                ratingDelta = delta
                                ModalOrchestrator.showModal(ModalType.CLASH_RESULT)
                                profileManager.addStarCurrency(stars)
                                profileManager.updateRatingPoints(delta)

                                scope.launch(Dispatchers.IO) {
                                    ClashReplayRepository.uploadReplay(profileManager.activeTitle.value, replay)
                                }
                            }
                            onStageDefeat = { ModalOrchestrator.showModal(ModalType.GAME_OVER) }
                            gameViewRef = this

                            when (gameMode) {
                                Screen.GAME_CLASSIC -> startClassicMatch()
                                Screen.TIME_BLITZ -> startTimeBlitzMatch()
                                Screen.DAILY_GLITCH -> startGlitchMode()
                                Screen.BLITZ_CLASH -> {
                                    scope.launch {
                                        val rivalReplay = ClashReplayRepository.fetchRandomRivalReplay()
                                        startBlitzClashDuel(rivalReplay)
                                    }
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

                ComboBadgeOverlay(
                    comboStreak = combo,
                    sectorId = if (gameMode == Screen.GAME_ADVENTURE) (activeBlueprint?.sectorId ?: 1) else 1,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp)
                )
            }
        }

        // Modal Dialogs
        if (activeModal == ModalType.PAUSE) {
            InGamePauseDialog(
                onResumeClicked = {
                    ModalOrchestrator.dismissModal(ModalType.PAUSE)
                    gameViewRef?.resumeEngine()
                },
                onRestartClicked = {
                    ModalOrchestrator.dismissModal(ModalType.PAUSE)
                    score = 0L
                    combo = 1
                    linesClearedTotal = 0
                    coresDestroyedTotal = 0
                    activeProgressCount = 0
                    relicManager.resetEnergy()
                    gameViewRef?.quickRestartMatch()
                },
                onResetSectorRunClicked = if (gameMode == Screen.GAME_ADVENTURE) {
                    {
                        ModalOrchestrator.dismissModal(ModalType.PAUSE)
                        profileManager.clearActiveSectorAugments()
                        score = 0L
                        combo = 1
                        linesClearedTotal = 0
                        coresDestroyedTotal = 0
                        activeProgressCount = 0
                        relicManager.resetEnergy()
                        val firstStageInSector = (((currentStageIndex - 1) / 9) * 9) + 1
                        currentStageIndex = firstStageInSector
                        gameViewRef?.quickRestartMatch()
                    }
                } else null,
                onSettingsClicked = {
                    ModalOrchestrator.dismissModal(ModalType.PAUSE)
                    ModalOrchestrator.showModal(ModalType.SETTINGS)
                },
                onQuitClicked = {
                    ModalOrchestrator.dismissModal(ModalType.PAUSE)
                    onNavigateBack(false)
                },
                stageTitle = if (gameMode == Screen.GAME_ADVENTURE) activeBlueprint?.stageName else null,
                stageDirective = if (gameMode == Screen.GAME_ADVENTURE) activeBlueprint?.directive else null,
                benchmarkInfo = if (gameMode == Screen.GAME_ADVENTURE) activeBenchmark?.let { "≤ ${it.moveBudgetStar2} Moves or < ${it.timeLimitSecStar2}s" } else null,
                masteryFeatInfo = if (gameMode == Screen.GAME_ADVENTURE) activeBenchmark?.masteryFeat?.description else null,
                activeAugments = if (gameMode == Screen.GAME_ADVENTURE) activeAugments else emptyList(),
                activeRelicAbility = if (gameMode == Screen.GAME_ADVENTURE) activeRelicAbility else null
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
                gameViewRef?.isTimeBlitzModeActive == true -> "TIME EXPIRED // FINAL SCORE: $score"
                else -> "MATRIX SATURATION // LINES CLEARED: $linesClearedTotal"
            }

            MatrixFailureReviveDialog(
                finalScore = score,
                canRevive = !hasUsedRevive,
                starBalance = starsBalance,
                objectiveType = activeBlueprint?.objective?.type ?: ObjectiveType.INFECTED_PURGE,
                adventureCoreProgress = if (activeBlueprint != null) Pair(coresDestroyedTotal, activeBlueprint.objective.targetAmount) else null,
                failureSubtitle = failureSubtitle,
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
                onAbort = { onNavigateBack(false) }
            )
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
                starsEarned = 0,
                ratingDelta = ratingDelta,
                maxCombo = maxComboInClash,
                linesCleared = linesClearedInClash,
                replayData = matchReplayData,
                onWatchReplay = {
                    ModalOrchestrator.dismissModal(ModalType.CLASH_RESULT)
                    ModalOrchestrator.showModal(ModalType.REPLAY_THEATER)
                },
                onRematch = {
                    ModalOrchestrator.dismissModal(ModalType.CLASH_RESULT)
                    score = 0L
                    combo = 1
                    linesClearedTotal = 0
                    elapsedSeconds = 75
                    gameViewRef?.startBlitzClashDuel()
                },
                onExit = { onNavigateBack(false) }
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
    }
}
