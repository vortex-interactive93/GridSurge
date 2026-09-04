package com.example.gridsurge.game.logic

import kotlin.random.Random

/**
 * Generates start-of-match tactical seed layouts for Classic Mode.
 * 50% Pristine empty board / 50% Tactical Seed Layouts (Scatter, Corners, Center Island, Pillars).
 */
object ClassicSeedGenerator {

    enum class SeedType {
        PRISTINE,        // Pure empty 8x8 grid
        SCATTER_SEED,    // 5-6 isolated single blocks
        CORNER_ANCHORS,  // 4 corner block clusters
        CENTER_ISLAND,   // 2x2 central block cluster
        PILLAR_COLUMNS   // Two 2-cell vertical pillars
    }

    data class SeedResult(
        val seedType: SeedType,
        val initialBlockCount: Int,
        val grid: Array<IntArray>, // 8x8 color IDs
        val preSeededMask: Array<BooleanArray> // 8x8 discrete pre-seeded tile tracking mask
    )

    fun generateSeed(prng: Random = Random.Default): SeedResult {
        val isPristine = prng.nextBoolean() // 50% chance pristine
        val grid = Array(8) { IntArray(8) { 0 } }
        val mask = Array(8) { BooleanArray(8) { false } }

        if (isPristine) {
            return SeedResult(SeedType.PRISTINE, 0, grid, mask)
        }

        val type = SeedType.entries.filter { it != SeedType.PRISTINE }.random(prng)
        var count = 0

        val seedColors = intArrayOf(1, 2, 3, 4, 5)

        when (type) {
            SeedType.SCATTER_SEED -> {
                val targets = listOf(
                    1 to 2, 2 to 5, 5 to 1, 6 to 4, 3 to 6, 4 to 3
                ).shuffled(prng).take(5)
                targets.forEach { (r, c) ->
                    grid[r][c] = seedColors.random(prng)
                    mask[r][c] = true
                    count++
                }
            }
            SeedType.CORNER_ANCHORS -> {
                val corners = listOf(0 to 0, 0 to 7, 7 to 0, 7 to 7)
                corners.forEach { (r, c) ->
                    grid[r][c] = seedColors.random(prng)
                    mask[r][c] = true
                    count++
                }
            }
            SeedType.CENTER_ISLAND -> {
                val center = listOf(3 to 3, 3 to 4, 4 to 3, 4 to 4)
                center.forEach { (r, c) ->
                    grid[r][c] = seedColors.random(prng)
                    mask[r][c] = true
                    count++
                }
            }
            SeedType.PILLAR_COLUMNS -> {
                val pillars = listOf(2 to 1, 3 to 1, 4 to 6, 5 to 6)
                pillars.forEach { (r, c) ->
                    grid[r][c] = seedColors.random(prng)
                    mask[r][c] = true
                    count++
                }
            }
            else -> {}
        }

        return SeedResult(type, count, grid, mask)
    }
}
