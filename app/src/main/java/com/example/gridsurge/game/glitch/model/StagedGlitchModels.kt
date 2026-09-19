package com.example.gridsurge.game.glitch.model

enum class AnomalyStage(val turnsRemaining: Int, val isSlag: Boolean) {
    STAGE_1_CONTAINED(3, false),
    STAGE_2_UNSTABLE(2, false),
    STAGE_3_CRITICAL(1, false),
    STAGE_4_OBSIDIAN_SLAG(0, true)
}

data class StagedGlitchBlock(
    val gridIndex: Int,
    var currentStage: AnomalyStage = AnomalyStage.STAGE_1_CONTAINED
) {
    val turnsRemaining: Int get() = currentStage.turnsRemaining
}
