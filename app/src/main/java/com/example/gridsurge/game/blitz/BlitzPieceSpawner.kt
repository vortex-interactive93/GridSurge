package com.example.gridsurge.game.blitz

import com.example.gridsurge.game.model.PolyShape
import com.example.gridsurge.game.model.PolyominoCatalog
import com.example.gridsurge.game.spawner.PieceSpawner
import kotlin.random.Random

class BlitzPieceSpawner : PieceSpawner {

    private val prng = Random.Default

    override fun nextTray(boardOccupancy: Float, comboStreak: Int, boardMask: ULong): Array<PolyShape?> {
        val tray = arrayOfNulls<PolyShape>(3)

        // Under high congestion (>= 50%), dispense 2 micros to maintain speed
        if (boardOccupancy >= 0.50f) {
            tray[0] = PolyominoCatalog.instantiate(PolyominoCatalog.MICRO_FAMILIES.random(prng).random(prng))
            tray[1] = PolyominoCatalog.instantiate(PolyominoCatalog.MICRO_FAMILIES.random(prng).random(prng))
            tray[2] = PolyominoCatalog.instantiate(PolyominoCatalog.TETRO_FAMILIES.random(prng).random(prng))
        } else {
            tray[0] = PolyominoCatalog.instantiate(PolyominoCatalog.MICRO_FAMILIES.random(prng).random(prng))
            tray[1] = PolyominoCatalog.instantiate(PolyominoCatalog.TETRO_FAMILIES.random(prng).random(prng))
            tray[2] = PolyominoCatalog.instantiate(PolyominoCatalog.TETRO_FAMILIES.random(prng).random(prng))
        }

        return tray
    }

    override fun onMoveCommitted() {}
    override fun reset() {}
}
