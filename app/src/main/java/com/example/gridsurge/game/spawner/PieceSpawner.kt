package com.example.gridsurge.game.spawner

import com.example.gridsurge.game.model.PolyShape

interface PieceSpawner {
    fun nextTray(boardOccupancy: Float, comboStreak: Int, boardMask: ULong): Array<PolyShape?>
    fun onMoveCommitted()
    fun reset()
}
