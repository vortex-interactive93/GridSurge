package com.example.gridsurge.features.adventure.engine

import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.features.adventure.model.BossBattleState
import com.example.gridsurge.features.adventure.model.BossPhase
import com.example.gridsurge.core.GridEngine
import kotlin.random.Random

class BossBattleEngine {

    var state = BossBattleState()
        private set

    private val bossCoreIndices = setOf(27, 28, 35, 36) // Central 2x2 Boss Core

    fun initializeBoss(sectorId: Int, isBossStage: Boolean) {
        state = if (isBossStage) {
            when (sectorId) {
                1 -> BossBattleState(
                    bossName = "NEON GUARDIAN",
                    maxHp = 100,
                    currentHp = 100,
                    phase = BossPhase.SHIELDED,
                    shieldPylonIndices = setOf(9, 14, 49, 54), // (1,1), (1,6), (6,1), (6,6)
                    movesUntilJammerPulse = 4 // Passive hazard telegraph
                )
                2 -> BossBattleState(
                    bossName = "SOL TITAN",
                    maxHp = 100,
                    currentHp = 100,
                    phase = BossPhase.SHIELDED,
                    shieldPylonIndices = setOf(11, 13, 25, 30, 9, 54), // Adjusted for stage config
                    movesUntilJammerPulse = 999 // Disable Jammer for S2
                )
                3 -> BossBattleState(
                    bossName = "NULL SINGULARITY",
                    maxHp = 100,
                    currentHp = 100,
                    phase = BossPhase.SHIELDED,
                    shieldPylonIndices = setOf(18, 19, 20, 26, 28, 34, 35, 36),
                    movesUntilJammerPulse = 4
                )
                4 -> BossBattleState(
                    bossName = "APEX OVERLORD",
                    maxHp = 100,
                    currentHp = 100,
                    phase = BossPhase.SHIELDED,
                    shieldPylonIndices = setOf(9, 14, 49, 54),
                    movesUntilJammerPulse = 3
                )
                else -> BossBattleState(currentHp = 0, phase = BossPhase.DEFEATED)
            }
        } else {
            BossBattleState(currentHp = 0, phase = BossPhase.DEFEATED)
        }
    }

    /**
     * Evaluates line clears against boss shields and core health.
     * @return Damage dealt to boss (0 to 100)
     */
    fun onLinesCleared(clearedRows: List<Int>, clearedCols: List<Int>, engine: GridEngine): Int {
        if (state.phase == BossPhase.DEFEATED) return 0

        var damageDealt = 0
        val remainingPylons = state.shieldPylonIndices.toMutableSet()
        val destroyedThisMove = mutableSetOf<Int>()

        // 1. Relay Overload Logic (Sector 1 and potentially others)
        for (pylonIdx in remainingPylons) {
            val r = pylonIdx / 8
            val c = pylonIdx % 8
            if (clearedRows.contains(r) || clearedCols.contains(c)) {
                destroyedThisMove.add(pylonIdx)
                engine.setGridValue(c, r, com.example.gridsurge.core.CellType.EMPTY.id)
                engine.setCellColor(c, r, 0)
                SfxManager.playSfx(SfxType.CORE_CRACK)
                
                // For Sector 1, each relay deals 18% damage (4 pylons = 72%)
                if (state.bossName == "NEON GUARDIAN") {
                    damageDealt += 18
                }
            }
        }
        
        if (destroyedThisMove.isNotEmpty()) {
            remainingPylons.removeAll(destroyedThisMove)
            val newHp = (state.currentHp - damageDealt).coerceAtLeast(0)
            val newPhase = if (remainingPylons.isEmpty()) BossPhase.OVERDRIVE_VULNERABLE else BossPhase.SHIELDED
            
            state = state.copy(
                shieldPylonIndices = remainingPylons,
                currentHp = newHp,
                phase = newPhase
            )
            return damageDealt
        }

        // 2. Standard Boss Logic (Phase 2 - Apex Core)
        if (state.phase == BossPhase.OVERDRIVE_VULNERABLE) {
            // Apex core is at (3,3) which is index 27
            val hitsCore = clearedRows.contains(3) || clearedCols.contains(3)
            if (hitsCore) {
                onBossCoreDamaged()
                // Each hit on the apex core deals 10% damage (2 hits to finish the remaining 20%)
                damageDealt = 10
                val newHp = (state.currentHp - damageDealt).coerceAtLeast(0)

                state = state.copy(
                    currentHp = newHp,
                    phase = if (newHp <= 0) BossPhase.DEFEATED else BossPhase.OVERDRIVE_VULNERABLE
                )

                if (newHp <= 0) {
                    engine.setGridValue(3, 3, com.example.gridsurge.core.CellType.EMPTY.id)
                    engine.setCellColor(3, 3, 0)
                }
            }
        }

        return damageDealt
    }

    /**
     * Specifically handles pylon destruction from non-line sources (Hammer / Specials).
     */
    fun onPylonsDestroyed(destroyedIndices: Set<Int>) {
        if (state.phase != BossPhase.SHIELDED) return

        val remainingPylons = state.shieldPylonIndices.toMutableSet()
        val hit = remainingPylons.intersect(destroyedIndices)
        
        if (hit.isNotEmpty()) {
            remainingPylons.removeAll(hit)
            if (remainingPylons.isEmpty()) {
                state = state.copy(
                    phase = BossPhase.OVERDRIVE_VULNERABLE,
                    shieldPylonIndices = emptySet(),
                    currentHp = 100
                )
            } else {
                state = state.copy(shieldPylonIndices = remainingPylons)
            }
        }
    }

    fun onApexCoreDirectHit() {
        if (state.phase == BossPhase.OVERDRIVE_VULNERABLE) {
            onBossCoreDamaged()
            state = state.copy(currentHp = 0, phase = BossPhase.DEFEATED)
        }
    }

    fun onBossCoreDamaged() {
        state = state.copy(movesWithoutBossDamage = 0)
    }

    /**
     * Advances the EMP Jammer pulse counter after each player move.
     * Also handles Sector 04 Phase 2 Glitch Flux corruption.
     */
    fun onMoveCommitted(engine: GridEngine, prng: Random = Random.Default, sectorId: Int = 1): Boolean {
        if (state.phase == BossPhase.DEFEATED) return false

        // Phase 2 Boss Escalation: Neon Guardian (Sector 1) Pylon Reconstitution
        if (state.bossName == "NEON GUARDIAN" && state.phase == BossPhase.OVERDRIVE_VULNERABLE) {
            val nextIdle = state.movesWithoutBossDamage + 1
            if (nextIdle >= 4) {
                val cornerPylons = listOf(9, 14, 49, 54)
                val vacantCorners = cornerPylons.filter { engine.getGridValue(it % 8, it / 8) == com.example.gridsurge.core.CellType.EMPTY.id }
                if (vacantCorners.isNotEmpty()) {
                    val revivedPylon = vacantCorners.random()
                    engine.setGridValue(revivedPylon % 8, revivedPylon / 8, com.example.gridsurge.core.CellType.CORE_INTACT.id)
                    engine.setCellColor(revivedPylon % 8, revivedPylon / 8, 0) // Cores handle their own coloring usually
                    state = state.copy(
                        phase = BossPhase.SHIELDED,
                        shieldPylonIndices = setOf(revivedPylon),
                        movesWithoutBossDamage = 0
                    )
                    return true 
                }
            }
            state = state.copy(movesWithoutBossDamage = nextIdle)
        }

        // Sector-based Jammer Lock
        if (sectorId < 3) return false

        // Deadlock Prevention: If only 1 slot is jammed and others are empty, clear it
        val dockCount = (0..2).count { engine.dock[it] != null }
        if (dockCount <= 1 && state.jammedSlotIndex != null) {
            clearJammer()
            return false
        }

        // Sector 4: Master Overlord Glitch Reconstruction
        if (state.bossName == "APEX OVERLORD" && state.phase == BossPhase.OVERDRIVE_VULNERABLE) {
            // Corrupt 1 random empty tile every 4 turns
            if (state.movesUntilJammerPulse % 4 == 0) {
                val emptyIndices = (0 until 64).filter { engine.getGridValue(it % 8, it / 8) == com.example.gridsurge.core.CellType.EMPTY.id }
                if (emptyIndices.isNotEmpty()) {
                    val target = emptyIndices[prng.nextInt(emptyIndices.size)]
                    engine.setGridValue(target % 8, target / 8, com.example.gridsurge.core.CellType.INFECTED.id)
                    engine.setCellColor(target % 8, target / 8, 0) // Infected handle their own color too
                }
            }
        }

        // EMP Jammer pulse logic for Sector 3 & Sector 4
        var jammedSlot = state.jammedSlotIndex
        var jammedTurns = state.jammedTurnsRemaining
        var movesUntilPulse = state.movesUntilJammerPulse - 1
        var pulseTriggered = false

        if (jammedTurns > 0) {
            jammedTurns--
            if (jammedTurns == 0) jammedSlot = null
        }

        if (movesUntilPulse <= 0 && jammedSlot == null) {
            jammedSlot = prng.nextInt(3)
            jammedTurns = 2 // Jammed for 2 full turns
            movesUntilPulse = if (state.bossName == "NULL SINGULARITY") 4 else 3
            pulseTriggered = true
        }

        state = state.copy(
            movesUntilJammerPulse = movesUntilPulse,
            jammedSlotIndex = jammedSlot,
            jammedTurnsRemaining = jammedTurns
        )

        return pulseTriggered
    }

    fun clearJammer() {
        state = state.copy(jammedSlotIndex = null, jammedTurnsRemaining = 0)
    }
}
