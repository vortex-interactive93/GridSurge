package com.example.gridsurge.meta.engine

import java.util.concurrent.TimeUnit

data class CallsignRenamePolicy(
    val canRename: Boolean,
    val isFree: Boolean,
    val costStars: Int,
    val cooldownRemainingDays: Int,
    val cooldownFormatted: String?
)

object RenameEconomyEngine {
    private const val COOLDOWN_DAYS = 30L
    private val COOLDOWN_MILLIS = TimeUnit.DAYS.toMillis(COOLDOWN_DAYS)
    const val RENAME_STAR_COST = 250

    fun evaluatePolicy(
        currentCallsign: String,
        lastRenameTimestamp: Long,
        totalRenameCount: Int,
        playerStarCurrency: Int
    ): CallsignRenamePolicy {
        val isDefaultGuest = currentCallsign.startsWith("OPERATIVE_") || currentCallsign.startsWith("AGENT_") || currentCallsign == "OPERATIVE_X"

        // Rule 1: Initial setup / claim is 100% free with no cooldown
        if (isDefaultGuest || totalRenameCount == 0) {
            return CallsignRenamePolicy(
                canRename = true,
                isFree = true,
                costStars = 0,
                cooldownRemainingDays = 0,
                cooldownFormatted = null
            )
        }

        // Rule 2: Check 30-day cooldown
        val now = System.currentTimeMillis()
        val elapsed = now - lastRenameTimestamp

        if (elapsed < COOLDOWN_MILLIS) {
            val remainingMillis = COOLDOWN_MILLIS - elapsed
            val remainingDays = TimeUnit.MILLISECONDS.toDays(remainingMillis).toInt()
            val remainingHours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24

            return CallsignRenamePolicy(
                canRename = false,
                isFree = false,
                costStars = RENAME_STAR_COST,
                cooldownRemainingDays = remainingDays,
                cooldownFormatted = "${remainingDays}D${remainingHours}H"
            )
        }

        // Rule 3: Grace change or Star Currency requirement
        val isGraceChange = totalRenameCount == 1
        val hasEnoughCurrency = playerStarCurrency >= RENAME_STAR_COST

        return CallsignRenamePolicy(
            canRename = isGraceChange || hasEnoughCurrency,
            isFree = isGraceChange,
            costStars = if (isGraceChange) 0 else RENAME_STAR_COST,
            cooldownRemainingDays = 0,
            cooldownFormatted = null
        )
    }
}
