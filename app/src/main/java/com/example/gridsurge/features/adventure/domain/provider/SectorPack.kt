package com.example.gridsurge.features.adventure.domain.provider

import com.example.gridsurge.features.adventure.domain.model.SectorMetadata
import com.example.gridsurge.features.adventure.domain.model.StageDefinition

interface SectorPack {
    val metadata: SectorMetadata
    val totalStages: Int get() = metadata.totalStages
    fun getStage(stageIndex: Int): StageDefinition?
    fun getAllStages(): List<StageDefinition>
    fun validateIntegrity()
}

abstract class BaseSectorPack(
    override val metadata: SectorMetadata,
    private val stages: List<StageDefinition>
) : SectorPack {

    private val stageIndexMap: Map<Int, StageDefinition> = stages.associateBy { it.stageId.stageIndex }

    override val totalStages: Int = stages.size

    override fun getStage(stageIndex: Int): StageDefinition? = stageIndexMap[stageIndex]

    override fun getAllStages(): List<StageDefinition> = stages

    override fun validateIntegrity() {
        check(stages.isNotEmpty()) {
            "Sector ${metadata.sectorId} (${metadata.sectorName}) contains zero stages"
        }
        stages.forEachIndexed { idx, stage ->
            val expectedIndex = idx + 1
            check(stage.stageId.stageIndex == expectedIndex) {
                "Sector ${metadata.sectorId} stage at index $idx has stageIndex ${stage.stageId.stageIndex} (expected $expectedIndex)"
            }
            check(stage.stageId.sectorId == metadata.sectorId) {
                "Sector ${metadata.sectorId} contains stage with mismatched sectorId ${stage.stageId.sectorId}"
            }
        }
    }
}
