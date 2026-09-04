package com.example.gridsurge.game.glitch

import com.example.gridsurge.game.model.PolyShape
import com.example.gridsurge.game.model.PolyominoCatalog
import com.example.gridsurge.game.model.SpecialBlockFeatureConfig
import com.example.gridsurge.game.model.SpecialBlockType
import com.example.gridsurge.game.spawner.PieceSpawner
import kotlin.random.Random

class GlitchPieceSpawner : PieceSpawner {

    private val prng = Random.Default

    override fun nextTray(boardOccupancy: Float, comboStreak: Int, boardMask: ULong): Array<PolyShape?> {
        val tray = arrayOfNulls<PolyShape>(3)

        tray[0] = PolyominoCatalog.instantiate(PolyominoCatalog.MICRO_FAMILIES.random(prng).random(prng))
        tray[1] = PolyominoCatalog.instantiate(PolyominoCatalog.TETRO_FAMILIES.random(prng).random(prng))

        // Slot 2: Catalyst Crosshair if enabled, otherwise a Tetromino
        tray[2] = if (SpecialBlockFeatureConfig.isCatalystCrosshairEnabled && prng.nextFloat() < 0.25f) {
            PolyominoCatalog.instantiateSpecial(SpecialBlockType.CATALYST_CROSSHAIR)
        } else {
            PolyominoCatalog.instantiate(PolyominoCatalog.TETRO_FAMILIES.random(prng).random(prng))
        }

        return tray
    }

    override fun onMoveCommitted() {}
    override fun reset() {}
}
