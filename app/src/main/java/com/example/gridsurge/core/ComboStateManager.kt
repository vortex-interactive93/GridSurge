package com.example.gridsurge.core

enum class ComboState {
    INACTIVE,          // Streak = 0, no multiplier
    ACTIVE_STREAK,     // Streak >= 1, full grace buffer (2/2)
    GRACE_WARNING,     // Streak >= 1, buffer draining (1/2)
    CRITICAL_LAST_MOVE // Streak >= 1, last move before decay (0/2)
}

data class ComboEvaluationResult(
    val currentStreak: Int,
    val previousStreak: Int,
    val graceMovesRemaining: Int,
    val maxGraceMoves: Int,
    val state: ComboState,
    val isStreakIncremented: Boolean,
    val isStreakPreservedByGrace: Boolean,
    val isStreakBroken: Boolean
)

class ComboStateManager(var maxGraceMoves: Int = 2) {

    var currentStreak: Int = 0
        private set

    var comboMultiplier: Int = 1
        private set

    var graceMovesRemaining: Int = maxGraceMoves
        private set

    val currentState: ComboState
        get() = when {
            currentStreak == 0 -> ComboState.INACTIVE
            graceMovesRemaining == maxGraceMoves -> ComboState.ACTIVE_STREAK
            graceMovesRemaining == 1 -> ComboState.GRACE_WARNING
            else -> ComboState.CRITICAL_LAST_MOVE
        }

    fun onMoveCommitted(linesCleared: Int): ComboEvaluationResult {
        val prevStreak = currentStreak

        if (linesCleared > 0) {
            // FIXED: combo = combo + N (Weighted Line Accumulator)
            currentStreak += linesCleared
            
            // Weighted Multiplier Bonus: 1 line = +1, 2 lines = +2, 3 lines = +3, 4+ lines = +4
            val bonus = when (linesCleared) {
                1 -> 1
                2 -> 2
                3 -> 3
                else -> 4
            }
            comboMultiplier += bonus
            
            graceMovesRemaining = maxGraceMoves

            return ComboEvaluationResult(
                currentStreak = currentStreak,
                previousStreak = prevStreak,
                graceMovesRemaining = graceMovesRemaining,
                maxGraceMoves = maxGraceMoves,
                state = currentState,
                isStreakIncremented = true,
                isStreakPreservedByGrace = false,
                isStreakBroken = false
            )
        } else {
            // Non-clearing drop: evaluate buffer
            if (currentStreak > 0) {
                if (graceMovesRemaining > 0) {
                    // Buffer absorbs the non-clearing placement
                    graceMovesRemaining--
                    return ComboEvaluationResult(
                        currentStreak = currentStreak,
                        previousStreak = prevStreak,
                        graceMovesRemaining = graceMovesRemaining,
                        maxGraceMoves = maxGraceMoves,
                        state = currentState,
                        isStreakIncremented = false,
                        isStreakPreservedByGrace = true,
                        isStreakBroken = false
                    )
                } else {
                    // Buffer exhausted: streak terminates
                    currentStreak = 0
                    comboMultiplier = 1
                    graceMovesRemaining = maxGraceMoves
                    return ComboEvaluationResult(
                        currentStreak = 0,
                        previousStreak = prevStreak,
                        graceMovesRemaining = graceMovesRemaining,
                        maxGraceMoves = maxGraceMoves,
                        state = ComboState.INACTIVE,
                        isStreakIncremented = false,
                        isStreakPreservedByGrace = false,
                        isStreakBroken = true
                    )
                }
            } else {
                return ComboEvaluationResult(
                    currentStreak = 0,
                    previousStreak = 0,
                    graceMovesRemaining = maxGraceMoves,
                    maxGraceMoves = maxGraceMoves,
                    state = ComboState.INACTIVE,
                    isStreakIncremented = false,
                    isStreakPreservedByGrace = false,
                    isStreakBroken = false
                )
            }
        }
    }

    fun reset() {
        currentStreak = 0
        comboMultiplier = 1
        graceMovesRemaining = maxGraceMoves
    }
}
