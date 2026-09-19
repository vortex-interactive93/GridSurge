package com.example.gridsurge.meta.model

data class OperativeServiceTelemetry(
    val topScore: Int,
    val starReserves: Int,
    val clearanceLevel: String,
    val maxComboRank: String = "OVERDRIVE x8",
    val accountAgeFormatted: String = "ACTIVE RECORD"
)
