package com.example.gridsurge.game.logic

import com.example.gridsurge.game.glitch.GlitchPieceSpawner
import com.example.gridsurge.game.glitch.GlitchEngine
import com.example.gridsurge.core.ClearResult
import com.example.gridsurge.core.GridEngine

import com.example.gridsurge.game.fx.JuiceCoordinator

class GlitchModeController(
    private val engine: GridEngine,
    private val glitchEngine: GlitchEngine,
    private val juice: JuiceCoordinator
) {
    val spawner = GlitchPieceSpawner()

    fun processMove(result: ClearResult): Int {
        val turnResult = glitchEngine.onTurnResolved(
            clearedRows = result.clearedRows,
            clearedCols = result.clearedCols,
            currentGrid = engine.getGridArray()
        )

        if (turnResult.purgedCount > 0) {
            juice.triggerShake(0.5f)
        }

        turnResult.spreadEvents.forEach { event ->
            juice.spawnCorruptionSpread(event.fromIndex, event.toIndex)
        }

        glitchEngine.activeInfections.forEach { (index, _) ->
            val cx = index % 8
            val cy = index / 8
            if (engine.getGridValue(cx, cy) == 0) {
                engine.setGridValue(cx, cy, 9)
            }
        }
        return turnResult.purgedCount
    }
}
