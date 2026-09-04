package com.example.gridsurge.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClientProvider {

    // Replace these with your Supabase Project Settings -> API credentials when ready.
    // Leaving them as placeholders will NEVER crash the app.
    private const val SUPABASE_URL = "https://gbufjjheghppluzuwaxv.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdidWZqamhlZ2hwcGx1enV3YXh2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc3MTI1OTEsImV4cCI6MjEwMzI4ODU5MX0.yla87nca0fXrlvI0joC26xMAHmk5x92YEcyRXckTj24"

    val isConfigured: Boolean
        get() = SUPABASE_URL.startsWith("https://") && 
                !SUPABASE_URL.contains("your-project-ref") && 
                SUPABASE_ANON_KEY != "your-anon-public-key"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = if (isConfigured) SUPABASE_URL else "https://placeholder.supabase.co",
            supabaseKey = if (isConfigured) SUPABASE_ANON_KEY else "placeholder-key"
        ) {
            install(Postgrest)
            install(Auth)
        }
    }
}
