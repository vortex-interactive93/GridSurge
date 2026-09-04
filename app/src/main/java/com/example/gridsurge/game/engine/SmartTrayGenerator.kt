package com.example.gridsurge.game.engine

import com.example.gridsurge.features.adventure.model.ObjectiveType
import com.example.gridsurge.game.model.PolyShape
import com.example.gridsurge.game.model.PolyominoCatalog
import com.example.gridsurge.game.model.ShapeBlueprint

object SmartTrayGenerator {

    fun generateTrayPieces(
        boardGrid: Array<IntArray>,
        activeDirective: ObjectiveType,
        boardFillRatio: Float = 0f,
        comboStreak: Int = 0,
        isBossStage: Boolean = false,
        isBlitzClash: Boolean = false,
        matchSeed: Long = 0L,
        moveIndex: Int = 0,
        activeCoreCount: Int = 0
    ): List<PolyShape> {
        val random = if (isBlitzClash) java.util.Random(matchSeed + moveIndex) else java.util.Random()
        val tray = mutableListOf<PolyShape>()
        val chosenFamilyKeys = mutableSetOf<String>()

        val isCriticalDanger = boardFillRatio >= 0.78f
        val shouldBanPento = isBossStage || isBlitzClash || boardFillRatio >= 0.65f
        val isFragmented = activeCoreCount >= 2 && boardFillRatio >= 0.45f

        // --- 1. CRITICAL DANGER OVERRIDE ---
        if (isCriticalDanger) {
            repeat(3) {
                val family = PolyominoCatalog.MICRO_FAMILIES[random.nextInt(PolyominoCatalog.MICRO_FAMILIES.size)]
                val fittingRotations = family.filter { rotation -> 
                    PolyominoCatalog.getShapesThatFit(listOf(rotation), boardGrid).isNotEmpty() &&
                    getFamilyKey(rotation.id) !in chosenFamilyKeys
                }
                var chosen = if (fittingRotations.isNotEmpty()) fittingRotations[random.nextInt(fittingRotations.size)] else family[random.nextInt(family.size)]
                
                // For synthesis stages, ensure at least one rescue piece is a conduit
                if (activeDirective == ObjectiveType.CHROMA_SYNTHESIS && it == 0) {
                    chosen = PolyominoCatalog.SPECIAL_CIRCUIT[random.nextInt(PolyominoCatalog.SPECIAL_CIRCUIT.size)]
                }
                
                tray.add(PolyominoCatalog.instantiate(chosen))
                chosenFamilyKeys.add(getFamilyKey(chosen.id))
            }
            return validateTray(tray, boardGrid)
        }

        val vacantCount = PolyominoCatalog.countVacantCells(boardGrid)
        val isCongested = vacantCount < 32

        // --- 2. SLOT 1: Boss Line-Maker Guarantee or Legal Placement ---
        var shape1: ShapeBlueprint
        if (isBossStage || isBlitzClash) {
            // Force precision carving piece
            val precisionPool = (PolyominoCatalog.MICRO_FAMILIES + PolyominoCatalog.TETRO_FAMILIES.filter { family ->
                family.first().id.contains("LINE") || family.first().id.contains("DOMINO") 
            }).flatten()
            val validPrecision = PolyominoCatalog.getShapesThatFit(precisionPool, boardGrid)
            shape1 = if (validPrecision.isNotEmpty()) validPrecision[random.nextInt(validPrecision.size)] else PolyominoCatalog.MONOMINO_1X1
        } else {
            val slot1Families = when {
                isFragmented -> PolyominoCatalog.MICRO_FAMILIES
                isCongested -> PolyominoCatalog.MICRO_FAMILIES + PolyominoCatalog.TETRO_FAMILIES
                else -> PolyominoCatalog.TETRO_FAMILIES + PolyominoCatalog.MICRO_FAMILIES
            }
            val allLegalShapes = PolyominoCatalog.getShapesThatFit(slot1Families.flatten(), boardGrid)
            shape1 = if (allLegalShapes.isNotEmpty()) allLegalShapes[random.nextInt(allLegalShapes.size)] else PolyominoCatalog.MONOMINO_1X1
        }
        
        if (activeDirective == ObjectiveType.CHROMA_SYNTHESIS) {
            shape1 = PolyominoCatalog.SPECIAL_CIRCUIT[random.nextInt(PolyominoCatalog.SPECIAL_CIRCUIT.size)]
        }

        tray.add(PolyominoCatalog.instantiate(shape1))
        chosenFamilyKeys.add(getFamilyKey(shape1.id))

        // --- 3. SLOT 2: Diverse Tetromino ---
        val availableTetroFamilies = (if (isFragmented) PolyominoCatalog.MICRO_FAMILIES else PolyominoCatalog.TETRO_FAMILIES).filterNot { family ->
            getFamilyKey(family.first().id) in chosenFamilyKeys
        }
        val chosenTetroFamily = if (availableTetroFamilies.isNotEmpty()) availableTetroFamilies[random.nextInt(availableTetroFamilies.size)] else PolyominoCatalog.TETRO_FAMILIES[random.nextInt(PolyominoCatalog.TETRO_FAMILIES.size)]
        val shape2 = chosenTetroFamily[random.nextInt(chosenTetroFamily.size)]
        
        tray.add(PolyominoCatalog.instantiate(shape2))
        chosenFamilyKeys.add(getFamilyKey(shape2.id))

        // --- 4. SLOT 3: Controlled Wildcard (Deduplicated) ---
        var shape3 = if (comboStreak >= 4 && !isBlitzClash && activeDirective != ObjectiveType.CHROMA_SYNTHESIS) {
            val specialFamily = PolyominoCatalog.SPECIAL_FAMILIES[random.nextInt(PolyominoCatalog.SPECIAL_FAMILIES.size)]
            specialFamily[random.nextInt(specialFamily.size)]
        } else {
            val roll = random.nextInt(100) + 1
            val targetFamilies = when {
                isFragmented -> PolyominoCatalog.MICRO_FAMILIES
                isCongested -> PolyominoCatalog.MICRO_FAMILIES
                roll <= 60 -> PolyominoCatalog.TETRO_FAMILIES
                roll <= 90 -> PolyominoCatalog.MICRO_FAMILIES
                else -> (if (shouldBanPento) emptyList() else PolyominoCatalog.PENTO_FAMILIES) + (if (isBlitzClash) emptyList() else PolyominoCatalog.SPECIAL_FAMILIES)
            }.filterNot { family ->
                getFamilyKey(family.first().id) in chosenFamilyKeys
            }
            
            val finalFamily = if (targetFamilies.isNotEmpty()) targetFamilies[random.nextInt(targetFamilies.size)] else {
                // Last ditch effort: find any family not used
                val unused = (PolyominoCatalog.MICRO_FAMILIES + (if (isBlitzClash) emptyList() else PolyominoCatalog.TETRO_FAMILIES)).filterNot { getFamilyKey(it.first().id) in chosenFamilyKeys }
                if (unused.isNotEmpty()) unused[random.nextInt(unused.size)] else PolyominoCatalog.MICRO_FAMILIES[random.nextInt(PolyominoCatalog.MICRO_FAMILIES.size)]
            }
            finalFamily[random.nextInt(finalFamily.size)]
        }

        // Force Slot 3 to be a Circuit Conduit in Synthesis stages if it's not already a special block
        if (!isBlitzClash && activeDirective == ObjectiveType.CHROMA_SYNTHESIS && shape3.specialType == com.example.gridsurge.game.model.SpecialBlockType.NONE) {
            shape3 = PolyominoCatalog.SPECIAL_CIRCUIT[random.nextInt(PolyominoCatalog.SPECIAL_CIRCUIT.size)]
        }
        
        tray.add(PolyominoCatalog.instantiate(shape3))

        return validateTray(tray, boardGrid)
    }

    private fun validateTray(tray: List<PolyShape>, boardGrid: Array<IntArray>): List<PolyShape> {
        return tray.map { piece ->
            // Convert PolyShape offsets back to blueprint to check fit using helper
            val blueprint = ShapeBlueprint(piece.id, "", piece.offsets, piece.color, piece.specialType)
            val fits = PolyominoCatalog.getShapesThatFit(listOf(blueprint), boardGrid)
            if (fits.isEmpty()) {
                // Rescue with a Dot if it doesn't fit
                PolyominoCatalog.instantiate(PolyominoCatalog.MONOMINO_1X1)
            } else {
                piece
            }
        }
    }

    private fun getFamilyKey(id: String): String {
        return when {
            id.contains('_') -> id.substringBeforeLast('_')
            else -> id
        }
    }
}
