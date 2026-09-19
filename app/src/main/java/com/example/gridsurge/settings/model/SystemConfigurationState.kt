package com.example.gridsurge.settings.model

enum class FrameRateTarget(val fps: Int, val label: String) {
    FPS_60(60, "60 FPS // BALANCED"),
    FPS_120(120, "120 FPS // ULTRA FLUID")
}

data class SystemConfigurationState(
    val bgmVolume: Float = 0.80f,      // 0.0f .. 1.0f (Stored linear normalized)
    val sfxVolume: Float = 1.00f,
    val voxVolume: Float = 0.90f,
    val isHapticsEnabled: Boolean = true,
    val isScreenShakeEnabled: Boolean = true,
    val targetFrameRate: FrameRateTarget = FrameRateTarget.FPS_120,
    val agentCallsign: String = "AGENT_881",
    val buildVersion: String = "GS-PROD-V1.4.2-B142",
    val isDirty: Boolean = false
)

sealed interface SettingsEvent {
    data class UpdateBgm(val value: Float) : SettingsEvent
    data class UpdateSfx(val value: Float) : SettingsEvent
    data class UpdateVox(val value: Float) : SettingsEvent
    data class ToggleHaptics(val enabled: Boolean) : SettingsEvent
    data class ToggleScreenShake(val enabled: Boolean) : SettingsEvent
    data class SetTargetFrameRate(val target: FrameRateTarget) : SettingsEvent
    object ResetDefaults : SettingsEvent
    object SaveAndCommit : SettingsEvent
}
