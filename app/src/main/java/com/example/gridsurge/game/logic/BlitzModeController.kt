package com.example.gridsurge.game.logic

import com.example.gridsurge.core.ClearResult
import com.example.gridsurge.core.GridEngine
import com.example.gridsurge.game.blitz.BlitzPieceSpawner
import com.example.gridsurge.game.blitz.TimeBlitzEngine
import com.example.gridsurge.game.fx.JuiceCoordinator

class BlitzModeController(
    private val engine: GridEngine,
    val blitzEngine: TimeBlitzEngine,
    private val juiceCoordinator: JuiceCoordinator
) {
    val spawner = BlitzPieceSpawner()

    fun processMove(result: ClearResult, comboStreak: Int): Long {
        val resolution = blitzEngine.onMoveResolved(
            linesCleared = result.totalLines,
            comboStreak = comboStreak
        )
        return blitzEngine.blitzScore
    }

    fun reset() {
        blitzEngine.reset()
        spawner.reset()
    }
}
