package com.example.gridsurge.features.adventure.engine

import com.example.gridsurge.features.adventure.model.MasteryFeatType
import com.example.gridsurge.features.adventure.model.MatchTelemetrySnapshot
import com.example.gridsurge.features.adventure.model.StageStarBenchmark
import com.example.gridsurge.features.adventure.model.StarEvaluationResult

object StarRatingEvaluator {

    fun evaluateMatch(
        benchmark: StageStarBenchmark,
        telemetry: MatchTelemetrySnapshot
    ): StarEvaluationResult {
        // Star 1: Primary Directive Completed
        val star1 = telemetry.isObjectiveCompleted

        // Star 2: Tactical Efficiency (Move Budget OR Speed Benchmark)
        val moveQualified = telemetry.movesUsed <= benchmark.moveBudgetStar2
        val timeQualified = telemetry.elapsedSeconds <= benchmark.timeLimitSecStar2
        val star2 = star1 && (moveQualified || timeQualified)

        // Star 3: Stage Mastery Feat
        val feat = benchmark.masteryFeat
        val star3 = star1 && when (feat.featType) {
            MasteryFeatType.MIN_COMBO_STREAK -> telemetry.maxComboStreak >= feat.targetValue
            MasteryFeatType.MULTI_LINE_CLEAR -> telemetry.maxSimultaneousLines >= feat.targetValue
            MasteryFeatType.NO_EMP_JAMMED -> !telemetry.empJamOccurred
            MasteryFeatType.SCORE_THRESHOLD -> telemetry.finalScore >= feat.targetValue
            MasteryFeatType.RELIC_ABILITY_USED -> telemetry.relicActivationsCount >= feat.targetValue
            MasteryFeatType.NONE -> star2
        }

        val starCount = (if (star1) 1 else 0) + (if (star2) 1 else 0) + (if (star3) 1 else 0)

        val star2DetailText = if (star2) {
            if (moveQualified) "${telemetry.movesUsed} / ${benchmark.moveBudgetStar2} Moves (Secured)"
            else "${telemetry.elapsedSeconds}s / ${benchmark.timeLimitSecStar2}s (Secured)"
        } else {
            "${telemetry.movesUsed} Moves / ${telemetry.elapsedSeconds}s (Target: ≤${benchmark.moveBudgetStar2} Moves or <${benchmark.timeLimitSecStar2}s)"
        }

        val star3DetailText = when (feat.featType) {
            MasteryFeatType.MIN_COMBO_STREAK -> "${telemetry.maxComboStreak}x Streak (Target: ${feat.targetValue}x)"
            MasteryFeatType.MULTI_LINE_CLEAR -> "${telemetry.maxSimultaneousLines} Lines Clear (Target: ${feat.targetValue}+)"
            MasteryFeatType.NO_EMP_JAMMED -> if (telemetry.empJamOccurred) "Slot Jammed" else "Zero Jams"
            MasteryFeatType.SCORE_THRESHOLD -> "${telemetry.finalScore} Pts (Target: ${feat.targetValue})"
            MasteryFeatType.RELIC_ABILITY_USED -> "${telemetry.relicActivationsCount} Activations"
            MasteryFeatType.NONE -> "Mastery Synchronized"
        }

        return StarEvaluationResult(
            totalStars = starCount,
            star1Secured = star1,
            star2Secured = star2,
            star3Secured = star3,
            star1Title = "Primary Directive",
            star2Title = "Tactical Efficiency",
            star3Title = "Sector Mastery",
            star1Detail = if (star1) "Objective Secured" else "Incomplete",
            star2Detail = star2DetailText,
            star3Detail = star3DetailText
        )
    }
}
