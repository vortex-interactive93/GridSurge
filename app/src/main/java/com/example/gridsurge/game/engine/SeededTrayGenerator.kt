package com.example.gridsurge.game.engine

import com.example.gridsurge.core.ShapeRegistry
import com.example.gridsurge.game.model.PolyShape
import kotlin.random.Random

class SeededTrayGenerator(matchSeed: Long) {
    private val rng = Random(matchSeed)
    private var heavyCooldownRounds = 0

    fun nextTrayTrio(): Array<PolyShape?> {
        val trio = arrayOfNulls<PolyShape>(3)
        var tier4Included = false

        // Slot 1: Micro Fillers / Low-friction builders (40% Tier 1 Saviors, 60% Tier 2 Workers)
        val slot1Pool = if (rng.nextFloat() < 0.40f) ShapeRegistry.TIER_1_SAVIORS else ShapeRegistry.TIER_2_WORKERS
        trio[0] = slot1Pool.random(rng)

        // Slot 2: Core Tactical / Maneuvers (50% Tier 2 Workers, 50% Tier 2/3)
        val slot2Pool = if (rng.nextFloat() < 0.50f) ShapeRegistry.TIER_2_WORKERS else ShapeRegistry.TIER_2_WORKERS
        trio[1] = slot2Pool.random(rng)

        // Slot 3: Wildcard with Heavy Piece Lockout (Max 1 Heavy Piece per tray + 2-round cooldown)
        val roll = rng.nextFloat()
        if (roll < 0.18f && heavyCooldownRounds <= 0 && !tier4Included) {
            tier4Included = true
            heavyCooldownRounds = 2 // Lock out heavy pieces for next 2 tray refills
            trio[2] = ShapeRegistry.TIER_3_HEAVIES.random(rng)
        } else if (roll < 0.45f) {
            trio[2] = ShapeRegistry.TIER_1_SAVIORS.random(rng)
        } else {
            trio[2] = ShapeRegistry.TIER_2_WORKERS.random(rng)
        }

        if (!tier4Included && heavyCooldownRounds > 0) {
            heavyCooldownRounds--
        }

        return trio
    }
}
