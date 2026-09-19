package com.example.gridsurge.game.logic

import android.graphics.Color
import com.example.gridsurge.core.ClearResult
import com.example.gridsurge.core.GridEngine
import com.example.gridsurge.game.blitz.BlitzPieceSpawner
import com.example.gridsurge.game.blitz.TimeBlitzEngine
import com.example.gridsurge.game.blitz.model.ChronoRefundTier
import com.example.gridsurge.game.fx.JuiceCoordinator

class BlitzModeController(
    private val engine: GridEngine,
    val blitzEngine: TimeBlitzEngine,
    private val juiceCoordinator: JuiceCoordinator
) {
    val spawner = BlitzPieceSpawner()

    fun processMove(result: ClearResult, comboStreak: Int): Long {
        blitzEngine.onPieceCommitted()

        if (result.totalLines > 0) {
            val secondsRefund = blitzEngine.onLinesCleared(result.totalLines, comboStreak)
            if (secondsRefund > 0) {
                val tier = when (result.totalLines) {
                    2 -> ChronoRefundTier.DOUBLE_CLEAR
                    3 -> ChronoRefundTier.TRIPLE_CLEAR
                    else -> ChronoRefundTier.MEGA_BLITZ
                }
                val colorInt = when (result.totalLines) {
                    2 -> Color.parseColor("#00E5FF")
                    3 -> Color.parseColor("#FFD600")
                    else -> Color.parseColor("#FF1744")
                }

                juiceCoordinator.spawnPopup(
                    juiceCoordinator.boardRect.centerX(),
                    juiceCoordinator.boardRect.centerY() - 40f,
                    "${tier.tag} (+${secondsRefund}s)",
                    colorInt,
                    1200L
                )
            }
        } else {
            blitzEngine.onMoveResolved(0, comboStreak)
        }

        return blitzEngine.blitzScore
    }

    fun reset() {
        blitzEngine.reset()
        spawner.reset()
    }
}
