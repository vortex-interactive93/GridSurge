package com.example.gridsurge.game.clash.engine

import androidx.compose.ui.graphics.Color
import com.example.gridsurge.game.clash.model.RivalCombatant
import kotlin.random.Random

object RivalMatchmakingRegistry {
    private val callsigns = listOf(
        "NEXUS_99", "VIPER_CORE", "VOID_WALKER", "CYBER_GHOST",
        "TITAN_PULSE", "ZERO_DAY", "SPECTRE_404", "SYNTH_BLADE",
        "PHANTOM_X", "SURGE_APEX", "NEURAL_RAVEN", "KINETIC_FOX"
    )

    fun generateOpponent(playerMmr: Int = 1840): RivalCombatant {
        val callsign = callsigns.random()
        val mmrVariance = Random.nextInt(-65, 75)
        val rivalMmr = (playerMmr + mmrVariance).coerceAtLeast(1000)

        val tier = when {
            rivalMmr >= 2400 -> "GRANDMASTER"
            rivalMmr >= 2000 -> "MASTER I"
            rivalMmr >= 1750 -> "DIAMOND II"
            rivalMmr >= 1500 -> "PLATINUM III"
            else -> "GOLD I"
        }

        val targetScore = (32000L + (rivalMmr * 5.5f) + Random.nextInt(-2500, 3500)).toLong()
        val basePpm = Random.nextInt(32, 38)

        return RivalCombatant(
            callsign = callsign,
            tierTitle = tier,
            currentMmr = rivalMmr,
            nextTierMmr = ((rivalMmr / 250) + 1) * 250,
            prevTierMmr = (rivalMmr / 250) * 250,
            targetScore = targetScore,
            avgPpm = basePpm
        )
    }
}
