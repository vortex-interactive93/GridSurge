package com.example.gridsurge.theme

import androidx.annotation.DrawableRes
import com.example.gridsurge.R
import com.example.gridsurge.leaderboard.model.GameModeType

object BackgroundThemeManager {

    @DrawableRes
    fun getBackgroundForMode(
        mode: GameModeType,
        sectorId: Int = 1,
        equippedThemeKey: String = ThemeNormalizer.GLASS
    ): Int {
        return when (mode) {
            GameModeType.CLASSIC_SURGE -> {
                when (ThemeNormalizer.normalize(equippedThemeKey)) {
                    ThemeNormalizer.SOLAR -> R.drawable.bg_sector_solar_flare
                    ThemeNormalizer.VOID -> R.drawable.bg_sector_quantum_singularity
                    ThemeNormalizer.CYBER -> R.drawable.bg_sector_neon_grid
                    else -> R.drawable.bg_classic
                }
            }
            GameModeType.DAILY_GLITCH -> R.drawable.bg_glitch_mode
            GameModeType.TIME_BLITZ -> R.drawable.bg_sector_quantum_singularity
        }
    }

    @DrawableRes
    fun getBackgroundForSector(sectorId: Int): Int {
        return when (sectorId) {
            1 -> R.drawable.bg_sector_neon_grid
            2 -> R.drawable.bg_sector_solar_flare
            3 -> R.drawable.bg_sector_toxic_surge
            4 -> R.drawable.bg_sector_quantum_singularity
            else -> R.drawable.bg_sector_neon_grid
        }
    }
}
