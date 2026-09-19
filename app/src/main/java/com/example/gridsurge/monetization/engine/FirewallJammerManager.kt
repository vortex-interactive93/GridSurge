package com.example.gridsurge.monetization.engine

import android.content.Context
import android.content.SharedPreferences
import com.example.gridsurge.monetization.model.FirewallJammerState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.min

class FirewallJammerManager(
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("grid_surge_monetization_vault", Context.MODE_PRIVATE)

    private val KEY_JAMMER_EXPIRY = "jammer_expiry_epoch_ms"
    private val MAX_CAP_MS = 60 * 60 * 1000L      // 60 Minutes
    private val INCREMENT_MS = 5 * 60 * 1000L     // 5 Minutes

    private val _jammerState = MutableStateFlow(FirewallJammerState())
    val jammerState: StateFlow<FirewallJammerState> = _jammerState.asStateFlow()

    private var isPermanentVip: Boolean = false
    private var tickerJob: Job? = null

    init {
        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                refreshState()
                delay(1000L)
            }
        }
    }

    @Synchronized
    fun refreshState() {
        if (isPermanentVip) {
            _jammerState.value = FirewallJammerState(isPermanentNoAds = true)
            return
        }

        val now = System.currentTimeMillis()
        val expiryMs = prefs.getLong(KEY_JAMMER_EXPIRY, 0L)
        val remainingMs = (expiryMs - now).coerceAtLeast(0L)
        val remainingSec = remainingMs / 1000L

        _jammerState.value = _jammerState.value.copy(
            remainingSeconds = remainingSec,
            isPermanentNoAds = false
        )
    }

    @Synchronized
    fun stackJammerTime(): Boolean {
        if (isPermanentVip) return false

        val now = System.currentTimeMillis()
        val currentExpiry = prefs.getLong(KEY_JAMMER_EXPIRY, 0L)
        val baseTime = if (currentExpiry > now) currentExpiry else now

        val proposedExpiry = baseTime + INCREMENT_MS
        val cappedExpiry = min(proposedExpiry, now + MAX_CAP_MS)

        prefs.edit().putLong(KEY_JAMMER_EXPIRY, cappedExpiry).apply()
        refreshState()
        return true
    }

    fun isInterstitialBlocked(): Boolean {
        if (isPermanentVip) return true
        val now = System.currentTimeMillis()
        val expiry = prefs.getLong(KEY_JAMMER_EXPIRY, 0L)
        return expiry > now
    }

    fun setPermanentVip(enabled: Boolean) {
        isPermanentVip = enabled
        refreshState()
    }
}
