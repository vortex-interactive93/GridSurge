package com.example.gridsurge.moderation.model

enum class FilterSeverity {
    ZERO_TOLERANCE,      // Hate speech, racial slurs, illegal content
    PROFANITY,           // General vulgarity, sexual content
    SYSTEM_IMPERSONATION // Staff/Dev/System handles
}

sealed interface ModerationResult {
    object Allowed : ModerationResult
    data class Blocked(
        val matchedToken: String,
        val severity: FilterSeverity,
        val localizedReason: String
    ) : ModerationResult
}

data class ProfanityPattern(
    val rootWord: String,
    val severity: FilterSeverity,
    val matchAsSubstring: Boolean // true = Tier 1 (infix); false = Tier 2/3 (bounded)
)
