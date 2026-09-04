package com.example.gridsurge.game.model

import androidx.core.graphics.toColorInt

data class ShapeBlueprint(
    val id: String,
    val name: String,
    val offsets: List<PolyOffset>,
    val defaultColor: Int,
    val specialType: SpecialBlockType = SpecialBlockType.NONE
) {
    val isSpecial: Boolean get() = specialType != SpecialBlockType.NONE
    
    val bitmask: ULong by lazy {
        var mask = 0uL
        for (offset in offsets) {
            val bitIndex = (offset.y * 8) + offset.x
            mask = mask or (1uL shl bitIndex)
        }
        mask
    }

    val width: Int by lazy { offsets.maxOf { it.x } - offsets.minOf { it.x } + 1 }
    val height: Int by lazy { offsets.maxOf { it.y } - offsets.minOf { it.y } + 1 }
}

object PolyominoCatalog {

    // --- COLOR PALETTE DEFINITIONS ---
    val COLOR_CYAN = "#00E5FF".toColorInt()
    val COLOR_GREEN = "#00FF66".toColorInt()
    val COLOR_YELLOW = "#FFD600".toColorInt()
    val COLOR_ORANGE = "#FF9100".toColorInt()
    val COLOR_BLUE = "#2979FF".toColorInt()
    val COLOR_PURPLE = "#EA80FC".toColorInt()
    val COLOR_RED = "#FF1744".toColorInt()
    val COLOR_DEEP_ORANGE = "#FF6D00".toColorInt()
    val COLOR_PINK = "#FF4081".toColorInt()
    val COLOR_INDIGO = "#7C4DFF".toColorInt()

    // --- 1. MICRO RESCUE FAMILIES (1-3 Tiles) ---
    val FAMILY_DOT = listOf(
        ShapeBlueprint("dot_1x1", "Dot 1x1", listOf(PolyOffset(0, 0)), COLOR_CYAN)
    )

    val FAMILY_DOMINO = listOf(
        ShapeBlueprint("domino_2x1_h", "Domino 2x1 H", listOf(PolyOffset(0, 0), PolyOffset(1, 0)), COLOR_CYAN),
        ShapeBlueprint("domino_1x2_v", "Domino 1x2 V", listOf(PolyOffset(0, 0), PolyOffset(0, 1)), COLOR_CYAN)
    )

    val FAMILY_TRIO_LINE = listOf(
        ShapeBlueprint("trio_3x1_h", "Trio 3x1 H", listOf(PolyOffset(0, 0), PolyOffset(1, 0), PolyOffset(2, 0)), COLOR_GREEN),
        ShapeBlueprint("trio_1x3_v", "Trio 1x3 V", listOf(PolyOffset(0, 0), PolyOffset(0, 1), PolyOffset(0, 2)), COLOR_GREEN)
    )

    val FAMILY_TRIO_CORNER = listOf(
        ShapeBlueprint("corner_2x2_tl", "Corner TL", listOf(PolyOffset(0, 0), PolyOffset(1, 0), PolyOffset(0, 1)), COLOR_GREEN),
        ShapeBlueprint("corner_2x2_tr", "Corner TR", listOf(PolyOffset(0, 0), PolyOffset(1, 0), PolyOffset(1, 1)), COLOR_GREEN),
        ShapeBlueprint("corner_2x2_bl", "Corner BL", listOf(PolyOffset(0, 0), PolyOffset(0, 1), PolyOffset(1, 1)), COLOR_GREEN),
        ShapeBlueprint("corner_2x2_br", "Corner BR", listOf(PolyOffset(1, 0), PolyOffset(0, 1), PolyOffset(1, 1)), COLOR_GREEN)
    )

    val MICRO_FAMILIES = listOf(FAMILY_DOT, FAMILY_DOMINO, FAMILY_TRIO_LINE, FAMILY_TRIO_CORNER)

    // --- 2. TETROMINO FAMILIES (4 Tiles) ---
    val FAMILY_TETRO_I = listOf(
        ShapeBlueprint("tetro_i_h", "Tetro I H", listOf(PolyOffset(0, 0), PolyOffset(1, 0), PolyOffset(2, 0), PolyOffset(3, 0)), COLOR_CYAN),
        ShapeBlueprint("tetro_i_v", "Tetro I V", listOf(PolyOffset(0, 0), PolyOffset(0, 1), PolyOffset(0, 2), PolyOffset(0, 3)), COLOR_CYAN)
    )

    val FAMILY_TETRO_O = listOf(
        ShapeBlueprint("tetro_o", "Tetro O", listOf(PolyOffset(0, 0), PolyOffset(1, 0), PolyOffset(0, 1), PolyOffset(1, 1)), COLOR_YELLOW)
    )

    val FAMILY_TETRO_T = listOf(
        ShapeBlueprint("tetro_t_up", "Tetro T Up", listOf(PolyOffset(1, 0), PolyOffset(0, 1), PolyOffset(1, 1), PolyOffset(2, 1)), COLOR_PURPLE),
        ShapeBlueprint("tetro_t_down", "Tetro T Down", listOf(PolyOffset(0, 0), PolyOffset(1, 0), PolyOffset(2, 0), PolyOffset(1, 1)), COLOR_PURPLE),
        ShapeBlueprint("tetro_t_left", "Tetro T Left", listOf(PolyOffset(1, 0), PolyOffset(0, 1), PolyOffset(1, 1), PolyOffset(1, 2)), COLOR_PURPLE),
        ShapeBlueprint("tetro_t_right", "Tetro T Right", listOf(PolyOffset(0, 0), PolyOffset(0, 1), PolyOffset(1, 1), PolyOffset(0, 2)), COLOR_PURPLE)
    )

    val FAMILY_TETRO_L = listOf(
        ShapeBlueprint("tetro_l_0", "Tetro L 0", listOf(PolyOffset(0, 0), PolyOffset(0, 1), PolyOffset(0, 2), PolyOffset(1, 2)), COLOR_ORANGE),
        ShapeBlueprint("tetro_l_90", "Tetro L 90", listOf(PolyOffset(0, 0), PolyOffset(1, 0), PolyOffset(2, 0), PolyOffset(0, 1)), COLOR_ORANGE),
        ShapeBlueprint("tetro_l_180", "Tetro L 180", listOf(PolyOffset(0, 0), PolyOffset(1, 0), PolyOffset(1, 1), PolyOffset(1, 2)), COLOR_ORANGE),
        ShapeBlueprint("tetro_l_270", "Tetro L 270", listOf(PolyOffset(2, 0), PolyOffset(0, 1), PolyOffset(1, 1), PolyOffset(2, 1)), COLOR_ORANGE)
    )

    val FAMILY_TETRO_J = listOf(
        ShapeBlueprint("tetro_j_0", "Tetro J 0", listOf(PolyOffset(1, 0), PolyOffset(1, 1), PolyOffset(0, 2), PolyOffset(1, 2)), COLOR_BLUE),
        ShapeBlueprint("tetro_j_90", "Tetro J 90", listOf(PolyOffset(0, 0), PolyOffset(0, 1), PolyOffset(1, 1), PolyOffset(2, 1)), COLOR_BLUE),
        ShapeBlueprint("tetro_j_180", "Tetro J 180", listOf(PolyOffset(0, 0), PolyOffset(1, 0), PolyOffset(0, 1), PolyOffset(0, 2)), COLOR_BLUE),
        ShapeBlueprint("tetro_j_270", "Tetro J 270", listOf(PolyOffset(0, 0), PolyOffset(1, 0), PolyOffset(2, 0), PolyOffset(2, 1)), COLOR_BLUE)
    )

    val FAMILY_TETRO_S = listOf(
        ShapeBlueprint("tetro_s_h", "Tetro S H", listOf(PolyOffset(1, 0), PolyOffset(2, 0), PolyOffset(0, 1), PolyOffset(1, 1)), COLOR_GREEN),
        ShapeBlueprint("tetro_s_v", "Tetro S V", listOf(PolyOffset(0, 0), PolyOffset(0, 1), PolyOffset(1, 1), PolyOffset(1, 2)), COLOR_GREEN)
    )

    val FAMILY_TETRO_Z = listOf(
        ShapeBlueprint("tetro_z_h", "Tetro Z H", listOf(PolyOffset(0, 0), PolyOffset(1, 0), PolyOffset(1, 1), PolyOffset(2, 1)), COLOR_RED),
        ShapeBlueprint("tetro_z_v", "Tetro Z V", listOf(PolyOffset(1, 0), PolyOffset(0, 1), PolyOffset(1, 1), PolyOffset(0, 2)), COLOR_RED)
    )

    val TETRO_FAMILIES = listOf(
        FAMILY_TETRO_I, FAMILY_TETRO_O, FAMILY_TETRO_T,
        FAMILY_TETRO_L, FAMILY_TETRO_J, FAMILY_TETRO_S, FAMILY_TETRO_Z
    )

    // --- 3. CLEAN PENTOMINO FAMILIES (5 Tiles - Flat-Docking Compliant) ---
    val FAMILY_PENTO_LINE = listOf(
        ShapeBlueprint("pento_5x1_h", "Line 5x1 H", listOf(PolyOffset(0, 0), PolyOffset(1, 0), PolyOffset(2, 0), PolyOffset(3, 0), PolyOffset(4, 0)), COLOR_RED),
        ShapeBlueprint("pento_1x5_v", "Line 1x5 V", listOf(PolyOffset(0, 0), PolyOffset(0, 1), PolyOffset(0, 2), PolyOffset(0, 3), PolyOffset(0, 4)), COLOR_RED)
    )

    // Legacy Aliases for toxic shapes mapped safely to clean 5-bar line
    val FAMILY_PENTO_PLUS = FAMILY_PENTO_LINE
    val FAMILY_PENTO_U = FAMILY_PENTO_LINE
    val FAMILY_PENTO_W = FAMILY_PENTO_LINE
    val FAMILY_PENTO_X = FAMILY_PENTO_LINE
    val FAMILY_PENTO_F = FAMILY_PENTO_LINE

    val PENTO_FAMILIES = listOf(FAMILY_PENTO_LINE)

    // --- 4. EXOTIC & HEAVY FAMILIES (Modular & Flat-Docking Compliant) ---
    val FAMILY_HEAVY_RECT = listOf(
        ShapeBlueprint("rect_3x2_h", "Rect 3x2", listOf(PolyOffset(0, 0), PolyOffset(1, 0), PolyOffset(2, 0), PolyOffset(0, 1), PolyOffset(1, 1), PolyOffset(2, 1)), COLOR_DEEP_ORANGE),
        ShapeBlueprint("rect_2x3_v", "Rect 2x3", listOf(PolyOffset(0, 0), PolyOffset(1, 0), PolyOffset(0, 1), PolyOffset(1, 1), PolyOffset(0, 2), PolyOffset(1, 2)), COLOR_DEEP_ORANGE)
    )

    val FAMILY_HEAVY_BOX = FAMILY_HEAVY_RECT
    val FAMILY_SAVIOR_FRAME = FAMILY_HEAVY_RECT

    val EXOTIC_FAMILIES = listOf(FAMILY_HEAVY_RECT)

    // --- 5. SPECIAL TILES ---
    val SPECIAL_WARP = ShapeBlueprint("special_warp", "Quantum Warp", listOf(PolyOffset(0, 0)), COLOR_PURPLE, SpecialBlockType.QUANTUM_WARP_VORTEX)
    val SPECIAL_CATALYST = ShapeBlueprint("special_catalyst", "Catalyst Crosshair", listOf(PolyOffset(0, 0)), COLOR_YELLOW, SpecialBlockType.CATALYST_CROSSHAIR)
    val SPECIAL_NOVA_CORE = ShapeBlueprint("special_nova_core", "Supercharged Nova Core", listOf(PolyOffset(0, 0)), COLOR_CYAN, SpecialBlockType.NOVA_CORE_EXPLOSION)
    val SPECIAL_CIRCUIT = listOf(
        ShapeBlueprint("special_circuit", "Circuit Conduit", listOf(PolyOffset(0, 0)), COLOR_CYAN, SpecialBlockType.CIRCUIT_CONDUIT)
    )

    val SPECIAL_FAMILIES = listOf(listOf(SPECIAL_WARP), listOf(SPECIAL_CATALYST), listOf(SPECIAL_NOVA_CORE), SPECIAL_CIRCUIT)

    // --- Legacy Aliases for Engines ---
    val MONOMINO_1X1 = FAMILY_DOT.first()
    val DOT = FAMILY_DOT
    val DOMINO = FAMILY_DOMINO
    val LINE_3 = FAMILY_TRIO_LINE
    val CORNER_L_3 = FAMILY_TRIO_CORNER
    val MONOS_AND_DOMINOS = FAMILY_DOT + FAMILY_DOMINO
    val TRIOMINOS_AND_SMALL_CORNERS = FAMILY_TRIO_LINE + FAMILY_TRIO_CORNER

    val TETRO_I = FAMILY_TETRO_I
    val TETRO_O = FAMILY_TETRO_O
    val TETRO_T = FAMILY_TETRO_T
    val TETRO_L = FAMILY_TETRO_L
    val TETRO_J = FAMILY_TETRO_J
    val TETRO_S = FAMILY_TETRO_S
    val TETRO_Z = FAMILY_TETRO_Z

    val PENTO_PLUS = FAMILY_PENTO_PLUS
    val PENTO_U = FAMILY_PENTO_U
    val PENTO_LINE_5 = FAMILY_PENTO_LINE
    val PENTO_W = FAMILY_PENTO_W
    val PENTO_X = FAMILY_PENTO_X
    val PENTO_F = FAMILY_PENTO_F
    val HEAVY_RECT = FAMILY_HEAVY_RECT
    val BIG_CORNER = listOf(ShapeBlueprint("corner_4x4", "Corner 4x4", listOf(PolyOffset(0,0), PolyOffset(1,0), PolyOffset(2,0), PolyOffset(3,0), PolyOffset(0,1), PolyOffset(0,2), PolyOffset(0,3)), COLOR_RED))

    val STANDARD_POOL: List<ShapeBlueprint> = (MICRO_FAMILIES.flatten() + TETRO_FAMILIES.flatten())

    fun instantiate(blueprint: ShapeBlueprint): PolyShape {
        return PolyShape(
            id = blueprint.id,
            offsets = blueprint.offsets,
            color = blueprint.defaultColor,
            isSpecial = blueprint.isSpecial,
            specialType = blueprint.specialType
        )
    }

    fun instantiateSpecial(specialType: SpecialBlockType): PolyShape {
        val blueprint = when (specialType) {
            SpecialBlockType.QUANTUM_WARP_VORTEX -> SPECIAL_WARP
            SpecialBlockType.CATALYST_CROSSHAIR -> SPECIAL_CATALYST
            SpecialBlockType.NOVA_CORE_EXPLOSION -> SPECIAL_NOVA_CORE
            SpecialBlockType.CIRCUIT_CONDUIT -> SPECIAL_CIRCUIT.first()
            else -> SPECIAL_WARP
        }
        return instantiate(blueprint)
    }

    // --- Helper Methods for Engines ---
    fun getShapesThatFit(pool: List<ShapeBlueprint>, boardGrid: Array<IntArray>): List<ShapeBlueprint> {
        return pool.filter { canShapeFit(it, boardGrid) }
    }

    private fun canShapeFit(shape: ShapeBlueprint, boardGrid: Array<IntArray>): Boolean {
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                if (canPlaceAt(shape.offsets, boardGrid, r, c)) return true
            }
        }
        return false
    }

    private fun canPlaceAt(offsets: List<PolyOffset>, boardGrid: Array<IntArray>, row: Int, col: Int): Boolean {
        for (offset in offsets) {
            val r = row + offset.y
            val c = col + offset.x
            if (r !in 0 until 8 || c !in 0 until 8) return false
            if (boardGrid[r][c] != 0) return false
        }
        return true
    }

    fun countVacantCells(boardGrid: Array<IntArray>): Int = boardGrid.sumOf { row -> row.count { it == 0 } }
}
