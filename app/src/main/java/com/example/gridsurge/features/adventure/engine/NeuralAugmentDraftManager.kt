package com.example.gridsurge.features.adventure.engine

import com.example.gridsurge.R
import com.example.gridsurge.features.adventure.model.AugmentRarity
import com.example.gridsurge.features.adventure.model.AugmentType
import com.example.gridsurge.features.adventure.model.NeuralAugment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class NeuralAugmentDraftManager {

    private val augmentCatalog = listOf(
        NeuralAugment("aug_cardinal", AugmentType.CARDINAL_OVERCLOCK, "CARDINAL OVERCLOCK", "Line clears fire secondary perpendicular lasers across the board.", AugmentRarity.LEGENDARY, R.drawable.ic_aug_cardinal_overclock, 1),
        NeuralAugment("aug_buffer", AugmentType.BUFFER_OPTIMIZER, "BUFFER OPTIMIZER", "+2 Combo Grace Moves (Buffer pips never decay on 1st miss).", AugmentRarity.COMMON, R.drawable.ic_aug_buffer_optimizer, 1),
        NeuralAugment("aug_molten", AugmentType.MOLTEN_HARVEST, "MOLTEN HARVEST", "Multi-line clears grant +2.5x score multiplier.", AugmentRarity.RARE, R.drawable.ic_aug_molten_harvest, 1),
        NeuralAugment("aug_kinetic", AugmentType.KINETIC_BURST, "KINETIC BURST", "Clearing 2+ lines simultaneously deals 1 damage to all active cores.", AugmentRarity.RARE, R.drawable.ic_aug_chrono_siphon, 1),
        NeuralAugment("aug_cavity", AugmentType.CAVITY_COMPRESSOR, "CAVITY COMPRESSOR", "Spawns smaller pieces when the board is getting full.", AugmentRarity.COMMON, R.drawable.ic_aug_cavity_compressor, 1),
        NeuralAugment("aug_warp", AugmentType.WARP_INJECTOR, "WARP INJECTOR", "+50% Phase Resonance energy charge rate on line clears.", AugmentRarity.LEGENDARY, R.drawable.ic_aug_warp_injector, 1),
        NeuralAugment("aug_corrosion", AugmentType.CORROSION_SHIELD, "CORROSION SHIELD", "Hazard slag and toxic slime cannot spread to adjacent cells.", AugmentRarity.LEGENDARY, R.drawable.ic_aug_corrosion_shield, 3)
    )

    private val _installedAugments = MutableStateFlow<List<NeuralAugment>>(emptyList())
    val activeAugments: StateFlow<List<NeuralAugment>> = _installedAugments.asStateFlow()

    private val _draftOptions = MutableStateFlow<List<NeuralAugment>>(emptyList())
    val draftOptions: StateFlow<List<NeuralAugment>> = _draftOptions.asStateFlow()

    fun rollAugmentDraft(sectorId: Int = 1, prng: Random = Random.Default): List<NeuralAugment> {
        val installedIds = _installedAugments.value.map { it.id }.toSet()
        val eligible = augmentCatalog.filter { 
            sectorId >= it.minSectorRequired && it.id !in installedIds 
        }
        // Always present up to 3 shuffled options from the eligible pool
        val draft = eligible.shuffled(prng).take(3)
        _draftOptions.value = draft
        return draft
    }

    fun syncFromSavedAugmentIds(savedIds: Set<String>) {
        val loaded = augmentCatalog.filter { it.id in savedIds }
        _installedAugments.value = loaded
    }

    fun selectAugment(augment: NeuralAugment) {
        if (!_installedAugments.value.any { it.id == augment.id }) {
            _installedAugments.value = _installedAugments.value + augment
        }
        _draftOptions.value = emptyList()
    }

    fun hasAugment(type: AugmentType): Boolean = _installedAugments.value.any { it.type == type }

    fun resetRun() {
        _installedAugments.value = emptyList()
        _draftOptions.value = emptyList()
    }
}
