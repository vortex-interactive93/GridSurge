package com.example.gridsurge.game.glitch

import com.example.gridsurge.game.glitch.model.*
import java.util.Random

class DailyGlitchEngine(private val seedMetadata: DailySeedMetadata) {

    private val deterministicRandom = Random(seedMetadata.seedNumeric)
    val activeCatalysts = mutableMapOf<Int, GlitchCatalyst>()
    
    var purity: Float = 1.0f
        private set
    var purgedCount: Int = 0
        private set
    var isCompromised: Boolean = false
        private set

    init {
        initializeDailyBoard()
    }

    /**
     * Seeds identical initial anomaly clusters for every player worldwide.
     */
    fun initializeDailyBoard() {
        activeCatalysts.clear()
        purity = 1.0f
        purgedCount = 0
        isCompromised = false

        // Pick 4 deterministic anchor coordinates (avoiding edges)
        val initialIndices = mutableSetOf<Int>()
        while (initialIndices.size < 4) {
            val r = 1 + deterministicRandom.nextInt(6)
            val c = 1 + deterministicRandom.nextInt(6)
            initialIndices.add(r * 8 + c)
        }

        initialIndices.forEach { idx ->
            activeCatalysts[idx] = GlitchCatalyst(
                index = idx,
                phase = AnomalyCellPhase.DORMANT,
                turnsRemaining = 3 + deterministicRandom.nextInt(2)
            )
        }
    }

    /**
     * Ticks anomaly countdowns on every polyomino placement.
     * Evaluates rupture and contagion spread if catalysts hit 0.
     */
    fun onMoveCommitted(): List<Int> {
        val rupturedIndices = mutableListOf<Int>()

        val iterator = activeCatalysts.values.iterator()
        while (iterator.hasNext()) {
            val catalyst = iterator.next()
            if (!catalyst.isSlag) {
                catalyst.turnsRemaining--
                when (catalyst.turnsRemaining) {
                    2 -> catalyst.phase = AnomalyCellPhase.UNSTABLE
                    1 -> catalyst.phase = AnomalyCellPhase.CRITICAL
                    0 -> {
                        // MUTATE INTO OBSIDIAN SLAG (DO NOT REMOVE!)
                        catalyst.phase = AnomalyCellPhase.RUPTURED
                        catalyst.isSlag = true
                        rupturedIndices.add(catalyst.index)
                    }
                }
            }
        }

        // Rupture Consequence: Deduct Purity & Contaminate Adjacent Cells
        if (rupturedIndices.isNotEmpty()) {
            purity = (purity - (rupturedIndices.size * 0.12f)).coerceAtLeast(0f)
            if (purity <= 0f) {
                isCompromised = true
            }
            // Contagion: Spawn new anomaly adjacent to ruptured cell
            rupturedIndices.forEach { rIdx ->
                spreadContagion(rIdx)
            }
        }

        return rupturedIndices
    }

    private fun spreadContagion(originIdx: Int) {
        if (activeCatalysts.size >= 8) return // Clamp maximum concurrency

        val r = originIdx / 8
        val c = originIdx % 8
        val neighbors = listOf(
            (r - 1) * 8 + c,
            (r + 1) * 8 + c,
            r * 8 + (c - 1),
            r * 8 + (c + 1)
        ).filter { it in 0..63 && !activeCatalysts.containsKey(it) }

        if (neighbors.isNotEmpty()) {
            val target = neighbors[deterministicRandom.nextInt(neighbors.size)]
            activeCatalysts[target] = GlitchCatalyst(
                index = target,
                phase = AnomalyCellPhase.UNSTABLE,
                turnsRemaining = 2
            )
        }
    }

    /**
     * Resolves line clears intersecting anomaly catalysts & slag blocks.
     */
    fun onLinesCleared(clearedCellIndices: Set<Int>): Pair<Int, Int> {
        var purgedThisTurn = 0
        var slagDissolved = 0
        clearedCellIndices.forEach { idx ->
            val catalyst = activeCatalysts[idx]
            if (catalyst != null) {
                if (catalyst.isSlag) {
                    slagDissolved++
                } else {
                    purgedThisTurn++
                    purgedCount++
                    // Line clears restore 4% system purity
                    purity = (purity + 0.04f).coerceAtMost(1.0f)
                }
                activeCatalysts.remove(idx)
            }
        }
        return Pair(purgedThisTurn, slagDissolved)
    }
}
