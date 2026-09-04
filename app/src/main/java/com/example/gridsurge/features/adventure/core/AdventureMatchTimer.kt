package com.example.gridsurge.features.adventure.core

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class AdventureMatchTimer(
    private val scope: CoroutineScope,
    private val onTick: (formattedTime: String, elapsedSeconds: Long) -> Unit
) {
    private var tickerJob: Job? = null
    private var accumulatedMillis: Long = 0L
    private var lastStartTime: Long = 0L

    var isRunning: Boolean = false
        private set

    /** Starts the timer from zero. */
    fun start() {
        stop()
        accumulatedMillis = 0L
        resume()
    }

    /** Resumes timing from the current accumulated time. */
    fun resume() {
        if (isRunning) return
        isRunning = true
        lastStartTime = SystemClock.elapsedRealtime()

        tickerJob = scope.launch {
            while (isActive && isRunning) {
                val currentElapsedMillis = getElapsedMillis()
                val totalSeconds = currentElapsedMillis / 1000
                
                onTick(formatTime(totalSeconds), totalSeconds)
                delay(250L) // Poll at 4 Hz for responsive second rollover without CPU waste
            }
        }
    }

    /** Pauses the timer and stores accumulated duration. */
    fun pause() {
        if (!isRunning) return
        accumulatedMillis += SystemClock.elapsedRealtime() - lastStartTime
        isRunning = false
        tickerJob?.cancel()
        tickerJob = null
    }

    /** Stops timing and returns the final completed seconds. */
    fun stop(): Long {
        if (isRunning) {
            accumulatedMillis += SystemClock.elapsedRealtime() - lastStartTime
            isRunning = false
        }
        tickerJob?.cancel()
        tickerJob = null
        return accumulatedMillis / 1000
    }

    fun reset() {
        stop()
        accumulatedMillis = 0L
    }

    fun refundSeconds(seconds: Int) {
        accumulatedMillis = (accumulatedMillis - (seconds * 1000L)).coerceAtLeast(0L)
    }

    fun getElapsedSeconds(): Long = getElapsedMillis() / 1000

    private fun getElapsedMillis(): Long {
        return if (isRunning) {
            accumulatedMillis + (SystemClock.elapsedRealtime() - lastStartTime)
        } else {
            accumulatedMillis
        }
    }

    private fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, remainingSeconds)
    }
}
