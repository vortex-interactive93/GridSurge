package com.example.gridsurge.game.glitch

import android.graphics.Point
import kotlin.random.Random

enum class InfectionPhase {
    INCUBATING, // Turns = 3 (Dormant Toxic Green)
    WARNING,    // Turns = 2 (Amber Caution)
    CRITICAL,   // Turns = 1 (Flashing Crimson Glitch)
    STUNNED     // Stalled this turn by adjacent placement (Cyan Energy Shield)
}

data class GlitchSpreadEvent(
    val fromIndex: Int,
    val toIndex: Int
)

data class GlitchTurnResult(
    val purgedCount: Int,
    val spreadEvents: List<GlitchSpreadEvent>
)

class InfectedCell(val index: Int) {
    var turnsRemaining: Int = 3
    val maxTurns: Int = 3
    var phase: InfectionPhase = InfectionPhase.INCUBATING
    var isStunnedThisTurn: Boolean = false

    fun applyStun() {
        isStunnedThisTurn = true
        turnsRemaining = (turnsRemaining + 1).coerceAtMost(maxTurns)
        phase = InfectionPhase.STUNNED
    }

    fun tickTurn(): Boolean {
        if (isStunnedThisTurn) {
            isStunnedThisTurn = false
            phase = if (turnsRemaining <= 1) InfectionPhase.CRITICAL else InfectionPhase.INCUBATING
            return false // Stalled: does not spread this turn
        }

        turnsRemaining--
        phase = when (turnsRemaining) {
            1 -> InfectionPhase.CRITICAL
            2 -> InfectionPhase.WARNING
            else -> InfectionPhase.INCUBATING
        }
        return turnsRemaining <= 0
    }

    fun resetCounter() {
        turnsRemaining = maxTurns
        phase = InfectionPhase.INCUBATING
        isStunnedThisTurn = false
    }
}

class GlitchEngine(val gridSize: Int = 8) {
    val activeInfections = HashMap<Int, InfectedCell>()

    private var prng: Random = Random.Default

    var currentWave: Int = 1
        private set
    var totalPurgedCount: Int = 0
        private set
    var waveInfectionsSpawned: Int = 0
    private val maxSimultaneousInfections: Int
        get() = (4 + currentWave).coerceAtMost(7)

    fun initializeGlitchMatch(random: Random = Random.Default) {
        this.prng = random
        activeInfections.clear()
        currentWave = 1
        totalPurgedCount = 0
        waveInfectionsSpawned = 0
        startNewOutbreakWave()
    }

    fun startNewOutbreakWave() {
        activeInfections.clear()
        waveInfectionsSpawned = 0

        // Spawn 2 initial Patient Zero nodes in central area
        val centralPositions = intArrayOf(19, 20, 27, 28, 35, 36, 43, 44)
        val p1 = centralPositions[prng.nextInt(centralPositions.size)]
        var p2 = centralPositions[prng.nextInt(centralPositions.size)]
        while (p2 == p1) {
            p2 = centralPositions[prng.nextInt(centralPositions.size)]
        }

        spawnInfectionAt(p1)
        spawnInfectionAt(p2)
    }

    fun spawnInfectionAt(index: Int): Boolean {
        if (activeInfections.size >= maxSimultaneousInfections) return false
        if (index !in 0 until (gridSize * gridSize)) return false
        if (activeInfections.containsKey(index)) return false

        activeInfections[index] = InfectedCell(index)
        waveInfectionsSpawned++
        return true
    }

    fun onTurnResolved(
        clearedRows: List<Int>,
        clearedCols: List<Int>,
        currentGrid: IntArray
    ): GlitchTurnResult {
        val clearedIndices = mutableSetOf<Int>()
        for (r in clearedRows) {
            for (c in 0 until gridSize) clearedIndices.add(r * gridSize + c)
        }
        for (c in clearedCols) {
            for (r in 0 until gridSize) clearedIndices.add(r * gridSize + c)
        }

        var purgedThisTurn = 0
        val spreadEvents = mutableListOf<GlitchSpreadEvent>()
        val iterator = activeInfections.entries.iterator()

        while (iterator.hasNext()) {
            val entry = iterator.next()
            val index = entry.key
            val infection = entry.value

            if (clearedIndices.contains(index)) {
                // Permanently remove destroyed infection
                iterator.remove()
                totalPurgedCount++
                purgedThisTurn++
            } else {
                // Decrement turn countdown
                infection.turnsRemaining--

                if (infection.turnsRemaining <= 0) {
                    // SPREAD INFECTION: Infect an adjacent cardinal neighbor
                    val spreadTarget = findAdjacentSpreadTarget(index, currentGrid)
                    if (spreadTarget != -1) {
                        spreadEvents.add(GlitchSpreadEvent(fromIndex = index, toIndex = spreadTarget))
                    }
                    // Reset this catalyst's countdown
                    infection.turnsRemaining = 4
                    infection.phase = InfectionPhase.INCUBATING
                } else {
                    infection.phase = when {
                        infection.turnsRemaining <= 1 -> InfectionPhase.CRITICAL
                        infection.turnsRemaining <= 2 -> InfectionPhase.WARNING
                        else -> InfectionPhase.INCUBATING
                    }
                }
            }
        }

        // Add newly spread infections
        spreadEvents.forEach { event ->
            if (!activeInfections.containsKey(event.toIndex)) {
                activeInfections[event.toIndex] = InfectedCell(event.toIndex).apply {
                    turnsRemaining = 4
                    phase = InfectionPhase.INCUBATING
                }
                // Mark in grid array so spawnNewCatalyst doesn't pick it
                currentGrid[event.toIndex] = 9
            }
        }

        // Ensure at least 2 active catalysts exist on board until 20 total are cleared
        while (activeInfections.size < 2 && (totalPurgedCount + activeInfections.size) < 20) {
            spawnNewCatalyst(currentGrid)
        }

        return GlitchTurnResult(purgedThisTurn, spreadEvents)
    }

    private fun findAdjacentSpreadTarget(index: Int, grid: IntArray): Int {
        val r = index / gridSize
        val c = index % gridSize
        val candidates = mutableListOf<Int>()
        
        val neighbors = arrayOf(
            Point(c, r - 1), Point(c, r + 1), Point(c - 1, r), Point(c + 1, r)
        ).filter { it.x in 0 until gridSize && it.y in 0 until gridSize }

        for (pt in neighbors) {
            val nIdx = pt.y * gridSize + pt.x
            if (grid[nIdx] == 0 && !activeInfections.containsKey(nIdx)) {
                candidates.add(nIdx)
            }
        }

        return if (candidates.isNotEmpty()) {
            candidates[prng.nextInt(candidates.size)]
        } else -1
    }

    private fun spawnNewCatalyst(currentGrid: IntArray) {
        val emptyIndices = mutableListOf<Int>()
        for (i in 0 until (gridSize * gridSize)) {
            if (currentGrid[i] == 0 && !activeInfections.containsKey(i)) {
                emptyIndices.add(i)
            }
        }
        if (emptyIndices.isNotEmpty()) {
            val nextIdx = emptyIndices[prng.nextInt(emptyIndices.size)]
            spawnInfectionAt(nextIdx)
            currentGrid[nextIdx] = 9
        }
    }

    /**
     * Stuns any infected block that physically touches newly placed blocks.
     */
    fun checkAdjacentContainmentStun(placedIndices: List<Int>): Int {
        var stunnedCount = 0
        for (idx in placedIndices) {
            val r = idx / gridSize
            val c = idx % gridSize

            val neighbors = arrayOf(
                Point(c, r - 1), Point(c, r + 1), Point(c - 1, r), Point(c + 1, r)
            ).filter { it.x in 0 until gridSize && it.y in 0 until gridSize }

            for (pt in neighbors) {
                val neighborIdx = pt.y * gridSize + pt.x
                val infected = activeInfections[neighborIdx]
                if (infected != null && !infected.isStunnedThisTurn) {
                    infected.applyStun()
                    stunnedCount++
                }
            }
        }
        return stunnedCount
    }

    /**
     * Executes spread tick and enforces the global contagion cap.
     */
    fun onPlayerMoveCommitted(gridMatrix: IntArray): List<Int> {
        val newlyInfected = mutableListOf<Int>()
        val spreadingCells = mutableListOf<InfectedCell>()

        for (cell in activeInfections.values) {
            if (cell.tickTurn()) {
                spreadingCells.add(cell)
            }
        }

        for (source in spreadingCells) {
            if (activeInfections.size >= maxSimultaneousInfections) {
                source.resetCounter()
                continue // Reached cap: resets timer without flooding board
            }

            val r = source.index / gridSize
            val c = source.index % gridSize

            val uninfectedNeighbors = arrayOf(
                Point(c, r - 1), Point(c, r + 1), Point(c - 1, r), Point(c + 1, r)
            ).filter { it.x in 0 until gridSize && it.y in 0 until gridSize }
             .filter { !activeInfections.containsKey(it.y * gridSize + it.x) }

            if (uninfectedNeighbors.isNotEmpty()) {
                val target = uninfectedNeighbors[prng.nextInt(uninfectedNeighbors.size)]
                val targetIdx = target.y * gridSize + target.x

                spawnInfectionAt(targetIdx)
                gridMatrix[targetIdx] = 9 // SPECIAL_CORE / Virus ID
                newlyInfected.add(targetIdx)
            }
            source.resetCounter()
        }

        return newlyInfected
    }

    /**
     * EMP Cleanse: Line clears destroy infected blocks + cleanse 1 adjacent tile radius.
     */
    fun checkAndPurgeLines(rowsMask: Int, colsMask: Int, gridMatrix: IntArray): Pair<Int, Boolean> {
        var purgedCount = 0
        val directHits = mutableListOf<Int>()

        // 1. Identify directly hit infected tiles in cleared rows/columns
        for (entry in activeInfections.entries) {
            val r = entry.key / gridSize
            val c = entry.key % gridSize
            if ((rowsMask and (1 shl r)) != 0 || (colsMask and (1 shl c)) != 0) {
                directHits.add(entry.key)
            }
        }

        // 2. Cleanse direct hits + orthogonal neighbors (EMP Shockwave)
        val allPurgedIndices = HashSet<Int>(directHits)
        for (idx in directHits) {
            val r = idx / gridSize
            val c = idx % gridSize
            val neighbors = arrayOf(
                Point(c, r - 1), Point(c, r + 1), Point(c - 1, r), Point(c + 1, r)
            ).filter { it.x in 0 until gridSize && it.y in 0 until gridSize }

            for (pt in neighbors) {
                val nIdx = pt.y * gridSize + pt.x
                if (activeInfections.containsKey(nIdx)) {
                    allPurgedIndices.add(nIdx)
                }
            }
        }

        for (idx in allPurgedIndices) {
            activeInfections.remove(idx)
            gridMatrix[idx] = 0 // Clear cell
            purgedCount++
        }

        totalPurgedCount += purgedCount

        // 3. Check for Total Wave Purge (Outbreak Cleared!)
        val isWaveCleared = activeInfections.isEmpty()
        if (isWaveCleared) {
            currentWave++
        }

        return Pair(purgedCount, isWaveCleared)
    }
}
