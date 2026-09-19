package com.example.gridsurge.meta.model

sealed interface CallsignValidationState {
    object Idle : CallsignValidationState
    object CheckingAvailability : CallsignValidationState
    object ValidAvailable : CallsignValidationState
    data class Invalid(val reason: String) : CallsignValidationState
}

data class CallsignRuleConfig(
    val minLength: Int = 3,
    val maxLength: Int = 16,
    val validFormatRegex: Regex = Regex("^[a-zA-Z0-9][a-zA-Z0-9_-]{1,14}[a-zA-Z0-9]$"),
    val reservedWords: Set<String> = setOf(
        "admin", "administrator", "dev", "developer", "moderator",
        "mod", "staff", "system", "gridsurge", "official", "support", "null"
    )
)
