package com.example.gridsurge.game.spawner

import com.example.gridsurge.game.model.PolyShape
import com.example.gridsurge.game.model.PolyominoCatalog
import kotlin.random.Random

/**
 * Smart Classic Mode Piece Spawner.
 * Enforces the Trio Balance Architecture (Anchor + Slicer + Lifeline),
 * Anti-Monotony Laws (Deduplication & Heavy Shape Hard-Cap),
 * and Slot-Shuffling.
 */
class ClassicPieceSpawner : PieceSpawner {

    companion object {
        private const val CONGESTION_THRESHOLD = 0.65f
        private const val PENTO_UNLOCK_THRESHOLD = 0.40f
    }

    private val prng = Random.Default

    // Anchor Pool (Space occupiers: 2x2 Square, T, L, J, 3x2 Rect)
    private val anchorFamilies = listOf(
        PolyominoCatalog.FAMILY_TETRO_O,
        PolyominoCatalog.FAMILY_TETRO_T,
        PolyominoCatalog.FAMILY_TETRO_L,
        PolyominoCatalog.FAMILY_TETRO_J,
        PolyominoCatalog.FAMILY_HEAVY_RECT
    )

    // Slicer Pool (Straight line clearers: 2-bar, 3-bar, 4-bar, 5-bar)
    private val slicerFamilies = listOf(
        PolyominoCatalog.FAMILY_DOMINO,
        PolyominoCatalog.FAMILY_TRIO_LINE,
        PolyominoCatalog.FAMILY_TETRO_I,
        PolyominoCatalog.FAMILY_PENTO_LINE
    )

    // Lifeline / Balancer Pool (Small rescue pieces: 1x1 Dot, Domino, Trio Corner)
    private val lifelineFamilies = listOf(
        PolyominoCatalog.FAMILY_DOT,
        PolyominoCatalog.FAMILY_DOMINO,
        PolyominoCatalog.FAMILY_TRIO_CORNER
    )

    override fun nextTray(boardOccupancy: Float, comboStreak: Int, boardMask: ULong): Array<PolyShape?> {
        val rawDeal = arrayOfNulls<PolyShape>(3)
        val chosenFamilyKeys = mutableSetOf<String>()

        val isCongested = boardOccupancy >= CONGESTION_THRESHOLD
        val canSpawnPento = boardOccupancy >= PENTO_UNLOCK_THRESHOLD && !isCongested

        // 1. Tool A — The Anchor (Space Occupier)
        val availableAnchors = anchorFamilies.filterNot { getFamilyKey(it.first().id) in chosenFamilyKeys }
        val anchorFamily = availableAnchors.random(prng)
        val anchorBlueprint = anchorFamily.random(prng)
        rawDeal[0] = PolyominoCatalog.instantiate(anchorBlueprint)
        chosenFamilyKeys.add(getFamilyKey(anchorBlueprint.id))

        // 2. Tool B — The Slicer (Straight Line Cleaner)
        val availableSlicers = slicerFamilies
            .filterNot { getFamilyKey(it.first().id) in chosenFamilyKeys }
            .filter { family -> if (!canSpawnPento) family != PolyominoCatalog.FAMILY_PENTO_LINE else true }
        val slicerFamily = (if (availableSlicers.isNotEmpty()) availableSlicers else slicerFamilies).random(prng)
        val slicerBlueprint = slicerFamily.random(prng)
        rawDeal[1] = PolyominoCatalog.instantiate(slicerBlueprint)
        chosenFamilyKeys.add(getFamilyKey(slicerBlueprint.id))

        // 3. Tool C — The Lifeline / Balancer (Small Rescue Piece)
        val availableLifelines = lifelineFamilies.filterNot { getFamilyKey(it.first().id) in chosenFamilyKeys }
        val lifelineFamily = (if (availableLifelines.isNotEmpty()) availableLifelines else lifelineFamilies).random(prng)
        val lifelineBlueprint = lifelineFamily.random(prng)
        rawDeal[2] = PolyominoCatalog.instantiate(lifelineBlueprint)

        // 4. Slot-Shuffling: Randomly shuffle the 3 tools across the tray slots
        val shuffledIndices = listOf(0, 1, 2).shuffled(prng)
        val finalTray = arrayOfNulls<PolyShape>(3)
        for (i in 0..2) {
            finalTray[i] = rawDeal[shuffledIndices[i]]
        }

        return finalTray
    }

    private fun getFamilyKey(shapeId: String): String {
        return shapeId.split("_").take(2).joinToString("_")
    }

    override fun onMoveCommitted() {}
    override fun reset() {}
}
