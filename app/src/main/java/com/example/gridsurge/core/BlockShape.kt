package com.example.gridsurge.core

import com.example.gridsurge.game.model.SpecialBlockType

data class BlockShape(
    val id: Int,
    val width: Int,
    val height: Int,
    val matrix: Array<IntArray>,
    val defaultColorHex: Long,
    val specialType: SpecialBlockType = SpecialBlockType.NONE
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlockShape) return false
        if (id != other.id) return false
        if (!matrix.contentDeepEquals(other.matrix)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + matrix.contentDeepHashCode()
        return result
    }
}
