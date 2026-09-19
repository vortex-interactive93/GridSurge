package com.example.gridsurge.ui.quests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.meta.data.DailyMissionsRepository
import com.example.gridsurge.meta.quests.DailyMission
import com.example.gridsurge.meta.quests.QuestState
import com.example.gridsurge.meta.quests.QuestType
import com.example.gridsurge.operations.data.OperationsCatalog
import com.example.gridsurge.operations.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DailyMissionsViewModel(
    private val repository: DailyMissionsRepository,
    private val profileManager: PlayerProfileManager
) : ViewModel() {

    private val missionSpecs = listOf(
        Triple("q1", QuestType.COMBO, "Trigger a 5x Combo"),
        Triple("q2", QuestType.LINES, "Clear 30 Lines"),
        Triple("q3", QuestType.SURGE_CORE, "Destroy 5 Surge Cores"),
        Triple("q4", QuestType.TIME_BLITZ, "Score 10k in Time Blitz"),
        Triple("q5", QuestType.BLITZ_CLASH, "Complete 2 Clash Duels")
    )

    private val _inFlightClaims = MutableStateFlow<Set<String>>(emptySet())
    val inFlightClaims: StateFlow<Set<String>> = _inFlightClaims.asStateFlow()

    val isCrateClaimed: StateFlow<Boolean> = repository.isMissionClaimed("daily_crate").stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val claimMutex = Mutex()

    private val _missions = MutableStateFlow<List<DailyMission>>(emptyList())
    val missions: StateFlow<List<DailyMission>> = _missions.asStateFlow()

    val operationsState: StateFlow<DailyOperationsState> = combine(
        combine(
            OperationsCatalog.DEFAULT_DIRECTIVES.map { directive ->
                combine(
                    repository.getMissionProgress(directive.id),
                    repository.isMissionClaimed(directive.id)
                ) { rawProgress, claimed ->
                    val clamped = minOf(rawProgress, directive.targetProgress)
                    val status = when {
                        claimed -> DirectiveStatus.CLAIMED
                        clamped >= directive.targetProgress -> DirectiveStatus.READY_TO_CLAIM
                        else -> DirectiveStatus.IN_PROGRESS
                    }
                    directive.copy(
                        currentProgress = clamped,
                        status = status
                    )
                }
            }
        ) { directivesArray -> directivesArray.toList() },
        combine(
            MilestoneTier.entries.map { tier ->
                val milestoneKey = "milestone_${tier.name.lowercase()}"
                repository.isMissionClaimed(milestoneKey).map { claimed ->
                    MilestoneCacheState(tier = tier, isClaimed = claimed)
                }
            }
        ) { milestonesArray -> milestonesArray.toList() }
    ) { directivesList, milestoneStatesList ->
        val totalOpsPoints = directivesList
            .filter { it.status == DirectiveStatus.CLAIMED }
            .sumOf { it.rewardPoints }
            .coerceAtMost(100)

        DailyOperationsState(
            currentOpsPoints = totalOpsPoints,
            maxOpsPoints = 100,
            resetCountdownFormatted = "08:14:22",
            directives = directivesList,
            milestoneStates = milestoneStatesList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailyOperationsState(
            currentOpsPoints = 0,
            maxOpsPoints = 100,
            resetCountdownFormatted = "08:14:22",
            directives = OperationsCatalog.DEFAULT_DIRECTIVES,
            milestoneStates = OperationsCatalog.DEFAULT_MILESTONES
        )
    )

    init {
        loadMissions()
    }

    private fun loadMissions() {
        combine(
            missionSpecs.map { spec ->
                combine(
                    repository.getMissionProgress(spec.first),
                    repository.isMissionClaimed(spec.first),
                    _inFlightClaims
                ) { rawProgress, claimed, inFlight ->
                    val target = when (spec.second) {
                        QuestType.COMBO -> 5
                        QuestType.LINES -> 30
                        QuestType.SURGE_CORE -> 5
                        QuestType.TIME_BLITZ -> 10000
                        QuestType.BLITZ_CLASH -> 2
                    }
                    val reward = when (spec.second) {
                        QuestType.COMBO -> 25
                        QuestType.LINES -> 35
                        QuestType.SURGE_CORE -> 50
                        QuestType.TIME_BLITZ -> 50
                        QuestType.BLITZ_CLASH -> 60
                    }
                    val clampedProgress = minOf(rawProgress, target)
                    val isLocked = claimed || inFlight.contains(spec.first)
                    val state = when {
                        claimed -> QuestState.CLAIMED
                        isLocked -> QuestState.CLAIMED
                        clampedProgress >= target -> QuestState.CLAIMABLE
                        else -> QuestState.IN_PROGRESS
                    }
                    DailyMission(
                        id = spec.first,
                        type = spec.second,
                        title = spec.third,
                        description = "Quest mission protocol",
                        currentProgress = clampedProgress,
                        targetProgress = target,
                        starReward = reward,
                        state = state
                    )
                }
            }
        ) { missionsArray ->
            missionsArray.toList()
        }.onEach {
            _missions.value = it
        }.launchIn(viewModelScope)
    }

    fun claimMission(missionId: String) {
        val mission = _missions.value.find { it.id == missionId } ?: return
        if (mission.state != QuestState.CLAIMABLE) return

        viewModelScope.launch {
            claimMutex.withLock {
                if (_inFlightClaims.value.contains(missionId)) return@launch
                _inFlightClaims.update { it + missionId }
            }

            try {
                repository.setMissionClaimed(missionId, true)
                profileManager.addStarCurrency(mission.starReward)
            } finally {
                claimMutex.withLock {
                    _inFlightClaims.update { it - missionId }
                }
            }
        }
    }

    fun claimOperationDirective(directiveId: String) {
        val currentState = operationsState.value
        val directive = currentState.directives.find { it.id == directiveId } ?: return
        if (directive.status != DirectiveStatus.READY_TO_CLAIM) return

        viewModelScope.launch {
            claimMutex.withLock {
                if (_inFlightClaims.value.contains(directiveId)) return@launch
                _inFlightClaims.update { it + directiveId }
            }

            try {
                repository.setMissionClaimed(directiveId, true)
                profileManager.addStarCurrency(directive.rewardStars)
            } finally {
                claimMutex.withLock {
                    _inFlightClaims.update { it - directiveId }
                }
            }
        }
    }

    fun claimMilestoneConduit(tier: MilestoneTier) {
        val milestoneKey = "milestone_${tier.name.lowercase()}"
        val currentState = operationsState.value
        if (currentState.currentOpsPoints < tier.targetPoints) return

        viewModelScope.launch {
            claimMutex.withLock {
                if (_inFlightClaims.value.contains(milestoneKey)) return@launch
                _inFlightClaims.update { it + milestoneKey }
            }

            try {
                repository.setMissionClaimed(milestoneKey, true)
                profileManager.addStarCurrency(tier.starReward)
            } finally {
                claimMutex.withLock {
                    _inFlightClaims.update { it - milestoneKey }
                }
            }
        }
    }

    fun claimDailyCrate() {
        if (isCrateClaimed.value) return
        val allClaimed = _missions.value.all { it.state == QuestState.CLAIMED }
        if (!allClaimed) return

        viewModelScope.launch {
            repository.setMissionClaimed("daily_crate", true)
            profileManager.addStarCurrency(200)
        }
    }
}
