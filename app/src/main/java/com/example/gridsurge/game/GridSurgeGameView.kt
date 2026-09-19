package com.example.gridsurge.game

import android.app.Activity
import android.content.Context
import android.graphics.*
import com.example.gridsurge.ads.AdManager
import android.os.SystemClock
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import com.example.gridsurge.features.adventure.engine.RelicCyberWareManager
import com.example.gridsurge.features.adventure.engine.StarRatingEvaluator
import com.example.gridsurge.features.adventure.model.*
import com.example.gridsurge.features.adventure.data.AdventureSectorRegistry
import com.example.gridsurge.features.adventure.core.AdventureEventListener
import com.example.gridsurge.features.adventure.core.AdventureBoardManager
import com.example.gridsurge.features.adventure.core.AdventureMatchTimer
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.audio.VoxAction
import com.example.gridsurge.core.CellType
import com.example.gridsurge.core.ClearResult
import com.example.gridsurge.core.GridEngine
import com.example.gridsurge.game.blitz.model.BlitzPhase
import com.example.gridsurge.game.blitz.model.BlitzTerminalPhase
import com.example.gridsurge.game.blitz.model.BlitzTerminalSequenceState
import com.example.gridsurge.game.blitz.TimeBlitzEngine
import com.example.gridsurge.monetization.engine.SmartAdPacingEngine
import com.example.gridsurge.monetization.model.AdEligibilityResult
import com.example.gridsurge.game.fx.*
import com.example.gridsurge.game.glitch.GlitchEngine
import com.example.gridsurge.game.glitch.SeededGlitchMatchController
import com.example.gridsurge.game.model.*
import com.example.gridsurge.game.particle.CyberParticleSystem
import com.example.gridsurge.game.render.*
import com.example.gridsurge.features.adventure.rendering.*
import com.example.gridsurge.game.clash.network.engine.DeterministicPieceStream
import com.example.gridsurge.game.engine.BitboardFeasibilityEngine
import com.example.gridsurge.game.engine.GhostDuelEngine
import com.example.gridsurge.game.input.InteractionHandler
import com.example.gridsurge.game.input.InteractionListener
import com.example.gridsurge.game.logic.AdventureModeController
import com.example.gridsurge.game.logic.BlitzModeController
import com.example.gridsurge.game.logic.GlitchModeController
import com.example.gridsurge.game.logic.ClassicModeController
import com.example.gridsurge.game.replay.MatchReplayData
import com.example.gridsurge.game.replay.MatchTelemetryRecorder
import com.example.gridsurge.game.ui.ModalOrchestrator
import com.example.gridsurge.meta.quests.QuestType
import com.example.gridsurge.theme.ThemeNormalizer
import kotlinx.coroutines.*
import kotlinx.coroutines.delay
import java.util.Locale

class GridSurgeGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), AdventureEventListener, InteractionListener {

    private var _mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val mainScope: CoroutineScope
        get() {
            if (!CoroutineScope(_mainScope.coroutineContext).isActive) {
                _mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
            }
            return _mainScope
        }
    private val density = resources.displayMetrics.density

    // --- Engines & Systems ---
    val engine = GridEngine().apply { resetGame() }
    val blitzEngine = TimeBlitzEngine(initialTimeSec = 90f)
    private val _internalGlitchEngine = GlitchEngine(gridSize = 8)
    val glitchEngine: GlitchEngine get() = seededGlitchController?.glitchEngine ?: _internalGlitchEngine
    private var seededGlitchController: SeededGlitchMatchController? = null
    
    val bossEngine = com.example.gridsurge.features.adventure.engine.BossBattleEngine()
    val adventureBoard = AdventureBoardManager(eventListener = this)
    val progressionEngine = com.example.gridsurge.features.adventure.engine.AdventureProgressionEngine()
    val runState = AdventureRunState()
    var highestSectorCleared: Int = 0
        set(value) {
            field = value
            runState.isWarpUnlocked = value >= 1
        }
    var relicManager: RelicCyberWareManager? = null
    var activeAugments: List<com.example.gridsurge.features.adventure.model.NeuralAugment> = emptyList()
        set(value) {
            field = value
            runState.installedAugments = value.toMutableList()
            applyAugmentModifiers()
        }

    private fun applyAugmentModifiers() {
        val extraGrace = if (runState.hasAugment(com.example.gridsurge.features.adventure.model.AugmentType.BUFFER_OPTIMIZER)) 2 else 0
        engine.comboManager.maxGraceMoves = 2 + extraGrace
    }

    // --- Visual Systems ---
    private val textureCache = BlockTextureCache(context)
    private val sectorCoreRenderer = SectorCoreTextureRenderer(context, density)
    private val particleSystem = CyberParticleSystem(density, maxParticles = 240)
    private val scorePopupManager = FloatingScoreManager(density, maxPopups = 16)
    private val overdriveFx = OverdriveChassisFx(density)
    private val juiceFx = JuiceFxEngine(density)
    private val glitchSpriteVfx = GlitchSpriteVfx(context)
    private val warpVortexFx = WarpVortexFxEngine(density)
    private val spriteVfxEngine = OneShotSpriteVfxEngine(context)
    private val vfxPool = VfxPoolManager()
    private val vfxRenderer = VfxCanvasRenderer()
    private val juiceCoordinator = JuiceCoordinator(density, vfxPool, juiceFx, spriteVfxEngine, particleSystem, scorePopupManager, warpVortexFx).apply {
        onTriggerShake = { trauma = it }
    }
    private val warpController = WarpBlockController(density, textureCache).apply {
        onAnimationComplete = { gx, gy -> finalizeWarpDrop(gx, gy) }
        onPlaySound = { cue ->
            when (cue) {
                "vacuum_whoosh" -> SfxManager.playSfx(SfxType.MODAL_WHOOSH, overridePitch = 0.6f)
                "snap_pop" -> SfxManager.playSfx(SfxType.SNAP_TICK, overridePitch = 0.5f)
            }
        }
    }

    val matchTimer = AdventureMatchTimer(mainScope) { _, seconds ->
        elapsedSeconds = seconds.toInt()
        notifyAdventureState()
    }

    // --- Extracted Controllers ---
    private val renderer = MasterRenderer(context, density)
    private val clashHudRenderer = BlitzClashHudRenderer(density)
    private val interactionHandler = InteractionHandler(density, this)
    private val adventureController = AdventureModeController(engine, adventureBoard, bossEngine, runState, juiceCoordinator)
    val blitzController = BlitzModeController(engine, blitzEngine, juiceCoordinator)
    private val glitchController = GlitchModeController(engine, glitchEngine, juiceCoordinator)
    private val classicController = ClassicModeController(engine, juiceCoordinator)

    init {
        blitzEngine.onFeverActivated = {
            SfxManager.playSfx(SfxType.OVERDRIVE_ACTIVATE)
            SfxManager.playVox(VoxAction.OVERDRIVE)
            trauma = 0.60f
            onDirectiveEvent?.invoke("dir_fever_surge", 1)
        }
        blitzEngine.onFeverDeactivated = {
            renderer.exhaustRenderer.triggerExhaustVent(boardRect)
            SfxManager.playSfx(SfxType.MODAL_WHOOSH, overridePitch = 0.5f)
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
        blitzEngine.onFeverExtended = { event ->
            juiceCoordinator.spawnPopup(
                boardRect.centerX(),
                boardRect.centerY() - (45f * density),
                event.displayTag,
                Color.parseColor("#FFFFD600"),
                1200L
            )
            SfxManager.playSfx(SfxType.SNAP_TICK, overridePitch = 1.45f)
            trauma = (trauma + 0.35f).coerceAtMost(1.0f)
        }
    }

    val smartAdPacingEngine = SmartAdPacingEngine()
    var activePreloaderElapsedMs = 0L
    private var previousScoreDelta = 0L

    // Pre-allocated closure reference for zero-GC render loop
    private val pieceFitChecker: (List<PolyOffset>) -> Boolean = { offsets -> canPieceFitOnGrid(offsets) }

    // --- State ---
    var isAdventureModeActive = false
    var isGlitchModeActive = false
    var isTimeBlitzModeActive = false
    var isClashModeActive = false
    var isEnginePaused = false
    var isTouchLocked = false
    var matchPhase = MatchPhase.IN_PROGRESS
    var isObjectiveMet = false
    private var meltdownJob: Job? = null
    private var buzzerGraceTimer = 0.5f
    private var hasBuzzerBeaterGraceExpired = false
    private var rebootStrikeCount = 0
    var isRebootLockoutActive = false
    var rebootTimerSec = 0f
    val blitzTerminalSequenceState = BlitzTerminalSequenceState()
    var currentScore: Long = 0L
    var movesPlayedThisStage = 0
    var elapsedSeconds = 0
    private var matchElapsedAccSec = 0f
    var activeThemeKey: String = ThemeNormalizer.GLASS
        set(value) {
            val canonical = ThemeNormalizer.normalize(value)
            field = canonical
            textureCache.refreshCache(currentCellSizePx, canonical)
            invalidate()
        }

    private var currentCellSizePx = 0
    private val boardRect = RectF()
    private val dockSlotBounds = Array(3) { RectF() }
    private val dockShapes = arrayOfNulls<PolyShape>(3)
    private val landingStartTimes = LongArray(64)
    private var trauma = 0f
    private var lastFrameTime = System.nanoTime()
    private var lastRealTimeMs = 0L
    private var animationTimeMs = 0L
    private var maxSimultaneousLinesCleared = 0
    private var relicActivationsCountThisStage = 0
    private var empJamOccurredThisStage = false
    private var activeMilestoneBanner = MilestoneBannerState("", Color.CYAN, 0L, 1400L, false)
    val ghostDuelEngine = GhostDuelEngine(mainScope, { postInvalidateOnAnimation() }, { if(it) SfxManager.playVox(VoxAction.LEAD_SECURED) else SfxManager.playVox(VoxAction.LEAD_LOST) }, { w, p, r -> handleDuelFinished(w, p, r) }, { t -> handleDuelTimer(t) }, { s -> triggerClashJammer(s) }, { s, c -> logRivalMove(s, c) })

    var hasUsedReviveThisRun = false
    var currentAdventureLevelNumber: Int = 1
    val activeBlueprint: AdventureLevelBlueprint? get() = adventureBoard.activeBlueprint
    val currentRivalScore: Long get() = ghostDuelEngine.rivalScore
    val currentDuelRemainingSeconds: Int get() = ghostDuelEngine.matchSecondsRemaining
    private var liveStartEpochMs: Long = 0L

    private var lastDropPxX: Float = 0f
    private var lastDropPxY: Float = 0f

    private var maxComboInMatch = 1
    private var totalLinesInMatch = 0

    // --- Reactive Callbacks ---
    var onAdventureStateUpdated: ((Int, Long, Int, Int, Boolean, Int, Int, Int, Int, Int, Int, Float, Float, Float, Float) -> Unit)? = null
    var onScoreChanged: ((Long, Int) -> Unit)? = null
    var onLinesCleared: ((Int) -> Unit)? = null
    var onGameOver: (() -> Unit)? = null
    var onStageVictoryEvaluated: ((Int, Long, StarEvaluationResult, Int) -> Unit)? = null
    var onStageDefeat: (() -> Unit)? = null
    var onClashFinished: ((Boolean, Long, Long, Int, Int, Int, Int, MatchReplayData) -> Unit)? = null
    var onBroadcastLiveMove: ((Long, Int, Int, Boolean, Boolean) -> Unit)? = null
    var onMissionEvent: ((QuestType, Int) -> Unit)? = null
    var onDirectiveEvent: ((String, Int) -> Unit)? = null
    var onVectorLineClear: ((List<ClearedLineEvent>) -> Unit)? = null

    init {
        ghostDuelEngine.playerScoreProvider = { currentScore }
        adventureBoard.onCoreHarvested = {
            if (isAdventureModeActive) {
                runState.addResonance(AdventureRunState.CORE_HARVEST_ENERGY)
                notifyAdventureState()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return

        val horizontalMargin = 16f * density
        val cellSpacing = 3f * density
        val maxAvailableWidth = w - horizontalMargin * 2f

        // 1. Correct Safe Zones
        // Compose's Modifier.weight(1f) already pushed us below the HUD. We only need minor breathing room.
        val topClearance = 16f * density 
        // We still need full bottom clearance to fit both the Emote Button and the Dock blocks
        val bottomClearance = 180f * density 

        val availableHeight = (h - topClearance - bottomClearance).coerceAtLeast(100f * density)

        // 2. Constrain Cell Size safely
        val cellSizeFromWidth = (maxAvailableWidth - (cellSpacing * 9f)) / 8f
        val cellSizeFromHeight = (availableHeight - (cellSpacing * 9f)) / 8f

        val calculatedCellSize = minOf(cellSizeFromWidth, cellSizeFromHeight).coerceAtLeast(24f * density)
        val boardSideLength = calculatedCellSize * 8f + cellSpacing * 9f

        // 3. Center the board strictly inside the remaining space
        val verticalRemainder = (availableHeight - boardSideLength).coerceAtLeast(0f)
        val boardLeft = (w - boardSideLength) / 2f
        val boardTop = topClearance + (verticalRemainder / 2f) 

        interactionHandler.cellSize = calculatedCellSize
        currentCellSizePx = calculatedCellSize.toInt()
        renderer.stagedAnomalyBitmapRenderer.prewarmBitmaps(currentCellSizePx)
        interactionHandler.cellSpacing = cellSpacing

        interactionHandler.boardRect.set(boardLeft, boardTop, boardLeft + boardSideLength, boardTop + boardSideLength)
        boardRect.set(interactionHandler.boardRect)
        juiceCoordinator.boardRect.set(boardRect)
        juiceCoordinator.cellSize = calculatedCellSize
        juiceCoordinator.cellSpacing = cellSpacing

        // 4. Position the Dock (Anchored precisely 24dp from the bottom)
        val dockReservedHeight = 85f * density
        val bottomMargin = 24f * density
        val dockTop = h - dockReservedHeight - bottomMargin
        
        val slotSpacing = 8f * density
        val slotWidth = (maxAvailableWidth - slotSpacing * 2f) / 3f

        for (i in 0 until 3) {
            interactionHandler.dockSlotBounds[i].set(
                horizontalMargin + i * (slotWidth + slotSpacing),
                dockTop,
                horizontalMargin + i * (slotWidth + slotSpacing) + slotWidth,
                dockTop + dockReservedHeight
            )
            dockSlotBounds[i].set(interactionHandler.dockSlotBounds[i])
        }

        textureCache.refreshCache(currentCellSizePx, activeThemeKey)
        sectorCoreRenderer.prepareBitmaps(currentCellSizePx)
    }

    override fun onDraw(canvas: Canvas) {
        if (width <= 0 || height <= 0 || boardRect.width() <= 0) return

        val now = SystemClock.uptimeMillis()
        if (!isEnginePaused && lastRealTimeMs > 0) animationTimeMs += (now - lastRealTimeMs)
        lastRealTimeMs = now
        val rawDt = if (isEnginePaused) 0f else (System.nanoTime() - lastFrameTime) / 1_000_000_000f
        lastFrameTime = System.nanoTime()
        val dt = rawDt.coerceIn(0f, 0.1f)
        
        if (!isEnginePaused && dt > 0f) {
            vfxPool.update(dt)
            juiceFx.updateFrame(dt)
            interactionHandler.updatePhysics(dt)

            // Live Time Blitz countdown or elapsed match timer
            if (isTimeBlitzModeActive) {
                val phase = blitzEngine.updateFrame(dt)
                val updatedSec = blitzEngine.secondsRemaining.toInt()
                if (updatedSec != elapsedSeconds) {
                    elapsedSeconds = updatedSec
                    notifyAdventureState()
                }

                if (phase == BlitzPhase.SESSION_COMPLETE || blitzEngine.isTimeExpired) {
                    val isActivelyDragging = interactionHandler.dragState.isDragging
                    if (isActivelyDragging && !hasBuzzerBeaterGraceExpired) {
                        buzzerGraceTimer -= dt
                        if (buzzerGraceTimer <= 0f) {
                            hasBuzzerBeaterGraceExpired = true
                        }
                    }

                    if (!isActivelyDragging || hasBuzzerBeaterGraceExpired) {
                        if (blitzTerminalSequenceState.phase == BlitzTerminalPhase.RUNNING) {
                            blitzTerminalSequenceState.phase = BlitzTerminalPhase.FREEZE_STASIS
                            blitzTerminalSequenceState.sequenceElapsedSec = 0f
                            isTouchLocked = true
                            trauma = 0.55f
                            SfxManager.playSfx(SfxType.MEGA_BLITZ)
                        } else if (blitzTerminalSequenceState.phase == BlitzTerminalPhase.FREEZE_STASIS) {
                            blitzTerminalSequenceState.sequenceElapsedSec += dt
                            if (blitzTerminalSequenceState.sequenceElapsedSec >= blitzTerminalSequenceState.freezeDurationSec) {
                                blitzTerminalSequenceState.phase = BlitzTerminalPhase.DEBRIEF_MOUNTED
                                isEnginePaused = true
                                onGameOver?.invoke()
                            }
                        }
                    }
                } else {
                    buzzerGraceTimer = 0.5f
                    hasBuzzerBeaterGraceExpired = false
                    blitzTerminalSequenceState.phase = BlitzTerminalPhase.RUNNING
                    blitzTerminalSequenceState.sequenceElapsedSec = 0f
                }
            }
        }

        val rootSaveCount = canvas.save()
        try {
            renderer.render(
                canvas = canvas,
                engine = engine,
                adventureGrid = adventureBoard.grid,
                hazardGrid = progressionEngine.hazardGrid,
                bossThreatState = progressionEngine.bossState,
                bossBattleState = bossEngine.state,
                bossEngineState = bossEngine.state,
                blitzEngine = blitzEngine,
                glitchEngine = glitchEngine,
                dragState = interactionHandler.dragState,
                boardRect = boardRect,
                dockSlotBounds = dockSlotBounds,
                dockShapes = dockShapes,
                cellSize = interactionHandler.cellSize,
                cellSpacing = interactionHandler.cellSpacing,
                dockCellSizePx = (currentCellSizePx * 0.70f).toInt(),
                activeThemeKey = activeThemeKey,
                textureCache = textureCache,
                vfxPool = vfxPool,
                vfxRenderer = vfxRenderer,
                spriteVfxEngine = spriteVfxEngine,
                warpVortexFx = warpVortexFx,
                juiceFx = juiceFx,
                scorePopupManager = scorePopupManager,
                particleSystem = particleSystem,
                overdriveFx = overdriveFx,
                glitchSpriteVfx = glitchSpriteVfx,
                sectorCoreRenderer = sectorCoreRenderer,
                comboStreak = engine.comboManager.currentStreak,
                comboState = engine.comboManager.currentState,
                graceMovesRemaining = engine.comboManager.graceMovesRemaining,
                maxGraceMoves = engine.comboManager.maxGraceMoves,
                activeAugments = activeAugments,
                isAdventureModeActive = isAdventureModeActive,
                isGlitchModeActive = isGlitchModeActive,
                isTimeBlitzModeActive = isTimeBlitzModeActive,
                isClashModeActive = isClashModeActive,
                isCurrentStageBoss = isAdventureModeActive && ((adventureBoard.activeBlueprint?.levelNumber ?: 0) % 9 == 0),
                trauma = trauma,
                boardFillRatio = engine.getOccupiedRatio(),
                activeMilestoneBanner = activeMilestoneBanner,
                landingStartTimes = landingStartTimes,
                canPieceFit = pieceFitChecker,
                warpController = warpController,
                sectorId = if (isAdventureModeActive) (adventureBoard.activeBlueprint?.sectorId ?: 1) else 1,
                now = animationTimeMs,
                dt = dt,
                blitzTerminalSequenceState = blitzTerminalSequenceState,
                activePreloaderElapsedMs = activePreloaderElapsedMs,
                isRebootLockoutActive = isRebootLockoutActive,
                rebootTimerSec = rebootTimerSec
            )

            if (isClashModeActive) {
                if (liveStartEpochMs > 0L) {
                    ghostDuelEngine.updateLiveTimer(startEpochMs = liveStartEpochMs, totalSeconds = 90)
                }
                // Native canvas HUD disabled in favor of Compose OverchargeMomentumBar
            }
        } finally {
            canvas.restoreToCount(rootSaveCount)
        }

        if (!isEnginePaused) {
            trauma = (trauma - dt * 3.2f).coerceAtLeast(0f)
            postInvalidateOnAnimation()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        interactionHandler.dockShapes = dockShapes
        return interactionHandler.handleTouchEvent(event)
    }

    // --- InteractionListener Implementation ---
    override fun onCommitDrop(shape: PolyShape, slotIndex: Int, col: Int, row: Int) {
        movesPlayedThisStage++
        
        // 1. Tick Spawners
        when {
            isAdventureModeActive -> adventureController.spawner.onMoveCommitted()
            isTimeBlitzModeActive -> blitzController.spawner.onMoveCommitted()
            isGlitchModeActive -> glitchController.spawner.onMoveCommitted()
            else -> classicController.spawner.onMoveCommitted()
        }

        if (shape.specialType == SpecialBlockType.NOVA_CORE_EXPLOSION) {
            SfxManager.playPlacementSound(isSpecial = true)
            if (slotIndex >= 0) consumePiece(slotIndex)
            handleNovaCoreExplosion(col, row)
        } else if (shape.specialType == SpecialBlockType.QUANTUM_WARP_VORTEX) {
            SfxManager.playSfx(SfxType.WARP_VORTEX)
            if (slotIndex >= 0) consumePiece(slotIndex)
            handleWarpBlockDetonation(col, row, shape.color)
            if (isAdventureModeActive) {
                relicManager?.resetEnergy()
                runState.consumeWarp()
                // Warp detonation also counts as a turn for furnaces
                adventureBoard.onMoveCommitted(elapsedSeconds)
            }
        } else {
            // Track drop cell pixel coordinates: Offset 1 cell UP and 1 cell RIGHT on boardRect
            val targetRow = (row - 1).coerceAtLeast(0)
            val targetCol = (col + 1).coerceAtMost(7)
            lastDropPxX = boardRect.left + (targetCol + 0.5f) * currentCellSizePx
            lastDropPxY = boardRect.top + (targetRow + 0.5f) * currentCellSizePx

            // Commit drop with explicit shape color stored in engine
            val placedCoords = shape.offsets.map { offset -> Pair(col + offset.x, row + offset.y) }
            val result = engine.placeShape(slotIndex, col, row)
            shape.offsets.forEach { offset ->
                engine.setCellColor(col + offset.x, row + offset.y, shape.color)
            }
            SfxManager.playPlacementSound(isSpecial = shape.specialType != SpecialBlockType.NONE, skinId = activeThemeKey)
            juiceCoordinator.onPiecePlaced(placedCoords, shape.color)

            // Record telemetry for replay theater
            MatchTelemetryRecorder.logPlayerMove(
                slotIndex = slotIndex,
                shapeId = shape.id.hashCode(),
                targetRow = row,
                targetCol = col,
                occupiedOffsets = shape.offsets.map { Pair(it.y, it.x) },
                colorInt = shape.color,
                linesCleared = result.totalLines,
                scoreAfterMove = engine.score,
                comboStreak = engine.comboManager.currentStreak
            )

            if (isAdventureModeActive) {
                relicManager?.onPiecePlaced(engine.getOccupiedRatio())
            }
            consumePiece(slotIndex)
            completeCommitCycle(result, placedCoords)

            // 2. Tick Board state AFTER resolution (allows "last move" saves)
            if (isAdventureModeActive) {
                adventureBoard.onMoveCommitted(elapsedSeconds)
                val isCrit = adventureBoard.isAnyFurnaceCritical()
                runState.isPityActive = isCrit
                runState.isCriticalState = isCrit
            }
        }
        notifyAdventureState()
    }

    override fun onInvalidMove() {}
    override fun requestInvalidate() { invalidate() }
    override fun triggerHaptic(constant: Int) { performHapticFeedback(constant) }
    override fun isJammed(slotIndex: Int) = bossEngine.state.jammedSlotIndex == slotIndex
    override fun getFlatGrid() = IntArray(64) { if (engine.getGridValue(it % 8, it / 8) == 0) 0 else 1 }
    override fun getHazardGrid() = progressionEngine.hazardGrid
    override fun isInteractionLocked() = isEnginePaused || matchPhase == MatchPhase.STAGE_COMPLETED || isObjectiveMet || isTouchLocked
    override fun getCurrentTimeMs() = animationTimeMs

    // --- AdventureEventListener Implementation ---
    override fun onSectorInitialized(initialHp: Int, totalCores: Int) {
        runState.isWarpUnlocked = highestSectorCleared >= 1
        for (r in 0 until 8) for (c in 0 until 8) engine.setGridValue(c, r, adventureBoard.grid[r][c].toCellTypeValue())
    }

    override fun onCoresCracked(crackedCells: List<GridCell>) {
        crackedCells.forEach { cell ->
            engine.setGridValue(cell.col, cell.row, cell.toCellTypeValue())
            if (cell.coreIntegrity == CoreIntegrity.DESTROYED) {
                spawnShatterVfx(cell)
                if (isAdventureModeActive && (adventureBoard.activeBlueprint?.levelNumber ?: 0) % 9 == 0) bossEngine.onPylonsDestroyed(setOf(cell.row * 8 + cell.col))
            }
        }
    }

    override fun onCoreWaveSpawned(newCores: List<GridCell>) {
        newCores.forEach { engine.setGridValue(it.col, it.row, it.toCellTypeValue()) }
    }

    override fun onCrossBlastTriggered(originRow: Int, originCol: Int, clearedCells: List<GridCell>) {}
    override fun onBossDamaged(currentHp: Int, damageDealt: Int) { juiceCoordinator.spawnPopup(boardRect.centerX(), boardRect.centerY(), "-$damageDealt% HP", Color.RED, animationTimeMs) }
    override fun onCoreCountUpdated(remainingCores: Int) {}
    override fun onMilestoneReached(percent: Int) { activeMilestoneBanner.isActive = true; activeMilestoneBanner.text = "$percent% SYNC"; activeMilestoneBanner.startTimeMs = animationTimeMs }
    override fun onTimeRefundAwarded(seconds: Int) { matchTimer.refundSeconds(seconds) }
    override fun onClutchDefuse(row: Int, col: Int) {
        val rx = boardRect.left + (col + 0.5f) * currentCellSizePx
        val ry = boardRect.top + (row + 0.5f) * currentCellSizePx
        juiceCoordinator.spawnPopup(rx, ry, "CLUTCH DEFUSE", Color.parseColor("#FFD600"), animationTimeMs)
        currentScore += 1000
        engine.score = currentScore
        SfxManager.playSfx(SfxType.MEGA_BLITZ)
    }
    override fun onCriticalMeltdownExplosion(row: Int, col: Int, onFinished: () -> Unit) {
        isTouchLocked = true
        trauma = 1.0f
        
        val rect = RectF()
        calculateCellRect(row, col, interactionHandler.cellSize, interactionHandler.cellSpacing, boardRect, rect)
        
        // Large explosion VFX
        spriteVfxEngine.spawnVfx(SpriteVfxType.MEGA_BLITZ_BURST, rect, animationTimeMs)
        juiceCoordinator.spawnBurstParticles(rect.centerX(), rect.centerY(), Color.RED, 40)
        
        SfxManager.playSfx(SfxType.EMP_SHOCKWAVE)
        SfxManager.playVox(VoxAction.GRID_CRITICAL)

        mainScope.launch {
            delay(800L)
            isTouchLocked = false
            onFinished()
        }
    }
    override fun onRelicArsenalInjected(specialType: SpecialBlockType) { dockShapes[2] = PolyominoCatalog.instantiateSpecial(specialType); engine.dock[2] = dockShapes[2]; syncDockFromEngine() }
    override fun onSlagTransmutationTriggered(originRow: Int, originCol: Int, clearedSlag: List<GridCell>) {}
    override fun onSupernovaCollapseTriggered(originRow: Int, originCol: Int) {}
    override fun onCoreHitRegistered(row: Int, col: Int, isWarp: Boolean) = if (isAdventureModeActive) progressionEngine.registerCoreHit(row, col, isWarp) else true
    override fun onSectorVictory(stars: Int, elapsed: Int) { handleVictory(elapsed) }
    override fun onSectorDefeat() { onStageDefeat?.invoke() }

    // --- Private Helpers ---
    private fun consumePiece(idx: Int) {
        if (idx in 0..2) { dockShapes[idx] = null; engine.dock[idx] = null }
        smartAdPacingEngine.recordPiecePlaced()
        if (dockShapes.all { it == null }) {
            if (isClashModeActive && livePieceStream != null) {
                replenishLiveSeededDock()
            } else {
                checkSmartAdOrReplenishDock()
            }
        }
    }

    private fun checkSmartAdOrReplenishDock() {
        val eligibility = smartAdPacingEngine.evaluateEligibility(
            isNoAdsOwned = false,
            isRankedOrPvP = isClashModeActive,
            activeComboStreak = engine.comboManager.currentStreak,
            isDraggingPiece = interactionHandler.dragState.isDragging
        )

        if (eligibility == AdEligibilityResult.ELIGIBLE) {
            val activity = context as? Activity
            if (activity != null) {
                isTouchLocked = true
                isEnginePaused = true

                mainScope.launch {
                    val startTime = SystemClock.elapsedRealtime()
                    while (SystemClock.elapsedRealtime() - startTime < 1200L) {
                        activePreloaderElapsedMs = SystemClock.elapsedRealtime() - startTime
                        postInvalidateOnAnimation()
                        delay(16L)
                    }

                    activePreloaderElapsedMs = 0L
                    AdManager.showInterstitialAd(activity, false)
                    smartAdPacingEngine.recordInterstitialShown()

                    replenishDock()
                    isTouchLocked = false
                    isEnginePaused = false
                    postInvalidateOnAnimation()
                }
                return
            }
        }

        replenishDock()
    }

    private fun replenishDock() {
        val occupancy = engine.getOccupiedRatio()
        val streak = engine.comboManager.currentStreak
        val boardMask = BitboardFeasibilityEngine.calculateBoardMask(engine.getGridArray())
        
        val newTray = when {
            isAdventureModeActive -> adventureController.spawner.nextTray(occupancy, streak, boardMask)
            isTimeBlitzModeActive -> blitzController.spawner.nextTray(occupancy, streak, boardMask)
            isGlitchModeActive -> glitchController.spawner.nextTray(occupancy, streak, boardMask)
            else -> classicController.spawner.nextTray(occupancy, streak, boardMask)
        }

        for (i in 0 until 3) {
            dockShapes[i] = newTray.getOrNull(i)
            engine.dock[i] = dockShapes[i]
        }
        syncDockFromEngine()
    }

    private fun syncDockFromEngine() {
        for (i in 0 until 3) engine.dock[i] = dockShapes[i]
    }

    private fun completeCommitCycle(result: ClearResult, placedCoords: List<Pair<Int, Int>> = emptyList()) {
        maxSimultaneousLinesCleared = maxOf(maxSimultaneousLinesCleared, result.totalLines)

        if (result.totalLines > 0) {
            juiceCoordinator.onLinesCleared(result, engine.comboManager.currentStreak, animationTimeMs)
            onLinesCleared?.invoke(result.totalLines)
        }

        if (isAdventureModeActive) {
            adventureController.processMove(result, elapsedSeconds, currentScore)

            // --- Neural Augment / Skill Execution ---
            if (result.totalLines > 0) {
                // MOLTEN HARVEST: Multi-line clears grant +2.5x score multiplier
                if (result.totalLines >= 2 && runState.hasAugment(AugmentType.MOLTEN_HARVEST)) {
                    val bonusScore = (result.pointsEarned * 1.5f).toLong()
                    currentScore += bonusScore
                    engine.score = currentScore
                    juiceCoordinator.spawnPopup(
                        boardRect.centerX(), boardRect.centerY(),
                        "+$bonusScore // MOLTEN HARVEST", Color.parseColor("#FFD600"), animationTimeMs
                    )
                }

                // CHRONO SIPHON: Multi-line clears refund +10s
                if (result.totalLines >= 2 && runState.hasAugment(AugmentType.CHRONO_SIPHON)) {
                    matchTimer.refundSeconds(10)
                    juiceCoordinator.spawnPopup(
                        boardRect.centerX(), boardRect.centerY(),
                        "+10s // CHRONO SIPHON", Color.parseColor("#00E5FF"), animationTimeMs
                    )
                }

                // CARDINAL OVERCLOCK & KINETIC BURST
                val payload = com.example.gridsurge.game.engine.OverclockExecutionEngine.resolvePostClearAugments(
                    result, runState, engine, adventureBoard, elapsedSeconds, placedCoords
                )

                if (payload.pointsAwarded > 0) {
                    currentScore += payload.pointsAwarded
                    engine.score = currentScore
                    juiceCoordinator.spawnPopup(
                        boardRect.centerX(), boardRect.centerY(),
                        "+${payload.pointsAwarded} // OVERCLOCK BURST", Color.parseColor("#00E5FF"), animationTimeMs
                    )
                }

                if (payload.secondaryLaserRows.isNotEmpty() || payload.secondaryLaserCols.isNotEmpty()) {
                    SfxManager.playSfx(SfxType.LASER_SWEEP)
                    juiceCoordinator.triggerShake(0.3f)

                    val rowsMask = payload.secondaryLaserRows.fold(0) { mask, r -> mask or (1 shl r) }
                    val colsMask = payload.secondaryLaserCols.fold(0) { mask, c -> mask or (1 shl c) }
                    juiceCoordinator.spawnLaserVfx(rowsMask, colsMask, Color.parseColor("#00E5FF"))
                }
            }

            // --- Phase Resonance Engine Logic ---
            val totalLines = result.totalLines
            if (totalLines > 0) {
                // Base Energy
                var baseEnergy = when (totalLines) {
                    1 -> 15f
                    2 -> 35f
                    else -> 60f
                }
                if (runState.hasAugment(com.example.gridsurge.features.adventure.model.AugmentType.WARP_INJECTOR)) {
                    baseEnergy *= 1.5f
                }
                // Streak Multiplier: base * (1 + 0.5 * (streak-1))
                val streakMult = 1.0f + (0.5f * (engine.comboManager.currentStreak - 1))
                runState.addResonance(baseEnergy * streakMult)
            } else {
                // Decay on non-clear
                runState.applyDecay()
            }

            // Desperation Pulse
            if (engine.getOccupiedRatio() >= AdventureRunState.DANGER_OCCUPANCY_THRESHOLD) {
                runState.addResonance(AdventureRunState.DESPERATION_PULSE_ENERGY)
            }
        }

        // --- MISSION & DIRECTIVE PROGRESS DISPATCH ---
        onDirectiveEvent?.invoke("dir_place_blocks", 1)
        if (result.totalLines > 0) {
            totalLinesInMatch += result.totalLines
            maxComboInMatch = maxOf(maxComboInMatch, engine.comboManager.currentStreak)
            onMissionEvent?.invoke(QuestType.LINES, result.totalLines)
            if (result.totalLines >= 2) {
                onDirectiveEvent?.invoke("dir_clear_lines", 1)
            }
            // Defuse Stasis Jammers on cleared rows or columns
            for (r in result.clearedRows) {
                for (c in 0..7) {
                    if (progressionEngine.hazardGrid[r][c].hazardType == AdventureHazardType.EMP_LOCK) {
                        progressionEngine.hazardGrid[r][c] = HazardCellState(hazardType = AdventureHazardType.NONE)
                        val cx = boardRect.left + (c + 0.5f) * currentCellSizePx
                        val cy = boardRect.top + (r + 0.5f) * currentCellSizePx
                        juiceCoordinator.spawnPopup(cx, cy, "JAMMER DEFUSED!", Color.parseColor("#FFD600"), animationTimeMs)
                        SfxManager.playSfx(SfxType.CORE_EXPLOSION)
                    }
                }
            }
            for (c in result.clearedCols) {
                for (r in 0..7) {
                    if (progressionEngine.hazardGrid[r][c].hazardType == AdventureHazardType.EMP_LOCK) {
                        progressionEngine.hazardGrid[r][c] = HazardCellState(hazardType = AdventureHazardType.NONE)
                        val cx = boardRect.left + (c + 0.5f) * currentCellSizePx
                        val cy = boardRect.top + (r + 0.5f) * currentCellSizePx
                        juiceCoordinator.spawnPopup(cx, cy, "JAMMER DEFUSED!", Color.parseColor("#FFD600"), animationTimeMs)
                        SfxManager.playSfx(SfxType.CORE_EXPLOSION)
                    }
                }
            }
            val vectorEvents = mutableListOf<ClearedLineEvent>()
            result.clearedRows.forEach { r ->
                vectorEvents.add(ClearedLineEvent(index = r, isRow = true, colorLong = 0xFF00E5FFL, progress = 0f))
            }
            result.clearedCols.forEach { c ->
                vectorEvents.add(ClearedLineEvent(index = c, isRow = false, colorLong = 0xFF00E5FFL, progress = 0f))
            }
            if (vectorEvents.isNotEmpty()) {
                onVectorLineClear?.invoke(vectorEvents)
            }
        }
        if (engine.comboManager.currentStreak > 1) {
            onMissionEvent?.invoke(QuestType.COMBO, engine.comboManager.currentStreak)
        }
        if (isGlitchModeActive && glitchEngine.totalPurgedCount > 0) {
            onDirectiveEvent?.invoke("dir_anomaly_seed", glitchEngine.totalPurgedCount)
        }

        if (isTimeBlitzModeActive) currentScore = blitzController.processMove(result, engine.comboManager.currentStreak)
        if (isGlitchModeActive) {
            glitchController.processMove(result)
            checkPurityState(glitchEngine.purity)

            val targetPurge = 25
            if (glitchEngine.totalPurgedCount >= targetPurge && !isObjectiveMet) {
                isObjectiveMet = true
                isTouchLocked = true
                SfxManager.playSfx(SfxType.LEVEL_COMPLETE)
                SfxManager.playVox(VoxAction.OBJECTIVE_DONE)

                mainScope.launch {
                    delay(650L) // Allow line clear particle explosions & beam sweeps to finish animating
                    vfxPool.clearAll()
                    juiceFx.clearAll()
                    spriteVfxEngine.clearAll()
                    scorePopupManager.clearAll()
                    isEnginePaused = true
                    onGameOver?.invoke()
                }
                return
            }
        }
        if (!isAdventureModeActive && !isTimeBlitzModeActive && !isGlitchModeActive) classicController.processMove(result)
        
        // Resonance Overclock: +25% score bonus while Warp is ready
        if (isAdventureModeActive && runState.isWarpReady && result.pointsEarned > 0) {
            val bonus = (result.pointsEarned * 0.25f).toInt()
            engine.score += bonus
        }

        currentScore = engine.score 
        if (isClashModeActive) {
            ghostDuelEngine.evaluateLead(currentScore)

            // 1. Broadcast move to opponent
            onBroadcastLiveMove?.invoke(
                currentScore,
                result.totalLines,
                engine.comboManager.currentStreak,
                blitzEngine.state.isFeverActive,
                false
            )

            // 2. Kinetic attack volleys toward rival HUD on multi-clears
            if (result.totalLines >= 2) {
                renderer.clashAttackEmitter.spawnAttackVolley(
                    originX = lastDropPxX,
                    originY = lastDropPxY,
                    targetX = width * 0.82f,
                    targetY = 32f * density,
                    lineCount = result.totalLines
                )
                SfxManager.playSfx(SfxType.SNAP_TICK, overridePitch = 1.5f)
                trauma = (trauma + 0.35f).coerceAtMost(1.0f)
            }

            val currentDelta = currentScore - currentRivalScore
            if (previousScoreDelta < 0 && currentDelta >= 0) {
                trauma = 0.45f
                SfxManager.playSfx(SfxType.LEVEL_COMPLETE, overridePitch = 1.6f)
            } else if (previousScoreDelta >= 0 && currentDelta < 0) {
                trauma = 0.35f
                SfxManager.playSfx(SfxType.EMP_SHOCKWAVE, overridePitch = 0.85f)
            }
            previousScoreDelta = currentDelta
        }
        onScoreChanged?.invoke(currentScore, engine.comboManager.currentStreak)
        
        // --- PRIORITY RESOLUTION: Check Win Condition FIRST ---
        if (isObjectiveMet) return 

        checkGameOverOrVictory()
    }

    fun forceRefillFreshDock() {
        for (i in 0 until 3) {
            dockShapes[i] = null
            engine.dock[i] = null
        }
        if (livePieceStream != null) {
            replenishLiveSeededDock()
        } else {
            replenishDock()
        }
    }

    private fun checkGameOverOrVictory() {
        if (isObjectiveMet) return // Already won, don't trigger Game Over

        if (!canAnyPieceBePlaced()) {
            if (rebootStrikeCount == 0) {
                // --- STRIKE 1: EMERGENCY OVERRIDE // GRID FLUSH ---
                rebootStrikeCount = 1
                isTouchLocked = true
                isRebootLockoutActive = true
                rebootTimerSec = 2.0f
                trauma = 0.85f
                SfxManager.playSfx(SfxType.SYSTEM_OFFLINE)
                SfxManager.playVox(VoxAction.GRID_CRITICAL)

                // 1. Deduct 20% score tax & reset combo
                val penaltyTax = (currentScore * 0.20f).toLong()
                currentScore = (currentScore - penaltyTax).coerceAtLeast(0L)
                engine.score = currentScore
                engine.comboManager.reset()

                // 2. Vaporize center 4x4 core
                engine.clearCenter4x4()

                // Vector line clear disintegration effect for center 4x4
                val flushEvents = mutableListOf<ClearedLineEvent>()
                val orangeColor = 0xFFFF6D00L
                for (r in 2..5) {
                    flushEvents.add(ClearedLineEvent(index = r, isRow = true, colorLong = orangeColor, progress = 0f))
                }
                for (c in 2..5) {
                    flushEvents.add(ClearedLineEvent(index = c, isRow = false, colorLong = orangeColor, progress = 0f))
                }
                onVectorLineClear?.invoke(flushEvents)

                val taxFormatted = if (penaltyTax > 0) "-${String.format(Locale.US, "%,d", penaltyTax)}" else "0"
                juiceCoordinator.spawnPopup(
                    boardRect.centerX(),
                    boardRect.centerY(),
                    "[ $taxFormatted // EMERGENCY OVERRIDE TAX ]",
                    Color.RED,
                    animationTimeMs
                )

                // 3. Discard dead pieces & generate 3 fresh playable pieces
                forceRefillFreshDock()

                // 4. Start 2.0s System Reboot Timer
                mainScope.launch {
                    val startMs = SystemClock.uptimeMillis()
                    while (rebootTimerSec > 0f) {
                        delay(100L)
                        val elapsedSec = (SystemClock.uptimeMillis() - startMs) / 1000f
                        rebootTimerSec = (2.0f - elapsedSec).coerceAtLeast(0f)
                        postInvalidateOnAnimation()
                    }
                    isRebootLockoutActive = false
                    isTouchLocked = false
                    postInvalidateOnAnimation()
                }
                return
            }

            // --- STRIKE 2: CRITICAL CORE COLLAPSE (TKO) ---
            isTouchLocked = true
            matchTimer.stop()
            SfxManager.playSfx(SfxType.SYSTEM_OFFLINE)
            ModalOrchestrator.clearAll()

            mainScope.launch {
                delay(400L) // Allow placement VFX to complete
                vfxPool.clearAll()
                juiceFx.clearAll()
                spriteVfxEngine.clearAll()
                scorePopupManager.clearAll()
                isEnginePaused = true

                if (isClashModeActive) {
                    onBroadcastLiveMove?.invoke(currentScore, 0, 0, false, true)
                    ghostDuelEngine.concludeMatchWithDefeat(currentScore)
                } else if (isAdventureModeActive) {
                    onStageDefeat?.invoke()
                } else {
                    onGameOver?.invoke()
                }
            }
        }
    }

    private fun canAnyPieceBePlaced() = com.example.gridsurge.game.engine.PlacementSafetyEngine.canAnyPieceBePlaced(
        dockShapes, engine.getGridArray(), if (isAdventureModeActive) adventureBoard.grid else null, if (isAdventureModeActive) progressionEngine.hazardGrid else null
    )

    private fun canPieceFitOnGrid(offsets: List<PolyOffset>): Boolean {
        for (r in 0..7) for (c in 0..7) {
            var fits = true
            for (o in offsets) {
                val br = r + o.y; val bc = c + o.x
                if (bc !in 0..7 || br !in 0..7 || engine.getGridValue(bc, br) != 0 || progressionEngine.hazardGrid[br][bc].hazardType == AdventureHazardType.EMP_LOCK) {
                    fits = false
                    break
                }
            }
            if (fits) return true
        }
        return false
    }

    private fun handleWarpBlockDetonation(c: Int, r: Int, color: Int) {
        if (isAdventureModeActive) {
            adventureBoard.isAnimationDeferred = true
        }

        val targets = engine.resolveWarpDetonation(c, r)
        var totalDetonationPoints = 0
        relicActivationsCountThisStage++

        val sourceTilesForVfx = mutableListOf<Triple<Float, Float, Int>>()

        targets.forEach { t ->
            val cellCenterX = boardRect.left + (t.col + 0.5f) * currentCellSizePx
            val cellCenterY = boardRect.top + (t.row + 0.5f) * currentCellSizePx

            if (t.effect == DetonationEffect.STRAIN) {
                val wasDestroyed = if (isAdventureModeActive) {
                    adventureBoard.damageCore(t.row, t.col, elapsedSeconds, isWarp = true)
                } else {
                    engine.damageCore(t.row * 8 + t.col)
                }

                if (wasDestroyed) {
                    sourceTilesForVfx.add(Triple(cellCenterX, cellCenterY, Color.parseColor("#00E5FF")))
                    totalDetonationPoints += 1000
                    engine.setGridValue(t.col, t.row, 0)
                    engine.setCellColor(t.col, t.row, 0)
                } else {
                    totalDetonationPoints += 500
                }
            } else {
                val blockColor = engine.getCellColor(t.col, t.row)
                sourceTilesForVfx.add(Triple(cellCenterX, cellCenterY, if (blockColor != 0) blockColor else Color.CYAN))
                engine.setGridValue(t.col, t.row, 0)
                engine.setCellColor(t.col, t.row, 0)
                totalDetonationPoints += 50
            }
        }

        val targetPixelX = boardRect.left + (c + 0.5f) * currentCellSizePx
        val targetPixelY = boardRect.top + (r + 0.5f) * currentCellSizePx

        warpVortexFx.triggerWarpImplosion(targetPixelX, targetPixelY, sourceTilesForVfx, animationTimeMs)

        if (totalDetonationPoints > 0) {
            currentScore += totalDetonationPoints
            engine.score = currentScore
            juiceCoordinator.spawnPopup(
                targetPixelX, targetPixelY,
                "+$totalDetonationPoints", Color.parseColor("#FFEA80FC"), animationTimeMs
            )
            onScoreChanged?.invoke(currentScore, engine.comboManager.currentStreak)
        }

        warpController.startImplosion(
            c, r, targetPixelX, targetPixelY,
            interactionHandler.cellSize, interactionHandler.cellSpacing,
            engine, color, activeThemeKey
        )
        juiceCoordinator.triggerShake(0.5f)
    }

    var onRelicConsumed: (() -> Unit)? = null

    private fun handleNovaCoreExplosion(c: Int, r: Int) {
        var totalDetonationPoints = 0
        relicActivationsCountThisStage++

        // Refund +15 seconds
        matchTimer.refundSeconds(15)

        for (dy in -1..1) {
            for (dx in -1..1) {
                val targetCol = c + dx
                val targetRow = r + dy

                if (targetCol in 0..7 && targetRow in 0..7) {
                    val targetPixelX = boardRect.left + (targetCol + 0.5f) * currentCellSizePx
                    val targetPixelY = boardRect.top + (targetRow + 0.5f) * currentCellSizePx

                    val isCore = if (isAdventureModeActive) {
                        adventureBoard.grid[targetRow][targetCol].isCore
                    } else false

                    if (isCore) {
                        val wasDestroyed = adventureBoard.damageCore(targetRow, targetCol, elapsedSeconds, isWarp = false)
                        if (wasDestroyed) {
                            totalDetonationPoints += 1000
                            engine.setGridValue(targetCol, targetRow, 0)
                            engine.setCellColor(targetCol, targetRow, 0)
                            juiceCoordinator.spawnBurstParticles(targetPixelX, targetPixelY, Color.parseColor("#00E5FF"), 30)
                        } else {
                            totalDetonationPoints += 500
                            juiceCoordinator.spawnBurstParticles(targetPixelX, targetPixelY, Color.CYAN, 15)
                        }
                    } else {
                        val existingVal = engine.getGridValue(targetCol, targetRow)
                        if (existingVal != 0) {
                            totalDetonationPoints += 100
                            engine.setGridValue(targetCol, targetRow, 0)
                            engine.setCellColor(targetCol, targetRow, 0)
                            juiceCoordinator.spawnBurstParticles(targetPixelX, targetPixelY, Color.parseColor("#00E5FF"), 20)
                        }
                    }
                }
            }
        }

        val centerPixelX = boardRect.left + (c + 0.5f) * currentCellSizePx
        val centerPixelY = boardRect.top + (r + 0.5f) * currentCellSizePx

        if (totalDetonationPoints > 0) {
            currentScore += totalDetonationPoints
            engine.score = currentScore
            juiceCoordinator.spawnPopup(
                centerPixelX, centerPixelY,
                "+$totalDetonationPoints // NOVA EXPLOSION", Color.parseColor("#00E5FF"), animationTimeMs
            )
            onScoreChanged?.invoke(currentScore, engine.comboManager.currentStreak)
        } else {
            juiceCoordinator.spawnPopup(
                centerPixelX, centerPixelY,
                "+15s // NOVA CLEANSE", Color.parseColor("#00E5FF"), animationTimeMs
            )
        }

        SfxManager.playSfx(SfxType.MEGA_BLITZ)
        SfxManager.playVox(VoxAction.OVERDRIVE)
        juiceCoordinator.triggerShake(0.6f)
        onRelicConsumed?.invoke()

        if (isAdventureModeActive) {
            adventureBoard.onMoveCommitted(elapsedSeconds)
            adventureBoard.checkVictoryConditions(elapsedSeconds)
        }
        checkGameOverOrVictory()
    }

    private fun windowToLocalCoords(x: Float, y: Float): Pair<Float, Float> {
        val location = IntArray(2)
        getLocationInWindow(location)
        return Pair(x - location[0], y - location[1])
    }

    fun startRelicDrag(windowX: Float, windowY: Float) {
        val (localX, localY) = windowToLocalCoords(windowX, windowY)
        val activeAbility = relicManager?.relicState?.value?.abilityType ?: RelicAbilityType.NONE
        val specialType = when (activeAbility) {
            RelicAbilityType.SOLAR_CROSS_LASER -> SpecialBlockType.CATALYST_CROSSHAIR
            else -> SpecialBlockType.QUANTUM_WARP_VORTEX // Image #2 Void Singularity
        }
        interactionHandler.startExternalDrag(localX, localY, PolyominoCatalog.instantiateSpecial(specialType))
    }

    fun updateRelicDrag(windowX: Float, windowY: Float) {
        val (localX, localY) = windowToLocalCoords(windowX, windowY)
        interactionHandler.updateExternalDrag(localX, localY)
    }

    fun endRelicDrag() {
        interactionHandler.endExternalDrag()
    }

    fun cancelRelicDrag() {
        interactionHandler.cancelExternalDrag()
    }

    private fun finalizeWarpDrop(gx: Int, gy: Int) {
        if (isAdventureModeActive) {
            adventureBoard.isAnimationDeferred = false
            adventureBoard.checkVictoryConditions(elapsedSeconds)
        }
        checkGameOverOrVictory()
    }

    private fun spawnShatterVfx(cell: GridCell) {
        val type = if (isGlitchModeActive) SpriteVfxType.GLITCH_DETONATE else SpriteVfxType.CORE_SHATTER_CYBER
        val rect = RectF(); calculateCellRect(cell.row, cell.col, interactionHandler.cellSize, interactionHandler.cellSpacing, boardRect, rect)
        spriteVfxEngine.spawnVfx(type, rect, animationTimeMs)
        juiceCoordinator.spawnBurstParticles(rect.centerX(), rect.centerY(), if (isGlitchModeActive) Color.parseColor("#FFD600") else Color.CYAN, 25)
        juiceCoordinator.triggerShake(0.3f)
    }

    private fun calculateCellRect(r: Int, c: Int, s: Float, sp: Float, b: RectF, out: RectF) {
        val left = b.left + sp + c * (s + sp); val top = b.top + sp + r * (s + sp)
        out.set(left, top, left + s, top + s)
    }

    private fun notifyAdventureState() {
        val authoritativeLines = if (isAdventureModeActive) adventureBoard.linesClearedThisStage else classicController.linesClearedTotal
        val authoritativeMovesRemaining = if (isAdventureModeActive) adventureBoard.movesRemaining else movesPlayedThisStage
        onAdventureStateUpdated?.invoke(
            authoritativeMovesRemaining,
            currentScore,
            authoritativeLines,
            adventureBoard.totalPurgedThisStage,
            isObjectiveMet,
            adventureBoard.bossHp,
            elapsedSeconds,
            adventureBoard.activeCoresRemaining,
            glitchEngine.totalPurgedCount,
            adventureBoard.synthesisCount,
            adventureBoard.maxStreakReached,
            runState.resonanceEnergy,
            engine.getOccupiedRatio(),
            lastDropPxX,
            lastDropPxY
        )
    }

    private fun checkPurityState(newPurity: Float) {
        if (newPurity <= 0.05f && meltdownJob == null) {
            trauma = 0.85f
            SfxManager.playSfx(SfxType.EMP_SHOCKWAVE, overridePitch = 0.5f)
            SfxManager.playVox(VoxAction.GRID_CRITICAL)

            meltdownJob = mainScope.launch {
                for (i in 3 downTo 1) {
                    trauma = 0.7f
                    delay(1000L)
                }

                if (glitchEngine.purity <= 0.05f) {
                    isEnginePaused = true
                    isTouchLocked = true
                    SfxManager.playSfx(SfxType.SYSTEM_OFFLINE)
                    onGameOver?.invoke()
                } else {
                    meltdownJob = null
                }
            }
        }
    }

    private fun handleVictory(elapsedSec: Int) {
        isTouchLocked = true
        isObjectiveMet = true
        matchTimer.stop()

        mainScope.launch {
            val unusedMoves = adventureBoard.movesRemaining
            if (unusedMoves > 0) {
                for (m in unusedMoves downTo 1) {
                    delay(90L)
                    val bonus = 250L
                    currentScore += bonus
                    engine.score = currentScore
                    adventureBoard.movesRemaining = m - 1
                    notifyAdventureState()

                    trauma = 0.35f
                    SfxManager.playSfx(SfxType.SNAP_TICK, overridePitch = 1.0f + (unusedMoves - m) * 0.05f)
                    juiceCoordinator.spawnPopup(
                        boardRect.centerX(), boardRect.centerY(),
                        "+$bonus OVERDRIVE", Color.parseColor("#00FF66"), animationTimeMs
                    )
                }
                delay(400L)
            }

            isTouchLocked = false
            matchPhase = MatchPhase.STAGE_COMPLETED
            val levelNum = adventureBoard.activeBlueprint?.levelNumber ?: currentAdventureLevelNumber
            val benchmark = AdventureSectorRegistry.getBenchmark(levelNum)
            val telemetry = MatchTelemetrySnapshot(
                true, movesPlayedThisStage, elapsedSec, currentScore,
                engine.comboManager.currentStreak, maxSimultaneousLinesCleared,
                relicActivationsCountThisStage, empJamOccurredThisStage
            )
            val evaluation = StarRatingEvaluator.evaluateMatch(benchmark, telemetry)
            onStageVictoryEvaluated?.invoke(levelNum, currentScore, evaluation, elapsedSec)
        }
    }

    private fun handleDuelFinished(w: Boolean, p: Long, r: Long) {
        isTouchLocked = true
        isEnginePaused = true
        val replay = MatchTelemetryRecorder.finishSession(
            finalPlayerScore = p,
            finalRivalScore = r,
            matchDurationSec = 75
        )
        onClashFinished?.invoke(
            w,
            p,
            r,
            if (w) 50 else 15,
            maxComboInMatch,
            totalLinesInMatch,
            rebootStrikeCount,
            replay
        )
    }

    private fun handleDuelTimer(t: Int) {
        if (t != elapsedSeconds) {
            elapsedSeconds = t
            if (isClashModeActive) {
                notifyAdventureState()
            }
        }
    }

    fun hasActiveClashJammer(): Boolean {
        for (r in 0..7) {
            for (c in 0..7) {
                if (progressionEngine.hazardGrid[r][c].hazardType == AdventureHazardType.EMP_LOCK) {
                    return true
                }
            }
        }
        return false
    }

    fun applyClashJammerToBoard() {
        // MAX 1 ACTIVE JAMMER RULE: If an EMP Lock Jammer is already active on the board, skip spawning additional Jammers
        if (hasActiveClashJammer()) return

        empJamOccurredThisStage = true

        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0..7) {
            for (c in 0..7) {
                if (engine.getGridValue(c, r) == 0 &&
                    progressionEngine.hazardGrid[r][c].hazardType == AdventureHazardType.NONE) {
                    emptyCells.add(Pair(c, r))
                }
            }
        }

        if (emptyCells.isNotEmpty()) {
            val (jamCol, jamRow) = emptyCells.random()
            progressionEngine.hazardGrid[jamRow][jamCol] = HazardCellState(hazardType = AdventureHazardType.EMP_LOCK)

            SfxManager.playSfx(SfxType.EMP_SHOCKWAVE)
            SfxManager.playSfx(SfxType.STASIS_FIELD)
            trauma = 0.65f

            val jamPxX = boardRect.left + (jamCol + 0.5f) * currentCellSizePx
            val jamPxY = boardRect.top + (jamRow + 0.5f) * currentCellSizePx
            juiceCoordinator.spawnPopup(jamPxX, jamPxY, "STASIS JAMMED!", Color.RED, animationTimeMs)
        }
    }

    private fun triggerClashJammer(s: Int) {
        applyClashJammerToBoard()
    }

    private fun logRivalMove(s: Long, c: Int) {
        MatchTelemetryRecorder.logRivalMove(
            slotIndex = 0,
            shapeId = 1,
            targetRow = 0,
            targetCol = 0,
            occupiedOffsets = emptyList(),
            colorInt = Color.RED,
            linesCleared = 0,
            scoreAfterMove = s,
            comboStreak = c
        )
    }

    fun resumeEngine() {
        isEnginePaused = false
        lastFrameTime = System.nanoTime()
        lastRealTimeMs = SystemClock.uptimeMillis()
        matchTimer.resume()
        postInvalidateOnAnimation()
    }
    fun pauseEngine() { isEnginePaused = true; matchTimer.pause(); invalidate() }
    fun setTheme(t: String) { activeThemeKey = ThemeNormalizer.normalize(t) }
    
    fun startAdventureMatch(levelSpec: LevelNodeSpec) {
        isAdventureModeActive = true
        isGlitchModeActive = false
        isTimeBlitzModeActive = false
        isClashModeActive = false
        isEnginePaused = false
        isObjectiveMet = false
        matchPhase = MatchPhase.IN_PROGRESS

        currentAdventureLevelNumber = levelSpec.levelNumber
        movesPlayedThisStage = 0
        currentScore = 0L
        elapsedSeconds = 0
        maxSimultaneousLinesCleared = 0
        relicActivationsCountThisStage = 0
        empJamOccurredThisStage = false

        runState.isBossActive = levelSpec.isBossLevel
        engine.resetGame()
        val bp = AdventureSectorRegistry.getLevelBlueprint(levelSpec.levelNumber)
        adventureBoard.loadBlueprint(bp)
        bossEngine.initializeBoss(levelSpec.sectorIndex, levelSpec.isBossLevel)
        progressionEngine.initializeStage(bp, levelSpec.isBossLevel)

        (adventureController.spawner as? com.example.gridsurge.features.adventure.engine.AdventurePieceSpawner)?.apply {
            currentLevelNumber = levelSpec.levelNumber
            reset()
        }

        matchTimer.reset()
        matchTimer.start()
        matchElapsedAccSec = 0f
        elapsedSeconds = 0
        notifyAdventureState()
        replenishDock()
        postInvalidateOnAnimation()
    }

    fun startAdventureLevel(blueprint: AdventureLevelBlueprint) {
        ModalOrchestrator.clearAll()
        // --- 0. Pre-Transition Surface Flush ---
        isEnginePaused = true
        invalidate() // Force a final frame if needed
        
        isAdventureModeActive = true
        isGlitchModeActive = false
        isTimeBlitzModeActive = false
        isClashModeActive = false
        isTouchLocked = false
        isObjectiveMet = false
        hasUsedReviveThisRun = false
        matchPhase = MatchPhase.IN_PROGRESS

        currentAdventureLevelNumber = blueprint.levelNumber
        movesPlayedThisStage = 0
        currentScore = 0L
        matchElapsedAccSec = 0f
        elapsedSeconds = 0
        maxSimultaneousLinesCleared = 0
        relicActivationsCountThisStage = 0
        empJamOccurredThisStage = false

        // 1. Explicit Engine & Board Purge (Removes saturated Classic Grid)
        engine.resetGame()
        runState.reset()
        runState.isBossActive = blueprint.levelNumber % 9 == 0
        vfxPool.clearAll()
        juiceFx.clearAll()
        spriteVfxEngine.clearAll()
        scorePopupManager.clearAll()
        warpVortexFx.clearAll()

        // 2. Load Adventure Blueprint & Cores
        adventureBoard.loadBlueprint(blueprint)
        bossEngine.initializeBoss(blueprint.sectorId, blueprint.levelNumber % 9 == 0)
        progressionEngine.initializeStage(blueprint, blueprint.levelNumber % 9 == 0)

        // 3. Configure Adventure Spawner
        (adventureController.spawner as? com.example.gridsurge.features.adventure.engine.AdventurePieceSpawner)?.apply {
            currentLevelNumber = blueprint.levelNumber
            reset()
        }

        // 4. Start Timer & Populate Fresh Trays
        matchTimer.reset()
        matchTimer.start()
        notifyAdventureState()
        replenishDock()
        
        isEnginePaused = false
        postInvalidateOnAnimation()
    }

    fun advanceToNextAdventureLevel() {
        val nextLevelNumber = currentAdventureLevelNumber + 1
        val nextBp = AdventureSectorRegistry.getLevelBlueprint(nextLevelNumber)
        startAdventureLevel(nextBp)
    }

    fun startClassicMatch() {
        ModalOrchestrator.clearAll()
        isAdventureModeActive = false
        isGlitchModeActive = false
        isTimeBlitzModeActive = false
        isClashModeActive = false
        isEnginePaused = false
        isTouchLocked = false
        isObjectiveMet = false
        hasUsedReviveThisRun = false
        matchPhase = MatchPhase.IN_PROGRESS

        val seed = System.currentTimeMillis()
        MatchTelemetryRecorder.startSession(seed, "CLASSIC")

        currentScore = 0L
        movesPlayedThisStage = 0
        matchElapsedAccSec = 0f
        elapsedSeconds = 0
        engine.resetGame()
        vfxPool.clearAll()
        juiceFx.clearAll()
        spriteVfxEngine.clearAll()
        scorePopupManager.clearAll()

        classicController.initializeMatch()

        matchTimer.reset()
        matchTimer.start()
        notifyAdventureState()

        replenishDock()
        postInvalidateOnAnimation()
    }

    fun startTimeBlitzMatch() {
        ModalOrchestrator.clearAll()
        isTimeBlitzModeActive = true
        isAdventureModeActive = false
        isGlitchModeActive = false
        isClashModeActive = false
        isEnginePaused = false
        isTouchLocked = false
        isObjectiveMet = false
        hasUsedReviveThisRun = false

        val seed = System.currentTimeMillis()
        MatchTelemetryRecorder.startSession(seed, "TIME_BLITZ")

        lastFrameTime = System.nanoTime()
        lastRealTimeMs = SystemClock.uptimeMillis()

        currentScore = 0L
        movesPlayedThisStage = 0
        matchElapsedAccSec = 0f
        elapsedSeconds = 90
        engine.resetGame()
        blitzController.reset()
        vfxPool.clearAll()
        juiceFx.clearAll()
        spriteVfxEngine.clearAll()
        notifyAdventureState()
        replenishDock()
        postInvalidateOnAnimation()
    }

    fun startGlitchMode() {
        com.example.gridsurge.game.ui.ModalOrchestrator.clearAll()
        isGlitchModeActive = true
        isAdventureModeActive = false
        isTimeBlitzModeActive = false
        isClashModeActive = false
        isEnginePaused = false
        isTouchLocked = false
        isObjectiveMet = false
        hasUsedReviveThisRun = false

        val seed = System.currentTimeMillis()
        MatchTelemetryRecorder.startSession(seed, "DAILY_GLITCH")

        currentScore = 0L
        movesPlayedThisStage = 0
        matchElapsedAccSec = 0f
        elapsedSeconds = 0
        engine.resetGame()
        glitchController.spawner.reset()
        vfxPool.clearAll()
        juiceFx.clearAll()
        spriteVfxEngine.clearAll()
        matchTimer.reset()
        matchTimer.start()
        notifyAdventureState()
        replenishDock()
        postInvalidateOnAnimation()
    }

    fun deployEmpSurgeRevive() {
        hasUsedReviveThisRun = true
        isTouchLocked = false
        isEnginePaused = false
        if (isAdventureModeActive) {
            for (r in 2..5) {
                for (c in 2..5) {
                    val cell = adventureBoard.grid[r][c]
                    if (!cell.isCore) {
                        cell.isFilled = false
                        cell.blockColor = 0
                        engine.setGridValue(c, r, 0)
                        engine.setCellColor(c, r, 0)
                    } else {
                        // Cores remain intact and synced
                        engine.setGridValue(c, r, cell.toCellTypeValue())
                    }
                }
            }
            replenishDock()
        } else {
            // Purely reactive call: Delegate game state mutation & tray re-roll to controller
            val freshTray = classicController.executeEmpRevive()
            for (i in 0..2) {
                dockShapes[i] = freshTray.getOrNull(i)
                engine.dock[i] = dockShapes[i]
            }
            syncDockFromEngine()
        }
        matchTimer.resume()
        postInvalidateOnAnimation()
    }

    fun quickRestartMatch() {
        if (isAdventureModeActive && adventureBoard.activeBlueprint != null) {
            startAdventureLevel(adventureBoard.activeBlueprint!!)
        } else if (isTimeBlitzModeActive) {
            startTimeBlitzMatch()
        } else if (isGlitchModeActive) {
            startGlitchMode()
        } else {
            startClassicMatch()
        }
    }
    
    fun startBlitzClashDuel(rivalReplay: MatchReplayData? = null) {
        ModalOrchestrator.clearAll()
        isClashModeActive = true
        isAdventureModeActive = false
        isGlitchModeActive = false
        isTimeBlitzModeActive = false
        isEnginePaused = false
        isTouchLocked = false
        isObjectiveMet = false
        hasUsedReviveThisRun = false
        matchPhase = MatchPhase.IN_PROGRESS

        currentScore = 0L
        movesPlayedThisStage = 0
        elapsedSeconds = 75

        engine.resetGame()
        vfxPool.clearAll()
        juiceFx.clearAll()
        spriteVfxEngine.clearAll()
        scorePopupManager.clearAll()

        val seed = System.currentTimeMillis()
        MatchTelemetryRecorder.startSession(seed, "BLITZ_CLASH")
        ghostDuelEngine.reset()
        ghostDuelEngine.startDuel(seed, rivalReplay)

        replenishDock()
        postInvalidateOnAnimation()
    }

    private var livePieceStream: DeterministicPieceStream? = null

    /**
     * STEP 1: Pre-populates the board and tray in RAM while the countdown modal is STILL SHOWN.
     * Eliminates the 5-10 second empty tray lag entirely.
     */
    fun prepareLiveClashDuel(
        sharedSeed: Long,
        startEpochMs: Long = 0L,
        totalSeconds: Int = 90
    ) {
        ModalOrchestrator.clearAll()
        isClashModeActive = true
        isAdventureModeActive = false
        isGlitchModeActive = false
        isTimeBlitzModeActive = false

        // Hold engine in stasis until countdown finishes
        isEnginePaused = true
        isTouchLocked = true
        isObjectiveMet = false
        hasUsedReviveThisRun = false
        matchPhase = MatchPhase.IN_PROGRESS

        currentScore = 0L
        movesPlayedThisStage = 0
        elapsedSeconds = totalSeconds
        liveStartEpochMs = startEpochMs

        // SAFE: Zero-argument reset
        ghostDuelEngine.reset()

        // Wipe previous game state & hazards
        engine.resetGame()
        progressionEngine.clearHazards()
        rebootStrikeCount = 0
        maxComboInMatch = 1
        totalLinesInMatch = 0
        vfxPool.clearAll()
        juiceFx.clearAll()
        spriteVfxEngine.clearAll()
        scorePopupManager.clearAll()

        // Start Telemetry Recording Session for Live Match Replay
        MatchTelemetryRecorder.startSession(sharedSeed, "BLITZ_CLASH")

        // Initialize Deterministic Stream with Server Seed
        livePieceStream = DeterministicPieceStream(sharedSeed)

        // PRE-POPULATE DOCK PIECES IMMEDIATELY
        replenishLiveSeededDock()

        // Force immediate layout invalidation so pieces are ready on GPU
        invalidate()
    }

    /**
     * STEP 2: Launches the live clock and unlocks touch the exact millisecond countdown hits 0.
     */
    fun launchLiveClashDuel(
        sharedSeed: Long,
        startEpochMs: Long = System.currentTimeMillis(),
        totalSeconds: Int = 90
    ) {
        liveStartEpochMs = startEpochMs
        ghostDuelEngine.reset()
        ghostDuelEngine.startLiveDuel(seed = sharedSeed, startEpochMs = startEpochMs, totalSeconds = totalSeconds)

        // Unlock board for active combat
        isTouchLocked = false
        isEnginePaused = false
        lastFrameTime = System.nanoTime()
        lastRealTimeMs = SystemClock.uptimeMillis()

        postInvalidateOnAnimation()
    }

    /**
     * Starts an authoritative LIVE 1v1 Clash Match against a real player.
     */
    fun startLiveClashDuel(
        sharedSeed: Long,
        matchDurationSec: Int = 90
    ) {
        prepareLiveClashDuel(sharedSeed = sharedSeed, totalSeconds = matchDurationSec)
        launchLiveClashDuel(sharedSeed, totalSeconds = matchDurationSec)
    }

    /**
     * Ingests live telemetry broadcast from opponent's device.
     */
    fun onRemoteCombatTelemetryReceived(
        remoteScore: Long,
        remoteLines: Int,
        remoteCombo: Int,
        isFever: Boolean,
        isTko: Boolean
    ) {
        ghostDuelEngine.onLiveRivalTelemetryReceived(remoteScore, remoteLines, remoteCombo, isFever, isTko)

        // Record live rival move in replay recorder
        MatchTelemetryRecorder.logRivalMove(
            slotIndex = 0,
            shapeId = 1,
            targetRow = 0,
            targetCol = 0,
            occupiedOffsets = emptyList(),
            colorInt = Color.RED,
            linesCleared = remoteLines,
            scoreAfterMove = remoteScore,
            comboStreak = remoteCombo
        )

        if (remoteLines >= 2) {
            val rivalHudX = width * 0.82f
            val rivalHudY = 32f * density
            renderer.clashAttackEmitter.spawnAttackVolley(
                originX = rivalHudX,
                originY = rivalHudY,
                targetX = boardRect.centerX(),
                targetY = boardRect.centerY(),
                lineCount = remoteLines
            )
            trauma = (trauma + 0.40f).coerceAtMost(1.0f)
            SfxManager.playSfx(SfxType.SNAP_TICK, overridePitch = 0.8f)
        }

        if (remoteLines >= 4 || remoteCombo >= 5) {
            applyClashJammerToBoard()
        }

        if (isTko) {
            isTouchLocked = true
            isEnginePaused = true
            SfxManager.playSfx(SfxType.LEVEL_COMPLETE, overridePitch = 1.3f)
            ghostDuelEngine.concludeMatchWithVictory(currentScore)
        }

        postInvalidateOnAnimation()
    }

    /**
     * Deterministically populates the dock from the shared PRNG seed.
     */
    private fun replenishLiveSeededDock() {
        val stream = livePieceStream ?: run {
            replenishDock()
            return
        }

        val trio = stream.nextTrayTrioShapes()
        for (i in 0 until 3) {
            dockShapes[i] = trio[i]
            engine.dock[i] = trio[i]
        }
        syncDockFromEngine()
    }
    
    fun executeRelicCyberWareAbility(a: RelicAbilityType) {
        when (a) {
            RelicAbilityType.CHRONO_BURST -> {
                matchTimer.refundSeconds(15)
                dockShapes[2] = PolyominoCatalog.instantiateSpecial(SpecialBlockType.QUANTUM_WARP_VORTEX)
                engine.dock[2] = dockShapes[2]
                syncDockFromEngine()

                SfxManager.playSfx(SfxType.OVERDRIVE_ACTIVATE)
                SfxManager.playVox(VoxAction.OVERDRIVE)
                juiceCoordinator.spawnPopup(
                    boardRect.centerX(), boardRect.centerY(),
                    "+15s // NOVA CORE READY", Color.parseColor("#00E5FF"), animationTimeMs
                )
                juiceCoordinator.triggerShake(0.4f)
                postInvalidateOnAnimation()
            }
            RelicAbilityType.SOLAR_CROSS_LASER -> {
                dockShapes[2] = PolyominoCatalog.instantiateSpecial(SpecialBlockType.CATALYST_CROSSHAIR)
                engine.dock[2] = dockShapes[2]
                syncDockFromEngine()

                SfxManager.playSfx(SfxType.OVERDRIVE_ACTIVATE)
                SfxManager.playVox(VoxAction.OVERDRIVE)
                juiceCoordinator.spawnPopup(
                    boardRect.centerX(), boardRect.centerY(),
                    "CROSS-LASER READY", Color.parseColor("#FFD600"), animationTimeMs
                )
                juiceCoordinator.triggerShake(0.4f)
                postInvalidateOnAnimation()
            }
            RelicAbilityType.WARP_INJECTION -> {
                runState.addResonance(50f)
                dockShapes[2] = PolyominoCatalog.instantiateSpecial(SpecialBlockType.QUANTUM_WARP_VORTEX)
                engine.dock[2] = dockShapes[2]
                syncDockFromEngine()

                SfxManager.playSfx(SfxType.OVERDRIVE_ACTIVATE)
                SfxManager.playVox(VoxAction.OVERDRIVE)
                juiceCoordinator.spawnPopup(
                    boardRect.centerX(), boardRect.centerY(),
                    "QUANTUM WARP INJECTED", Color.parseColor("#FF0055"), animationTimeMs
                )
                juiceCoordinator.triggerShake(0.4f)
                postInvalidateOnAnimation()
            }
            RelicAbilityType.SLAG_TRANSMUTATION -> {
                var transmutedCount = 0
                for (r in 0 until 8) {
                    for (c in 0 until 8) {
                        val cell = adventureBoard.grid[r][c]
                        if (cell.isFilled && !cell.isCore) {
                            cell.isFilled = false
                            cell.blockColor = 0
                            engine.setGridValue(c, r, CellType.EMPTY.id)
                            engine.setCellColor(c, r, 0)
                            transmutedCount++
                        }
                    }
                }
                SfxManager.playSfx(SfxType.OVERDRIVE_ACTIVATE)
                SfxManager.playVox(VoxAction.OVERDRIVE)
                juiceCoordinator.spawnPopup(
                    boardRect.centerX(), boardRect.centerY(),
                    "BIO TRANSMUTATION // $transmutedCount TILES TRANSMUTED", Color.parseColor("#00FF66"), animationTimeMs
                )
                juiceCoordinator.triggerShake(0.5f)
                postInvalidateOnAnimation()
            }
            RelicAbilityType.SUPERNOVA_IMPLOSION -> {
                var purgedCount = 0
                for (r in 2..5) {
                    for (c in 2..5) {
                        val cell = adventureBoard.grid[r][c]
                        if (!cell.isCore && cell.isFilled) {
                            cell.isFilled = false
                            cell.blockColor = 0
                            engine.setGridValue(c, r, CellType.EMPTY.id)
                            engine.setCellColor(c, r, 0)
                            purgedCount++
                        }
                    }
                }
                SfxManager.playSfx(SfxType.OVERDRIVE_ACTIVATE)
                SfxManager.playVox(VoxAction.OVERDRIVE)
                juiceCoordinator.spawnPopup(
                    boardRect.centerX(), boardRect.centerY(),
                    "SUPERNOVA IMPLOSION // MATRIX CLEANSED", Color.parseColor("#EA80FC"), animationTimeMs
                )
                juiceCoordinator.triggerShake(0.6f)
                postInvalidateOnAnimation()
            }
            else -> {}
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mainScope.cancel()
    }
}
