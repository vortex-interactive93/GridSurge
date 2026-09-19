package com.example.gridsurge.auth.model

sealed interface NativeAuthState {
    object Idle : NativeAuthState
    object LaunchingSystemSheet : NativeAuthState
    object VerifyingWithSupabase : NativeAuthState
    data class Success(val email: String, val displayName: String?) : NativeAuthState
    data class Error(val message: String) : NativeAuthState
}
