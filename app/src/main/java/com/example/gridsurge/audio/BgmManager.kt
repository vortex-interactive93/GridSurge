package com.example.gridsurge.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.annotation.RawRes
import com.example.gridsurge.R
import kotlinx.coroutines.*

enum class BgmTrack(@RawRes val rawResId: Int) {
    MAIN_HUB(R.raw.bgm_main_hub),
    CLASSIC_ENDLESS(R.raw.bgm_mode_classic),
    SECTOR_01_NEON(R.raw.bgm_sector_01_neon),
    SECTOR_02_SOLAR(R.raw.bgm_sector_02_solar),
    SECTOR_03_CRIMSON(R.raw.bgm_sector_03_crimson),
    SECTOR_04_TOXIC(R.raw.bgm_sector_04_toxic),
    SECTOR_05_SINGULARITY(R.raw.bgm_sector_05_singularity),
    TIME_BLITZ(R.raw.bgm_mode_time_blitz),
    DAILY_GLITCH(R.raw.bgm_mode_daily_glitch),
    BLITZ_CLASH(R.raw.bgm_mode_blitz_clash),
    ARMORY_VAULT(R.raw.bgm_armory_vault),
    CORE_OVERDRIVE(R.raw.bgm_core_overdrive)
}

object BgmManager {
    private const val TAG = "BgmManager"

    private var activePlayer: MediaPlayer? = null
    private var fadingPlayer: MediaPlayer? = null
    var currentTrack: BgmTrack? = null
        private set

    private val audioScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var fadeJob: Job? = null

    private var masterVolume = 0.5f
    private var isMuted = false

    private var duckJob: Job? = null

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    fun playTrack(context: Context, track: BgmTrack, crossFadeDurationMs: Long = 400) {
        if (currentTrack == track && activePlayer != null) {
            if (activePlayer?.isPlaying == false && !isMuted) {
                activePlayer?.start()
            }
            return
        }

        fadeJob?.cancel()
        fadeJob = audioScope.launch {
            var nextPlayer: MediaPlayer? = null
            try {
                val appContext = context.applicationContext

                nextPlayer = MediaPlayer.create(appContext, track.rawResId)
                if (nextPlayer == null) {
                    Log.e(TAG, "Failed to instantiate MediaPlayer for track: ${track.name}")
                    return@launch
                }

                nextPlayer.setAudioAttributes(audioAttributes)
                nextPlayer.isLooping = true

                val targetVol = if (isMuted) 0f else masterVolume

                // First track or immediate switch
                if (activePlayer == null || crossFadeDurationMs <= 0) {
                    nextPlayer.setVolume(targetVol, targetVol)
                    nextPlayer.start()

                    activePlayer?.stop()
                    activePlayer?.release()
                    activePlayer = nextPlayer
                    currentTrack = track
                    Log.i(TAG, "Playing ${track.name} at volume $targetVol")
                    return@launch
                }

                // Cross-fade
                nextPlayer.setVolume(0f, 0f)
                nextPlayer.start()

                fadingPlayer = activePlayer
                activePlayer = nextPlayer
                currentTrack = track

                val steps = 10
                val stepDelay = (crossFadeDurationMs / steps).coerceAtLeast(10L)

                for (i in 1..steps) {
                    val progress = i / steps.toFloat()
                    val currentTargetVol = if (isMuted) 0f else masterVolume

                    nextPlayer.setVolume(progress * currentTargetVol, progress * currentTargetVol)
                    fadingPlayer?.setVolume((1f - progress) * currentTargetVol, (1f - progress) * currentTargetVol)
                    delay(stepDelay)
                }

                fadingPlayer?.stop()
                fadingPlayer?.release()
                fadingPlayer = null
            } catch (e: CancellationException) {
                val finalVol = if (isMuted) 0f else masterVolume
                nextPlayer?.setVolume(finalVol, finalVol)
            } catch (e: Exception) {
                Log.e(TAG, "Error playing track ${track.name}", e)
                nextPlayer?.release()
            }
        }
    }

    fun pause() {
        try {
            if (activePlayer?.isPlaying == true) {
                activePlayer?.pause()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing BGM", e)
        }
    }

    fun resume() {
        try {
            if (!isMuted && activePlayer != null && activePlayer?.isPlaying == false) {
                activePlayer?.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming BGM", e)
        }
    }

    fun duckVolume(durationMs: Long = 1100L, duckRatio: Float = 0.3f, fadeBackMs: Long = 300L) {
        if (isMuted) return
        duckJob?.cancel()
        duckJob = audioScope.launch {
            val originalVolume = masterVolume
            val duckedVolume = originalVolume * duckRatio
            
            activePlayer?.setVolume(duckedVolume, duckedVolume)
            delay(durationMs)

            // Fade back
            val steps = 10
            val stepDelay = fadeBackMs / steps
            val delta = (originalVolume - duckedVolume) / steps

            for (i in 1..steps) {
                val current = duckedVolume + delta * i
                activePlayer?.setVolume(current, current)
                delay(stepDelay)
            }
            activePlayer?.setVolume(originalVolume, originalVolume)
        }
    }

    fun setMasterVolume(volume: Float) {
        masterVolume = volume.coerceIn(0f, 1f)
        if (!isMuted) {
            activePlayer?.setVolume(masterVolume, masterVolume)
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        val vol = if (isMuted) 0f else masterVolume
        activePlayer?.setVolume(vol, vol)
    }

    fun release() {
        fadeJob?.cancel()
        try {
            activePlayer?.stop()
            activePlayer?.release()
            fadingPlayer?.stop()
            fadingPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing BGM", e)
        } finally {
            activePlayer = null
            fadingPlayer = null
            currentTrack = null
        }
    }
}