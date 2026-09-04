package com.example.gridsurge.features.adventure.model

import androidx.compose.runtime.Immutable
import com.example.gridsurge.game.model.PolyShape

enum class BossPhase {
    SHIELDED,              // Phase 1: 4 corner shield pylons protect the central core
    OVERDRIVE_VULNERABLE,  // Phase 2: Shield collapsed; adjacent line clears deal direct HP damage
    DEFEATED               // Boss destroyed; triggers warp distortion & victory celebration
}

enum class BoosterType(val displayName: String, val starCost: Int) {
    EMP_HAMMER("EMP STRIKE", 150),
    QUANTUM_REROLL("REROLL", 75),
    CATALYST_CONVERTER("OVERDRIVE", 200)
}

@Immutable
data class BoosterInventoryState(
    val empHammerCount: Int = 3,
    val quantumRerollCount: Int = 3,
    val catalystConverterCount: Int = 2,
    val activeTargetingBooster: BoosterType? = null
)

@Immutable
data class BossBattleState(
    val bossName: String = "GUARDIAN CORE",
    val maxHp: Int = 100,
    val currentHp: Int = 100,
    val phase: BossPhase = BossPhase.SHIELDED,
    val shieldPylonIndices: Set<Int> = setOf(9, 14, 49, 54), // Corner nodes protecting core
    val movesUntilJammerPulse: Int = 5,
    val jammedSlotIndex: Int? = null, // Dock slot (0, 1, or 2) locked by EMP
    val jammedTurnsRemaining: Int = 0,
    val movesWithoutBossDamage: Int = 0
)
