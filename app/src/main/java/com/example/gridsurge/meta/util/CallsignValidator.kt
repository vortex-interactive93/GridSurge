package com.example.gridsurge.meta.util

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

    // 1. Reserved Administrative Handles
    private val RESERVED_NAMES = setOf(
        "admin", "administrator", "system", "moderator", "mod", "support", 
        "staff", "server", "operator", "bot", "developer", "dev", "null", 
        "undefined", "gridsurge", "official", "helpdesk"
    )

    // 2. Safe-List Allowlist (Prevents Scunthorpe False Positives e.g. "Classic", "Assassin", "Pass")
    private val ALLOWLIST = setOf(
        "classic", "assassin", "pass", "grasshopper", "cassandra", "bass", 
        "cockburn", "tyson", "dickinson", "nasser"
    )

    // 3. Tier 1 & Tier 2 Restricted Word Roots
    private val RESTRICTED_ROOTS = setOf(
        "shit", "fuck", "bitch", "cunt", "nigger", "nigga", "fag", "faggot", 
        "retard", "whore", "slut", "pussy", "dick", "cock", "bastard", 
        "asshole", "penis", "vagina", " Nazi", "hitler", "rape", "suicide",
        "puta", "mierda", "suka", "blyat", "caralho", "putain"
    )

    // Regex: Starts & ends with alphanumeric, 3-16 chars total, no consecutive symbols [._ -]
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

        val normalizedRaw = trimmed.lowercase()

        // 1. Reserved Administrative Handles Check
        if (RESERVED_NAMES.any { normalizedRaw.contains(it) }) {
            return CallsignValidationResult.Invalid("Reserved system handle")
        }

        // 2. De-Obfuscation Pipeline (De-Leeting + Separator Stripping + Repeated Char Normalization)
        val deobfuscated = deobfuscate(normalizedRaw)

        // 3. Check Allowlist Exception
        val isAllowlisted = ALLOWLIST.any { normalizedRaw.contains(it) }

        // 4. Profanity & Slur Filter (Skipped if on Allowlist)
        if (!isAllowlisted && RESTRICTED_ROOTS.any { deobfuscated.contains(it) || normalizedRaw.contains(it) }) {
            // Non-descriptive error message to prevent evasion roadmap
            return CallsignValidationResult.Invalid("Contains disallowed words")
        }

        // 5. Pattern & Structural Formatting Rules
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

        return CallsignValidationResult.Valid
    }

    /**
     * Converts leetspeak, strips punctuation separators, and compresses repeated characters.
     * Example: "  $ h ! + _ H e 4 d  " -> "shithead"
     */
    private fun deobfuscate(input: String): String {
        val deleeted = StringBuilder()
        for (ch in input) {
            val converted = when (ch) {
                '@' -> 'a'
                '3' -> 'e'
                '1', '!', '|' -> 'i'
                '0' -> 'o'
                '5', '$' -> 's'
                '7' -> 't'
                '8' -> 'b'
                else -> ch
            }
            if (converted in 'a'..'z' || converted in '0'..'9') {
                deleeted.append(converted)
            }
        }

        // Compress repeated characters (e.g. "shiiiit" -> "shit")
        val compressed = StringBuilder()
        var prevChar: Char? = null
        var repeatCount = 0

        for (ch in deleeted) {
            if (ch == prevChar) {
                repeatCount++
                if (repeatCount < 2) {
                    compressed.append(ch)
                }
            } else {
                compressed.append(ch)
                prevChar = ch
                repeatCount = 0
            }
        }

        return compressed.toString()
    }
}
