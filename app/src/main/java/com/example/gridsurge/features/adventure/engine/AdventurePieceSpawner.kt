package com.example.gridsurge.features.adventure.engine

import com.example.gridsurge.features.adventure.model.AdventureRunState
import com.example.gridsurge.features.adventure.model.AugmentType
import com.example.gridsurge.game.engine.BitboardFeasibilityEngine
import com.example.gridsurge.game.model.PolyShape
import com.example.gridsurge.game.model.PolyominoCatalog
import com.example.gridsurge.game.model.ShapeBlueprint
import com.example.gridsurge.game.model.SpecialBlockType
import com.example.gridsurge.game.spawner.PieceSpawner
import kotlin.random.Random

class AdventurePieceSpawner(
    private val runState: AdventureRunState
) : PieceSpawner {

    companion object {
        private const val CONGESTION_THRESHOLD = 0.60f
        private const val CAVITY_COMPRESSOR_THRESHOLD = 0.68f
        private const val WARP_COOLDOWN_MOVES = 5
    }

    var currentLevelNumber: Int = 1
    private var movesSinceLastWarp = WARP_COOLDOWN_MOVES
    private val prng = Random.Default

    override fun nextTray(boardOccupancy: Float, comboStreak: Int, boardMask: ULong): Array<PolyShape?> {
        // Attempt to generate a solvable tray (up to 8 tries for solvency)
        var tray = generateRawTray(boardOccupancy, comboStreak)
        var fittingCount = tray.filterNotNull().count { BitboardFeasibilityEngine.canPlaceShape(boardMask, it.offsets) }

        val targetSolvableCount = if (boardOccupancy > 0.75f) 2 else 1
        var tries = 0
        while (fittingCount < targetSolvableCount && boardOccupancy < 0.90f && tries < 8) {
            tray = generateRawTray(boardOccupancy, comboStreak)
            fittingCount = tray.filterNotNull().count { BitboardFeasibilityEngine.canPlaceShape(boardMask, it.offsets) }
            tries++
        }

        // If still stuck or board is very full, force saviors
        if (fittingCount < 1) {
            tray[0] = PolyominoCatalog.instantiate(PolyominoCatalog.FAMILY_DOT.random(prng))
            tray[1] = PolyominoCatalog.instantiate(PolyominoCatalog.FAMILY_DOMINO.random(prng))
            // Slot 2 remains whatever it was or a small piece
            if (!BitboardFeasibilityEngine.canPlaceShape(boardMask, tray[1]!!.offsets)) {
                 tray[1] = PolyominoCatalog.instantiate(PolyominoCatalog.FAMILY_DOT.random(prng))
            }
        }

        return tray
    }

    private fun generateRawTray(boardOccupancy: Float, comboStreak: Int): Array<PolyShape?> {
        val tray = arrayOfNulls<PolyShape>(3)
        val hasCavityCompressor = runState.hasAugment(AugmentType.CAVITY_COMPRESSOR)
        val isCongested = boardOccupancy >= (if (hasCavityCompressor) CAVITY_COMPRESSOR_THRESHOLD else CONGESTION_THRESHOLD)
        val isCritical = runState.isCriticalState || boardOccupancy >= 0.70f
        val isBoss = runState.isBossActive

        // 1. Onboarding Safe Camp (Levels 1 to 3)
        if (currentLevelNumber in 1..3) {
            tray[0] = PolyominoCatalog.instantiate(PolyominoCatalog.MICRO_FAMILIES.random(prng).random(prng))
            tray[1] = PolyominoCatalog.instantiate(PolyominoCatalog.TETRO_FAMILIES.random(prng).random(prng))
            tray[2] = PolyominoCatalog.instantiate(PolyominoCatalog.TETRO_FAMILIES.random(prng).random(prng))
            return tray
        }

        // 2. CAVITY COMPRESSOR PASS: Board congested -> Force 1x1, 2x1, and Small L pieces
        if (isCongested && hasCavityCompressor) {
            tray[0] = PolyominoCatalog.instantiate(PolyominoCatalog.MONOMINO_1X1)
            tray[1] = PolyominoCatalog.instantiate(PolyominoCatalog.FAMILY_DOMINO.random(prng))
            tray[2] = PolyominoCatalog.instantiate(PolyominoCatalog.FAMILY_TRIO_CORNER.random(prng))
            return tray
        }

        // Slot 0: Savior piece / Director Logic
        val microFam = if (isCritical || isBoss) {
            PolyominoCatalog.FAMILY_DOT
        } else {
            PolyominoCatalog.MICRO_FAMILIES.random(prng)
        }
        tray[0] = PolyominoCatalog.instantiate(microFam.random(prng))

        // Slot 1: Workhorse Tetromino (Filtered during Boss/Critical)
        val tetroPool = if (isCritical || isBoss) {
            PolyominoCatalog.TETRO_FAMILIES.filter { it != PolyominoCatalog.FAMILY_TETRO_I }
        } else {
            PolyominoCatalog.TETRO_FAMILIES
        }
        tray[1] = PolyominoCatalog.instantiate(tetPool(tetroPool).random(prng))

        // Slot 2: Wildcard or Heavy Piece (Blacklist during Boss/Critical)
        if (boardOccupancy > 0.82f && prng.nextFloat() < 0.20f) {
            // Savior Frame or DOT
            val roll = prng.nextFloat()
            tray[2] = if (roll < 0.5f) {
                PolyominoCatalog.instantiate(PolyominoCatalog.MONOMINO_1X1)
            } else {
                PolyominoCatalog.instantiate(PolyominoCatalog.FAMILY_SAVIOR_FRAME.random(prng))
            }
        } else if (isCongested) {
            val fallback = PolyominoCatalog.TETRO_FAMILIES.random(prng)
            tray[2] = PolyominoCatalog.instantiate(fallback.random(prng))
        } else {
            // Full random with Blacklist
            val heavyPool = mutableListOf<List<ShapeBlueprint>>()
            heavyPool.addAll(PolyominoCatalog.PENTO_FAMILIES)
            heavyPool.addAll(PolyominoCatalog.EXOTIC_FAMILIES)

            if (isCritical || isBoss) {
                // Remove PLUS_5, BOX_3X3, LINE_5
                heavyPool.remove(PolyominoCatalog.FAMILY_PENTO_PLUS)
                heavyPool.remove(PolyominoCatalog.FAMILY_PENTO_LINE)
                heavyPool.remove(PolyominoCatalog.FAMILY_HEAVY_BOX)
                // Add Savior frames to heavy slot if tight
                heavyPool.add(PolyominoCatalog.FAMILY_SAVIOR_FRAME)
            }

            if (heavyPool.isEmpty()) {
                 tray[2] = PolyominoCatalog.instantiate(PolyominoCatalog.TETRO_FAMILIES.random(prng).random(prng))
            } else {
                tray[2] = PolyominoCatalog.instantiate(heavyPool.random(prng).random(prng))
            }
        }

        return tray
    }

    private fun tetPool(families: List<List<ShapeBlueprint>>): List<ShapeBlueprint> {
        return families.flatten()
    }

    override fun onMoveCommitted() {
        movesSinceLastWarp++
    }

    override fun reset() {
        movesSinceLastWarp = WARP_COOLDOWN_MOVES
    }
}
