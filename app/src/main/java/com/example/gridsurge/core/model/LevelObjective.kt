package com.example.gridsurge.core.model

import com.example.gridsurge.features.adventure.model.ObjectiveType

object LevelObjectiveFormatter {
    fun formatFailureProgress(type: ObjectiveType, current: Int, target: Int): String {
        return when (type) {
            ObjectiveType.INFECTED_PURGE -> "CORES PURGED: $current / $target"
            ObjectiveType.LINE_CLEANSE -> "LINES CLEARED: $current / $target"
            ObjectiveType.CHROMA_SYNTHESIS -> "CIRCUITS SYNTHESIZED: $current / $target"
            ObjectiveType.SURGE_STREAK_TARGET -> "MAX STREAK: $current / $target"
            ObjectiveType.SCORE_ATTACK -> "SCORE REACHED: $current / $target"
            else -> "OBJECTIVE PROGRESS: $current / $target"
        }
    }
}
