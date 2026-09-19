package com.example.gridsurge.meta.engine

import com.example.gridsurge.meta.model.CallsignValidationState
import com.example.gridsurge.moderation.engine.ProfanityModerator
import com.example.gridsurge.moderation.model.ModerationResult
import com.example.gridsurge.network.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CallsignValidator {

    private const val MIN_LENGTH = 3
    private const val MAX_LENGTH = 16

    /**
     * Synchronous local sanity check using the 3-Tier Profanity & Impersonation Engine.
     */
    fun validateLocally(input: String): CallsignValidationState {
        val trimmed = input.trim()

        if (trimmed.length < MIN_LENGTH) {
            return CallsignValidationState.Invalid("CALLSIGN TOO SHORT (MIN $MIN_LENGTH)")
        }
        if (trimmed.length > MAX_LENGTH) {
            return CallsignValidationState.Invalid("CALLSIGN EXCEEDS $MAX_LENGTH CHARACTERS")
        }
        if (!Regex("^[a-zA-Z0-9._-]+$").matches(trimmed)) {
            return CallsignValidationState.Invalid("ONLY ALPHANUMERIC, '.', '_' OR '-' ALLOWED")
        }

        // Run the 3-Tier Profanity & Impersonation Engine
        return when (val modCheck = ProfanityModerator.instance.inspectCallsign(trimmed)) {
            is ModerationResult.Blocked -> CallsignValidationState.Invalid(modCheck.localizedReason)
            is ModerationResult.Allowed -> CallsignValidationState.Idle
        }
    }

    /**
     * Remote database check: Verifies if the lower-cased callsign is taken.
     */
    suspend fun checkAvailabilityOnBackend(callsign: String): CallsignValidationState = withContext(Dispatchers.IO) {
        val cleanCallsign = callsign.trim()
        val localCheck = validateLocally(cleanCallsign)
        if (localCheck is CallsignValidationState.Invalid) {
            return@withContext localCheck
        }

        try {
            if (SupabaseClientProvider.isConfigured) {
                val count = SupabaseClientProvider.client.postgrest["profiles"]
                    .select {
                        filter {
                            ilike("callsign", cleanCallsign)
                        }
                    }.decodeList<Map<String, Any>>().size

                if (count > 0) {
                    CallsignValidationState.Invalid("CALLSIGN TAKEN // SELECT ALTERNATIVE")
                } else {
                    CallsignValidationState.ValidAvailable
                }
            } else {
                CallsignValidationState.ValidAvailable
            }
        } catch (e: Exception) {
            CallsignValidationState.ValidAvailable
        }
    }
}
