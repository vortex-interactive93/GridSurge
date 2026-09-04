package com.example.gridsurge.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.gridsurge.audio.BgmManager
import com.example.gridsurge.audio.SfxManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GameSettingsState(
    val isHapticsEnabled: Boolean = true,
    val bgmVolume: Float = 0.5f,
    val sfxVolume: Float = 0.8f,
    val voxVolume: Float = 1.0f
)

class SettingsManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("gridsurge_settings_prefs", Context.MODE_PRIVATE)

    private val _settingsState = MutableStateFlow(loadSettings())
    val settingsState: StateFlow<GameSettingsState> = _settingsState.asStateFlow()

    init {
        applySettingsToEngines(_settingsState.value)
    }

    private fun loadSettings(): GameSettingsState {
        return GameSettingsState(
            isHapticsEnabled = prefs.getBoolean(KEY_HAPTICS, true),
            bgmVolume = prefs.getFloat(KEY_BGM_VOLUME, 0.5f),
            sfxVolume = prefs.getFloat(KEY_SFX_VOLUME, 0.8f),
            voxVolume = prefs.getFloat(KEY_VOX_VOLUME, 1.0f)
        )
    }

    fun toggleHaptics() {
        updateState { copy(isHapticsEnabled = !isHapticsEnabled) }
    }

    fun setBgmVolume(volume: Float) {
        updateState { copy(bgmVolume = volume) }
    }

    fun setSfxVolume(volume: Float) {
        updateState { copy(sfxVolume = volume) }
    }

    fun setVoxVolume(volume: Float) {
        updateState { copy(voxVolume = volume) }
    }

    private inline fun updateState(transform: GameSettingsState.() -> GameSettingsState) {
        val newState = _settingsState.value.transform()
        _settingsState.value = newState
        saveSettings(newState)
        applySettingsToEngines(newState)
    }

    private fun saveSettings(state: GameSettingsState) {
        prefs.edit()
            .putBoolean(KEY_HAPTICS, state.isHapticsEnabled)
            .putFloat(KEY_BGM_VOLUME, state.bgmVolume)
            .putFloat(KEY_SFX_VOLUME, state.sfxVolume)
            .putFloat(KEY_VOX_VOLUME, state.voxVolume)
            .apply()
    }

    private fun applySettingsToEngines(state: GameSettingsState) {
        SfxManager.isSfxMuted = state.sfxVolume <= 0f
        SfxManager.isVoxMuted = state.voxVolume <= 0f
        SfxManager.isHapticsMuted = !state.isHapticsEnabled
        BgmManager.setMuted(state.bgmVolume <= 0f)

        SfxManager.voxVolumeScale = state.voxVolume
        SfxManager.sfxVolumeScale = state.sfxVolume
        BgmManager.setMasterVolume(state.bgmVolume)
    }

    companion object {
        private const val KEY_HAPTICS = "key_haptics_enabled"

        private const val KEY_BGM_VOLUME = "key_bgm_volume"
        private const val KEY_SFX_VOLUME = "key_sfx_volume"
        private const val KEY_VOX_VOLUME = "key_vox_volume"

        @Volatile
        private var instance: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return instance ?: synchronized(this) {
                instance ?: SettingsManager(context).also { instance = it }
            }
        }
    }
}
