package com.example.gridsurge.meta.quests

enum class QuestState {
    IN_PROGRESS,
    CLAIMABLE,
    CLAIMED
}

enum class QuestType {
    COMBO,
    LINES,
    SURGE_CORE,
    TIME_BLITZ,
    BLITZ_CLASH
}

data class DailyMission(
    val id: String,
    val type: QuestType,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val starReward: Int,
    val state: QuestState = if (currentProgress >= targetProgress) QuestState.CLAIMABLE else QuestState.IN_PROGRESS
) {
    val progressFraction: Float
        get() = (currentProgress.toFloat() / targetProgress.coerceAtLeast(1)).coerceIn(0f, 1f)
}
