package com.example.gridsurge.features.adventure.model

enum class MasteryFeatType {
    NONE,
    MIN_COMBO_STREAK,      // e.g., Reach a 3x/4x combo streak
    MULTI_LINE_CLEAR,      // e.g., Execute at least one 2+ or 3+ line clear
    NO_EMP_JAMMED,         // e.g., Win without getting a tray slot jammed by the boss
    SCORE_THRESHOLD,       // e.g., Score >= 4,000 points
    RELIC_ABILITY_USED     // e.g., Trigger active relic ability at least once
}

data class MasteryFeatSpec(
    val featType: MasteryFeatType,
    val targetValue: Int,
    val description: String
)

data class StageStarBenchmark(
    val stageNumber: Int,
    val moveBudgetStar2: Int,
    val timeLimitSecStar2: Int,
    val masteryFeat: MasteryFeatSpec
)

data class MatchTelemetrySnapshot(
    val isObjectiveCompleted: Boolean,
    val movesUsed: Int,
    val elapsedSeconds: Int,
    val finalScore: Long,
    val maxComboStreak: Int,
    val maxSimultaneousLines: Int,
    val relicActivationsCount: Int,
    val empJamOccurred: Boolean
)

data class StarEvaluationResult(
    val totalStars: Int,
    val star1Secured: Boolean,
    val star2Secured: Boolean,
    val star3Secured: Boolean,
    val star1Title: String,
    val star2Title: String,
    val star3Title: String,
    val star1Detail: String,
    val star2Detail: String,
    val star3Detail: String
)
