package com.example.gridsurge.game.glitch

import kotlin.random.Random

object DailyGlitchSeeder {

    /**
     * Generates a deterministic daily integer seed based on UTC date (e.g. 20260825).
     */
    fun getDailySeed(): Long {
        // Persistent 24-hour epoch seed
        return System.currentTimeMillis() / 86400000L
    }

    /**
     * Creates a seeded PRNG instance for generating identical piece sequences and virus nodes.
     */
    fun getDailyRandom(): Random = Random(getDailySeed())
}
