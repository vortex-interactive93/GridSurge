package com.example.gridsurge.core

enum class CellType(val id: Int) {
    EMPTY(0),
    STANDARD_BLOCK(1),
    CORE_INTACT(-1),
    CORE_CRACKED(-2),
    INFECTED(9);

    companion object {
        fun isCore(value: Int): Boolean = value == CORE_INTACT.id || value == CORE_CRACKED.id
        fun isFilled(value: Int): Boolean = value != EMPTY.id
    }
}
