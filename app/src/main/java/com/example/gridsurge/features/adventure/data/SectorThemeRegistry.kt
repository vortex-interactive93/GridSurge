package com.example.gridsurge.features.adventure.data

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.gridsurge.R

data class SectorThemePresentation(
    val visualThemeKey: String,
    val themeColorHex: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    @param:DrawableRes val backgroundRes: Int,
    @param:DrawableRes val rewardBadgeRes: Int,
)

object SectorThemeRegistry {
    private val THEMES = mapOf(
        "THEME_NEON_NEXUS" to SectorThemePresentation(
            visualThemeKey = "THEME_NEON_NEXUS",
            themeColorHex = "#4ECDC4",
            primaryColor = Color(0xFF4ECDC4),
            secondaryColor = Color(0xFF4ECDC4).copy(alpha = 0.6f),
            backgroundRes = R.drawable.bg_sector_neon_grid,
            rewardBadgeRes = R.drawable.bg_cyber_pill_badge
        ),
        "THEME_SOLAR_FOUNDRY" to SectorThemePresentation(
            visualThemeKey = "THEME_SOLAR_FOUNDRY",
            themeColorHex = "#FFD600",
            primaryColor = Color(0xFFFFD600),
            secondaryColor = Color(0xFFFFD600).copy(alpha = 0.6f),
            backgroundRes = R.drawable.bg_sector_solar_flare,
            rewardBadgeRes = R.drawable.bg_cyber_pill_badge
        ),
        "THEME_CRIMSON_BREACH" to SectorThemePresentation(
            visualThemeKey = "THEME_CRIMSON_BREACH",
            themeColorHex = "#FF1744",
            primaryColor = Color(0xFFFF1744),
            secondaryColor = Color(0xFFFF1744).copy(alpha = 0.6f),
            backgroundRes = R.drawable.bg_sector_crimson_breach,
            rewardBadgeRes = R.drawable.bg_cyber_pill_badge
        ),
        "THEME_TOXIC_SURGE" to SectorThemePresentation(
            visualThemeKey = "THEME_TOXIC_SURGE",
            themeColorHex = "#00E676",
            primaryColor = Color(0xFF00E676),
            secondaryColor = Color(0xFF00E676).copy(alpha = 0.6f),
            backgroundRes = R.drawable.bg_sector_toxic_surge,
            rewardBadgeRes = R.drawable.bg_cyber_pill_badge
        ),
        "THEME_QUANTUM_SINGULARITY" to SectorThemePresentation(
            visualThemeKey = "THEME_QUANTUM_SINGULARITY",
            themeColorHex = "#D500F9",
            primaryColor = Color(0xFFD500F9),
            secondaryColor = Color(0xFFD500F9).copy(alpha = 0.6f),
            backgroundRes = R.drawable.bg_sector_quantum_singularity,
            rewardBadgeRes = R.drawable.bg_cyber_pill_badge
        )
    )

    fun getTheme(visualThemeKey: String): SectorThemePresentation {
        return THEMES[visualThemeKey] ?: THEMES["THEME_NEON_NEXUS"]!!
    }
}
