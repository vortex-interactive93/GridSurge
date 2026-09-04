package com.example.gridsurge.features.adventure.domain.model

import com.example.gridsurge.features.adventure.model.AdventureStageObjective
import com.example.gridsurge.features.adventure.model.CorePlacementSpec
import com.example.gridsurge.features.adventure.model.MasteryFeatSpec

data class StageBenchmarks(
    val targetScore1Star: Int,
    val targetScore2Star: Int,
    val targetScore3Star: Int,
    val moveBudgetStar2: Int,
    val timeLimitSecStar2: Int,
    val masteryFeat: MasteryFeatSpec? = null
) {
    init {
        require(targetScore1Star <= targetScore2Star && targetScore2Star <= targetScore3Star) {
            "Benchmark scores must be ascending: $targetScore1Star <= $targetScore2Star <= $targetScore3Star"
        }
    }
}

data class StageBlueprint(
    val stageName: String,
    val directive: String,
    val objective: AdventureStageObjective,
    val initialCores: List<CorePlacementSpec> = emptyList(),
    val initialBoardLayout: List<Int>? = null,
    val hasSectorHazards: Boolean = false,
    val hazardIntervalMoves: Int = 0
)

data class StageDefinition(
    val stageId: StageId,
    val blueprint: StageBlueprint,
    val benchmarks: StageBenchmarks
)
