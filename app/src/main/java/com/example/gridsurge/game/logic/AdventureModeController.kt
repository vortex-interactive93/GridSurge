package com.example.gridsurge.game.logic

import android.graphics.Color
import com.example.gridsurge.core.ClearResult
import com.example.gridsurge.features.adventure.core.AdventureBoardManager
import com.example.gridsurge.features.adventure.engine.BossBattleEngine
import com.example.gridsurge.features.adventure.model.AdventureRunState
import com.example.gridsurge.features.adventure.model.BossPhase
import com.example.gridsurge.features.adventure.model.LevelNodeSpec
import com.example.gridsurge.core.GridEngine

import com.example.gridsurge.features.adventure.engine.AdventurePieceSpawner
import com.example.gridsurge.game.fx.JuiceCoordinator

class AdventureModeController(
    private val engine: GridEngine,
    private val adventureBoard: AdventureBoardManager,
    private val bossEngine: BossBattleEngine,
    private val runState: AdventureRunState,
    private val juice: JuiceCoordinator
) {
    val spawner = AdventurePieceSpawner(runState)

    fun processMove(result: ClearResult, elapsedSeconds: Int, currentScore: Long): Long {
        var updatedScore = currentScore
        
        if (result.totalLines > 0) {
            val laserColor = if (result.totalLines >= 2) 0xFF00E5FF.toInt() else 0xFF00FF66.toInt()
            juice.spawnLaserVfx(result.clearedRows.fold(0) { acc, r -> acc or (1 shl r) }, result.clearedCols.fold(0) { acc, c -> acc or (1 shl c) }, laserColor)
            if (result.totalLines >= 3) juice.triggerShake(0.6f)
            
            // P2 Fix: Localized Core Impact VFX to prevent "Vanish" feel
            result.damagedCores.forEach { idx ->
                juice.spawnBurstParticlesForCell(idx, Color.WHITE, 12)
            }
        }

        // 1. Sync blocks to adventure board
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                val v = engine.getGridValue(c, r)
                val cell = adventureBoard.grid[r][c]
                if (!cell.isCore) {
                    if (v != 0) {
                        cell.isFilled = true
                        cell.blockColor = engine.getCellColor(c, r)
                    } else {
                        cell.isFilled = false
                    }
                }
            }
        }

        // 2. Process clears
        adventureBoard.processLineClears(result.clearedRows, result.clearedCols, elapsedSeconds, updatedScore)
        adventureBoard.onComboCommitted(engine.comboManager.currentStreak, elapsedSeconds)

        // 3. Neural Augments (Simplified call for now, can be extracted further)
        // ...

        // 4. Sync back to engine
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                val cell = adventureBoard.grid[r][c]
                if (cell.isCore) {
                    engine.setGridValue(c, r, cell.toCellTypeValue())
                } else if (engine.getGridValue(c, r) < 0) { // If engine thinks it's a core but board doesn't
                    engine.setGridValue(c, r, 0)
                }
            }
        }
        
        // 5. Boss Logic
        val damage = bossEngine.onLinesCleared(result.clearedRows, result.clearedCols, engine)
        if (damage > 0) {
            // Damage handling handled in view via callbacks
        }

        return updatedScore
    }
}
