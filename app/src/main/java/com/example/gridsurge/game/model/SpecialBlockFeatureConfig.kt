package com.example.gridsurge.game.model

/**
 * Authoritative runtime feature gates for special blocks.
 * Setting these flags to false removes specials from all mode spawners
 * while preserving all math solvers, textures, and detonation pipelines.
 */
object SpecialBlockFeatureConfig {
    var isQuantumWarpEnabled: Boolean = true
    var isCatalystCrosshairEnabled: Boolean = false
    var isPrismLaserEnabled: Boolean = false
    var isCircuitConduitEnabled: Boolean = false

    /**
     * Master check used by spawners and engines to filter candidate piece pools.
     */
    fun isSpecialAllowed(type: SpecialBlockType): Boolean {
        return when (type) {
            SpecialBlockType.NONE -> true
            SpecialBlockType.QUANTUM_WARP_VORTEX -> isQuantumWarpEnabled
            SpecialBlockType.NOVA_CORE_EXPLOSION -> true
            SpecialBlockType.CATALYST_CROSSHAIR -> isCatalystCrosshairEnabled
            SpecialBlockType.PRISM_LASER -> isPrismLaserEnabled
            SpecialBlockType.CIRCUIT_CONDUIT -> isCircuitConduitEnabled
        }
    }
}
