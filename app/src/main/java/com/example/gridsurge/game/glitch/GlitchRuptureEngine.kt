package com.example.gridsurge.game.glitch

import com.example.gridsurge.game.glitch.model.GlitchRuptureEvent
import java.util.Random

class GlitchRuptureEngine(private val seedNumeric: Long) {

    private val rng = Random(seedNumeric)
    var systemPurity: Float = 1.0f
        private set
    var totalPurged: Int = 0
        private set

    // Track permanent dead cells created by unhandled ruptures
    val slagCellIndices = mutableSetOf<Int>()

    /**
     * Executes on every committed polyomino placement.
     * Evaluates countdowns and processes ruptures.
     */
    fun processMoveTick(
        activeCatalysts: MutableMap<Int, Int>, // index -> turnsRemaining
        onRupture: (GlitchRuptureEvent) -> Unit
    ): Boolean {
        val iterator = activeCatalysts.entries.iterator()
        var purityLost = 0f

        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.setValue(entry.value - 1)

            if (entry.value <= 0) {
                // Catalyst Ruptured: Transmute to Slag & Apply Purity Penalty
                val rupturedIdx = entry.key
                slagCellIndices.add(rupturedIdx)
                purityLost += 0.12f
                iterator.remove()

                onRupture(GlitchRuptureEvent(cellIndex = rupturedIdx))
            }
        }

        if (purityLost > 0f) {
            systemPurity = (systemPurity - purityLost).coerceAtLeast(0f)
        }

        return systemPurity <= 0f // Return true if system collapsed
    }

    /**
     * Line clears purge active catalysts and vaporize adjacent slag blocks.
     */
    fun processLineClear(clearedIndices: Set<Int>, activeCatalysts: MutableMap<Int, Int>): Int {
        var purgedCount = 0

        clearedIndices.forEach { idx ->
            if (activeCatalysts.remove(idx) != null) {
                purgedCount++
                totalPurged++
                // Successful purges restore 4% system purity
                systemPurity = (systemPurity + 0.04f).coerceAtMost(1.0f)
            }
            // Clear intersecting slag blocks
            slagCellIndices.remove(idx)
        }

        return purgedCount
    }

    fun reset(seed: Long) {
        systemPurity = 1.0f
        totalPurged = 0
        slagCellIndices.clear()
    }
}
