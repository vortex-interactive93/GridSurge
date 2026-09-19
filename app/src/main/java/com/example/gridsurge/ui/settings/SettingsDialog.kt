package com.example.gridsurge.ui.settings

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.audio.BgmManager
import com.example.gridsurge.audio.SettingsAudioManager
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.settings.SettingsManager
import com.example.gridsurge.settings.model.SystemConfigurationState
import com.example.gridsurge.ui.components.TacticalSegmentedSlider
import com.example.gridsurge.ui.components.TacticalSwitchCard
import com.example.gridsurge.ui.modifiers.cyberBorderGlow
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily

@Composable
fun SettingsDialog(
    settingsManager: SettingsManager,
    onDismiss: () -> Unit
) {
    val state by settingsManager.settingsState.collectAsState()
    val context = LocalContext.current

    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    var config by remember(state) {
        mutableStateOf(
            SystemConfigurationState(
                bgmVolume = state.bgmVolume,
                sfxVolume = state.sfxVolume,
                voxVolume = state.voxVolume,
                isHapticsEnabled = state.isHapticsEnabled,
                isScreenShakeEnabled = true,
                agentCallsign = "AGENT_001",
                buildVersion = "GS-PROD-V1.4.2-B142"
            )
        )
    }

    fun triggerDetent(enabled: Boolean) {
        if (!enabled) return
        SfxManager.playSfx(SfxType.CAROUSEL_SNAP, volume = 0.4f, pitchRate = 1.3f)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(10)
            }
        } catch (_: Exception) {}
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .cyberBorderGlow(
                    colors = listOf(Color(0xFF00E5FF), Color(0xFF005577)),
                    cornerRadius = 16.dp
                )
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xF0050811))
                .border(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Telemetry & Quick Dismiss Exit Cue
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Spacer(modifier = Modifier.width(28.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SYSTEM CONFIGURATION // FIRMWARE 2.4",
                            color = Color(0xFF00E5FF),
                            fontSize = 9.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "AUDIO & HARDWARE HAPTICS",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x22101826))
                            .border(1.dp, Color(0xFF1E2D44), RoundedCornerShape(6.dp))
                            .clickable {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onDismiss()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            color = Color(0xFF78909C),
                            fontSize = 11.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                val coroutineScope = rememberCoroutineScope()

                // Audio Sliders Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    TacticalSegmentedSlider(
                        label = "BACKGROUND MUSIC",
                        value = config.bgmVolume,
                        accentColor = Color(0xFF00E5FF),
                        onValueChange = {
                            config = config.copy(bgmVolume = it, isDirty = true)
                            settingsManager.setBgmVolume(it)
                            BgmManager.setMasterVolume(it)
                        },
                        onValueCommit = {
                            settingsManager.setBgmVolume(it)
                            BgmManager.setMasterVolume(it)
                        },
                        onDetent = { triggerDetent(config.isHapticsEnabled) }
                    )

                    TacticalSegmentedSlider(
                        label = "TACTICAL SFX",
                        value = config.sfxVolume,
                        accentColor = Color(0xFF00E5FF),
                        onValueChange = {
                            config = config.copy(sfxVolume = it, isDirty = true)
                            settingsManager.setSfxVolume(it)
                            SfxManager.sfxVolume = it
                            SfxManager.sfxVolumeScale = 1.0f
                        },
                        onValueCommit = {
                            settingsManager.setSfxVolume(it)
                            SfxManager.sfxVolume = it
                            SfxManager.sfxVolumeScale = 1.0f
                            SettingsAudioManager.auditionSfx(it)
                        },
                        onDetent = { triggerDetent(config.isHapticsEnabled) }
                    )

                    TacticalSegmentedSlider(
                        label = "NEURAL VOX COMMS",
                        value = config.voxVolume,
                        accentColor = Color(0xFFD500F9),
                        onValueChange = {
                            config = config.copy(voxVolume = it, isDirty = true)
                            settingsManager.setVoxVolume(it)
                            SfxManager.voxVolume = it
                            SfxManager.voxVolumeScale = 1.0f
                        },
                        onValueCommit = {
                            settingsManager.setVoxVolume(it)
                            SfxManager.voxVolume = it
                            SfxManager.voxVolumeScale = 1.0f
                            SettingsAudioManager.auditionVox(
                                scope = coroutineScope,
                                volume = it
                            )
                        },
                        onDetent = { triggerDetent(config.isHapticsEnabled) }
                    )
                }

                // Hardware Toggles
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TacticalSwitchCard(
                        title = "HAPTIC FEEDBACK MATRIX",
                        subtitle = "High-fidelity linear resonant motor telemetry",
                        enabled = config.isHapticsEnabled,
                        onToggle = { enabled ->
                            triggerDetent(true)
                            SfxManager.playSfx(SfxType.SETTING_TOGGLE)
                            config = config.copy(isHapticsEnabled = enabled, isDirty = true)
                            settingsManager.toggleHaptics()
                        }
                    )

                    TacticalSwitchCard(
                        title = "SPATIAL SCREEN SHAKE",
                        subtitle = "Explosive dynamic viewport impulse recoil",
                        enabled = config.isScreenShakeEnabled,
                        onToggle = { enabled ->
                            triggerDetent(config.isHapticsEnabled)
                            SfxManager.playSfx(SfxType.SETTING_TOGGLE)
                            config = config.copy(isScreenShakeEnabled = enabled, isDirty = true)
                        }
                    )
                }

                // Firmware Telemetry & Version Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "NODE: ${config.agentCallsign}",
                        color = Color(0xFF556980),
                        fontSize = 8.sp,
                        fontFamily = ChakraPetchFontFamily
                    )
                    Text(
                        text = config.buildVersion,
                        color = Color(0xFF556980),
                        fontSize = 8.sp,
                        fontFamily = ChakraPetchFontFamily
                    )
                }

                // Commitment Action Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFF0091EA))
                            )
                        )
                        .clickable {
                            SfxManager.playSfx(SfxType.MODE_LOCK_IN)
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶ COMMIT PROTOCOLS",
                        color = Color(0xFF040711),
                        fontSize = 12.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }
    }
}
