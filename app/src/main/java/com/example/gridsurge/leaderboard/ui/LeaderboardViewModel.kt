package com.example.gridsurge.leaderboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.leaderboard.data.LeaderboardRepository
import com.example.gridsurge.leaderboard.model.CloudLeaderboardEntry
import com.example.gridsurge.leaderboard.model.GameModeType
import com.example.gridsurge.leaderboard.model.LeaderboardSyncState
import com.example.gridsurge.meta.PlayerProfileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel(
    private val repository: LeaderboardRepository,
    private val profileManager: PlayerProfileManager,
    private val currentUserId: String
) : ViewModel() {

    private val _selectedMode = MutableStateFlow(GameModeType.DAILY_GLITCH)
    val selectedMode: StateFlow<GameModeType> = _selectedMode.asStateFlow()

    private val _syncState = MutableStateFlow<LeaderboardSyncState>(LeaderboardSyncState.Idle)
    val syncState: StateFlow<LeaderboardSyncState> = _syncState.asStateFlow()

    init {
        loadLeaderboard(GameModeType.DAILY_GLITCH)
    }

    fun selectMode(mode: GameModeType) {
        if (_selectedMode.value == mode) return
        SfxManager.playSfx(SfxType.SNAP_TICK)
        _selectedMode.value = mode
        loadLeaderboard(mode)
    }

    fun loadLeaderboard(mode: GameModeType) {
        _syncState.value = LeaderboardSyncState.Loading
        viewModelScope.launch {
            val result = repository.fetchLeaderboard(mode, currentUserId)
            result.onSuccess { (entries, userEntry) ->
                val enrichedUserEntry = userEntry?.copy(
                    title = profileManager.activeTitle.value,
                    badgeResId = profileManager.activeBadgeRes.value
                )
                
                _syncState.value = LeaderboardSyncState.Success(
                    topEntries = entries,
                    currentUserEntry = enrichedUserEntry,
                    totalCompetitors = entries.size
                )
            }.onFailure { error ->
                _syncState.value = LeaderboardSyncState.Error(error.localizedMessage ?: "UPLINK TIMEOUT")
            }
        }
    }
}
