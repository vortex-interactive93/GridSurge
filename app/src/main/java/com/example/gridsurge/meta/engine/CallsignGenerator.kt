package com.example.gridsurge.meta.engine

import java.util.UUID
import kotlin.math.abs

object CallsignGenerator {

    private val PREFIXES = arrayOf("AGENT", "OPERATIVE", "SURGE", "NEURAL", "CYBER")

    /**
     * Generates a zero-allocation, deterministic default name like "OPERATIVE_7429"
     * entirely on the client. Runs in <1 millisecond on cold start.
     */
    fun generateDeterministicGuestCallsign(): String {
        val uuid = UUID.randomUUID()
        val num = abs(uuid.leastSignificantBits % 9000).toInt() + 1000
        val prefix = PREFIXES[abs((uuid.mostSignificantBits % PREFIXES.size).toInt())]
        return "${prefix}_$num"
    }

    /**
     * Sanitizes raw Google display names ("Thomas Davis!", "T.J. 99")
     * into strict ASCII alphanumeric game handles ("THOMAS_DAVIS").
     */
    fun sanitizeGoogleDisplayName(rawName: String?): String {
        if (rawName.isNullOrBlank()) return generateDeterministicGuestCallsign()

        val clean = rawName.uppercase()
            .replace(" ", "_")
            .filter { it.isLetterOrDigit() || it == '_' }
            .take(14)

        return when {
            clean.length < 3 -> generateDeterministicGuestCallsign()
            clean.startsWith("_") -> "OP$clean"
            else -> clean
        }
    }
}
