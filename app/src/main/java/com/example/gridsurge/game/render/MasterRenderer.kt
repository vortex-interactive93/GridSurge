package com.example.gridsurge.game.render

import android.content.Context
import android.graphics.*
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withSave
import com.example.gridsurge.features.adventure.model.*
import com.example.gridsurge.features.adventure.rendering.*
import com.example.gridsurge.game.blitz.BlitzState
import com.example.gridsurge.game.blitz.TimeBlitzEngine
import com.example.gridsurge.game.fx.*
import com.example.gridsurge.game.glitch.GlitchEngine
import com.example.gridsurge.game.glitch.InfectionPhase
import com.example.gridsurge.game.juice.DangerLevel
import com.example.gridsurge.game.model.*
import com.example.gridsurge.game.particle.CyberParticleSystem
import com.example.gridsurge.core.ComboState
import com.example.gridsurge.core.GridEngine
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

import com.example.gridsurge.game.engine.SpecialBlockSolver

data class MilestoneBannerState(
    var text: String,
    var color: Int,
    var startTimeMs: Long,
    val durationMs: Long,
    var isActive: Boolean
)

class MasterRenderer(
    private val context: Context,
    private val density: Float
) {
    // --- Paints ---
    private val scrimBackplatePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#F2040711".toColorInt()
        style = Paint.Style.FILL
    }
    private val scrimBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#1C2C4A".toColorInt()
        strokeWidth = 2.5f * density
        style = Paint.Style.STROKE
    }
    private val socketPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#0A0F1D".toColorInt()
        style = Paint.Style.FILL
    }
    private val socketBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#152238".toColorInt()
        strokeWidth = 1.5f * density
        style = Paint.Style.STROKE
    }
    private val crosshairPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#3300E5FF".toColorInt()
        strokeWidth = 1f * density
        style = Paint.Style.STROKE
    }
    private val gradientBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2.5f * density
        style = Paint.Style.STROKE
    }
    private val gradientBorderGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 6f * density
        style = Paint.Style.STROKE
        maskFilter = BlurMaskFilter(4f * density, BlurMaskFilter.Blur.NORMAL)
    }
    private val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val cellCornerTickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1f * density
        style = Paint.Style.STROKE
    }
    private val radarReticlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1.2f * density
        style = Paint.Style.STROKE
    }
    private val sheenPath = Path()
    private val holoBeamPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val holoTracePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.2f * density
    }
    private val holoNodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val dockPadFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xD9080D1A.toInt() // Dark translucent cyber glass fill
        style = Paint.Style.FILL
    }
    private val dockPadBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x4400E5FF.toInt() // Thin subtle cyan rim
        strokeWidth = 1.2f * density
        style = Paint.Style.STROKE
    }
    private val dockPadCornerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x8800E5FF.toInt() // Corner accent brackets
        strokeWidth = 2.0f * density
        style = Paint.Style.STROKE
    }
    private val ghostBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * density
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val ghostGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f * density
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        maskFilter = BlurMaskFilter(3f * density, BlurMaskFilter.Blur.NORMAL)
    }
    private val ghostScanlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val ghostPath = Path()
    private val tempGhostPath = Path()

    private val holoTrackFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val holoTrackBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
    }
    private val comboTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val bufferPipFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val bufferPipBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
    }
    private val countdownTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 14f * density
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.CENTER
        setShadowLayer(4f * density, 0f, 0f, "#00FF66".toColorInt())
    }
    private val countdownRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 2.5f * density
    }
    private val catalystBeamPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#4DFFD600")
    }
    private val catalystBeamCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * density
        color = Color.parseColor("#FFFFD600")
    }
    private val vortexAuraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#44EA80FC")
    }
    private val vortexRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f * density
        color = Color.parseColor("#FFEA80FC")
    }
    private val bossShieldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * density
        color = Color.parseColor("#00E5FF")
    }
    private val bossShieldFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#4D00E5FF")
    }
    private val hazardLockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#99FF0055")
    }
    private val dangerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.5f * density
    }
    private val milestoneBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#E6040A17")
    }
    private val milestoneBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
    }
    private val milestoneGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f * density
        maskFilter = BlurMaskFilter(4f * density, BlurMaskFilter.Blur.NORMAL)
    }
    private val milestoneTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        textSize = 13f * density
    }
    private val milestoneCornerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * density
    }

    private val predictiveBeamPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#4DFFD600")
    }
    private val predictiveBeamCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * density
        color = Color.parseColor("#FFFFD600")
    }

    private val emblemPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val tetherPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
        color = Color.parseColor("#00E5FF")
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
    }
    private val tetherPath = Path()
    private val tempMilestoneRect = RectF()
    private val tempCellRect = RectF()
    private val tempLineRect = RectF()
    private val tempRectF = RectF()

    val bossHudRenderer = BossHudRenderer(density)
    val blitzHudRenderer = TimeBlitzHudRenderer(density)

    private val disabledSlotBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * density
    }

    init {
        val size = (8 * density).toInt().coerceAtLeast(4)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            strokeWidth = 1.2f * density
            style = Paint.Style.STROKE
        }
        canvas.drawLine(0f, size.toFloat(), size.toFloat(), 0f, linePaint)
        canvas.drawLine(-1f, 1f, 1f, -1f, linePaint)
        canvas.drawLine(size - 1f, size + 1f, size + 1f, size - 1f, linePaint)

        ghostScanlinePaint.shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    fun render(
        canvas: Canvas,
        engine: GridEngine,
        adventureGrid: Array<Array<GridCell>>,
        hazardGrid: Array<Array<HazardCellState>>?,
        bossThreatState: BossThreatState,
        bossBattleState: BossBattleState,
        bossEngineState: BossBattleState,
        blitzEngine: TimeBlitzEngine,
        glitchEngine: GlitchEngine,
        dragState: DragState,
        boardRect: RectF,
        dockSlotBounds: Array<RectF>,
        dockShapes: Array<PolyShape?>,
        cellSize: Float,
        cellSpacing: Float,
        dockCellSizePx: Int,
        activeThemeKey: String,
        textureCache: BlockTextureCache,
        vfxPool: VfxPoolManager,
        vfxRenderer: VfxCanvasRenderer,
        spriteVfxEngine: OneShotSpriteVfxEngine,
        warpVortexFx: WarpVortexFxEngine,
        juiceFx: JuiceFxEngine,
        scorePopupManager: FloatingScoreManager,
        particleSystem: CyberParticleSystem,
        overdriveFx: OverdriveChassisFx,
        glitchSpriteVfx: GlitchSpriteVfx,
        sectorCoreRenderer: SectorCoreTextureRenderer,
        comboStreak: Int,
        comboState: ComboState,
        graceMovesRemaining: Int,
        maxGraceMoves: Int,
        activeAugments: List<NeuralAugment>,
        isAdventureModeActive: Boolean,
        isGlitchModeActive: Boolean,
        isTimeBlitzModeActive: Boolean,
        isClashModeActive: Boolean,
        isCurrentStageBoss: Boolean,
        trauma: Float,
        boardFillRatio: Float,
        activeMilestoneBanner: MilestoneBannerState,
        landingStartTimes: LongArray,
        canPieceFit: (List<PolyOffset>) -> Boolean,
        warpController: WarpBlockController,
        sectorId: Int = 1,
        now: Long,
        dt: Float
    ) {
        if (boardRect.width() <= 0 || boardRect.height() <= 0 || cellSize <= 0) return

        val isBossVulnerable = isAdventureModeActive && isCurrentStageBoss && bossBattleState.phase == BossPhase.OVERDRIVE_VULNERABLE

        if (trauma > 0f) {
            val shake = trauma * trauma
            val offsetX = 18f * shake * (Random.nextFloat() * 2f - 1f)
            val offsetY = 18f * shake * (Random.nextFloat() * 2f - 1f)
            val angle = 1.4f * shake * (Random.nextFloat() * 2f - 1f)
            canvas.save()
            canvas.translate(offsetX, offsetY)
            canvas.rotate(angle, canvas.width / 2f, canvas.height / 2f)
        }

        // --- LAYER 1: BACKGROUND ---
        drawBackgroundSockets(canvas, engine, boardRect, cellSize, cellSpacing, dockSlotBounds, isBossVulnerable, sectorId, activeThemeKey, isAdventureModeActive)

        // --- LAYER 2: GRID PROJECTIONS (Ghost, Holograms) ---
        if (dragState.isDragging && dragState.isValidPlacement) {
            drawGhostPreview(canvas, dragState, boardRect, cellSize, cellSpacing, now)
            drawHolographicTargetLines(canvas, dragState, boardRect, cellSize, cellSpacing, now)
            renderSpecialHoverIndicator(canvas, dragState, boardRect, cellSize, cellSpacing, now)
            
            if (isAdventureModeActive) {
                renderPredictiveBeams(canvas, dragState, adventureGrid, boardRect, cellSize, cellSpacing, now)
            }
        }

        // --- LAYER 3: SETTLED BLOCKS & CORES ---
        drawGridContent(canvas, engine, adventureGrid, boardRect, cellSize, cellSpacing, isGlitchModeActive, glitchEngine, activeThemeKey, textureCache, glitchSpriteVfx, isAdventureModeActive, sectorCoreRenderer, landingStartTimes, juiceFx, warpController, now)

        // --- LAYER 4: IN-WORLD WORLD FX ---
        overdriveFx.renderOverdriveChassis(canvas, boardRect, comboStreak, now)
        drawQuantumTethers(canvas, boardRect, cellSize, cellSpacing, isAdventureModeActive, isCurrentStageBoss, bossBattleState, adventureGrid, now)
        
        vfxPool.render(canvas, vfxRenderer, now / 1000f)
        particleSystem.updateAndDraw(canvas, dt)
        warpVortexFx.render(canvas, now, cellSize)
        spriteVfxEngine.render(canvas, now)
        juiceFx.renderParticles(canvas)
        juiceFx.renderCorruptionPulses(canvas)

        // --- LAYER 5: BLOCK OVERLAYS ---
        drawHazards(canvas, boardRect, cellSize, cellSpacing, isAdventureModeActive, hazardGrid)
        renderBossCoreShield(canvas, boardRect, cellSize, cellSpacing, isCurrentStageBoss, bossBattleState, now)

        // --- LAYER 6: FLOATING UI & HAND ---
        scorePopupManager.render(canvas, now)
        drawComboIndicator(canvas, boardRect, comboStreak, comboState, graceMovesRemaining, maxGraceMoves, isClashModeActive, isTimeBlitzModeActive, now)
        drawDock(canvas, dockSlotBounds, dockShapes, canPieceFit, dockCellSizePx, activeThemeKey, textureCache, now, dragState)
        drawDragPiece(canvas, dragState, boardRect, cellSize, cellSpacing, activeThemeKey, textureCache, now)

        if (trauma > 0f) {
            canvas.restore()
        }

        // --- LAYER 7: HUD & SCREEN FX ---
        if (isCurrentStageBoss) {
            bossHudRenderer.renderBossHud(canvas, boardRect, bossEngineState, now)
        }
        if (isTimeBlitzModeActive) {
            blitzHudRenderer.renderBlitzHud(canvas, boardRect, blitzEngine, now)
        }
        if (juiceFx.isGlitchActive()) {
            juiceFx.renderGlitchOverlay(canvas, canvas.width.toFloat(), canvas.height.toFloat())
        }

        renderMilestoneBanner(canvas, boardRect, activeMilestoneBanner, now)
        renderDangerVignette(canvas, boardRect, boardFillRatio, now)
    }

    private fun drawBackgroundSockets(
        canvas: Canvas,
        engine: GridEngine,
        boardRect: RectF,
        cellSize: Float,
        cellSpacing: Float,
        dockSlotBounds: Array<RectF>,
        isBossVulnerable: Boolean,
        sectorId: Int = 1,
        activeThemeKey: String = "",
        isAdventureModeActive: Boolean = false
    ) {
        // 1. Base Obsidian Backplate
        canvas.drawRoundRect(boardRect, 16f * density, 16f * density, scrimBackplatePaint)

        // 2. Glossy Glass Sheen Reflection Overlay
        if (boardRect.width() > 0 && boardRect.height() > 0) {
            sheenPaint.shader = LinearGradient(
                boardRect.right, boardRect.top,
                boardRect.left, boardRect.bottom,
                intArrayOf(
                    0x00FFFFFF,
                    0x00FFFFFF,
                    0x1200E5FF.toInt(),
                    0x2BFFFFFF.toInt(),
                    0x00FFFFFF,
                    0x00FFFFFF
                ),
                floatArrayOf(0f, 0.35f, 0.45f, 0.55f, 0.65f, 1f),
                Shader.TileMode.CLAMP
            )
            sheenPath.reset()
            sheenPath.addRoundRect(boardRect, 16f * density, 16f * density, Path.Direction.CW)
            canvas.withSave {
                clipPath(sheenPath)
                drawRect(boardRect, sheenPaint)
            }
        }

        // 3. Dynamic Sector-Themed Complementary Neon Gradient Grid Frame
        if (boardRect.width() > 0 && boardRect.height() > 0) {
            val (c1, c2, c3) = when {
                isBossVulnerable -> Triple(Color.RED, Color.YELLOW, Color.RED)
                isAdventureModeActive && sectorId == 2 -> Triple(0xFFFFD600.toInt(), 0xFFFF6D00.toInt(), 0xFFFF1744.toInt()) // Sector 2: Solar Gold -> Molten Orange -> Crimson Red
                isAdventureModeActive && sectorId == 3 -> Triple(0xFFFF1744.toInt(), 0xFFD500F9.toInt(), 0xFFEA80FC.toInt()) // Sector 3: Crimson -> Magenta -> Violet
                isAdventureModeActive && sectorId == 4 -> Triple(0xFF00E676.toInt(), 0xFF00E5FF.toInt(), 0xFF00B0FF.toInt()) // Sector 4: Bio Emerald -> Lime -> Cyan
                isAdventureModeActive && sectorId == 5 -> Triple(0xFFE040FB.toInt(), 0xFF651FFF.toInt(), 0xFF00E5FF.toInt()) // Sector 5: Ultraviolet -> Deep Purple -> Cyan
                isAdventureModeActive && sectorId == 1 -> Triple(0xFF00E5FF.toInt(), 0xFF0088FF.toInt(), 0xFFEA80FC.toInt()) // Sector 1: Electric Cyan -> Azure -> Magenta
                else -> {
                    // Non-Adventure modes: match equipped block theme or default
                    val key = activeThemeKey.lowercase()
                    when {
                        key.contains("solar") -> Triple(0xFFFFD600.toInt(), 0xFFFF6D00.toInt(), 0xFFFF1744.toInt())
                        key.contains("void") -> Triple(0xFFE040FB.toInt(), 0xFF651FFF.toInt(), 0xFF00E5FF.toInt())
                        key.contains("cyber") -> Triple(0xFF00E676.toInt(), 0xFF00E5FF.toInt(), 0xFF00B0FF.toInt())
                        else -> Triple(0xFF00E5FF.toInt(), 0xFF0088FF.toInt(), 0xFFEA80FC.toInt())
                    }
                }
            }

            val borderShader = LinearGradient(
                boardRect.left, boardRect.top,
                boardRect.right, boardRect.top,
                intArrayOf(c1, c2, c3),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
            gradientBorderPaint.shader = borderShader
            gradientBorderGlowPaint.shader = borderShader

            // Outer Neon Glow
            gradientBorderGlowPaint.alpha = if (isBossVulnerable) 180 else 110
            canvas.drawRoundRect(boardRect, 16f * density, 16f * density, gradientBorderGlowPaint)

            // Crisp Main Border
            gradientBorderPaint.alpha = 255
            canvas.drawRoundRect(boardRect, 16f * density, 16f * density, gradientBorderPaint)
        } else {
            val baseBorderColor = if (isBossVulnerable) Color.RED else "#1C2C4A".toColorInt()
            scrimBorderPaint.color = baseBorderColor
            canvas.drawRoundRect(boardRect, 16f * density, 16f * density, scrimBorderPaint)
        }

        // 4. Empty Grid Cells with Tactical Corner Ticks & Corner Radar Reticles
        val tickLen = 3.5f * density
        val cornerOffset = 2.5f * density

        for (y in 0 until 8) {
            for (x in 0 until 8) {
                calculateCellRect(y, x, cellSize, cellSpacing, boardRect, tempCellRect)
                if (engine.getGridValue(x, y) == 0) {
                    canvas.drawRoundRect(tempCellRect, 6f * density, 6f * density, socketPaint)

                    socketBorderPaint.color = if (isBossVulnerable) Color.parseColor("#4DFF0000") else "#152238".toColorInt()
                    canvas.drawRoundRect(tempCellRect, 6f * density, 6f * density, socketBorderPaint)

                    val cx = tempCellRect.centerX()
                    val cy = tempCellRect.centerY()
                    val len = 2.5f * density

                    // Center Plus Crosshair
                    crosshairPaint.color = if (isBossVulnerable) Color.parseColor("#66FF0000") else "#3300E5FF".toColorInt()
                    canvas.drawLine(cx - len, cy, cx + len, cy, crosshairPaint)
                    canvas.drawLine(cx, cy - len, cx, cy + len, crosshairPaint)

                    // Micro Corner Ticks (`┌ ┐ └ ┘`)
                    val left = tempCellRect.left + cornerOffset
                    val right = tempCellRect.right - cornerOffset
                    val top = tempCellRect.top + cornerOffset
                    val bottom = tempCellRect.bottom - cornerOffset

                    cellCornerTickPaint.color = if (isBossVulnerable) Color.parseColor("#4DFF0000") else Color.parseColor("#3800E5FF")

                    // Top-Left corner (┌)
                    canvas.drawLine(left, top, left + tickLen, top, cellCornerTickPaint)
                    canvas.drawLine(left, top, left, top + tickLen, cellCornerTickPaint)

                    // Top-Right corner (┐)
                    canvas.drawLine(right, top, right - tickLen, top, cellCornerTickPaint)
                    canvas.drawLine(right, top, right, top + tickLen, cellCornerTickPaint)

                    // Bottom-Left corner (└)
                    canvas.drawLine(left, bottom, left + tickLen, bottom, cellCornerTickPaint)
                    canvas.drawLine(left, bottom, left, bottom - tickLen, cellCornerTickPaint)

                    // Bottom-Right corner (┘)
                    canvas.drawLine(right, bottom, right - tickLen, bottom, cellCornerTickPaint)
                    canvas.drawLine(right, bottom, right, bottom - tickLen, cellCornerTickPaint)

                    // Corner Radar Target Reticles (`⊕`) on the 4 outer grid corners
                    if ((x == 0 || x == 7) && (y == 0 || y == 7)) {
                        val reticleRadius = 5.5f * density
                        radarReticlePaint.color = if (isBossVulnerable) Color.parseColor("#99FF0000") else Color.parseColor("#8000E5FF")

                        // Circle
                        canvas.drawCircle(cx, cy, reticleRadius, radarReticlePaint)

                        // Extended Crosshair
                        val crossLen = reticleRadius + 2.5f * density
                        canvas.drawLine(cx - crossLen, cy, cx + crossLen, cy, radarReticlePaint)
                        canvas.drawLine(cx, cy - crossLen, cx, cy + crossLen, radarReticlePaint)
                    }
                }
            }
        }
    }

    private fun drawGridContent(canvas: Canvas, engine: GridEngine, adventureGrid: Array<Array<GridCell>>, boardRect: RectF, cellSize: Float, cellSpacing: Float, isGlitchModeActive: Boolean, glitchEngine: GlitchEngine, activeThemeKey: String, textureCache: BlockTextureCache, glitchSpriteVfx: GlitchSpriteVfx, isAdventureModeActive: Boolean, sectorCoreRenderer: SectorCoreTextureRenderer, landingStartTimes: LongArray, juiceFx: JuiceFxEngine, warpController: WarpBlockController, now: Long) {
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                val index = y * 8 + x
                calculateCellRect(y, x, cellSize, cellSpacing, boardRect, tempCellRect)

                val cellValue = engine.getGridValue(x, y)
                if (cellValue != 0) {
                    if (isGlitchModeActive && glitchEngine.activeInfections.containsKey(index)) {
                        drawInfectedCell(canvas, index, glitchEngine, activeThemeKey, textureCache, glitchSpriteVfx, now)
                    } else {
                        drawSettledBlock(canvas, index, engine, adventureGrid, cellSize, cellSpacing, boardRect, isAdventureModeActive, sectorCoreRenderer, activeThemeKey, textureCache, landingStartTimes, juiceFx, now)
                    }
                }
            }
        }
        warpController.draw(canvas, now)
    }

    private fun drawInfectedCell(canvas: Canvas, index: Int, glitchEngine: GlitchEngine, activeThemeKey: String, textureCache: BlockTextureCache, glitchSpriteVfx: GlitchSpriteVfx, now: Long) {
        val cell = glitchEngine.activeInfections[index]!!
        textureCache.drawCell(canvas, tempCellRect, activeThemeKey, 9, isDock = false, now = now)
        glitchSpriteVfx.drawGlitchOverlay(canvas, tempCellRect, now, index)

        if (cell.phase == InfectionPhase.CRITICAL || cell.phase == InfectionPhase.WARNING) {
            val badgeSize = 15f * density
            val bx = tempCellRect.right - (badgeSize / 2f) - (2f * density)
            val by = tempCellRect.top + (badgeSize / 2f) + (2f * density)

            countdownRingPaint.color = Color.parseColor("#CC0A0F1D")
            canvas.drawCircle(bx, by, badgeSize / 2f, countdownRingPaint)

            val auraColor = if (cell.phase == InfectionPhase.CRITICAL) Color.parseColor("#FF0055") else Color.parseColor("#FFD600")
            countdownRingPaint.color = auraColor
            canvas.drawCircle(bx, by, (badgeSize / 2f) * 0.85f, countdownRingPaint)
            canvas.drawText("${cell.turnsRemaining}", bx, by + (4f * density), countdownTextPaint)
        }
    }

    private fun drawSettledBlock(canvas: Canvas, cellIndex: Int, engine: GridEngine, adventureGrid: Array<Array<GridCell>>, cellSize: Float, cellSpacing: Float, boardRect: RectF, isAdventureModeActive: Boolean, sectorCoreRenderer: SectorCoreTextureRenderer, activeThemeKey: String, textureCache: BlockTextureCache, landingStartTimes: LongArray, juiceFx: JuiceFxEngine, now: Long) {
        val landingStart = landingStartTimes[cellIndex]
        var scale = 1.0f

        if (landingStart > 0L) {
            val elapsed = now - landingStart
            val progress = elapsed.toFloat() / 140f
            if (progress < 1.0f) {
                scale = 1.0f + 0.16f * sin(progress * Math.PI.toFloat()) * (1.0f - progress)
            }
        }

        canvas.withSave {
            if (scale != 1.0f) {
                scale(scale, scale, tempCellRect.centerX(), tempCellRect.centerY())
            }

            val r = cellIndex / 8
            val c = cellIndex % 8
            val cellValue = engine.getGridValue(c, r)
            val tintColor = engine.getCellColor(c, r)

            if (isAdventureModeActive && (cellValue == -1 || cellValue == -2)) {
                val cell = adventureGrid[r][c]
                val coreType = when (cell.coreKind) {
                    CoreKind.AMBER_FURNACE, CoreKind.THERMAL_CATALYST -> SectorCoreType.SOLAR_CRUCIBLE_SEC2
                    CoreKind.CRIMSON_CIPHER_LOCKED, CoreKind.CRIMSON_CIPHER_EXPOSED -> SectorCoreType.CRIMSON_CIPHER_SEC3
                    CoreKind.EMERALD_CONDUIT -> SectorCoreType.BIO_CONDUIT_SEC4
                    CoreKind.PURPLE_SINGULARITY -> SectorCoreType.VOID_SINGULARITY_SEC5
                    else -> SectorCoreType.CHRONO_REACTOR_SEC1
                }

                val isCracked = cellValue == -2 || cell.coreIntegrity == CoreIntegrity.CRACKED || cell.hitsRemaining == 1

                sectorCoreRenderer.drawSectorCore(
                    canvas = canvas,
                    rect = tempCellRect,
                    coreType = coreType,
                    isCracked = isCracked,
                    isUnlocked = !cell.isInvulnerable,
                    isMeltdown = cell.isMeltdownActive,
                    now = now,
                    maxAllowedWidth = cellSize,
                    turnsRemaining = cell.turnsRemaining
                )
            } else {
                textureCache.drawCell(
                    canvas = canvas,
                    rect = tempCellRect,
                    themeKey = activeThemeKey,
                    cellValue = cellValue,
                    tintColor = tintColor,
                    specialType = engine.getSpecialValue(cellIndex),
                    isDock = false,
                    now = now
                )
                juiceFx.renderCellFlash(canvas, tempCellRect, cellIndex)
            }
        }
    }

    private fun drawDock(canvas: Canvas, dockSlotBounds: Array<RectF>, dockShapes: Array<PolyShape?>, canPieceFit: (List<PolyOffset>) -> Boolean, dockCellSizePx: Int, activeThemeKey: String, textureCache: BlockTextureCache, now: Long, dragState: DragState) {
        val cornerRadius = 10f * density
        val padMarginX = 2f * density
        val padMarginY = -2f * density
        val notchLen = 8f * density

        for (i in 0 until 3) {
            val slot = dockSlotBounds[i]
            val isDraggingThisSlot = (dragState.isDragging || dragState.isSpringing) && dragState.dockSlotIndex == i
            val shape = dockShapes[i]

            // 1. Draw Sleek Translucent Docking Pad
            val padRect = tempRectF
            padRect.set(slot.left + padMarginX, slot.top + padMarginY, slot.right - padMarginX, slot.bottom - padMarginY)

            // Dark glass background fill
            canvas.drawRoundRect(padRect, cornerRadius, cornerRadius, dockPadFillPaint)

            // Subtle glowing border
            canvas.drawRoundRect(padRect, cornerRadius, cornerRadius, dockPadBorderPaint)

            // Cyber corner accent brackets
            val padL = padRect.left
            val padT = padRect.top
            val padR = padRect.right
            val padB = padRect.bottom

            // Top-Left Corner
            canvas.drawLine(padL + cornerRadius, padT, padL + cornerRadius + notchLen, padT, dockPadCornerPaint)
            canvas.drawLine(padL, padT + cornerRadius, padL, padT + cornerRadius + notchLen, dockPadCornerPaint)

            // Bottom-Right Corner
            canvas.drawLine(padR - cornerRadius - notchLen, padB, padR - cornerRadius, padB, dockPadCornerPaint)
            canvas.drawLine(padR, padB - cornerRadius - notchLen, padR, padB - cornerRadius, dockPadCornerPaint)

            // 2. Draw Floating Piece inside Dock
            if (shape != null && !isDraggingThisSlot) {
                drawDockShape(canvas, i, dockSlotBounds, shape, canPieceFit, dockCellSizePx, activeThemeKey, textureCache, now)
            }
        }
    }

    private fun drawDockShape(canvas: Canvas, slotIndex: Int, dockSlotBounds: Array<RectF>, shape: PolyShape, canPieceFit: (List<PolyOffset>) -> Boolean, dockCellSizePx: Int, activeThemeKey: String, textureCache: BlockTextureCache, now: Long) {
        val slot = dockSlotBounds[slotIndex]
        val isBomb = shape.specialType == SpecialBlockType.QUANTUM_WARP_VORTEX ||
                shape.specialType == SpecialBlockType.CATALYST_CROSSHAIR ||
                shape.specialType == SpecialBlockType.NOVA_CORE_EXPLOSION

        val isPlaceable = if (isBomb) true else canPieceFit(shape.offsets)
        val alpha = if (isPlaceable) 255 else 90

        if (!isPlaceable) {
            disabledSlotBorderPaint.color = Color.parseColor("#4DFF0055")
            canvas.drawRoundRect(slot, 12f * density, 12f * density, disabledSlotBorderPaint)
        }

        val minX = shape.offsets.minOf { it.x }
        val maxX = shape.offsets.maxOf { it.x }
        val minY = shape.offsets.minOf { it.y }
        val maxY = shape.offsets.maxOf { it.y }

        val pieceCols = maxX - minX + 1
        val pieceRows = maxY - minY + 1
        val maxDimension = maxOf(pieceCols, pieceRows)
        
        // Dynamic scale factor ensures ALL block patterns (1x1, 2x2, 3x3, L/J shapes, 4-bars, 5-cell shapes)
        // fit comfortably centered with generous padding inside the cyber glass pad
        val targetMaxCells = 2.3f
        val scaleFactor = if (maxDimension >= 3) targetMaxCells / maxDimension.toFloat() else 0.85f
        val effectiveCellSize = dockCellSizePx * scaleFactor

        val pieceWidthPx = pieceCols * effectiveCellSize
        val pieceHeightPx = pieceRows * effectiveCellSize
        val originX = slot.centerX() - (pieceWidthPx / 2f) - (minX * effectiveCellSize)
        val originY = slot.centerY() - (pieceHeightPx / 2f) - (minY * effectiveCellSize)

        shape.offsets.forEach { offset ->
            val cx = originX + offset.x * effectiveCellSize
            val cy = originY + offset.y * effectiveCellSize
            tempCellRect.set(cx, cy, cx + effectiveCellSize, cy + effectiveCellSize)

            textureCache.drawCell(
                canvas = canvas,
                rect = tempCellRect,
                themeKey = activeThemeKey,
                cellValue = 1,
                tintColor = shape.color,
                specialType = shape.specialType,
                isDock = true,
                alpha = alpha,
                now = now
            )
        }
    }

    private fun drawDragPiece(canvas: Canvas, dragState: DragState, boardRect: RectF, cellSize: Float, cellSpacing: Float, activeThemeKey: String, textureCache: BlockTextureCache, now: Long) {
        if ((dragState.isDragging || dragState.isSpringing) && dragState.shape != null) {
            val shape = dragState.shape!!
            val visualLeft = dragState.visualPieceBounds.left
            val visualTop = dragState.visualPieceBounds.top
            val scale = dragState.scale

            canvas.save()
            if (scale != 1.0f) {
                canvas.scale(scale, scale, visualLeft + (dragState.visualPieceBounds.width() / 2f), visualTop + (dragState.visualPieceBounds.height() / 2f))
            }

            shape.offsets.forEach { offset ->
                val x = visualLeft + offset.x * (cellSize + cellSpacing)
                val y = visualTop + offset.y * (cellSize + cellSpacing)
                tempCellRect.set(x, y, x + cellSize, y + cellSize)
                textureCache.drawCell(
                    canvas = canvas,
                    rect = tempCellRect,
                    themeKey = activeThemeKey,
                    cellValue = 1,
                    tintColor = shape.color,
                    specialType = shape.specialType,
                    isDock = false,
                    now = now
                )
            }
            canvas.restore()
        }
    }

    private fun drawGhostPreview(canvas: Canvas, dragState: DragState, boardRect: RectF, cellSize: Float, cellSpacing: Float, now: Long) {
        val shape = dragState.shape ?: return
        val pieceColor = shape.color

        // Breathing neon pulse calculation
        val pulse = (sin(now / 90.0) * 0.5 + 0.5).toFloat()
        val fillAlpha = (60 + pulse * 40).toInt()
        val glowAlpha = (100 + pulse * 110).toInt()
        val borderAlpha = (200 + pulse * 55).toInt()

        ghostPath.reset()
        for (i in 0 until dragState.projectedCount) {
            val r = dragState.projectedCoords[i * 2]
            val c = dragState.projectedCoords[i * 2 + 1]
            calculateCellRect(r, c, cellSize, cellSpacing, boardRect, tempCellRect)

            tempGhostPath.reset()
            tempGhostPath.addRoundRect(
                tempCellRect.left + 1f, tempCellRect.top + 1f, tempCellRect.right - 1f, tempCellRect.bottom - 1f,
                6f * density, 6f * density,
                Path.Direction.CW
            )
            ghostPath.addPath(tempGhostPath)
        }

        val fillAlphaColor = ColorUtils.setAlphaComponent(pieceColor, fillAlpha)
        ghostScanlinePaint.colorFilter = PorterDuffColorFilter(fillAlphaColor, PorterDuff.Mode.SRC_IN)
        canvas.drawPath(ghostPath, ghostScanlinePaint)

        ghostGlowPaint.color = pieceColor
        ghostGlowPaint.alpha = glowAlpha
        ghostGlowPaint.strokeWidth = (5f + pulse * 2f) * density
        canvas.drawPath(ghostPath, ghostGlowPaint)

        ghostBorderPaint.color = pieceColor
        ghostBorderPaint.alpha = borderAlpha
        ghostBorderPaint.strokeWidth = (2.5f + pulse * 1f) * density
        canvas.drawPath(ghostPath, ghostBorderPaint)

        // Render tactical corner reticles for ghost cells
        val reticleLen = 4f * density
        val cornerOffset = 2f * density
        cellCornerTickPaint.color = pieceColor
        cellCornerTickPaint.alpha = borderAlpha

        for (i in 0 until dragState.projectedCount) {
            val r = dragState.projectedCoords[i * 2]
            val c = dragState.projectedCoords[i * 2 + 1]
            calculateCellRect(r, c, cellSize, cellSpacing, boardRect, tempCellRect)

            val left = tempCellRect.left + cornerOffset
            val right = tempCellRect.right - cornerOffset
            val top = tempCellRect.top + cornerOffset
            val bottom = tempCellRect.bottom - cornerOffset

            canvas.drawLine(left, top, left + reticleLen, top, cellCornerTickPaint)
            canvas.drawLine(left, top, left, top + reticleLen, cellCornerTickPaint)
            canvas.drawLine(right, top, right - reticleLen, top, cellCornerTickPaint)
            canvas.drawLine(right, top, right, top + reticleLen, cellCornerTickPaint)
            canvas.drawLine(left, bottom, left + reticleLen, bottom, cellCornerTickPaint)
            canvas.drawLine(left, bottom, left, bottom - reticleLen, cellCornerTickPaint)
            canvas.drawLine(right, bottom, right - reticleLen, bottom, cellCornerTickPaint)
            canvas.drawLine(right, bottom, right, bottom - reticleLen, cellCornerTickPaint)
        }
    }

    private fun drawHolographicTargetLines(canvas: Canvas, dragState: DragState, boardRect: RectF, cellSize: Float, cellSpacing: Float, now: Long) {
        if (dragState.totalLines <= 0) return
        val pulse = (sin(now / 110.0) * 0.5 + 0.5).toFloat()
        val color = dragState.shape?.color ?: Color.CYAN

        holoTrackFillPaint.color = color
        holoTrackFillPaint.alpha = (40 + pulse * 60).toInt()
        holoTrackBorderPaint.color = color
        holoTrackBorderPaint.alpha = (130 + pulse * 120).toInt()

        for (r in 0 until 8) {
            if ((dragState.rowsToClearMask and (1 shl r)) != 0) {
                val top = boardRect.top + cellSpacing + r * (cellSize + cellSpacing)
                tempLineRect.set(boardRect.left + cellSpacing, top, boardRect.right - cellSpacing, top + cellSize)
                canvas.drawRoundRect(tempLineRect, 6f * density, 6f * density, holoTrackFillPaint)
                canvas.drawRoundRect(tempLineRect, 6f * density, 6f * density, holoTrackBorderPaint)
            }
        }
        for (c in 0 until 8) {
            if ((dragState.colsToClearMask and (1 shl c)) != 0) {
                val left = boardRect.left + cellSpacing + c * (cellSize + cellSpacing)
                tempLineRect.set(left, boardRect.top + cellSpacing, left + cellSize, boardRect.bottom - cellSpacing)
                canvas.drawRoundRect(tempLineRect, 6f * density, 6f * density, holoTrackFillPaint)
                canvas.drawRoundRect(tempLineRect, 6f * density, 6f * density, holoTrackBorderPaint)
            }
        }
    }

    private fun renderSpecialHoverIndicator(canvas: Canvas, dragState: DragState, boardRect: RectF, cellSize: Float, cellSpacing: Float, now: Long) {
        val shape = dragState.shape ?: return
        when (shape.specialType) {
            SpecialBlockType.CATALYST_CROSSHAIR -> {
                val special = SpecialBlockSolver.evaluateSpecialClear(shape, dragState.targetCol, dragState.targetRow)
                for (r in 0 until 8) {
                    if ((special.affectedRowsMask and (1 shl r)) != 0) {
                        val top = boardRect.top + cellSpacing + r * (cellSize + cellSpacing)
                        val bottom = top + cellSize
                        tempRectF.set(boardRect.left, top, boardRect.right, bottom)
                        canvas.drawRect(tempRectF, catalystBeamPaint)
                        canvas.drawLine(boardRect.left, (top + bottom) / 2f, boardRect.right, (top + bottom) / 2f, catalystBeamCorePaint)
                    }
                }
                for (c in 0 until 8) {
                    if ((special.affectedColsMask and (1 shl c)) != 0) {
                        val left = boardRect.left + cellSpacing + c * (cellSize + cellSpacing)
                        val right = left + cellSize
                        tempRectF.set(left, boardRect.top, right, boardRect.bottom)
                        canvas.drawRect(tempRectF, catalystBeamPaint)
                        canvas.drawLine((left + right) / 2f, boardRect.top, (left + right) / 2f, boardRect.bottom, catalystBeamCorePaint)
                    }
                }
            }
            SpecialBlockType.QUANTUM_WARP_VORTEX, SpecialBlockType.NOVA_CORE_EXPLOSION -> {
                val special = SpecialBlockSolver.evaluateSpecialClear(shape, dragState.targetCol, dragState.targetRow)
                for (index in special.directClearedCellIndices) {
                    val r = index / 8
                    val c = index % 8
                    calculateCellRect(r, c, cellSize, cellSpacing, boardRect, tempCellRect)
                    canvas.drawRoundRect(tempCellRect, 8f * density, 8f * density, vortexAuraPaint)
                    canvas.drawRoundRect(tempCellRect, 8f * density, 8f * density, vortexRingPaint)
                }

                val centerX = boardRect.left + cellSpacing + (dragState.targetCol + 0.5f) * (cellSize + cellSpacing)
                val centerY = boardRect.top + cellSpacing + (dragState.targetRow + 0.5f) * (cellSize + cellSpacing)
                val vortexRadius = cellSize * 1.5f
                val rotation = (now % 2000L) / 2000f * 360f

                canvas.withSave {
                    rotate(rotation, centerX, centerY)
                    drawCircle(centerX, centerY, vortexRadius, vortexRingPaint)
                    repeat(4) {
                        rotate(90f, centerX, centerY)
                        drawLine(centerX + vortexRadius * 0.7f, centerY, centerX + vortexRadius, centerY, vortexRingPaint)
                    }
                }
            }
            else -> {}
        }
    }

    private fun drawComboIndicator(canvas: Canvas, boardRect: RectF, comboStreak: Int, comboState: ComboState, graceMovesRemaining: Int, maxGraceMoves: Int, isClashModeActive: Boolean, isTimeBlitzModeActive: Boolean, now: Long) {
        if (comboStreak < 2) return

        val hudCenterX = boardRect.centerX()
        val hudCenterY = when {
            isClashModeActive -> boardRect.top + (16f * density)
            isTimeBlitzModeActive -> boardRect.top - (14f * density)
            else -> boardRect.top - (26f * density)
        }

        val primaryColor: Int
        val pipColor: Int
        when (comboState) {
            ComboState.CRITICAL_LAST_MOVE -> {
                val pulse = (sin(now / 70.0) * 0.5 + 0.5).toFloat()
                val alpha = (155 + pulse * 100).toInt()
                val baseColor = Color.parseColor("#FF0055")
                primaryColor = (alpha shl 24) or (baseColor and 0x00FFFFFF)
                pipColor = primaryColor
            }
            ComboState.GRACE_WARNING -> {
                primaryColor = Color.parseColor("#FFD600")
                pipColor = Color.parseColor("#FFD600")
            }
            else -> {
                primaryColor = Color.parseColor("#00E5FF")
                pipColor = Color.parseColor("#00FF66")
            }
        }

        comboTextPaint.color = primaryColor
        comboTextPaint.textSize = if (isClashModeActive) 12f * density else 14f * density
        canvas.drawText("COMBO x$comboStreak", hudCenterX - (24f * density), hudCenterY + (4f * density), comboTextPaint)

        val pipRadius = 4f * density
        val pipSpacing = 12f * density
        val pipStartX = hudCenterX + (24f * density)

        for (i in 0 until maxGraceMoves) {
            val cx = pipStartX + i * pipSpacing
            val cy = hudCenterY
            if (i < graceMovesRemaining) {
                bufferPipFillPaint.color = pipColor
                bufferPipFillPaint.alpha = 255
                canvas.drawCircle(cx, cy, pipRadius, bufferPipFillPaint)
                bufferPipBorderPaint.color = Color.WHITE
                bufferPipBorderPaint.alpha = 200
                canvas.drawCircle(cx, cy, pipRadius, bufferPipBorderPaint)
            } else {
                bufferPipFillPaint.color = Color.parseColor("#152238")
                bufferPipFillPaint.alpha = 180
                canvas.drawCircle(cx, cy, pipRadius, bufferPipFillPaint)
                bufferPipBorderPaint.color = Color.parseColor("#263859")
                bufferPipBorderPaint.alpha = 140
                canvas.drawCircle(cx, cy, pipRadius, bufferPipBorderPaint)
            }
        }
    }

    private fun drawQuantumTethers(
        canvas: Canvas,
        boardRect: RectF,
        cellSize: Float,
        cellSpacing: Float,
        isAdventureModeActive: Boolean,
        isCurrentStageBoss: Boolean,
        bossBattleState: BossBattleState,
        adventureGrid: Array<Array<GridCell>>?,
        now: Long
    ) {
        if (!isAdventureModeActive || !isCurrentStageBoss) return
        if (bossBattleState.phase != BossPhase.SHIELDED || bossBattleState.shieldPylonIndices.isEmpty()) return

        val stride = cellSize + cellSpacing
        val left = boardRect.left + cellSpacing
        val top = boardRect.top + cellSpacing

        val bossCenterX = left + 3.5f * stride
        val bossCenterY = top + 3.5f * stride

        for (pylonIdx in bossBattleState.shieldPylonIndices) {
            val pr = pylonIdx / 8
            val pc = pylonIdx % 8

            // Verify core is actually still active on board
            if (adventureGrid != null) {
                val cell = adventureGrid[pr][pc]
                if (!cell.isCore || cell.coreIntegrity == CoreIntegrity.DESTROYED) continue
            }

            val pylonX = left + pc * stride + (cellSize / 2f)
            val pylonY = top + pr * stride + (cellSize / 2f)

            tetherPath.reset()
            tetherPath.moveTo(bossCenterX, bossCenterY)
            val waveOffset = sin((now / 1000f * 12f) + pr + pc) * 6f * density
            tetherPath.quadTo((bossCenterX + pylonX) / 2f + waveOffset, (bossCenterY + pylonY) / 2f - waveOffset, pylonX, pylonY)
            
            // P2 Fix: Animated Pulse/UV on tethers using Dash effect
            val pulseOffset = (now / 15L) % 200
            tetherPaint.pathEffect = DashPathEffect(floatArrayOf(12f * density, 24f * density), pulseOffset.toFloat() * density)
            
            tetherPaint.alpha = (180 + sin(now / 1000f * 8f) * 60).toInt().coerceIn(0, 255)
            canvas.drawPath(tetherPath, tetherPaint)
            tetherPaint.pathEffect = null // Reset for next pylon

            // P2 Fix: Particle Flow along tether
            val pm = PathMeasure(tetherPath, false)
            val len = pm.length
            val particleCount = 3
            for (p in 0 until particleCount) {
                val pProgress = ((now / 800f) + (p.toFloat() / particleCount)) % 1f
                val pos = FloatArray(2)
                pm.getPosTan(pProgress * len, pos, null)
                
                val pAlpha = if (pProgress < 0.2f) pProgress / 0.2f else if (pProgress > 0.8f) (1f - pProgress) / 0.2f else 1f
                emblemPaint.color = Color.WHITE
                emblemPaint.alpha = (pAlpha * 255).toInt()
                emblemPaint.style = Paint.Style.FILL
                canvas.drawCircle(pos[0], pos[1], 1.5f * density, emblemPaint)
            }
        }
    }

    private fun drawHazards(canvas: Canvas, boardRect: RectF, cellSize: Float, cellSpacing: Float, isAdventureModeActive: Boolean, hazardGrid: Array<Array<HazardCellState>>?) {
        if (!isAdventureModeActive || hazardGrid == null) return
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                val hazard = hazardGrid[r][c]
                if (hazard.hazardType == AdventureHazardType.EMP_LOCK) {
                    calculateCellRect(r, c, cellSize, cellSpacing, boardRect, tempCellRect)
                    canvas.drawRoundRect(tempCellRect, 8f * density, 8f * density, hazardLockPaint)
                }
            }
        }
    }

    private fun renderBossCoreShield(canvas: Canvas, boardRect: RectF, cellSize: Float, cellSpacing: Float, isCurrentStageBoss: Boolean, bossBattleState: BossBattleState, now: Long) {
        if (!isCurrentStageBoss) return
        val stride = cellSize + cellSpacing
        val centerX = boardRect.left + cellSpacing + 3.5f * stride
        val centerY = boardRect.top + cellSpacing + 3.5f * stride

        if (bossBattleState.phase == BossPhase.SHIELDED) {
            val pulse = (sin(now / 150.0) * 0.5 + 0.5).toFloat()
            val radius = cellSize * (1.1f + pulse * 0.1f)

            bossShieldFillPaint.color = Color.parseColor("#00B0FF")
            bossShieldFillPaint.alpha = (60 + pulse * 40).toInt()
            canvas.drawCircle(centerX, centerY, radius, bossShieldFillPaint)

            bossShieldPaint.color = Color.parseColor("#00E5FF")
            bossShieldPaint.alpha = (180 + pulse * 75).toInt()
            canvas.drawCircle(centerX, centerY, radius, bossShieldPaint)
        } else if (bossBattleState.phase != BossPhase.DEFEATED) {
            // Phase 2: Vulnerable Pulsing Crimson Warning Aura
            val pulse = (sin(now / 80.0) * 0.5 + 0.5).toFloat()
            val radius = cellSize * (1.15f + pulse * 0.15f)

            bossShieldFillPaint.color = Color.parseColor("#FF0055")
            bossShieldFillPaint.alpha = (80 + pulse * 60).toInt()
            canvas.drawCircle(centerX, centerY, radius, bossShieldFillPaint)

            bossShieldPaint.color = Color.parseColor("#FFD600")
            bossShieldPaint.alpha = (200 + pulse * 55).toInt()
            canvas.drawCircle(centerX, centerY, radius, bossShieldPaint)
        }
    }

    private fun renderMilestoneBanner(canvas: Canvas, boardRect: RectF, banner: MilestoneBannerState, now: Long) {
        if (!banner.isActive) return
        val elapsed = now - banner.startTimeMs
        if (elapsed >= banner.durationMs) {
            banner.isActive = false
            return
        }
        val progress = elapsed.toFloat() / banner.durationMs.toFloat()
        val alpha = when {
            progress < 0.15f -> (progress / 0.15f)
            progress > 0.75f -> ((1.0f - progress) / 0.25f)
            else -> 1.0f
        }.coerceIn(0f, 1f)
        val scale = when {
            progress < 0.15f -> 0.85f + (progress / 0.15f) * 0.15f
            progress < 0.25f -> 1.0f + sin((progress - 0.15f) / 0.10f * Math.PI.toFloat()) * 0.08f
            else -> 1.0f
        }
        val cx = boardRect.centerX()
        val cy = boardRect.top + (70f * density) - progress * (16f * density)
        val bw = 240f * density
        val bh = 36f * density
        tempMilestoneRect.set(cx - bw / 2f, cy - bh / 2f, cx + bw / 2f, cy + bh / 2f)

        canvas.withSave {
            scale(scale, scale, cx, cy)
            milestoneBgPaint.alpha = (alpha * 230).toInt()
            canvas.drawRoundRect(tempMilestoneRect, 8f * density, 8f * density, milestoneBgPaint)
            milestoneGlowPaint.color = banner.color
            milestoneGlowPaint.alpha = (alpha * 90).toInt()
            canvas.drawRoundRect(tempMilestoneRect, 8f * density, 8f * density, milestoneGlowPaint)
            milestoneBorderPaint.color = banner.color
            milestoneBorderPaint.alpha = (alpha * 200).toInt()
            canvas.drawRoundRect(tempMilestoneRect, 8f * density, 8f * density, milestoneBorderPaint)

            milestoneTextPaint.color = banner.color
            milestoneTextPaint.alpha = (alpha * 255).toInt()
            canvas.drawText(banner.text, cx, cy + (4.5f * density), milestoneTextPaint)
        }
    }

    private fun renderDangerVignette(canvas: Canvas, boardRect: RectF, boardFillRatio: Float, now: Long) {
        if (boardFillRatio < 0.78f) return
        val pulse = (sin(now / 180.0) * 0.5 + 0.5).toFloat()
        val intensity = ((boardFillRatio - 0.78f) / 0.22f).coerceIn(0f, 1f)
        val alpha = ((0.35f + 0.65f * pulse) * intensity * 255).toInt()
        dangerBorderPaint.color = Color.argb(alpha, 255, 0, 85)
        canvas.drawRoundRect(boardRect, 14f * density, 14f * density, dangerBorderPaint)
    }

    private fun renderPredictiveBeams(
        canvas: Canvas,
        dragState: DragState,
        adventureGrid: Array<Array<GridCell>>,
        boardRect: RectF,
        cellSize: Float,
        cellSpacing: Float,
        now: Long
    ) {
        val rowsMask = dragState.rowsToClearMask
        val colsMask = dragState.colsToClearMask
        if (rowsMask == 0 && colsMask == 0) return

        val pulse = (sin(now / 100.0) * 0.4 + 0.6).toFloat()
        predictiveBeamPaint.alpha = (77 * pulse).toInt()
        predictiveBeamCorePaint.alpha = (255 * pulse).toInt()

        for (r in 0 until 8) {
            if ((rowsMask and (1 shl r)) != 0) {
                var hasTarget = false
                for (c in 0 until 8) {
                    val cell = adventureGrid[r][c]
                    if (cell.isCore && (cell.coreKind == CoreKind.AMBER_FURNACE || cell.coreKind == CoreKind.CRIMSON_CIPHER_LOCKED) && cell.coreIntegrity != CoreIntegrity.DESTROYED) {
                        hasTarget = true
                        break
                    }
                }
                if (hasTarget) {
                    val top = boardRect.top + cellSpacing + r * (cellSize + cellSpacing)
                    tempRectF.set(boardRect.left, top, boardRect.right, top + cellSize)
                    canvas.drawRect(tempRectF, predictiveBeamPaint)
                    canvas.drawLine(boardRect.left, top + cellSize / 2f, boardRect.right, top + cellSize / 2f, predictiveBeamCorePaint)
                }
            }
        }

        for (c in 0 until 8) {
            if ((colsMask and (1 shl c)) != 0) {
                var hasTarget = false
                for (r in 0 until 8) {
                    val cell = adventureGrid[r][c]
                    if (cell.isCore && (cell.coreKind == CoreKind.AMBER_FURNACE || cell.coreKind == CoreKind.CRIMSON_CIPHER_LOCKED) && cell.coreIntegrity != CoreIntegrity.DESTROYED) {
                        hasTarget = true
                        break
                    }
                }
                if (hasTarget) {
                    val left = boardRect.left + cellSpacing + c * (cellSize + cellSpacing)
                    tempRectF.set(left, boardRect.top, left + cellSize, boardRect.bottom)
                    canvas.drawRect(tempRectF, predictiveBeamPaint)
                    canvas.drawLine(left + cellSize / 2f, boardRect.top, left + cellSize / 2f, boardRect.bottom, predictiveBeamCorePaint)
                }
            }
        }
    }



    private fun calculateCellRect(r: Int, c: Int, size: Float, spacing: Float, board: RectF, out: RectF) {
        val left = board.left + spacing + c * (size + spacing)
        val top = board.top + spacing + r * (size + spacing)
        out.set(left, top, left + size, top + size)
    }
}
