package com.example.gridsurge.meta.engine

import com.example.gridsurge.meta.model.OperativeServiceTelemetry

object OperativeTelemetryEngine {
    fun compileTelemetry(
        highScore: Int,
        starCurrency: Int,
        isLinked: Boolean
    ): OperativeServiceTelemetry {
        val clearance = when {
            isLinked && highScore >= 50000 -> "TIER 3 // ELITE"
            isLinked && highScore >= 10000 -> "TIER 2 // VANGUARD"
            isLinked -> "TIER 1 // VERIFIED"
            else -> "RECON // GUEST"
        }

        val comboRank = when {
            highScore >= 30000 -> "APEX SURGE"
            highScore >= 15000 -> "HYPER COMBO"
            else -> "SUB-SURGE"
        }

        return OperativeServiceTelemetry(
            topScore = highScore,
            starReserves = starCurrency,
            clearanceLevel = clearance,
            maxComboRank = comboRank
        )
    }
}
