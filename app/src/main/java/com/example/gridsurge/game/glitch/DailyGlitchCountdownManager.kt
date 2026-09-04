package com.example.gridsurge.game.glitch

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object DailyGlitchCountdownManager {

    fun getMillisUntilNextUtcMidnight(): Long {
        val now = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val midnight = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.HOUR_OF_DAY, 24)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return (midnight.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
    }

    fun formatDurationHms(millis: Long): String {
        val totalSeconds = millis / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun getSeedHeaderDate(): String {
        val now = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val year = now.get(Calendar.YEAR)
        val month = now.get(Calendar.MONTH) + 1
        val day = now.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.US, "PROTOCOL SEED // %04d.%02d.%02d", year, month, day)
    }

    fun getDeterministicDailySeed(): Long {
        return DailyGlitchSeeder.getDailySeed()
    }

    fun createTickerFlow(): Flow<Pair<Long, String>> = flow {
        while (true) {
            val remaining = getMillisUntilNextUtcMidnight()
            emit(remaining to formatDurationHms(remaining))
            delay(1000L)
        }
    }
}
