package com.example.gridsurge.features.adventure.engine

import com.example.gridsurge.features.adventure.model.RelicAbilityType
import com.example.gridsurge.features.adventure.model.RelicCyberWareState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RelicCyberWareManager {

    private val _relicState = MutableStateFlow(
        RelicCyberWareState(
            abilityType = RelicAbilityType.NONE,
            currentEnergy = 0,
            maxEnergy = 100,
            isUnlocked = false,
            isReady = false
        )
    )
    val relicState: StateFlow<RelicCyberWareState> = _relicState.asStateFlow()

    fun configureRelic(equippedRelic: RelicAbilityType) {
        val unlocked = equippedRelic != RelicAbilityType.NONE

        _relicState.update {
            it.copy(
                abilityType = equippedRelic,
                currentEnergy = 0, // Always starts at 0% energy
                maxEnergy = equippedRelic.requiredEnergyPoints,
                isUnlocked = unlocked,
                isReady = false,
                activationCountThisRun = 0
            )
        }
    }

    fun configureSectorRelic(currentSectorId: Int, isRelicClaimed: Boolean) {
        val relic = if (isRelicClaimed) {
            RelicAbilityType.getRelicForSector(currentSectorId)
        } else {
            RelicAbilityType.NONE
        }
        configureRelic(relic)
    }

    fun updateOccupancy(boardOccupancy: Float) {
        val isDanger = boardOccupancy >= 0.75f
        if (_relicState.value.isOverclockDanger != isDanger) {
            _relicState.update { it.copy(isOverclockDanger = isDanger) }
        }
    }

    fun onPiecePlaced(boardOccupancy: Float) {
        if (!_relicState.value.isUnlocked || _relicState.value.isReady) return

        val isDanger = boardOccupancy >= 0.75f
        val gain = if (isDanger) 5 else 1 // +5% in danger, +1% normal

        _relicState.update { current ->
            val updated = (current.currentEnergy + gain).coerceAtMost(current.maxEnergy)
            current.copy(
                currentEnergy = updated,
                isReady = updated >= current.maxEnergy,
                isOverclockDanger = isDanger
            )
        }
    }

    fun onLinesCleared(linesCleared: Int, comboStreak: Int, boardOccupancy: Float = 0f) {
        if (!_relicState.value.isUnlocked || _relicState.value.isReady) return

        val isDanger = boardOccupancy >= 0.75f
        val baseEnergy = if (isDanger) {
            when (linesCleared) {
                1 -> 20 // Double in danger!
                2 -> 50
                else -> 100
            }
        } else {
            when (linesCleared) {
                1 -> 10 // Normal: +10%
                2 -> 25 // Normal: +25%
                3 -> 50 // Normal: +50%
                else -> 100 // Quad: 100%
            }
        }

        val comboBonus = if (comboStreak >= 2) (comboStreak * 3) else 0
        val gain = baseEnergy + comboBonus

        _relicState.update { current ->
            val updated = (current.currentEnergy + gain).coerceAtMost(current.maxEnergy)
            current.copy(
                currentEnergy = updated,
                isReady = updated >= current.maxEnergy,
                isOverclockDanger = isDanger
            )
        }
    }

    fun triggerActivation(): RelicAbilityType? {
        val current = _relicState.value
        if (!current.isUnlocked || !current.isReady) return null

        _relicState.update {
            it.copy(
                currentEnergy = 0,
                isReady = false,
                activationCountThisRun = it.activationCountThisRun + 1
            )
        }
        return current.abilityType
    }

    fun resetEnergy() {
        _relicState.update {
            it.copy(currentEnergy = 0, isReady = false)
        }
    }

    // Compatibility bridge for existing calls
    fun resetCooldownInstantly() {
        resetEnergy()
    }
}
