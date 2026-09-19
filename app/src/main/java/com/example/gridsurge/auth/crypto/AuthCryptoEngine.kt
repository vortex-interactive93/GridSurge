package com.example.gridsurge.auth.crypto

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

object AuthCryptoEngine {

    data class NoncePair(val rawNonce: String, val hashedNonce: String)

    /**
     * Generates a 32-byte secure random string and returns both the raw value
     * (for Supabase) and the SHA-256 hex digest (for Google Credential Manager).
     */
    fun generateNonce(): NoncePair {
        val rawNonce = UUID.randomUUID().toString() + "-" + SecureRandom().nextLong()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(rawNonce.toByteArray(Charsets.UTF_8))
        val hashedNonce = digest.joinToString("") { "%02x".format(it) }
        return NoncePair(rawNonce = rawNonce, hashedNonce = hashedNonce)
    }
}
