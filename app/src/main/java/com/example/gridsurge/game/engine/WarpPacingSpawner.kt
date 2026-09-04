package com.example.gridsurge.game.engine

import com.example.gridsurge.R
import com.example.gridsurge.core.ShapeRegistry
import com.example.gridsurge.game.model.PolyOffset
import com.example.gridsurge.game.model.PolyShape
import com.example.gridsurge.game.model.SpecialBlockType
import kotlin.random.Random

/**
 * Implements a 14-piece bag spawner (12 standard, 2 warp) with a 5-move cooldown
 * between Warp Vortex pieces.
 */
class WarpPacingSpawner {
    private val bag = mutableListOf<PolyShape>()
    private var movesSinceLastWarp = 99 // Start high so warp can spawn early

    fun onMoveCommitted() {
        movesSinceLastWarp++
    }

    fun replenishDock(prng: Random = Random.Default): Array<PolyShape?> {
        val tray = arrayOfNulls<PolyShape>(3)
        for (i in 0 until 3) {
            tray[i] = dispenseNext(prng)
        }
        return tray
    }

    private fun dispenseNext(prng: Random): PolyShape {
        if (bag.isEmpty()) {
            refillBag(prng)
        }

        var index = 0
        var shape = bag[index]

        // Check Warp Cooldown
        if (shape.specialType == SpecialBlockType.QUANTUM_WARP_VORTEX && movesSinceLastWarp < 5) {
            // Find first non-warp piece to swap
            val fallbackIndex = bag.indexOfFirst { it.specialType != SpecialBlockType.QUANTUM_WARP_VORTEX }
            if (fallbackIndex != -1) {
                index = fallbackIndex
                shape = bag[index]
            }
        }

        bag.removeAt(index)
        if (shape.specialType == SpecialBlockType.QUANTUM_WARP_VORTEX) {
            movesSinceLastWarp = 0
        }
        return shape
    }

    private fun refillBag(prng: Random) {
        val pool = mutableListOf<PolyShape>()
        // 12 Standard Pieces
        val standardPool = ShapeRegistry.ALL_STANDARD_SHAPES
        repeat(12) {
            pool.add(standardPool.random(prng))
        }
        // 2 Warp Pieces
        repeat(2) {
            pool.add(PolyShape(
                id = "special_warp_1x1",
                offsets = listOf(PolyOffset(0, 0)),
                color = 0xFFEA80FC.toInt(),
                isSpecial = true,
                specialType = SpecialBlockType.QUANTUM_WARP_VORTEX,
                textureResId = R.drawable.skin_warp_block
            ))
        }
        pool.shuffle(prng)
        bag.addAll(pool)
    }
}
