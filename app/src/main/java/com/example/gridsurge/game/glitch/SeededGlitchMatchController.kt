package com.example.gridsurge.game.glitch

import com.example.gridsurge.core.ShapeRegistry
import com.example.gridsurge.game.model.PolyShape
import kotlin.random.Random

class SeededGlitchMatchController(
    val seed: Long = DailyGlitchSeeder.getDailySeed()
) {
    private val prng = Random(seed)
    val glitchEngine = GlitchEngine(gridSize = 8)

    var currentScore: Long = 0L
    var isAttemptUsed: Boolean = false
    var wavesCleared: Int = 0

    fun startDailyMatch(gridMatrix: IntArray) {
        gridMatrix.fill(0)
        // Initialize engine with our seeded prng
        glitchEngine.initializeGlitchMatch(prng)
        
        // Note: GlitchEngine.initializeGlitchMatch calls startNewOutbreakWave()
        // which already places 2 nodes in the central area using the same prng.
        // We ensure they are reflected in the gridMatrix.
        for (entry in glitchEngine.activeInfections) {
            gridMatrix[entry.key] = 9 // SPECIAL_CORE ID
        }
    }

    /**
     * Generates the next 3 pieces deterministically from the daily seed.
     */
    fun generateNextDeterministicTray(): Array<PolyShape?> {
        val tray = arrayOfNulls<PolyShape>(3)
        val allShapes = ShapeRegistry.TIER_1_SAVIORS + ShapeRegistry.TIER_2_WORKERS + ShapeRegistry.TIER_3_HEAVIES
        for (i in 0 until 3) {
            tray[i] = allShapes[prng.nextInt(allShapes.size)]
        }
        return tray
    }
}
