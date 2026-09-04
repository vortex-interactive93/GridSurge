package com.example.gridsurge.core

import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.core.CellType
import com.example.gridsurge.game.model.DetonationEffect
import com.example.gridsurge.game.model.DetonationTarget
import com.example.gridsurge.game.model.PolyOffset
import com.example.gridsurge.game.model.PolyShape
import com.example.gridsurge.game.model.SpecialBlockType

data class ClearResult(
    val clearedRows: List<Int>,
    val clearedCols: List<Int>,
    val totalLines: Int,
    val pointsEarned: Int,
    val clearedCells: List<ClearedCell> = emptyList(),
    val damagedCores: List<Int> = emptyList(),
    val destroyedCores: List<Int> = emptyList()
)

data class ClearedCell(
    val x: Int,
    val y: Int,
    val color: Int,
    val specialType: SpecialBlockType = SpecialBlockType.NONE
)

class GridEngine {
    private val gridSize = 8
    private val grid = IntArray(64) { CellType.EMPTY.id }
    private val colorGrid = IntArray(64) { 0 }
    private val specialGrid = Array(64) { SpecialBlockType.NONE }
    
    val dock = arrayOfNulls<PolyShape>(3)
    val comboManager = ComboStateManager(maxGraceMoves = 2)
    var score: Long = 0L
    val comboStreak: Int get() = comboManager.currentStreak
    var destroyedCoresCount = 0
    var activeThemeKey: String = "digital"

    companion object {
        private const val MAX_SAFE_SCORE = Long.MAX_VALUE - 1_000_000L
    }

    fun resetGame() {
        grid.fill(CellType.EMPTY.id)
        colorGrid.fill(0)
        specialGrid.fill(SpecialBlockType.NONE)
        score = 0L
        comboManager.reset()
        destroyedCoresCount = 0
    }

    fun canPlaceShape(offsets: List<PolyOffset>, anchorCol: Int, anchorRow: Int): Boolean {
        for (offset in offsets) {
            val c = anchorCol + offset.x
            val r = anchorRow + offset.y
            if (c !in 0 until gridSize || r !in 0 until gridSize) return false
            if (grid[r * gridSize + c] != CellType.EMPTY.id) return false
        }
        return true
    }

    fun placeShape(dockIndex: Int, anchorCol: Int, anchorRow: Int): ClearResult {
        val shape = dock[dockIndex] ?: throw IllegalStateException("Dock index $dockIndex is empty")

        // 1. Strict 2D Bounds and Placement Validation
        for (offset in shape.offsets) {
            val c = anchorCol + offset.x
            val r = anchorRow + offset.y
            require(c in 0 until gridSize && r in 0 until gridSize) {
                "Placement out of bounds: ($c, $r) on shape ${shape.id}"
            }
            require(grid[r * gridSize + c] == CellType.EMPTY.id) {
                "Cell already occupied at ($c, $r)"
            }
        }

        // 2. Commit shape directly using canonical offsets
        val placePoints = (shape.offsets.size * 15 * (1f + 0.1f * comboStreak)).toInt()
        score += placePoints

        for (offset in shape.offsets) {
            val c = anchorCol + offset.x
            val r = anchorRow + offset.y
            val idx = r * gridSize + c
            grid[idx] = CellType.STANDARD_BLOCK.id
            colorGrid[idx] = shape.color
            specialGrid[idx] = shape.specialType
        }

        dock[dockIndex] = null

        // 3. Evaluate Line Clears
        val rowsToClear = mutableListOf<Int>()
        val colsToClear = mutableListOf<Int>()

        for (r in 0 until gridSize) {
            var full = true
            for (c in 0 until gridSize) {
                if (grid[r * gridSize + c] == CellType.EMPTY.id) {
                    full = false
                    break
                }
            }
            if (full) rowsToClear.add(r)
        }

        for (c in 0 until gridSize) {
            var full = true
            for (r in 0 until gridSize) {
                if (grid[r * gridSize + c] == CellType.EMPTY.id) {
                    full = false
                    break
                }
            }
            if (full) colsToClear.add(c)
        }

        val totalLines = rowsToClear.size + colsToClear.size
        val clearedCells = mutableListOf<ClearedCell>()
        val damagedCores = mutableListOf<Int>()
        val destroyedCores = mutableListOf<Int>()

        val comboResult = comboManager.onMoveCommitted(totalLines)
        var pointsEarned = placePoints

        if (totalLines > 0) {
            val affectedIndices = mutableSetOf<Int>()
            for (row in rowsToClear) {
                for (c in 0 until gridSize) affectedIndices.add(row * gridSize + c)
            }
            for (col in colsToClear) {
                for (r in 0 until gridSize) affectedIndices.add(r * gridSize + col)
            }

            for (idx in affectedIndices) {
                val value = grid[idx]
                if (CellType.isCore(value)) {
                    damagedCores.add(idx)
                } else if (value != CellType.EMPTY.id) {
                    clearedCells.add(ClearedCell(idx % gridSize, idx / gridSize, colorGrid[idx], specialGrid[idx]))
                    grid[idx] = CellType.EMPTY.id
                    colorGrid[idx] = 0
                    specialGrid[idx] = SpecialBlockType.NONE
                }
            }

            val currentStreak = comboResult.currentStreak
            val currentMultiplier = comboManager.comboMultiplier
            
            // Streak Multiplier for base line points: 1.0, 1.2, 1.4 ... capped at 5.0x
            val streakMultiplier = Math.min(1.0 + (currentStreak * 0.2), 5.0).toFloat()
            val lineScore = (totalLines * 250 * streakMultiplier * currentMultiplier).toLong()
            
            score = if (score > MAX_SAFE_SCORE - lineScore) MAX_SAFE_SCORE else score + lineScore
            pointsEarned = (placePoints + lineScore).toInt()

            SfxManager.playLineClear(activeThemeKey, totalLines, currentStreak)
            SfxManager.evaluateComboProgression(currentStreak, grid.all { it == CellType.EMPTY.id }, isClashMode = SfxManager.isPvpModeActive)
        }

        return ClearResult(rowsToClear, colsToClear, totalLines, pointsEarned, clearedCells, damagedCores, destroyedCores)
    }

    fun resolveWarpDetonation(centerCol: Int, centerRow: Int): List<DetonationTarget> {
        val targets = mutableListOf<DetonationTarget>()
        for (dy in -1..1) {
            for (dx in -1..1) {
                val c = centerCol + dx
                val r = centerRow + dy
                if (c in 0 until gridSize && r in 0 until gridSize) {
                    val idx = r * gridSize + c
                    val value = grid[idx]
                    if (value == CellType.EMPTY.id) continue

                    val effect = if (CellType.isCore(value)) {
                        DetonationEffect.STRAIN
                    } else {
                        DetonationEffect.SUCTION
                    }
                    targets.add(DetonationTarget(r, c, effect))
                }
            }
        }
        return targets
    }

    fun damageCore(index: Int): Boolean {
        val value = grid[index]
        return when (value) {
            CellType.CORE_INTACT.id -> {
                grid[index] = CellType.CORE_CRACKED.id
                false
            }
            CellType.CORE_CRACKED.id -> {
                grid[index] = CellType.EMPTY.id
                colorGrid[index] = 0
                specialGrid[index] = SpecialBlockType.NONE
                destroyedCoresCount++
                true
            }
            else -> false
        }
    }

    fun getGridValue(x: Int, y: Int): Int = grid[y * gridSize + x]
    fun setGridValue(x: Int, y: Int, value: Int) {
        grid[y * gridSize + x] = value
    }
    fun getCellColor(x: Int, y: Int): Int = colorGrid[y * gridSize + x]
    fun setCellColor(x: Int, y: Int, color: Int) {
        colorGrid[y * gridSize + x] = color
    }
    fun getSpecialValue(index: Int): SpecialBlockType = specialGrid[index]
    fun getGridArray(): IntArray = grid
    
    fun getGridMatrix(): Array<IntArray> {
        val matrix = Array(gridSize) { IntArray(gridSize) }
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                matrix[r][c] = grid[r * gridSize + c]
            }
        }
        return matrix
    }

    fun getOccupiedRatio(): Float {
        return grid.count { it != CellType.EMPTY.id } / 64f
    }
}
