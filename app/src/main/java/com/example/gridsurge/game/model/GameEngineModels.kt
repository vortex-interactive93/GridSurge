package com.example.gridsurge.game.model

enum class CoreKind {
    NONE,
    CYAN_REACTOR,
    AMBER_FURNACE,
    THERMAL_CATALYST,
    CRIMSON_CIPHER_LOCKED,
    CRIMSON_CIPHER_EXPOSED,
    EMERALD_CONDUIT,
    PURPLE_SINGULARITY,
    SHIELD_PYLON
}

enum class CoreIntegrity {
    INTACT,
    CRACKED,
    DESTROYED
}

data class GridCell(
    val row: Int,
    val col: Int,
    var isFilled: Boolean = false,
    var blockColor: Int = 0,
    var isCore: Boolean = false,
    var coreKind: CoreKind = CoreKind.NONE,
    var coreIntegrity: CoreIntegrity = CoreIntegrity.INTACT,
    var hitsRemaining: Int = 0,
    var turnsRemaining: Int = 99,
    var isInvulnerable: Boolean = false,
    var isMeltdownActive: Boolean = false
) {
    fun toCellTypeValue(): Int {
        if (!isCore) return if (isFilled) blockColor else 0
        return when (coreIntegrity) {
            CoreIntegrity.INTACT -> -1
            CoreIntegrity.CRACKED -> -2
            CoreIntegrity.DESTROYED -> 0
        }
    }
}

enum class DetonationEffect {
    SUCTION,
    STRAIN,
    NEUTRALIZATION
}

data class DetonationTarget(
    val row: Int,
    val col: Int,
    val effect: DetonationEffect
)

enum class SpawnerArchetype {
    STANDARD,
    WARP_PACING,
    BLITZ_PRESSURE
}
