package com.example.gridsurge.features.adventure.data

import com.example.gridsurge.features.adventure.core.ProceduralStageGenerator
import com.example.gridsurge.features.adventure.data.sectors.Sector01Pack
import com.example.gridsurge.features.adventure.data.sectors.Sector02Pack
import com.example.gridsurge.features.adventure.data.sectors.Sector03Pack
import com.example.gridsurge.features.adventure.data.sectors.Sector04Pack
import com.example.gridsurge.features.adventure.data.sectors.Sector05Pack
import com.example.gridsurge.features.adventure.domain.model.StageDefinition
import com.example.gridsurge.features.adventure.domain.model.StageId
import com.example.gridsurge.features.adventure.domain.provider.SectorPack
import com.example.gridsurge.features.adventure.model.AdventureLevelBlueprint
import com.example.gridsurge.features.adventure.model.StageStarBenchmark

object AdventureSectorRegistry {

    private val packs = mutableMapOf<Int, SectorPack>()

    init {
        registerPack(Sector01Pack)
        registerPack(Sector02Pack)
        registerPack(Sector03Pack)
        registerPack(Sector04Pack)
        registerPack(Sector05Pack)
    }

    fun registerPack(pack: SectorPack) {
        pack.validateIntegrity()
        packs[pack.metadata.sectorId] = pack
    }

    fun getSector(sectorId: Int): SectorPack? {
        return packs[sectorId]
    }

    fun getAllSectors(): List<SectorPack> {
        return packs.values.sortedBy { it.metadata.sectorId }
    }

    val SECTORS: List<SectorPack> get() = getAllSectors()

    fun getStage(stageId: StageId): StageDefinition {
        val pack = packs[stageId.sectorId]
        return pack?.getStage(stageId.stageIndex)
            ?: ProceduralStageGenerator.generateStage(stageId.sectorId, stageId.stageIndex)
    }

    /**
     * Helper to resolve global stage numbers (e.g. 1..18) to StageId.
     */
    fun getStageByGlobalIndex(globalStageNumber: Int): StageDefinition {
        val sectorId = ((globalStageNumber - 1) / 9) + 1
        val stageIndex = ((globalStageNumber - 1) % 9) + 1
        return getStage(StageId(sectorId, stageIndex))
    }

    /**
     * Legacy adapter method for backwards compatibility with legacy engine callers.
     */
    fun getLevelBlueprint(levelNumber: Int): AdventureLevelBlueprint {
        val stage = getStageByGlobalIndex(levelNumber)
        return AdventureLevelBlueprint(
            levelNumber = levelNumber,
            sectorId = stage.stageId.sectorId,
            stageName = stage.blueprint.stageName,
            directive = stage.blueprint.directive,
            objective = stage.blueprint.objective,
            initialCores = stage.blueprint.initialCores,
            hasSectorHazards = stage.blueprint.hasSectorHazards,
            hazardIntervalMoves = stage.blueprint.hazardIntervalMoves
        )
    }

    /**
     * Legacy adapter method for benchmark lookups.
     */
    fun getBenchmark(globalStageNumber: Int): StageStarBenchmark {
        val stage = getStageByGlobalIndex(globalStageNumber)
        return StageStarBenchmark(
            stageNumber = globalStageNumber,
            moveBudgetStar2 = stage.benchmarks.moveBudgetStar2,
            timeLimitSecStar2 = stage.benchmarks.timeLimitSecStar2,
            masteryFeat = stage.benchmarks.masteryFeat ?: com.example.gridsurge.features.adventure.model.MasteryFeatSpec(
                featType = com.example.gridsurge.features.adventure.model.MasteryFeatType.NONE,
                targetValue = 0,
                description = "None"
            )
        )
    }
}
