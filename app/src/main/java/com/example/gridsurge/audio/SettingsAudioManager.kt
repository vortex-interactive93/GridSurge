package com.example.gridsurge.audio

import kotlinx.coroutines.*

object SettingsAudioManager {
    private var auditionJob: Job? = null
    private var lastAuditionTime = 0L
    private const val DEBOUNCE_THRESHOLD_MS = 250L

    /**
     * Auditions the Tactical VOX sample using the active voice pack.
     * Automatically ducks BGM through BgmManager's native audio bus.
     */
    fun auditionVox(
        scope: CoroutineScope,
        volume: Float,
        bgmController: ((Float) -> Unit)? = null,
        currentBgmVolume: Float = 1.0f
    ) {
        if (volume <= 0.01f) {
            auditionJob?.cancel()
            SfxManager.isVoxMuted = true
            return
        }
        SfxManager.isVoxMuted = false

        val now = System.currentTimeMillis()
        if (now - lastAuditionTime < DEBOUNCE_THRESHOLD_MS) return
        lastAuditionTime = now

        auditionJob?.cancel()
        auditionJob = scope.launch(Dispatchers.Main) {
            SfxManager.playVoxPreview(volume)
        }
    }

    /**
     * Auditions SFX on release with clean volume calibration.
     */
    fun auditionSfx(volume: Float) {
        if (volume <= 0.01f) {
            SfxManager.isSfxMuted = true
            return
        }
        SfxManager.isSfxMuted = false
        SfxManager.sfxVolume = volume
        SfxManager.sfxVolumeScale = 1.0f
        SfxManager.playSfx(SfxType.UI_CONFIRM, volume = 1.0f)
    }
}
