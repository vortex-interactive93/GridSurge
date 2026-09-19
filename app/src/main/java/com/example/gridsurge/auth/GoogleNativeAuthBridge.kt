package com.example.gridsurge.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.gridsurge.network.SupabaseClientProvider
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import java.security.MessageDigest
import java.util.UUID

object GoogleNativeAuthBridge {

    private const val TAG = "GoogleNativeAuthBridge"

    // Official Web Client ID from Google Cloud Console (GridSurge Web Client)
    const val SERVER_CLIENT_ID = "881244331647-lskfcgrkm2k11t4fop8ce1v9o2jg4lhg.apps.googleusercontent.com"

    /**
     * Triggers the native Android Google Accounts bottom sheet and hands the ID Token to Supabase.
     */
    suspend fun signInWithGoogleNative(
        context: Context,
        onSuccess: (email: String, displayName: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val credentialManager = CredentialManager.create(context)

        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(SERVER_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            if (credential is GoogleIdTokenCredential) {
                val googleIdToken = credential.idToken
                val email = credential.id
                val displayName = credential.displayName

                Log.d(TAG, "Native Google 1-Tap Auth token received for $email")

                if (SupabaseClientProvider.isConfigured) {
                    SupabaseClientProvider.client.auth.signInWith(IDToken) {
                        provider = Google
                        idToken = googleIdToken
                        nonce = rawNonce
                    }
                }

                onSuccess(email, displayName)
            } else {
                onError("UNRECOGNIZED_CREDENTIAL_TYPE")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User dismissed Google Sign-In bottom sheet.")
        } catch (e: Exception) {
            Log.e(TAG, "Native Google Auth Error: ${e.message}", e)
            onError("GOOGLE_AUTH_FAILED: ${e.localizedMessage ?: e.javaClass.simpleName}")
        }
    }
}
