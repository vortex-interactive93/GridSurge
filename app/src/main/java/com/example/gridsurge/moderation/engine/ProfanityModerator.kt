package com.example.gridsurge.moderation.engine

import com.example.gridsurge.moderation.model.FilterSeverity
import com.example.gridsurge.moderation.model.ModerationResult
import com.example.gridsurge.moderation.model.ProfanityPattern

class ProfanityModerator private constructor() {

    private val rootTrie = TrieNode()

    init {
        loadDefaultDictionary()
    }

    private class TrieNode {
        val children = HashMap<Char, TrieNode>(16)
        var matchedPattern: ProfanityPattern? = null
    }

    /**
     * Complete Normalization Pipeline:
     * 1. Unicode Homoglyph folding
     * 2. Leetspeak substitution
     * 3. Delimiter stripping
     * 4. Consecutive duplicate collapse (e.g., "FUUUUCK" -> "FUCK")
     */
    fun normalizeForInspection(raw: String): NormalizedForms {
        val lower = raw.trim().lowercase()

        // 1. Homoglyph & Leetspeak substitution
        val sbSubstituted = StringBuilder(lower.length)
        for (ch in lower) {
            val mapped = LEET_HOMOGLYPH_MAP[ch] ?: ch
            sbSubstituted.append(mapped)
        }
        val substituted = sbSubstituted.toString()

        // 2. Delimiter-stripped form (e.g., "F_U_C_K" -> "FUCK")
        val stripped = substituted.filter { it in 'a'..'z' || it in '0'..'9' }

        // 3. Repeated-character collapsed form (e.g., "FUUUUUUUCK" -> "FUCK")
        val collapsed = buildString(stripped.length) {
            var lastChar: Char? = null
            for (ch in stripped) {
                if (ch != lastChar) {
                    append(ch)
                    lastChar = ch
                }
            }
        }

        return NormalizedForms(
            originalLower = lower,
            substituted = substituted,
            stripped = stripped,
            collapsed = collapsed
        )
    }

    /**
     * Multi-pass validation:
     * - Pass A: Exact Tier 3 system impersonation check
     * - Pass B: Tier 1 Zero-Tolerance (Substring scan)
     * - Pass C: Tier 2 General Profanity (Bounded scan)
     */
    fun inspectCallsign(callsign: String): ModerationResult {
        val forms = normalizeForInspection(callsign)

        // Pass A: System Reserved Exact Match (Solves the "NotTheDev" problem!)
        RESERVED_SYSTEM_WORDS.forEach { reserved ->
            // Banned: "DEV", "DEV_THOMAS", "ADMIN", "ADMINISTRATOR"
            // Allowed: "NOTTHEDEV", "DEVON", "DEVELOPMENT"
            if (forms.stripped == reserved ||
                forms.originalLower.startsWith("${reserved}_") ||
                forms.originalLower.endsWith("_${reserved}")) {
                return ModerationResult.Blocked(
                    matchedToken = reserved,
                    severity = FilterSeverity.SYSTEM_IMPERSONATION,
                    localizedReason = "RESERVED SYSTEM IDENTIFIER"
                )
            }
        }

        // Pass B & C: Check stripped and collapsed forms against the Trie
        val formsToCheck = listOf(forms.stripped, forms.collapsed, forms.originalLower)
        for (candidate in formsToCheck) {
            val result = scanTextWithTrie(candidate)
            if (result is ModerationResult.Blocked) {
                return result
            }
        }

        return ModerationResult.Allowed
    }

    private fun scanTextWithTrie(text: String): ModerationResult {
        val len = text.length
        for (i in 0 until len) {
            var current: TrieNode? = rootTrie
            for (j in i until len) {
                val ch = text[j]
                current = current?.children?.get(ch) ?: break

                val pattern = current.matchedPattern
                if (pattern != null) {
                    if (pattern.matchAsSubstring) {
                        // Tier 1: Zero tolerance inside any word
                        return ModerationResult.Blocked(
                            matchedToken = pattern.rootWord,
                            severity = pattern.severity,
                            localizedReason = "PROHIBITED TERMINOLOGY // STRICT BAN"
                        )
                    } else {
                        // Tier 2: Check word boundary (ensures "CLASS" doesn't trigger on "ASS")
                        val isStartBoundary = (i == 0 || !text[i - 1].isLetter())
                        val isEndBoundary = (j == len - 1 || !text[j + 1].isLetter())
                        if (isStartBoundary && isEndBoundary) {
                            return ModerationResult.Blocked(
                                matchedToken = pattern.rootWord,
                                severity = pattern.severity,
                                localizedReason = "INAPPROPRIATE CALLSIGN DETECTED"
                            )
                        }
                    }
                }
            }
        }
        return ModerationResult.Allowed
    }

    private fun insert(pattern: ProfanityPattern) {
        var current = rootTrie
        for (ch in pattern.rootWord.lowercase()) {
            current = current.children.getOrPut(ch) { TrieNode() }
        }
        current.matchedPattern = pattern
    }

    private fun loadDefaultDictionary() {
        // Tier 1: Severe Infix (Substring = true)
        listOf("nigger", "nigga", "fag", "faggot", "retard", "nazi", "hitler", "rape").forEach {
            insert(ProfanityPattern(it, FilterSeverity.ZERO_TOLERANCE, matchAsSubstring = true))
        }

        // Tier 2: Vulgarities (Substring = false to avoid Scunthorpe issues)
        listOf("fuck", "shit", "bitch", "cunt", "dick", "pussy", "asshole", "tits", "penis", "whore", "slut", "cock", "vagina", "bastard").forEach {
            insert(ProfanityPattern(it, FilterSeverity.PROFANITY, matchAsSubstring = false))
        }
    }

    data class NormalizedForms(
        val originalLower: String,
        val substituted: String,
        val stripped: String,
        val collapsed: String
    )

    companion object {
        val instance by lazy { ProfanityModerator() }

        // Exact keywords reserved for game staff
        private val RESERVED_SYSTEM_WORDS = setOf(
            "admin", "administrator", "dev", "developer", "moderator",
            "mod", "staff", "system", "gridsurge", "official", "support", "helpdesk"
        )

        // Comprehensive Homoglyph & Leetspeak Lookup
        private val LEET_HOMOGLYPH_MAP = mapOf(
            // Leetspeak Numerics & Symbols
            '0' to 'o', '1' to 'i', '3' to 'e', '4' to 'a', '@' to 'a',
            '5' to 's', '$' to 's', '7' to 't', '8' to 'b', '9' to 'g',
            // Cyrillic Lookalikes (Homoglyphs)
            'а' to 'a', 'в' to 'b', 'е' to 'e', 'к' to 'k', 'м' to 'm',
            'н' to 'h', 'о' to 'o', 'р' to 'p', 'с' to 'c', 'т' to 't',
            'у' to 'y', 'х' to 'x'
        )
    }
}
