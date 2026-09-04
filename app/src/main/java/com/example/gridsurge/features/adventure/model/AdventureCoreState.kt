package com.example.gridsurge.features.adventure.model

data class ActiveCoreEntity(
    val col: Int,
    val row: Int,
    val coreType: SectorCoreType,
    var currentHitsRemaining: Int,
    var isLocked: Boolean,
    var isDestroyed: Boolean = false
)

data class AdventureRunState(
    var currentLevelNumber: Int = 1,
    var currentSectorId: Int = 1,
    var installedAugments: MutableList<NeuralAugment> = mutableListOf(),
    var totalStarsEarnedInRun: Int = 0,
    var resonanceEnergy: Float = 0f,
    var isWarpReady: Boolean = false,
    var isWarpUnlocked: Boolean = false,
    var isPityActive: Boolean = false,
    var isCriticalState: Boolean = false,
    var isBossActive: Boolean = false
) {
    companion object {
        const val MAX_RESONANCE = 100f
        const val DECAY_PER_NON_CLEAR = 8f
        const val CORE_HARVEST_ENERGY = 30f
        const val DESPERATION_PULSE_ENERGY = 2f
        const val DANGER_OCCUPANCY_THRESHOLD = 0.85f
    }

    fun hasAugment(type: AugmentType): Boolean = installedAugments.any { it.type == type }

    fun installAugment(augment: NeuralAugment) {
        if (!installedAugments.any { it.type == augment.type }) {
            installedAugments.add(augment)
        }
    }

    fun addResonance(amount: Float) {
        if (!isWarpUnlocked || isWarpReady) return // Only charge if unlocked and not already ready
        resonanceEnergy = (resonanceEnergy + amount).coerceIn(0f, MAX_RESONANCE)
        if (resonanceEnergy >= MAX_RESONANCE) {
            isWarpReady = true
            resonanceEnergy = MAX_RESONANCE
        }
    }

    fun applyDecay() {
        if (!isWarpUnlocked || isWarpReady) return
        resonanceEnergy = (resonanceEnergy - DECAY_PER_NON_CLEAR).coerceAtLeast(0f)
    }

    fun consumeWarp() {
        resonanceEnergy = 0f
        isWarpReady = false
    }

    fun reset() {
        resonanceEnergy = 0f
        isWarpReady = false
    }
}
