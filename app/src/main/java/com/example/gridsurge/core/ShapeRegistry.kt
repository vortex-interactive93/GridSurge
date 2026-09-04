package com.example.gridsurge.core

import com.example.gridsurge.game.model.PolyShape

object ShapeRegistry {

    // 6-Color Calibrated High-Voltage Palette
    const val COLOR_CYAN = 0xFF00E5FF.toInt()     // 1. Electric Cyan (Minis & Dominos)
    const val COLOR_EMERALD = 0xFF00FF66.toInt()  // 2. Neon Emerald (Tromino Corners)
    const val COLOR_GOLD = 0xFFFFD600.toInt()     // 3. Cyber Gold (2x2 Square & L-Tetrominoes)
    const val COLOR_AMETHYST = 0xFFBD00FF.toInt() // 4. Royal Amethyst (T-Shapes & Straight 3s)
    const val COLOR_ORANGE = 0xFFFF6D00.toInt()   // 5. Hyper Orange (4-Bars & Mega Corners)
    const val COLOR_RED = 0xFFFF0055.toInt()      // 6. High-Voltage Red (5-Bars & 3x3 Heavy)

    // --- TIER 1: SAVIORS & MINIS (1 to 3 tiles) ---
    val MONO_1X1 = PolyShape("1x1", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0)), COLOR_CYAN)
    val DOMINO_H = PolyShape("2x1_h", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(1, 0)), COLOR_CYAN)
    val DOMINO_V = PolyShape("1x2_v", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(0, 1)), COLOR_CYAN)

    // Tromino 2x2 Corner L-Shapes (Strictly 0..1 coords)
    val CORNER_TL = PolyShape("c_tl", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(1, 0), com.example.gridsurge.game.model.PolyOffset(0, 1)), COLOR_EMERALD)
    val CORNER_TR = PolyShape("c_tr", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(1, 0), com.example.gridsurge.game.model.PolyOffset(1, 1)), COLOR_EMERALD)
    val CORNER_BL = PolyShape("c_bl", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(0, 1), com.example.gridsurge.game.model.PolyOffset(1, 1)), COLOR_EMERALD)
    val CORNER_BR = PolyShape("c_br", listOf(com.example.gridsurge.game.model.PolyOffset(1, 0), com.example.gridsurge.game.model.PolyOffset(0, 1), com.example.gridsurge.game.model.PolyOffset(1, 1)), COLOR_EMERALD)

    // --- TIER 2: WORKHORSE BUILDERS (3 to 4 tiles) ---
    val LINE_3_H = PolyShape("3x1_h", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(1, 0), com.example.gridsurge.game.model.PolyOffset(2, 0)), COLOR_AMETHYST)
    val LINE_3_V = PolyShape("1x3_v", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(0, 1), com.example.gridsurge.game.model.PolyOffset(0, 2)), COLOR_AMETHYST)
    val SQUARE_2X2 = PolyShape("sq_2x2", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(1, 0), com.example.gridsurge.game.model.PolyOffset(0, 1), com.example.gridsurge.game.model.PolyOffset(1, 1)), COLOR_GOLD)

    // Tetromino L-Shapes (4 Rotations)
    val L_0 = PolyShape("l_0", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(0, 1), com.example.gridsurge.game.model.PolyOffset(0, 2), com.example.gridsurge.game.model.PolyOffset(1, 2)), COLOR_GOLD)
    val L_90 = PolyShape("l_90", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(1, 0), com.example.gridsurge.game.model.PolyOffset(2, 0), com.example.gridsurge.game.model.PolyOffset(0, 1)), COLOR_GOLD)
    val L_180 = PolyShape("l_180", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(1, 0), com.example.gridsurge.game.model.PolyOffset(1, 1), com.example.gridsurge.game.model.PolyOffset(1, 2)), COLOR_GOLD)
    val L_270 = PolyShape("l_270", listOf(com.example.gridsurge.game.model.PolyOffset(2, 0), com.example.gridsurge.game.model.PolyOffset(0, 1), com.example.gridsurge.game.model.PolyOffset(1, 1), com.example.gridsurge.game.model.PolyOffset(2, 1)), COLOR_GOLD)

    // Tetromino T-Shapes (4 Rotations)
    val T_UP = PolyShape("t_up", listOf(com.example.gridsurge.game.model.PolyOffset(1, 0), com.example.gridsurge.game.model.PolyOffset(0, 1), com.example.gridsurge.game.model.PolyOffset(1, 1), com.example.gridsurge.game.model.PolyOffset(2, 1)), COLOR_AMETHYST)
    val T_DOWN = PolyShape("t_down", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(1, 0), com.example.gridsurge.game.model.PolyOffset(2, 0), com.example.gridsurge.game.model.PolyOffset(1, 1)), COLOR_AMETHYST)
    val T_LEFT = PolyShape("t_left", listOf(com.example.gridsurge.game.model.PolyOffset(1, 0), com.example.gridsurge.game.model.PolyOffset(0, 1), com.example.gridsurge.game.model.PolyOffset(1, 1), com.example.gridsurge.game.model.PolyOffset(1, 2)), COLOR_AMETHYST)
    val T_RIGHT = PolyShape("t_right", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(0, 1), com.example.gridsurge.game.model.PolyOffset(1, 1), com.example.gridsurge.game.model.PolyOffset(0, 2)), COLOR_AMETHYST)

    // --- TIER 3: HEAVY CLEAVERS & PENTOMINOES (4 to 9 tiles) ---
    val LINE_4_H = PolyShape("4x1_h", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(1, 0), com.example.gridsurge.game.model.PolyOffset(2, 0), com.example.gridsurge.game.model.PolyOffset(3, 0)), COLOR_ORANGE)
    val LINE_4_V = PolyShape("1x4_v", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(0, 1), com.example.gridsurge.game.model.PolyOffset(0, 2), com.example.gridsurge.game.model.PolyOffset(0, 3)), COLOR_ORANGE)

    // 3x3 Mega Corners (Strictly 0..2 coords)
    val BIG_CORNER_TL = PolyShape("bc_tl", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(1, 0), com.example.gridsurge.game.model.PolyOffset(2, 0), com.example.gridsurge.game.model.PolyOffset(0, 1), com.example.gridsurge.game.model.PolyOffset(0, 2)), COLOR_ORANGE)
    val BIG_CORNER_TR = PolyShape("bc_tr", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(1, 0), com.example.gridsurge.game.model.PolyOffset(2, 0), com.example.gridsurge.game.model.PolyOffset(2, 1), com.example.gridsurge.game.model.PolyOffset(2, 2)), COLOR_ORANGE)
    val BIG_CORNER_BL = PolyShape("bc_bl", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(0, 1), com.example.gridsurge.game.model.PolyOffset(0, 2), com.example.gridsurge.game.model.PolyOffset(1, 2), com.example.gridsurge.game.model.PolyOffset(2, 2)), COLOR_ORANGE)
    val BIG_CORNER_BR = PolyShape("bc_br", listOf(com.example.gridsurge.game.model.PolyOffset(2, 0), com.example.gridsurge.game.model.PolyOffset(2, 1), com.example.gridsurge.game.model.PolyOffset(0, 2), com.example.gridsurge.game.model.PolyOffset(1, 2), com.example.gridsurge.game.model.PolyOffset(2, 2)), COLOR_ORANGE)

    // 5-Bars & Solid Square
    val LINE_5_H = PolyShape("5x1_h", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(1, 0), com.example.gridsurge.game.model.PolyOffset(2, 0), com.example.gridsurge.game.model.PolyOffset(3, 0), com.example.gridsurge.game.model.PolyOffset(4, 0)), COLOR_RED)
    val LINE_5_V = PolyShape("1x5_v", listOf(com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(0, 1), com.example.gridsurge.game.model.PolyOffset(0, 2), com.example.gridsurge.game.model.PolyOffset(0, 3), com.example.gridsurge.game.model.PolyOffset(0, 4)), COLOR_RED)
    val SQUARE_3X3 = PolyShape(
        id = "sq_3x3",
        offsets = listOf(
            com.example.gridsurge.game.model.PolyOffset(0, 0), com.example.gridsurge.game.model.PolyOffset(1, 0), com.example.gridsurge.game.model.PolyOffset(2, 0),
            com.example.gridsurge.game.model.PolyOffset(0, 1), com.example.gridsurge.game.model.PolyOffset(1, 1), com.example.gridsurge.game.model.PolyOffset(2, 1),
            com.example.gridsurge.game.model.PolyOffset(0, 2), com.example.gridsurge.game.model.PolyOffset(1, 2), com.example.gridsurge.game.model.PolyOffset(2, 2)
        ),
        color = COLOR_RED
    )

    val TIER_1_SAVIORS = listOf(MONO_1X1, DOMINO_H, DOMINO_V, CORNER_TL, CORNER_TR, CORNER_BL, CORNER_BR)
    val TIER_2_WORKERS = listOf(LINE_3_H, LINE_3_V, SQUARE_2X2, L_0, L_90, L_180, L_270, T_UP, T_DOWN, T_LEFT, T_RIGHT)
    val TIER_3_HEAVIES = listOf(LINE_4_H, LINE_4_V, BIG_CORNER_TL, BIG_CORNER_TR, BIG_CORNER_BL, BIG_CORNER_BR, LINE_5_H, LINE_5_V, SQUARE_3X3)
    val ALL_STANDARD_SHAPES = TIER_1_SAVIORS + TIER_2_WORKERS + TIER_3_HEAVIES
}
