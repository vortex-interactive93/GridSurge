package com.example.gridsurge.meta.util

import com.example.gridsurge.moderation.engine.ProfanityModerator
import com.example.gridsurge.moderation.model.ModerationResult

sealed class CallsignValidationResult {
    object Valid : CallsignValidationResult()
    data class Invalid(val reason: String) : CallsignValidationResult()
}

/**
 * Industry-Standard Callsign Validation & De-Obfuscation Profanity Filter.
 * Complies with Google Play Store & Apple App Store UGC Guidelines (Guideline 1.2).
 */
object CallsignValidator {

    private const val MIN_LENGTH = 3
    private const val MAX_LENGTH = 16

    private val VALID_PATTERN = Regex("^[a-zA-Z0-9](?!.*[ ._-]{2})[a-zA-Z0-9 ._-]{1,14}[a-zA-Z0-9]$")

    /**
     * Validates a player's proposed callsign against esports and app store standards.
     */
    fun validate(input: String): CallsignValidationResult {
        val trimmed = input.trim()

        if (trimmed.length < MIN_LENGTH) {
            return CallsignValidationResult.Invalid("Must be at least $MIN_LENGTH characters")
        }

        if (trimmed.length > MAX_LENGTH) {
            return CallsignValidationResult.Invalid("Cannot exceed $MAX_LENGTH characters")
        }

        if (!VALID_PATTERN.matches(trimmed)) {
            return when {
                trimmed.first().toString().matches(Regex("[^a-zA-Z0-9]")) -> 
                    CallsignValidationResult.Invalid("Cannot start with a symbol or space")
                trimmed.last().toString().matches(Regex("[^a-zA-Z0-9]")) -> 
                    CallsignValidationResult.Invalid("Cannot end with a symbol or space")
                trimmed.contains(Regex("[ ._-]{2}")) -> 
                    CallsignValidationResult.Invalid("No consecutive symbols or double spaces")
                else -> 
                    CallsignValidationResult.Invalid("Only letters, numbers, spaces, '.', '_' or '-'")
            }
        }

        return when (val result = ProfanityModerator.instance.inspectCallsign(trimmed)) {
            is ModerationResult.Blocked -> CallsignValidationResult.Invalid(result.localizedReason)
            is ModerationResult.Allowed -> CallsignValidationResult.Valid
        }
    }
}
