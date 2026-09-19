package com.example.gridsurge.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.gridsurge.auth.crypto.AuthCryptoEngine
import com.example.gridsurge.auth.model.NativeAuthState
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.network.SupabaseClientProvider
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NativeGoogleAuthBridge(
    private val context: Context,
    private val profileManager: PlayerProfileManager,
    private val webServerClientId: String = "881244331647-lskfcgrkm2k11t4fop8ce1v9o2jg4lhg.apps.googleusercontent.com"
) {
    private val TAG = "NativeGoogleAuthBridge"
    private val credentialManager = CredentialManager.create(context)

    private val _authState = MutableStateFlow<NativeAuthState>(NativeAuthState.Idle)
    val authState: StateFlow<NativeAuthState> = _authState.asStateFlow()

    suspend fun launchNativeGoogleSignIn() {
        _authState.value = NativeAuthState.LaunchingSystemSheet

        val noncePair = AuthCryptoEngine.generateNonce()

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webServerClientId)
            .setAutoSelectEnabled(false)
            .setNonce(noncePair.hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            when {
                credential is CustomCredential && 
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idTokenString = googleIdTokenCredential.idToken
                        val userEmail = googleIdTokenCredential.id
                        val userDisplayName = googleIdTokenCredential.displayName

                        Log.d(TAG, "Successfully unpacked Google ID Token for $userEmail")
                        _authState.value = NativeAuthState.VerifyingWithSupabase

                        if (SupabaseClientProvider.isConfigured) {
                            SupabaseClientProvider.client.auth.signInWith(IDToken) {
                                provider = Google
                                idToken = idTokenString
                            }
                        }

                        profileManager.syncGoogleAccount(userEmail, userDisplayName)
                        _authState.value = NativeAuthState.Success(userEmail, userDisplayName)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Token parse error: ${e.message}", e)
                        _authState.value = NativeAuthState.Error("TOKEN_PARSE_ERROR: ${e.localizedMessage}")
                    }
                }
                else -> {
                    Log.w(TAG, "Unexpected credential type: ${credential::class.java.simpleName} (${credential.type})")
                    _authState.value = NativeAuthState.Error("UNEXPECTED_CREDENTIAL: ${credential::class.java.simpleName}")
                }
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User backed out of native Google Play Services bottom sheet.")
            _authState.value = NativeAuthState.Idle
        } catch (e: NoCredentialException) {
            Log.w(TAG, "No Play Services Google account found on device.")
            _authState.value = NativeAuthState.Error("NO_PLAY_SERVICES_ACCOUNT_FOUND")
        } catch (e: Exception) {
            Log.e(TAG, "Native Google Auth Error: ${e.message}", e)
            _authState.value = NativeAuthState.Error(e.localizedMessage ?: "AUTH_TRANSACTION_FAILED")
        }
    }
}
