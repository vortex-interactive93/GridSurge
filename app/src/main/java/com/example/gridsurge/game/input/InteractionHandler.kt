package com.example.gridsurge.game.input

import android.graphics.RectF
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import com.example.gridsurge.audio.HapticType
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.game.model.DragState
import com.example.gridsurge.game.model.LinePreviewState
import com.example.gridsurge.game.model.PolyShape
import com.example.gridsurge.game.model.SpecialBlockType
import com.example.gridsurge.features.adventure.model.HazardCellState
import com.example.gridsurge.game.core.HologramLineSolver
import com.example.gridsurge.game.engine.PlacementProjectionEngine
import com.example.gridsurge.game.engine.SpringPhysicsEngine
import kotlin.math.max
import kotlin.math.roundToInt

interface InteractionListener {
    fun onCommitDrop(shape: PolyShape, slotIndex: Int, col: Int, row: Int)
    fun onInvalidMove()
    fun requestInvalidate()
    fun triggerHaptic(constant: Int)
    fun isJammed(slotIndex: Int): Boolean
    fun getFlatGrid(): IntArray
    fun getHazardGrid(): Array<Array<HazardCellState>>?
    fun isInteractionLocked(): Boolean
    fun getCurrentTimeMs(): Long
}

class InteractionHandler(
    private val density: Float,
    private val listener: InteractionListener
) {
    val dragState = DragState()
    private val previewState = LinePreviewState()
    private val projectionEngine = PlacementProjectionEngine(gridSize = 8)
    private val springEngine = SpringPhysicsEngine(naturalFrequency = 28f, dampingRatio = 0.72f)
    private var lastHapticCellIndex = -1

    // Velocity tracker
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var velocityX = 0f
    private var velocityY = 0f

    var cellSize = 0f
    var cellSpacing = 0f
    val boardRect = RectF()
    val dockSlotBounds = Array(3) { RectF() }
    var dockShapes = arrayOfNulls<PolyShape>(3)

    fun handleTouchEvent(event: MotionEvent): Boolean {
        if (listener.isInteractionLocked()) {
            if (dragState.isDragging) {
                resetDrag()
                listener.requestInvalidate()
            }
            return false
        }

        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // Cancel active spring if we touch the dock again
                if (dragState.isSpringing) {
                    dragState.isSpringing = false
                }
                
                val slotIndex = getTouchedTraySlot(x, y)
                if (slotIndex != null) {
                    val shape = dockShapes[slotIndex]!!

                    if (listener.isJammed(slotIndex)) {
                        SfxManager.playSfx(SfxType.INVALID_MOVE)
                        return true
                    }

                    dragState.isDragging = true
                    dragState.isSpringing = false // Cancel any active spring
                    dragState.shape = shape
                    dragState.dockSlotIndex = slotIndex
                    dragState.scale = 1.0f // Normal size to match grid
                    
                    lastTouchX = x
                    lastTouchY = y
                    velocityX = 0f
                    velocityY = 0f
                    
                    updateDragPosition(x, y)
                    SfxManager.playSfx(SfxType.TILE_PICKUP)
                    listener.requestInvalidate()
                    return true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (dragState.isDragging) {
                    updateDragPosition(x, y)
                    listener.requestInvalidate()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (dragState.isDragging) {
                    if (dragState.isValidPlacement && dragState.shape != null) {
                        listener.onCommitDrop(dragState.shape!!, dragState.dockSlotIndex, dragState.targetCol, dragState.targetRow)
                        resetDrag()
                    } else if (dragState.shape != null) {
                        // Initiate Spring-Back Return
                        startSpringBack()
                        SfxManager.playSfx(SfxType.INVALID_MOVE)
                        SfxManager.playSfx(SfxType.TILE_DROP)
                        listener.onInvalidMove()
                    } else {
                        resetDrag()
                    }
                }
                listener.requestInvalidate()
            }
        }
        return true
    }

    private fun resetDrag() {
        dragState.reset()
        dragState.isSpringing = false
        lastHapticCellIndex = -1
    }

    private fun startSpringBack() {
        val slot = dockSlotBounds.getOrNull(dragState.dockSlotIndex) ?: return
        
        dragState.isDragging = false
        dragState.isSpringing = true
        
        // Target: Center of the original dock slot
        val shape = dragState.shape ?: return
        val pieceCols = shape.offsets.maxOf { it.x } + 1
        val pieceRows = shape.offsets.maxOf { it.y } + 1
        
        val pieceW = pieceCols * cellSize + (pieceCols - 1) * cellSpacing
        val pieceH = pieceRows * cellSize + (pieceRows - 1) * cellSpacing
        
        val destX = slot.centerX() - pieceW / 2f
        val destY = slot.centerY() - pieceH / 2f
        
        springEngine.start(
            startX = dragState.visualPieceBounds.left,
            startY = dragState.visualPieceBounds.top,
            destX = destX,
            destY = destY,
            velX = velocityX.coerceIn(-2500f, 2500f),
            velY = velocityY.coerceIn(-2500f, 2500f)
        )
    }

    fun updatePhysics(dt: Float) {
        if (!dragState.isSpringing) return
        
        val pos = springEngine.update(dt)
        val pieceW = dragState.visualPieceBounds.width()
        val pieceH = dragState.visualPieceBounds.height()
        
        dragState.visualPieceBounds.set(pos.first, pos.second, pos.first + pieceW, pos.second + pieceH)
        
        // Scale blending: 1.15 lifted -> 1.0 settled
        dragState.scale = 1.0f + (dragState.scale - 1.0f) * max(0f, 1.0f - 15.0f * dt)
        
        if (springEngine.isSettled) {
            resetDrag()
        }
    }

    fun getVelocity(): Pair<Float, Float> = Pair(velocityX, velocityY)

    private fun getTouchedTraySlot(touchX: Float, touchY: Float): Int? {
        val touchSlop = 12f * density
        for (i in 0 until 3) {
            val slotRect = dockSlotBounds[i]
            if (touchX >= slotRect.left - touchSlop && touchX <= slotRect.right + touchSlop &&
                touchY >= slotRect.top - touchSlop && touchY <= slotRect.bottom + touchSlop) {
                if (dockShapes[i] != null) return i
            }
        }
        return null
    }

    private fun updateDragPosition(x: Float, y: Float) {
        val shape = dragState.shape ?: return
        
        // Update velocity
        velocityX = 0.6f * velocityX + 0.4f * ((x - lastTouchX) / 0.016f)
        velocityY = 0.6f * velocityY + 0.4f * ((y - lastTouchY) / 0.016f)
        lastTouchX = x
        lastTouchY = y

        dragState.touchX = x
        dragState.touchY = y

        val pieceCols = shape.offsets.maxOf { it.x } + 1
        val pieceRows = shape.offsets.maxOf { it.y } + 1

        val pieceW = pieceCols * cellSize + (pieceCols - 1) * cellSpacing
        val pieceH = pieceRows * cellSize + (pieceRows - 1) * cellSpacing

        val liftOffset = 52f * density
        val visualLeft = x - pieceW / 2f
        val visualTop = y - liftOffset - pieceH
        dragState.visualPieceBounds.set(visualLeft, visualTop, visualLeft + pieceW, visualTop + pieceH)

        val step = cellSize + cellSpacing
        var col = ((visualLeft - (boardRect.left + cellSpacing)) / step).roundToInt()
        var row = ((visualTop - (boardRect.top + cellSpacing)) / step).roundToInt()

        // P2 Fix: Center-clamping logic for 3x3 special abilities to fix perimeter desync
        val is3x3Special = shape.specialType == SpecialBlockType.QUANTUM_WARP_VORTEX ||
                shape.specialType == SpecialBlockType.NOVA_CORE_EXPLOSION ||
                shape.specialType == SpecialBlockType.CATALYST_CROSSHAIR
        
        if (is3x3Special) {
            col = col.coerceIn(1, 6)
            row = row.coerceIn(1, 6)
        }

        dragState.targetCol = col
        dragState.targetRow = row

        evaluatePlacement(shape, col, row)

        val cellIdx = row * 8 + col
        if (dragState.isValidPlacement && cellIdx != lastHapticCellIndex) {
            SfxManager.triggerHaptic(HapticType.LIGHT_TICK)
            lastHapticCellIndex = cellIdx
        } else if (!dragState.isValidPlacement) {
            lastHapticCellIndex = -1
        }
    }

    fun startExternalDrag(touchX: Float, touchY: Float, shape: PolyShape) {
        if (listener.isInteractionLocked()) return
        resetDrag()
        dragState.isDragging = true
        dragState.shape = shape
        dragState.dockSlotIndex = -1
        dragState.scale = 1.0f
        lastTouchX = touchX
        lastTouchY = touchY
        velocityX = 0f
        velocityY = 0f
        updateDragPosition(touchX, touchY)
        SfxManager.playSfx(SfxType.TILE_PICKUP)
        listener.requestInvalidate()
    }

    fun updateExternalDrag(touchX: Float, touchY: Float) {
        if (!dragState.isDragging) return
        updateDragPosition(touchX, touchY)
        listener.requestInvalidate()
    }

    fun endExternalDrag() {
        if (!dragState.isDragging) return
        if (dragState.isValidPlacement && dragState.shape != null) {
            listener.onCommitDrop(dragState.shape!!, -1, dragState.targetCol, dragState.targetRow)
            resetDrag()
        } else {
            SfxManager.playSfx(SfxType.INVALID_MOVE)
            resetDrag()
        }
        listener.requestInvalidate()
    }

    fun cancelExternalDrag() {
        resetDrag()
        listener.requestInvalidate()
    }

    private fun evaluatePlacement(shape: PolyShape, anchorCol: Int, anchorRow: Int) {
        dragState.isValidPlacement = false
        dragState.rowsToClearMask = 0
        dragState.colsToClearMask = 0
        dragState.totalLines = 0

        val isBombPlacement = shape.specialType == SpecialBlockType.QUANTUM_WARP_VORTEX ||
                shape.specialType == SpecialBlockType.CATALYST_CROSSHAIR ||
                shape.specialType == SpecialBlockType.NOVA_CORE_EXPLOSION

        val currentGrid = listener.getFlatGrid()
        val hazardGrid = listener.getHazardGrid()

        HologramLineSolver.evaluateCandidatePlacement(
            gridMatrix = currentGrid,
            shapeOffsets = shape.offsets,
            anchorCol = anchorCol,
            anchorRow = anchorRow,
            outState = previewState,
            isSpecial = isBombPlacement,
            hazardMatrix = hazardGrid
        )

        dragState.isValidPlacement = previewState.isValidPlacement
        dragState.rowsToClearMask = previewState.rowMask
        dragState.colsToClearMask = previewState.colMask
        dragState.totalLines = previewState.totalLines

        projectionEngine.calculateProjection(
            dragState = dragState,
            gridMatrix = currentGrid,
            isSpecial = isBombPlacement,
            hazardMatrix = hazardGrid
        )
    }
}
