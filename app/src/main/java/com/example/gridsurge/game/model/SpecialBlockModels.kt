package com.example.gridsurge.game.model

import androidx.annotation.DrawableRes
import com.example.gridsurge.R

enum class SpecialBlockType {
    NONE,
    CATALYST_CROSSHAIR, // Solar Crosshair: Cleaves entire row and column
    QUANTUM_WARP_VORTEX, // Void Singularity: Obliterates 3x3 surrounding zone
    NOVA_CORE_EXPLOSION, // Supercharged Nova Core: 3x3 Radial Detonation
    PRISM_LASER,         // Perpendicular 4-way laser beam sweeps
    CIRCUIT_CONDUIT      // Synthesis tile: Standalone 1x1 objective module
}

data class SpecialBlockDefinition(
    val type: SpecialBlockType,
    val name: String,
    @param:DrawableRes val drawableRes: Int,
    val primaryColor: Int
)

object SpecialBlockRegistry {
    val CATALYST = SpecialBlockDefinition(
        type = SpecialBlockType.CATALYST_CROSSHAIR,
        name = "CATALYST BOMB",
        drawableRes = R.drawable.skin_catalyst_block,
        primaryColor = 0xFFFFD600.toInt()
    )

    val WARP = SpecialBlockDefinition(
        type = SpecialBlockType.QUANTUM_WARP_VORTEX,
        name = "WARP VORTEX",
        drawableRes = R.drawable.skin_warp_block,
        primaryColor = 0xFFEA80FC.toInt()
    )

    val NOVA_CORE = SpecialBlockDefinition(
        type = SpecialBlockType.NOVA_CORE_EXPLOSION,
        name = "SUPERCHARGED NOVA CORE",
        drawableRes = R.drawable.hud_nova_core_supercharged,
        primaryColor = 0xFF00E5FF.toInt()
    )

    fun getDrawableForSpecialType(type: SpecialBlockType): Int {
        return when (type) {
            SpecialBlockType.CATALYST_CROSSHAIR -> CATALYST.drawableRes
            SpecialBlockType.QUANTUM_WARP_VORTEX -> WARP.drawableRes
            SpecialBlockType.NOVA_CORE_EXPLOSION -> NOVA_CORE.drawableRes
            SpecialBlockType.PRISM_LASER -> R.drawable.vfx_cross_laser
            SpecialBlockType.CIRCUIT_CONDUIT -> R.drawable.overlay_circuit_rim
            SpecialBlockType.NONE -> 0
        }
    }
}
