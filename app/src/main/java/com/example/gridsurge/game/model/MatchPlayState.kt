package com.example.gridsurge.game.model

enum class MatchPlayState {
    RUNNING,      // Active gameplay, loop ticking
    PAUSED,       // Ticker stopped, Pause Modal open
    SETTINGS_OPEN,// In-game settings overlay open
    GAME_OVER     // Final score modal active
}

data class PauseMenuUiState(
    val isPaused: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val currentScore: Long = 0L,
    val currentStage: Int = 1,
    val currentModeName: String = "CLASSIC SURGE"
)
