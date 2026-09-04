package com.example.gridsurge.features.adventure.domain.model

@JvmInline
value class StageId(val packed: Long) {
    constructor(sectorId: Int, stageIndex: Int) : this(
        (sectorId.toLong() shl 32) or (stageIndex.toLong() and 0xFFFFFFFFL)
    )

    val sectorId: Int get() = (packed ushr 32).toInt()
    val stageIndex: Int get() = packed.toInt()

    val formatted: String get() = "SEC_${sectorId}_STG_${stageIndex}"

    override fun toString(): String = formatted

    companion object {
        val UNKNOWN = StageId(0, 0)

        fun parse(key: String): StageId? {
            val parts = key.split("_")
            if (parts.size != 4 || parts[0] != "SEC" || parts[2] != "STG") return null
            val sec = parts[1].toIntOrNull() ?: return null
            val stg = parts[3].toIntOrNull() ?: return null
            return StageId(sec, stg)
        }
    }
}
