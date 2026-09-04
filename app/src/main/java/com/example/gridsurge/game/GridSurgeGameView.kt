package com.example.gridsurge.game

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.util.AttributeSet
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
import com.example.gridsurge.game.blitz.BlitzState
import com.example.gridsurge.game.blitz.TimeBlitzEngine
import com.example.gridsurge.game.fx.*
import com.example.gridsurge.game.glitch.GlitchEngine
import com.example.gridsurge.game.glitch.SeededGlitchMatchController
import com.example.gridsurge.game.model.*
import com.example.gridsurge.game.particle.CyberParticleSystem
import com.example.gridsurge.game.render.*
import com.example.gridsurge.features.adventure.rendering.*
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
    private val blitzController = BlitzModeController(engine, blitzEngine, juiceCoordinator)
    private val glitchController = GlitchModeController(engine, glitchEngine, juiceCoordinator)
    private val classicController = ClassicModeController(engine, juiceCoordinator)

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
    var currentScore: Long = 0L
    var movesPlayedThisStage = 0
    var elapsedSeconds = 0
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
    private val ghostDuelEngine = GhostDuelEngine(mainScope, { postInvalidateOnAnimation() }, { if(it) SfxManager.playVox(VoxAction.LEAD_SECURED) else SfxManager.playVox(VoxAction.LEAD_LOST) }, { w, p, r -> handleDuelFinished(w, p, r) }, { t -> handleDuelTimer(t) }, { s -> triggerClashJammer(s) }, { s, c -> logRivalMove(s, c) })

    var hasUsedReviveThisRun = false
    var currentAdventureLevelNumber: Int = 1
    val activeBlueprint: AdventureLevelBlueprint? get() = adventureBoard.activeBlueprint
    val currentRivalScore: Long get() = ghostDuelEngine.rivalScore
    val currentDuelRemainingSeconds: Int get() = ghostDuelEngine.matchSecondsRemaining

    private var lastDropPxX: Float = 0f
    private var lastDropPxY: Float = 0f

    // --- Reactive Callbacks ---
    var onAdventureStateUpdated: ((Int, Long, Int, Int, Boolean, Int, Int, Int, Int, Int, Int, Float, Float, Float, Float) -> Unit)? = null
    var onScoreChanged: ((Long, Int) -> Unit)? = null
    var onLinesCleared: ((Int) -> Unit)? = null
    var onGameOver: (() -> Unit)? = null
    var onStageVictoryEvaluated: ((Int, Long, StarEvaluationResult, Int) -> Unit)? = null
    var onStageDefeat: (() -> Unit)? = null
    var onClashFinished: ((Boolean, Long, Long, Int, Int, Int, MatchReplayData) -> Unit)? = null
    var onMissionEvent: ((QuestType, Int) -> Unit)? = null

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
        val availableWidth = w - horizontalMargin * 2f

        // Position 8x8 grid lower to fill the dark space below header (Yellow Dot placement)
        val topMargin = (h * 0.165f).coerceIn(120f * density, 150f * density)

        interactionHandler.cellSize = (availableWidth - (3.5f * density * 9)) / 8f
        currentCellSizePx = interactionHandler.cellSize.toInt()
        interactionHandler.cellSpacing = 3.5f * density
        interactionHandler.boardRect.set(horizontalMargin, topMargin, horizontalMargin + availableWidth, topMargin + availableWidth)
        boardRect.set(interactionHandler.boardRect)

        juiceCoordinator.boardRect.set(boardRect)
        juiceCoordinator.cellSize = interactionHandler.cellSize
        juiceCoordinator.cellSpacing = interactionHandler.cellSpacing

        // Position floating dock pieces lower down closer to emitter pods (Red Arrow placement)
        val dockTop = boardRect.bottom + 65f * density
        val slotSpacing = 8f * density
        val slotWidth = (availableWidth - slotSpacing * 2) / 3f
        val slotHeight = 95f * density

        for (i in 0 until 3) {
            interactionHandler.dockSlotBounds[i].set(
                horizontalMargin + i * (slotWidth + slotSpacing),
                dockTop,
                horizontalMargin + i * (slotWidth + slotSpacing) + slotWidth,
                dockTop + slotHeight
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
        val dt = if (isEnginePaused) 0f else (System.nanoTime() - lastFrameTime) / 1_000_000_000f
        lastFrameTime = System.nanoTime()
        
        if (!isEnginePaused && dt > 0f) {
            vfxPool.update(dt)
            juiceFx.updateFrame(dt)
            interactionHandler.updatePhysics(dt)

            // Live Time Blitz countdown & state management
            if (isTimeBlitzModeActive) {
                val blitzState = blitzEngine.updateFrame(dt)
                val updatedSec = blitzEngine.secondsRemaining.toInt()
                if (updatedSec != elapsedSeconds) {
                    elapsedSeconds = updatedSec
                    notifyAdventureState()
                }
                if (blitzState == BlitzState.TIME_EXPIRED) {
                    isEnginePaused = true
                    onGameOver?.invoke()
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
                dt = dt
            )

            if (isClashModeActive) {
                clashHudRenderer.prepareDimensions(context)
                clashHudRenderer.renderDuelHud(
                    canvas = canvas,
                    anchorRect = boardRect,
                    playerScore = currentScore,
                    rivalScore = ghostDuelEngine.rivalScore,
                    secondsRemaining = ghostDuelEngine.matchSecondsRemaining,
                    rivalComboActive = ghostDuelEngine.rivalCombo > 1,
                    now = animationTimeMs
                )
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
            SfxManager.playPlacementSound(isSpecial = true)
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
            SfxManager.playPlacementSound(isSpecial = shape.specialType != SpecialBlockType.NONE)
            juiceCoordinator.onPiecePlaced(placedCoords, shape.color)
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
    override fun getHazardGrid() = if (isAdventureModeActive) progressionEngine.hazardGrid else null
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
        if (dockShapes.all { it == null }) replenishDock()
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
            
            val cx = boardRect.centerX()
            val cy = boardRect.centerY()
            juiceCoordinator.spawnPopup(cx, cy, "+${result.pointsEarned}", Color.CYAN, animationTimeMs)
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

        if (isTimeBlitzModeActive) currentScore = blitzController.processMove(result, engine.comboManager.currentStreak)
        if (isGlitchModeActive) glitchController.processMove(result)
        if (!isAdventureModeActive && !isTimeBlitzModeActive && !isGlitchModeActive) classicController.processMove(result)
        
        // Resonance Overclock: +25% score bonus while Warp is ready
        if (isAdventureModeActive && runState.isWarpReady && result.pointsEarned > 0) {
            val bonus = (result.pointsEarned * 0.25f).toInt()
            engine.score += bonus
        }

        currentScore = engine.score 
        if (isClashModeActive) {
            ghostDuelEngine.evaluateLead(currentScore)
        }
        onScoreChanged?.invoke(currentScore, engine.comboManager.currentStreak)
        
        // --- PRIORITY RESOLUTION: Check Win Condition FIRST ---
        if (isObjectiveMet) return 

        checkGameOverOrVictory()
    }

    private fun checkGameOverOrVictory() {
        if (isObjectiveMet) return // Already won, don't trigger Game Over

        if (!canAnyPieceBePlaced()) {
            isEnginePaused = true
            matchTimer.stop()
            SfxManager.playSfx(SfxType.SYSTEM_OFFLINE)
            ModalOrchestrator.clearAll()
            if (isAdventureModeActive) onStageDefeat?.invoke() else onGameOver?.invoke()
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
                if (bc !in 0..7 || br !in 0..7 || engine.getGridValue(bc, br) != 0 || (isAdventureModeActive && progressionEngine.hazardGrid[br][bc].hazardType == AdventureHazardType.EMP_LOCK)) {
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
        onAdventureStateUpdated?.invoke(0, currentScore, authoritativeLines, adventureBoard.totalPurgedThisStage, isObjectiveMet, adventureBoard.bossHp, elapsedSeconds, adventureBoard.activeCoresRemaining, glitchEngine.totalPurgedCount, adventureBoard.synthesisCount, adventureBoard.maxStreakReached, runState.resonanceEnergy, engine.getOccupiedRatio(), lastDropPxX, lastDropPxY)
    }

    private fun handleVictory(elapsedSec: Int) {
        isObjectiveMet = true
        matchPhase = MatchPhase.STAGE_COMPLETED
        matchTimer.stop()
        val levelNum = adventureBoard.activeBlueprint?.levelNumber ?: currentAdventureLevelNumber
        val benchmark = AdventureSectorRegistry.getBenchmark(levelNum)
        val telemetry = MatchTelemetrySnapshot(true, movesPlayedThisStage, elapsedSec, currentScore, engine.comboManager.currentStreak, maxSimultaneousLinesCleared, relicActivationsCountThisStage, empJamOccurredThisStage)
        val evaluation = StarRatingEvaluator.evaluateMatch(benchmark, telemetry)
        onStageVictoryEvaluated?.invoke(levelNum, currentScore, evaluation, elapsedSec)
    }

    private fun handleDuelFinished(w: Boolean, p: Long, r: Long) {
        isTouchLocked = true
        isEnginePaused = true
        val replay = MatchReplayData(
            matchId = "CLASH_${System.currentTimeMillis()}",
            matchSeed = ghostDuelEngine.matchSeed,
            gameMode = "BLITZ_CLASH",
            matchDurationSec = 75,
            finalPlayerScore = p,
            finalRivalScore = r,
            isVictory = w,
            playerMoves = emptyList(),
            rivalMoves = emptyList()
        )
        onClashFinished?.invoke(w, p, r, 0, 0, 0, replay)
    }

    private fun handleDuelTimer(t: Int) {
        if (t != elapsedSeconds) {
            elapsedSeconds = t
            if (isClashModeActive) {
                notifyAdventureState()
            }
        }
    }

    private fun triggerClashJammer(s: Int) {
        empJamOccurredThisStage = true
        SfxManager.playSfx(SfxType.EMP_SHOCKWAVE)
        juiceCoordinator.spawnPopup(boardRect.centerX(), boardRect.centerY(), "STASIS JAMMED!", Color.RED, animationTimeMs)
    }

    private fun logRivalMove(s: Long, c: Int) {}

    fun resumeEngine() { isEnginePaused = false; matchTimer.resume(); postInvalidateOnAnimation() }
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

        matchTimer.start()
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

        currentScore = 0L
        movesPlayedThisStage = 0
        elapsedSeconds = 0
        engine.resetGame()
        vfxPool.clearAll()
        juiceFx.clearAll()
        spriteVfxEngine.clearAll()
        scorePopupManager.clearAll()

        classicController.initializeMatch()

        matchTimer.reset()
        matchTimer.start()

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

        currentScore = 0L
        movesPlayedThisStage = 0
        engine.resetGame()
        blitzController.reset()
        vfxPool.clearAll()
        juiceFx.clearAll()
        spriteVfxEngine.clearAll()
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

        currentScore = 0L
        movesPlayedThisStage = 0
        engine.resetGame()
        glitchController.spawner.reset()
        vfxPool.clearAll()
        juiceFx.clearAll()
        spriteVfxEngine.clearAll()
        replenishDock()
        postInvalidateOnAnimation()
    }

    fun deployEmpSurgeRevive() {
        hasUsedReviveThisRun = true
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
            }
        }
        isEnginePaused = false
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
        ghostDuelEngine.reset()
        ghostDuelEngine.startDuel(seed, rivalReplay)

        replenishDock()
        postInvalidateOnAnimation()
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
