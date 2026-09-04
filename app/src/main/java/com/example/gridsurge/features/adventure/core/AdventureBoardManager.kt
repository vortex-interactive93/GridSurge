package com.example.gridsurge.features.adventure.core

import com.example.gridsurge.features.adventure.model.*
import com.example.gridsurge.game.model.CoreIntegrity
import com.example.gridsurge.game.model.CoreKind
import com.example.gridsurge.game.model.GridCell
import com.example.gridsurge.game.model.SpecialBlockType

interface AdventureEventListener {
    fun onSectorInitialized(initialHp: Int, totalCores: Int)
    fun onCoresCracked(crackedCells: List<GridCell>)
    fun onCoreWaveSpawned(newCores: List<GridCell>)
    fun onCrossBlastTriggered(originRow: Int, originCol: Int, clearedCells: List<GridCell>)
    fun onBossDamaged(currentHp: Int, damageDealt: Int)
    fun onCoreCountUpdated(remainingCores: Int)
    fun onMilestoneReached(percent: Int)
    fun onTimeRefundAwarded(seconds: Int)
    fun onRelicArsenalInjected(specialType: SpecialBlockType)
    fun onSlagTransmutationTriggered(originRow: Int, originCol: Int, clearedSlag: List<GridCell>)
    fun onSupernovaCollapseTriggered(originRow: Int, originCol: Int)
    fun onCoreHitRegistered(row: Int, col: Int, isWarp: Boolean): Boolean
    fun onClutchDefuse(row: Int, col: Int)
    fun onCriticalMeltdownExplosion(row: Int, col: Int, onFinished: () -> Unit)
    fun onSectorVictory(stars: Int, elapsed: Int)
    fun onSectorDefeat()
}

class AdventureBoardManager(private val eventListener: AdventureEventListener) {

    val grid: Array<Array<GridCell>> = Array(8) { r -> Array(8) { c -> GridCell(row = r, col = c) } }
    var activeBlueprint: AdventureLevelBlueprint? = null
        private set

    var activeCoresRemaining: Int = 0
    var totalPurgedThisStage: Int = 0
    var linesClearedThisStage: Int = 0
    var synthesisCount: Int = 0
    var maxStreakReached: Int = 0
    var bossHp: Int = 100
    var isBossDefeated: Boolean = false
    var isAnimationDeferred: Boolean = false
    private var isWave2Spawned: Boolean = false
    private var isVictoryDispatched: Boolean = false
    private var isDefeatDispatched: Boolean = false

    // Unboxed Ring Buffer for Zero-GC Cascading Detonations (8x8 = 64 cells maximum)
    private val detonationQueue = IntArray(64)
    private var queueHead = 0
    private var queueTail = 0

    // Zero-GC Bitmask-tracked cracked cells accumulator
    private val crackedList = ArrayList<GridCell>(64)
    private var crackedMask: Long = 0L

    var onCoreHarvested: (() -> Unit)? = null

    fun loadStage(stageDefinition: com.example.gridsurge.features.adventure.domain.model.StageDefinition) {
        val bp = AdventureLevelBlueprint(
            levelNumber = stageDefinition.stageId.stageIndex,
            sectorId = stageDefinition.stageId.sectorId,
            stageName = stageDefinition.blueprint.stageName,
            directive = stageDefinition.blueprint.directive,
            objective = stageDefinition.blueprint.objective,
            initialCores = stageDefinition.blueprint.initialCores,
            hasSectorHazards = stageDefinition.blueprint.hasSectorHazards,
            hazardIntervalMoves = stageDefinition.blueprint.hazardIntervalMoves
        )
        loadBlueprint(bp)
    }

    fun loadBlueprint(blueprint: AdventureLevelBlueprint) {
        activeBlueprint = blueprint
        totalPurgedThisStage = 0
        linesClearedThisStage = 0
        synthesisCount = 0
        maxStreakReached = 0
        bossHp = 100
        isBossDefeated = false
        isWave2Spawned = false
        isVictoryDispatched = false
        isDefeatDispatched = false

        for (r in 0 until 8) {
            for (c in 0 until 8) {
                grid[r][c] = GridCell(row = r, col = c)
            }
        }

        val totalFurnaces = blueprint.initialCores.count { blueprint.sectorId == 2 && !it.isLocked }
        val baseFurnaceMoves = when {
            totalFurnaces >= 4 -> 14
            totalFurnaces == 3 -> 12
            else -> 10
        }

        blueprint.initialCores.forEach { spec ->
            if (spec.row in 0 until 8 && spec.col in 0 until 8) {
                val kind = when (blueprint.sectorId) {
                    2 -> CoreKind.AMBER_FURNACE
                    3 -> CoreKind.CRIMSON_CIPHER_LOCKED
                    4 -> CoreKind.EMERALD_CONDUIT
                    5 -> CoreKind.PURPLE_SINGULARITY
                    else -> CoreKind.CYAN_REACTOR
                }
                grid[spec.row][spec.col].apply {
                    isFilled = true
                    isCore = true
                    coreKind = kind
                    coreIntegrity = CoreIntegrity.INTACT
                    hitsRemaining = spec.maxHits
                    turnsRemaining = if (blueprint.sectorId == 2 && !spec.isLocked) baseFurnaceMoves else 99
                    isInvulnerable = spec.isLocked
                    isMeltdownActive = false
                }
            }
        }

        activeCoresRemaining = blueprint.initialCores.size
        eventListener.onSectorInitialized(initialHp = 100, totalCores = blueprint.objective.targetAmount)
        eventListener.onCoreCountUpdated(activeCoresRemaining)
    }

    fun damageCore(row: Int, col: Int, elapsedSec: Int, isWarp: Boolean = false): Boolean {
        val cell = grid[row][col]
        if (!cell.isCore || cell.coreIntegrity == CoreIntegrity.DESTROYED) return false

        val hitAllowed = eventListener.onCoreHitRegistered(row, col, isWarp)
        if (!hitAllowed || cell.isInvulnerable) return false

        cell.hitsRemaining--
        onCoreHarvested?.invoke()

        // --- Dynamic Surge: Award Scaled Refunds (+3 to account for -1 move cost) ---
        if (activeBlueprint?.sectorId == 2) {
            val bonusMoves = 3
            applyMoveExtension(bonusMoves)
            eventListener.onTimeRefundAwarded(bonusMoves) 
        }

        val wasDestroyed = cell.hitsRemaining <= 0
        if (wasDestroyed) {
            if (cell.coreKind == CoreKind.AMBER_FURNACE) {
                triggerThermalDetonationChain(row, col, elapsedSec)
            } else {
                neutralizeCore(row, col, elapsedSec)
            }
        } else {
            cell.coreIntegrity = CoreIntegrity.CRACKED
            crackedList.clear()
            crackedList.add(cell)
            eventListener.onCoresCracked(crackedList)
        }
        return wasDestroyed
    }

    private fun neutralizeCore(row: Int, col: Int, elapsedSec: Int) {
        val cell = grid[row][col]
        if (!cell.isCore && cell.coreIntegrity == CoreIntegrity.DESTROYED) return

        if (cell.isMeltdownActive) {
            eventListener.onClutchDefuse(row, col)
        }

        totalPurgedThisStage++
        cell.coreIntegrity = CoreIntegrity.DESTROYED
        cell.isCore = false
        cell.isFilled = false
        cell.hitsRemaining = 0

        activeCoresRemaining = (activeCoresRemaining - 1).coerceAtLeast(0)
        eventListener.onCoreCountUpdated(activeCoresRemaining)

        val bp = activeBlueprint ?: return
        val isBossStage = bp.levelNumber % 9 == 0

        if (isBossStage && row == 3 && col == 3) {
            isBossDefeated = true
        }

        if (isBossStage && activeCoresRemaining == 1 && grid[3][3].isInvulnerable) {
            grid[3][3].isInvulnerable = false
            if (grid[3][3].coreKind == CoreKind.CRIMSON_CIPHER_LOCKED) {
                grid[3][3].coreKind = CoreKind.CRIMSON_CIPHER_EXPOSED
            }
            eventListener.onMilestoneReached(50)
        }

        if (activeCoresRemaining == 0 && totalPurgedThisStage < bp.objective.targetAmount && !isWave2Spawned) {
            val needed = bp.objective.targetAmount - totalPurgedThisStage
            val wave2Placements = when (needed) {
                1 -> listOf(1 to 1)
                2 -> listOf(1 to 1, 6 to 6)
                3 -> listOf(1 to 1, 6 to 6, 3 to 3)
                else -> listOf(1 to 1, 6 to 6)
            }
            spawnWave2(wave2Placements, bp.sectorId)
            return
        }

        checkVictoryConditions(elapsedSec)
    }

    fun processLineClears(
        clearedRows: List<Int>,
        clearedCols: List<Int>,
        elapsedSec: Int,
        currentScore: Long
    ) {
        val bp = activeBlueprint ?: return
        val totalLines = clearedRows.size + clearedCols.size

        if (bp.objective.type == ObjectiveType.LINE_CLEANSE && totalLines > 0) {
            linesClearedThisStage += totalLines
        }

        // Fast Bitmask Conversion (Eliminates O(N) list scans and Integer Boxing)
        var rowMask = 0
        for (i in 0 until clearedRows.size) {
            rowMask = rowMask or (1 shl clearedRows[i])
        }
        var colMask = 0
        for (i in 0 until clearedCols.size) {
            colMask = colMask or (1 shl clearedCols[i])
        }

        if (bp.objective.type == ObjectiveType.CHROMA_SYNTHESIS && totalLines > 0) {
            var validCleared = 0
            for (r in 0 until 8) {
                val rowHit = (rowMask and (1 shl r)) != 0
                for (c in 0 until 8) {
                    val colHit = (colMask and (1 shl c)) != 0
                    if ((rowHit || colHit) && !grid[r][c].isCore) {
                        validCleared++
                    }
                }
            }
            synthesisCount = (synthesisCount + validCleared).coerceAtMost(bp.objective.targetAmount)
        }

        crackedList.clear()
        crackedMask = 0L
        queueHead = 0
        queueTail = 0
        var enqueuedMask: Long = 0L

        // Pass 1: Line Damage Application
        for (r in 0 until 8) {
            val rowHit = (rowMask and (1 shl r)) != 0
            for (c in 0 until 8) {
                val colHit = (colMask and (1 shl c)) != 0
                if (rowHit || colHit) {
                    val cell = grid[r][c]
                    val packed = (r shl 3) or c
                    val bit = 1L shl packed

                    if (cell.isCore && cell.coreIntegrity != CoreIntegrity.DESTROYED && !cell.isInvulnerable) {
                        cell.hitsRemaining--
                        
                        // --- Dynamic Surge: Award Scaled Refunds (+3 or +5 to account for -1 move cost) ---
                        if (activeBlueprint?.sectorId == 2) {
                            val bonusMoves = if (totalLines >= 2) 5 else 3
                            applyMoveExtension(bonusMoves)
                            eventListener.onTimeRefundAwarded(bonusMoves)
                        }

                        if (cell.hitsRemaining <= 0) {
                            if (cell.coreKind == CoreKind.AMBER_FURNACE) {
                                if ((enqueuedMask and bit) == 0L) {
                                    enqueuedMask = enqueuedMask or bit
                                    detonationQueue[queueTail++] = packed
                                }
                            } else {
                                neutralizeCore(r, c, elapsedSec)
                            }
                        } else {
                            cell.coreIntegrity = CoreIntegrity.CRACKED
                            if ((crackedMask and bit) == 0L) {
                                crackedMask = crackedMask or bit
                                crackedList.add(cell)
                            }
                        }
                    } else if (cell.isFilled && !cell.isCore && cell.blockColor == 9) {
                        // Slag Dissolution via Direct Line Clear
                        cell.isFilled = false
                        cell.blockColor = 0
                    }
                }
            }
        }

        // Pass 2: Cascade Resolution via 3x3 Impact Kernel
        while (queueHead < queueTail) {
            val packed = detonationQueue[queueHead++]
            val r = (packed shr 3) and 0x07
            val c = packed and 0x07

            neutralizeCore(r, c, elapsedSec)

            for (dy in -1..1) {
                val tr = r + dy
                if (tr !in 0 until 8) continue
                for (dx in -1..1) {
                    val tc = c + dx
                    if (tc !in 0 until 8) continue

                    val target = grid[tr][tc]
                    val tPacked = (tr shl 3) or tc
                    val tBit = 1L shl tPacked

                    if (target.isCore && target.coreIntegrity != CoreIntegrity.DESTROYED) {
                        if (!target.isInvulnerable) {
                            target.hitsRemaining--
                            if (target.hitsRemaining <= 0) {
                                if (target.coreKind == CoreKind.AMBER_FURNACE) {
                                    if ((enqueuedMask and tBit) == 0L) {
                                        enqueuedMask = enqueuedMask or tBit
                                        detonationQueue[queueTail++] = tPacked
                                    }
                                } else {
                                    neutralizeCore(tr, tc, elapsedSec)
                                }
                            } else {
                                target.coreIntegrity = CoreIntegrity.CRACKED
                                if ((crackedMask and tBit) == 0L) {
                                    crackedMask = crackedMask or tBit
                                    crackedList.add(target)
                                }
                            }
                        }
                    } else if (target.isFilled && !target.isCore) {
                        // Blast vaporizes blocks & Slag within detonation perimeter
                        target.isFilled = false
                        target.blockColor = 0
                    }
                }
            }
        }

        if (crackedList.isNotEmpty()) {
            eventListener.onCoresCracked(crackedList)
        }

        checkVictoryConditions(elapsedSec)
    }

    private fun triggerThermalDetonationChain(originR: Int, originC: Int, elapsedSec: Int) {
        queueHead = 0
        queueTail = 0
        var enqueuedMask = 0L

        val initialPacked = (originR shl 3) or originC
        enqueuedMask = enqueuedMask or (1L shl initialPacked)
        detonationQueue[queueTail++] = initialPacked

        while (queueHead < queueTail) {
            val packed = detonationQueue[queueHead++]
            val r = (packed shr 3) and 0x07
            val c = packed and 0x07

            neutralizeCore(r, c, elapsedSec)

            for (dy in -1..1) {
                val tr = r + dy
                if (tr !in 0 until 8) continue
                for (dx in -1..1) {
                    val tc = c + dx
                    if (tc !in 0 until 8) continue

                    val target = grid[tr][tc]
                    val tPacked = (tr shl 3) or tc
                    val tBit = 1L shl tPacked

                    if (target.isCore && target.coreIntegrity != CoreIntegrity.DESTROYED && !target.isInvulnerable) {
                        target.hitsRemaining--

                        // --- Dynamic Surge: Award Scaled Refunds (+3 to account for -1 move cost) ---
                        if (activeBlueprint?.sectorId == 2) {
                            val bonusMoves = 3 // Cascade hit extension
                            applyMoveExtension(bonusMoves)
                            eventListener.onTimeRefundAwarded(bonusMoves)
                        }

                        if (target.hitsRemaining <= 0) {
                            if (target.coreKind == CoreKind.AMBER_FURNACE) {
                                if ((enqueuedMask and tBit) == 0L) {
                                    enqueuedMask = enqueuedMask or tBit
                                    detonationQueue[queueTail++] = tPacked
                                }
                            } else {
                                neutralizeCore(tr, tc, elapsedSec)
                            }
                        } else {
                            target.coreIntegrity = CoreIntegrity.CRACKED
                        }
                    } else if (target.isFilled && !target.isCore) {
                        target.isFilled = false
                        target.blockColor = 0
                    }
                }
            }
        }
        checkVictoryConditions(elapsedSec)
    }

    fun onMoveCommitted(elapsedSec: Int) {
        if (activeBlueprint?.sectorId == 2) {
            for (r in 0 until 8) {
                for (c in 0 until 8) {
                    val cell = grid[r][c]
                    if (cell.isCore && cell.coreKind == CoreKind.AMBER_FURNACE && cell.coreIntegrity != CoreIntegrity.DESTROYED && !cell.isInvulnerable) {
                        if (cell.isMeltdownActive) {
                            // Meltdown grace failed
                            eventListener.onCriticalMeltdownExplosion(r, c) {
                                transmuteToSlag(cell, r, c)
                                if (activeBlueprint?.objective?.type == ObjectiveType.INFECTED_PURGE) {
                                    triggerDefeat()
                                }
                            }
                            return
                        } else {
                            cell.turnsRemaining--
                            if (cell.turnsRemaining <= 0) {
                                cell.turnsRemaining = 0
                                cell.isMeltdownActive = true
                            }
                        }
                    }
                }
            }
        }
        checkVictoryConditions(elapsedSec)
    }

    private fun transmuteToSlag(cell: GridCell, r: Int, c: Int) {
        cell.coreIntegrity = CoreIntegrity.DESTROYED
        cell.isCore = false
        cell.isFilled = true
        cell.blockColor = 9
        cell.isMeltdownActive = false
        activeCoresRemaining = (activeCoresRemaining - 1).coerceAtLeast(0)
        eventListener.onCoreCountUpdated(activeCoresRemaining)
        eventListener.onSlagTransmutationTriggered(r, c, listOf(cell))
    }

    private fun triggerDefeat() {
        if (isDefeatDispatched || isVictoryDispatched) return
        isDefeatDispatched = true
        eventListener.onSectorDefeat()
    }

    private fun applyMoveExtension(moves: Int) {
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                val cell = grid[r][c]
                if (cell.isCore && cell.coreKind == CoreKind.AMBER_FURNACE && cell.coreIntegrity != CoreIntegrity.DESTROYED) {
                    cell.turnsRemaining = (cell.turnsRemaining + moves).coerceAtMost(15)
                    if (cell.turnsRemaining > 0) cell.isMeltdownActive = false
                }
            }
        }
    }

    fun unlockCrimsonCiphers() {
        var unlockedAny = false
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                val cell = grid[r][c]
                if (cell.isCore && cell.isInvulnerable && cell.coreIntegrity != CoreIntegrity.DESTROYED) {
                    cell.isInvulnerable = false
                    if (cell.coreKind == CoreKind.CRIMSON_CIPHER_LOCKED) {
                        cell.coreKind = CoreKind.CRIMSON_CIPHER_EXPOSED
                    }
                    unlockedAny = true
                }
            }
        }
        if (unlockedAny) {
            eventListener.onMilestoneReached(100)
        }
    }

    fun onComboCommitted(comboStreak: Int, elapsedSec: Int) {
        maxStreakReached = maxOf(maxStreakReached, comboStreak)
        if (comboStreak >= 2 && activeBlueprint?.sectorId == 3) {
            unlockCrimsonCiphers()
        }
        val bp = activeBlueprint
        if (bp != null && bp.objective.type == ObjectiveType.SURGE_STREAK_TARGET) {
            checkVictoryConditions(elapsedSec)
        }
    }

    fun triggerBossVictory(elapsedSec: Int) {
        isBossDefeated = true
        checkVictoryConditions(elapsedSec)
    }

    fun isAnyFurnaceCritical(): Boolean {
        if (activeBlueprint?.sectorId != 2) return false
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                val cell = grid[r][c]
                if (cell.isCore && cell.coreKind == CoreKind.AMBER_FURNACE && cell.coreIntegrity != CoreIntegrity.DESTROYED && !cell.isInvulnerable) {
                    if (cell.turnsRemaining <= 2) return true
                }
            }
        }
        return false
    }

    fun checkVictoryConditions(elapsedSec: Int) {
        if (isVictoryDispatched || isDefeatDispatched || isAnimationDeferred) return
        val bp = activeBlueprint ?: return

        val isBoss = bp.levelNumber % 9 == 0
        val isVictorious = when {
            isBoss -> isBossDefeated
            bp.objective.type == ObjectiveType.LINE_CLEANSE -> linesClearedThisStage >= bp.objective.targetAmount
            bp.objective.type == ObjectiveType.CHROMA_SYNTHESIS -> synthesisCount >= bp.objective.targetAmount
            bp.objective.type == ObjectiveType.SURGE_STREAK_TARGET -> maxStreakReached >= bp.objective.targetAmount
            bp.objective.type == ObjectiveType.INFECTED_PURGE -> {
                totalPurgedThisStage >= bp.objective.targetAmount && activeCoresRemaining == 0
            }
            else -> false
        }

        if (isVictorious) {
            isVictoryDispatched = true
            val stars = when {
                elapsedSec <= bp.objective.star3TimeSec -> 3
                elapsedSec <= bp.objective.star2TimeSec -> 2
                else -> 1
            }
            eventListener.onSectorVictory(stars, elapsedSec)
        }
    }

    private fun spawnWave2(placements: List<Pair<Int, Int>>, sectorId: Int) {
        isWave2Spawned = true
        val spawned = mutableListOf<GridCell>()
        placements.forEach { (col, row) ->
            if (row in 0 until 8 && col in 0 until 8) {
                grid[row][col].apply {
                    isFilled = true
                    isCore = true
                    coreKind = when (sectorId) {
                        2 -> CoreKind.AMBER_FURNACE
                        3 -> CoreKind.CRIMSON_CIPHER_LOCKED
                        4 -> CoreKind.EMERALD_CONDUIT
                        5 -> CoreKind.PURPLE_SINGULARITY
                        else -> CoreKind.CYAN_REACTOR
                    }
                    coreIntegrity = CoreIntegrity.INTACT
                    hitsRemaining = 2
                    turnsRemaining = if (sectorId == 2) 10 else 99
                    isInvulnerable = false
                    isMeltdownActive = false
                }
                spawned.add(grid[row][col])
            }
        }
        activeCoresRemaining += spawned.size
        eventListener.onCoreWaveSpawned(spawned)
        eventListener.onCoreCountUpdated(activeCoresRemaining)
    }
}
