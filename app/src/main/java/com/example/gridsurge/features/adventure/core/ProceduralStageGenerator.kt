package com.example.gridsurge.features.adventure.core

import com.example.gridsurge.features.adventure.domain.model.StageBenchmarks
import com.example.gridsurge.features.adventure.domain.model.StageBlueprint
import com.example.gridsurge.features.adventure.domain.model.StageDefinition
import com.example.gridsurge.features.adventure.domain.model.StageId
import com.example.gridsurge.features.adventure.model.*
import java.util.Random
import kotlin.math.abs

enum class SymmetryType {
    POINT_SYMMETRY,     // 180° rotational symmetry around grid center
    DIAGONAL_MIRROR,    // Reflection along diagonal (x = y)
    VERTICAL_MIRROR,    // Left-right reflection
    RADIAL_QUADRANT     // 4-way rotational symmetry (best for 4 cores)
}

object ProceduralStageGenerator {

    private const val GRID_SIZE = 8

    /**
     * Generates a deterministic, balanced StageDefinition layout for any sector and stage.
     *
     * @param sectorNumber Sector index (e.g., 1..999)
     * @param stageNumber Stage within the sector (e.g., 1..10)
     */
    fun generateStage(sectorNumber: Int, stageNumber: Int): StageDefinition {
        // Deterministic seed ensures identical layouts across all player devices
        val seed = (sectorNumber.toLong() * 10000L) + stageNumber.toLong()
        val rng = Random(seed)

        val globalStageIndex = (sectorNumber - 1) * 10 + stageNumber

        // 1. Calculate Core Count (Scales from 2 to 6 cores maximum)
        val targetCoreCount = when {
            globalStageIndex <= 2 -> 2
            globalStageIndex <= 6 -> 3
            globalStageIndex <= 15 -> 4
            globalStageIndex <= 30 -> 5
            else -> 6
        }

        // 2. Select Architectural Symmetry
        val symmetry = when (targetCoreCount) {
            4 -> SymmetryType.RADIAL_QUADRANT
            else -> SymmetryType.entries[rng.nextInt(SymmetryType.entries.size)]
        }

        // 3. Generate Valid Core Coordinates
        val cores = generateSymmetricCores(targetCoreCount, symmetry, rng)

        // 4. Calculate Dynamic Speed Benchmarks
        val baseThreeStarTime = 30 + (cores.size * 10) - (sectorNumber.coerceAtMost(5) * 2)
        val threeStarSeconds = baseThreeStarTime.coerceAtLeast(35)
        val twoStarSeconds = (threeStarSeconds * 1.85f).toInt()

        val stageName = getProceduralStageName(sectorNumber, stageNumber, rng)
        val coreType = if (sectorNumber == 2) SectorCoreType.SOLAR_CRUCIBLE_SEC2 else SectorCoreType.CHRONO_REACTOR_SEC1

        return StageDefinition(
            stageId = StageId(sectorNumber, stageNumber),
            blueprint = StageBlueprint(
                stageName = stageName,
                directive = "Neutralize all procedural cores to stabilize the matrix.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "${cores.size} CORES PURGED",
                    targetAmount = cores.size,
                    star3TimeSec = threeStarSeconds,
                    star2TimeSec = twoStarSeconds
                ),
                initialCores = cores.map { (row, col) ->
                    CorePlacementSpec(col = col, row = row, coreType = coreType, maxHits = 2)
                }
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 1000 * sectorNumber,
                targetScore2Star = 2500 * sectorNumber,
                targetScore3Star = 4500 * sectorNumber,
                moveBudgetStar2 = 15 + cores.size,
                timeLimitSecStar2 = twoStarSeconds
            )
        )
    }

    private fun generateSymmetricCores(
        targetCount: Int,
        symmetry: SymmetryType,
        rng: Random
    ): List<Pair<Int, Int>> {
        val selectedCores = mutableSetOf<Pair<Int, Int>>()
        var attempts = 0

        while (selectedCores.size < targetCount && attempts < 100) {
            attempts++

            val baseRow = 1 + rng.nextInt(6)
            val baseCol = 1 + rng.nextInt(6)
            val seedPoint = Pair(baseRow, baseCol)

            val pointsToAdd = expandSymmetricPoints(seedPoint, symmetry)

            val candidateSet = selectedCores + pointsToAdd
            if (candidateSet.size <= targetCount && isValidLayout(candidateSet)) {
                selectedCores.addAll(pointsToAdd)
            }
        }

        while (selectedCores.size < targetCount) {
            val r = 1 + rng.nextInt(6)
            val c = 1 + rng.nextInt(6)
            val candidate = Pair(r, c)
            if (!selectedCores.contains(candidate) && isValidPlacement(selectedCores, candidate)) {
                selectedCores.add(candidate)
            }
        }

        return selectedCores.toList()
    }

    private fun expandSymmetricPoints(
        point: Pair<Int, Int>,
        symmetry: SymmetryType
    ): Set<Pair<Int, Int>> {
        val (r, c) = point
        val max = GRID_SIZE - 1
        val points = mutableSetOf<Pair<Int, Int>>()
        points.add(point)

        when (symmetry) {
            SymmetryType.POINT_SYMMETRY -> {
                points.add(Pair(max - r, max - c))
            }
            SymmetryType.DIAGONAL_MIRROR -> {
                points.add(Pair(c, r))
            }
            SymmetryType.VERTICAL_MIRROR -> {
                points.add(Pair(r, max - c))
            }
            SymmetryType.RADIAL_QUADRANT -> {
                points.add(Pair(r, c))
                points.add(Pair(c, max - r))
                points.add(Pair(max - r, max - c))
                points.add(Pair(max - c, r))
            }
        }
        return points
    }

    private fun isValidLayout(cores: Set<Pair<Int, Int>>): Boolean {
        val rowCounts = IntArray(GRID_SIZE)
        val colCounts = IntArray(GRID_SIZE)

        for ((r, c) in cores) {
            rowCounts[r]++
            colCounts[c]++

            if (rowCounts[r] > 2 || colCounts[c] > 2) return false
        }

        for (a in cores) {
            for (b in cores) {
                if (a != b) {
                    val dist = abs(a.first - b.first) + abs(a.second - b.second)
                    if (dist < 2) return false
                }
            }
        }

        return true
    }

    private fun isValidPlacement(existing: Set<Pair<Int, Int>>, candidate: Pair<Int, Int>): Boolean {
        return isValidLayout(existing + candidate)
    }

    private fun getProceduralStageName(sector: Int, stage: Int, rng: Random): String {
        val prefixes = arrayOf("NEXUS", "VERTEX", "SYNAPSE", "CONDUIT", "CIPHER", "QUANTUM", "PULSE")
        val greek = arrayOf("ALPHA", "BETA", "GAMMA", "DELTA", "EPSILON", "ZETA", "OMEGA")
        val prefix = prefixes[rng.nextInt(prefixes.size)]
        val designation = greek[((stage - 1) % greek.size)]
        return "SEC-$sector.$stage // $prefix-$designation"
    }
}
