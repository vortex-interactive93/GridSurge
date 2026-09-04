package com.example.gridsurge.theme

object ThemeNormalizer {

    const val GLASS = "glass"
    const val CYBER = "cyber"
    const val SOLAR = "solar"
    const val VOID = "void"
    const val HYPERCUBE = "hypercube"
    const val QUANTUM = "quantum"

    /**
     * Normalizes any skin ID variation into a canonical internal key.
     */
    fun normalize(rawKey: String?): String {
        if (rawKey == null) return GLASS
        val key = rawKey.lowercase().trim()
        return when {
            key.contains("hypercube") || key.contains("prism") -> HYPERCUBE
            key.contains("quantum") || key.contains("matrix") -> QUANTUM
            key.contains("solar") -> SOLAR
            key.contains("void") -> VOID
            key.contains("cyber") || key.contains("neon") -> CYBER
            key.contains("glass") || key.contains("midnight") -> GLASS
            else -> GLASS
        }
    }
}
