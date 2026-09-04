package com.example.gridsurge.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.annotation.RawRes
import com.example.gridsurge.R
import kotlinx.coroutines.*

enum class AudioPriority(val level: Int) {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4)
}

enum class VoicePackId(val prefix: String, val displayName: String) {
    DEFAULT("", "Standard Comm-Link"),
    CYBER_AI("cyber", "Cyber Nexus AI"),
    SOLAR_PILOT("solar", "Solar Strike Comms"),
    VOID_ORACLE("void", "Voidborn Entity")
}

enum class SfxType(@RawRes val rawResId: Int) {
    // Touch & Spawner
    TILE_PICKUP(R.raw.sfx_tile_pickup),
    SPECIAL_PICKUP(R.raw.sfx_special_pickup),
    SNAP_TICK(R.raw.sfx_snap_tick),
    BLOCK_PLACE_2(R.raw.sfx_block_place_2),
    BLOCK_PLACE_3(R.raw.sfx_block_place_3),
    SPECIAL_BLOCK_PLACE(R.raw.sfx_special_block_place),
    TILE_DROP(R.raw.sfx_tile_drop),
    INVALID_MOVE(R.raw.sfx_invalid_move),
    TRAY_REFILL(R.raw.sfx_tray_refill),

    // Theme-Aware Line Clears
    CLEAR_DIGITAL(R.raw.sfx_clear_digital),
    CLEAR_FIRE(R.raw.sfx_clear_fire),
    CLEAR_GLASS(R.raw.sfx_clear_glass),
    CLEAR_VOID(R.raw.sfx_clear_void),

    // Combos & Power Surges
    LASER_SWEEP(R.raw.sfx_laser_sweep),
    LIGHTNING_STRIKE(R.raw.sfx_lightning_strike),
    OVERDRIVE_ACTIVATE(R.raw.sfx_overdrive_activate),
    MEGA_BLITZ(R.raw.sfx_mega_blitz),
    EMP_SHOCKWAVE(R.raw.sfx_emp_shockwave),
    EMP_DENIAL_THUMP(R.raw.sfx_invalid_move), // Reusing invalid_move as fallback for denial thump

    // Objectives, Hazards & Lifecycles
    CORE_CRACK(R.raw.sfx_core_crack),
    CORE_SHATTER(R.raw.sfx_core_shatter),
    SYSTEM_OFFLINE(R.raw.sfx_system_offline),
    LEVEL_COMPLETE(R.raw.sfx_level_complete),
    OBJECTIVE_COMPLETE(R.raw.sfx_objective_complete),
    BONUS_UNLOCKED(R.raw.sfx_objective_complete),
    STAR_REWARD(R.raw.sfx_star_reward),

    // UI & Navigation (Supports both BUTTON_CLICK and UI_CONFIRM)
    BUTTON_CLICK(R.raw.sfx_ui_confirm),
    UI_CONFIRM(R.raw.sfx_ui_confirm),
    MODAL_WHOOSH(R.raw.sfx_modal_whoosh)
}

enum class VoxAction(val actionKey: String, val priority: AudioPriority, val cooldownMs: Long) {
    DEPLOY("deploy", AudioPriority.HIGH, 1000L),
    DOUBLE_BLITZ("double_blitz", AudioPriority.MEDIUM, 2000L),
    TRIPLE_BLITZ("triple_blitz", AudioPriority.MEDIUM, 2500L),
    MEGA_BLITZ("mega_blitz", AudioPriority.HIGH, 3000L),
    OVERDRIVE("overdrive", AudioPriority.HIGH, 3500L),
    UNSTOPPABLE("unstoppable", AudioPriority.HIGH, 4000L),
    CATALYST_DETONATED("catalyst", AudioPriority.HIGH, 2500L),
    GUARDIAN_NEUTRALIZED("catalyst", AudioPriority.HIGH, 2500L),
    CORE_CRACKED("core_breached", AudioPriority.MEDIUM, 2000L),
    ENERGY_HARMONIZED("perfect_clear", AudioPriority.MEDIUM, 3000L),
    EMP_DEPLOYED("emp_deployed", AudioPriority.HIGH, 3000L),
    GRID_CRITICAL("grid_critical", AudioPriority.CRITICAL, 8000L),
    PERFECT_CLEAR("perfect_clear", AudioPriority.CRITICAL, 1500L),
    OBJECTIVE_DONE("objective_done", AudioPriority.HIGH, 2000L),
    SECTOR_CLEARED("sector_cleared", AudioPriority.CRITICAL, 2000L),
    NEW_RECORD("new_record", AudioPriority.HIGH, 2000L),
    RIVAL_PASS("rival_pass", AudioPriority.HIGH, 4000L),
    TOP_100("top_100", AudioPriority.HIGH, 3000L),
    LEAD_SECURED("objective_done", AudioPriority.HIGH, 2000L),
    LEAD_LOST("rival_pass", AudioPriority.HIGH, 4000L),
    VICTORY("sector_cleared", AudioPriority.CRITICAL, 2000L),
    GAME_OVER("game_over", AudioPriority.CRITICAL, 0L)
}

// Backward-compatible typealias for legacy screens referencing VoxCue
typealias VoxCue = VoxAction

enum class HapticType {
    LIGHT_TICK,
    CLICK,
    HEAVY_IMPACT,
    DOUBLE_CRACK,
    SURGE_EXPLOSION
}

object SfxManager {
    private const val TAG = "SfxManager"
    private const val MAX_STREAMS = 16

    private var soundPool: SoundPool? = null
    
    // Flat primitive lookup tables (Zero GC allocation in game loop)
    private val sfxSoundIds = IntArray(SfxType.entries.size)
    private val voxSoundMatrix = Array(VoicePackId.entries.size) { IntArray(VoxAction.entries.size) }

    // Single-Channel VOX State Machine
    private var activeVoxStreamId: Int = 0
    private var activeVoxPriority: AudioPriority = AudioPriority.LOW
    private var lastVoxTimestamp: Long = 0L
    private var lastCriticalWarningTimestamp: Long = 0L
    private const val MIN_VOX_GAP_MS = 450L

    // Runtime Configuration
    var activeVoicePack: VoicePackId = VoicePackId.DEFAULT
    var activeVoxPackKey: String
        get() = activeVoicePack.prefix
        set(value) {
            activeVoicePack = VoicePackId.entries.firstOrNull { it.prefix == value } ?: VoicePackId.DEFAULT
        }
    var isSfxMuted: Boolean = false
    var isVoxMuted: Boolean = false
        set(value) {
            field = value
            if (value && activeVoxStreamId != 0) {
                soundPool?.stop(activeVoxStreamId)
                activeVoxStreamId = 0
            }
        }
    var isHapticsMuted: Boolean = false
    var sfxVolume: Float = 0.72f
    var voxVolume: Float = 1.0f
    var bgmVolume: Float = 0.5f

    var isPvpModeActive: Boolean = false

    var voxVolumeScale: Float = 1.0f
    var sfxVolumeScale: Float = 1.0f
    var bgmVolumeScale: Float = 1.0f

    private var placeAlternateFlag = false
    // Removed lastSnapTimestamp as dragging is now silent

    private var vibrator: Vibrator? = null

    private val audioScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var sfxDuckJob: Job? = null
    @Volatile private var activeVoxDuckingFactor: Float = 1.0f

    fun initialize(context: Context) {
        if (soundPool != null) return
        val appContext = context.applicationContext

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(audioAttributes)
            .build()

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        preloadAudioAssets(appContext)
    }

    private fun preloadAudioAssets(context: Context) {
        val pool = soundPool ?: return

        // 1. Preload SFX Assets
        SfxType.entries.forEachIndexed { index, sfx ->
            try {
                val resId = getSfxResourceId(sfx)
                if (resId != 0) {
                    val soundId = pool.load(context, resId, 1)
                    sfxSoundIds[index] = soundId
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed preloading SFX: ${sfx.name}", e)
            }
        }

        // 2. Preload 64 VOX Assets (4 Packs x 16 Lines)
        VoicePackId.entries.forEachIndexed { packIndex, pack ->
            VoxAction.entries.forEachIndexed { actionIndex, action ->
                val resId = getVoxResourceId(pack, action)
                if (resId != 0) {
                    voxSoundMatrix[packIndex][actionIndex] = pool.load(context, resId, 1)
                }
            }
        }
    }

    private fun getSfxResourceId(type: SfxType): Int = type.rawResId

    private fun getVoxResourceId(pack: VoicePackId, action: VoxAction): Int {
        return when (pack) {
            VoicePackId.DEFAULT -> when (action) {
                VoxAction.DEPLOY -> R.raw.vox_deploy
                VoxAction.DOUBLE_BLITZ -> R.raw.vox_double_blitz
                VoxAction.TRIPLE_BLITZ -> R.raw.vox_triple_blitz
                VoxAction.MEGA_BLITZ -> R.raw.vox_mega_blitz
                VoxAction.OVERDRIVE -> R.raw.vox_overdrive
                VoxAction.UNSTOPPABLE -> R.raw.vox_unstoppable
                VoxAction.CATALYST_DETONATED, VoxAction.GUARDIAN_NEUTRALIZED -> R.raw.vox_catalyst_blast
                VoxAction.CORE_CRACKED -> R.raw.vox_core_breached
                VoxAction.ENERGY_HARMONIZED, VoxAction.PERFECT_CLEAR -> R.raw.vox_perfect_clear
                VoxAction.EMP_DEPLOYED -> R.raw.vox_emp_deployed
                VoxAction.GRID_CRITICAL -> R.raw.vox_grid_critical
                VoxAction.OBJECTIVE_DONE, VoxAction.LEAD_SECURED -> R.raw.vox_objective_done
                VoxAction.SECTOR_CLEARED, VoxAction.VICTORY -> R.raw.vox_sector_cleared
                VoxAction.NEW_RECORD -> R.raw.vox_new_record
                VoxAction.RIVAL_PASS, VoxAction.LEAD_LOST -> R.raw.vox_rival_pass
                VoxAction.TOP_100 -> R.raw.vox_top_100
                VoxAction.GAME_OVER -> R.raw.vox_game_over
            }
            VoicePackId.CYBER_AI -> when (action) {
                VoxAction.DEPLOY -> R.raw.vox_cyber_deploy
                VoxAction.DOUBLE_BLITZ -> R.raw.vox_cyber_double_blitz
                VoxAction.TRIPLE_BLITZ -> R.raw.vox_cyber_triple_blitz
                VoxAction.MEGA_BLITZ -> R.raw.vox_cyber_mega_blitz
                VoxAction.OVERDRIVE -> R.raw.vox_cyber_overdrive
                VoxAction.UNSTOPPABLE -> R.raw.vox_cyber_unstoppable
                VoxAction.CATALYST_DETONATED, VoxAction.GUARDIAN_NEUTRALIZED -> R.raw.vox_cyber_catalyst
                VoxAction.ENERGY_HARMONIZED, VoxAction.PERFECT_CLEAR -> R.raw.vox_cyber_perfect_clear
                VoxAction.EMP_DEPLOYED -> R.raw.vox_cyber_emp_deployed
                VoxAction.GRID_CRITICAL -> R.raw.vox_cyber_grid_critical
                VoxAction.OBJECTIVE_DONE, VoxAction.LEAD_SECURED -> R.raw.vox_cyber_objective_done
                VoxAction.SECTOR_CLEARED, VoxAction.VICTORY -> R.raw.vox_cyber_sector_cleared
                VoxAction.NEW_RECORD -> R.raw.vox_cyber_new_record
                VoxAction.RIVAL_PASS, VoxAction.LEAD_LOST -> R.raw.vox_cyber_rival_pass
                VoxAction.TOP_100 -> R.raw.vox_cyber_top_100
                VoxAction.GAME_OVER -> R.raw.vox_cyber_game_over
                else -> getVoxResourceId(VoicePackId.DEFAULT, action)
            }
            VoicePackId.SOLAR_PILOT -> when (action) {
                VoxAction.DEPLOY -> R.raw.vox_solar_deploy
                VoxAction.DOUBLE_BLITZ -> R.raw.vox_solar_double_blitz
                VoxAction.TRIPLE_BLITZ -> R.raw.vox_solar_triple_blitz
                VoxAction.MEGA_BLITZ -> R.raw.vox_solar_mega_blitz
                VoxAction.OVERDRIVE -> R.raw.vox_solar_overdrive
                VoxAction.UNSTOPPABLE -> R.raw.vox_solar_unstoppable
                VoxAction.CATALYST_DETONATED, VoxAction.GUARDIAN_NEUTRALIZED -> R.raw.vox_solar_catalyst
                VoxAction.ENERGY_HARMONIZED, VoxAction.PERFECT_CLEAR -> R.raw.vox_solar_perfect_clear
                VoxAction.EMP_DEPLOYED -> R.raw.vox_solar_emp_deployed
                VoxAction.GRID_CRITICAL -> R.raw.vox_solar_grid_critical
                VoxAction.OBJECTIVE_DONE, VoxAction.LEAD_SECURED -> R.raw.vox_solar_objective_done
                VoxAction.SECTOR_CLEARED, VoxAction.VICTORY -> R.raw.vox_solar_sector_cleared
                VoxAction.NEW_RECORD -> R.raw.vox_solar_new_record
                VoxAction.RIVAL_PASS, VoxAction.LEAD_LOST -> R.raw.vox_solar_rival_pass
                VoxAction.TOP_100 -> R.raw.vox_solar_top_100
                VoxAction.GAME_OVER -> R.raw.vox_solar_game_over
                else -> getVoxResourceId(VoicePackId.DEFAULT, action)
            }
            VoicePackId.VOID_ORACLE -> when (action) {
                VoxAction.DEPLOY -> R.raw.vox_void_deploy
                VoxAction.DOUBLE_BLITZ -> R.raw.vox_void_double_blitz
                VoxAction.TRIPLE_BLITZ -> R.raw.vox_void_triple_blitz
                VoxAction.MEGA_BLITZ -> R.raw.vox_void_mega_blitz
                VoxAction.OVERDRIVE -> R.raw.vox_void_overdrive
                VoxAction.UNSTOPPABLE -> R.raw.vox_void_unstoppable
                VoxAction.CATALYST_DETONATED, VoxAction.GUARDIAN_NEUTRALIZED -> R.raw.vox_void_catalyst
                VoxAction.ENERGY_HARMONIZED, VoxAction.PERFECT_CLEAR -> R.raw.vox_void_perfect_clear
                VoxAction.EMP_DEPLOYED -> R.raw.vox_void_emp_deployed
                VoxAction.GRID_CRITICAL -> R.raw.vox_void_grid_critical
                VoxAction.OBJECTIVE_DONE, VoxAction.LEAD_SECURED -> R.raw.vox_void_objective_done
                VoxAction.SECTOR_CLEARED, VoxAction.VICTORY -> R.raw.vox_void_sector_cleared
                VoxAction.NEW_RECORD -> R.raw.vox_void_new_record
                VoxAction.RIVAL_PASS, VoxAction.LEAD_LOST -> R.raw.vox_void_rival_pass
                VoxAction.TOP_100 -> R.raw.vox_void_top_100
                VoxAction.GAME_OVER -> R.raw.vox_void_game_over
                else -> getVoxResourceId(VoicePackId.DEFAULT, action)
            }
        }
    }

    // --- Dynamic Single-Channel VOX Engine ---

    fun playVox(action: VoxAction) {
        if (isVoxMuted || soundPool == null) return

        val now = SystemClock.elapsedRealtime()
        
        // Global minimum gap between ANY vox lines to prevent overlap/clipping
        if (now - lastVoxTimestamp < MIN_VOX_GAP_MS) return

        if (activeVoxStreamId != 0) {
            if (action.priority.level < activeVoxPriority.level && (now - lastVoxTimestamp) < action.cooldownMs) {
                return
            }
            soundPool?.stop(activeVoxStreamId)
        } else if ((now - lastVoxTimestamp) < action.cooldownMs) {
            return
        }

        val packIndex = activeVoicePack.ordinal
        val actionIndex = action.ordinal
        
        // Boundary Guard
        if (packIndex >= voxSoundMatrix.size || actionIndex >= voxSoundMatrix[packIndex].size) {
            Log.w(TAG, "VoxAction $action (index $actionIndex) out of bounds for voice matrix. Skipping audio playback.")
            return
        }

        var soundId = voxSoundMatrix[packIndex][actionIndex]

        if (soundId == 0) {
            val defaultOrdinal = VoicePackId.DEFAULT.ordinal
            if (defaultOrdinal < voxSoundMatrix.size && actionIndex < voxSoundMatrix[defaultOrdinal].size) {
                soundId = voxSoundMatrix[defaultOrdinal][actionIndex]
            }
        }
        if (soundId == 0) return

        // 1. Duck BGM
        val speechDurationMs = when (action) {
            VoxAction.OVERDRIVE, VoxAction.UNSTOPPABLE, VoxAction.CATALYST_DETONATED -> 2000L
            VoxAction.VICTORY, VoxAction.SECTOR_CLEARED -> 2400L
            else -> 1400L
        }
        BgmManager.duckVolume(durationMs = speechDurationMs, duckRatio = 0.25f, fadeBackMs = 300L)

        // 2. Duck Concurrent & Subsequent SFX
        sfxDuckJob?.cancel()
        sfxDuckJob = audioScope.launch {
            activeVoxDuckingFactor = 0.45f // Attenuate SFX by ~7dB
            delay(speechDurationMs)
            
            // Smooth ramp back to 1.0f
            val steps = 6
            val stepDelay = 200L / steps
            for (i in 1..steps) {
                activeVoxDuckingFactor = 0.45f + (0.55f * (i / steps.toFloat()))
                delay(stepDelay)
            }
            activeVoxDuckingFactor = 1.0f
        }

        // 3. Play Priority VOX
        val streamId = soundPool?.play(
            soundId,
            voxVolume * voxVolumeScale,
            voxVolume * voxVolumeScale,
            action.priority.level + 10, // Increased priority
            0,
            1.0f
        ) ?: 0

        if (streamId != 0) {
            activeVoxStreamId = streamId
            activeVoxPriority = action.priority
            lastVoxTimestamp = now
        }
    }

    // Legacy method overload for callers using triggerVoxCue()
    fun triggerVoxCue(cue: VoxAction) {
        playVox(cue)
    }

    fun evaluateComboProgression(comboStreak: Int, isBoardWiped: Boolean, isClashMode: Boolean = false) {
        if (isBoardWiped) {
            playVox(VoxAction.PERFECT_CLEAR)
            return
        }
        if (isClashMode) return // Suppress combo announcer in PVP to focus on lead changes
        
        when (comboStreak) {
            2 -> playVox(VoxAction.DOUBLE_BLITZ)
            3 -> playVox(VoxAction.TRIPLE_BLITZ)
            4, 5 -> playVox(VoxAction.MEGA_BLITZ)
            6 -> playVox(VoxAction.OVERDRIVE)
            7, 8, 10 -> playVox(VoxAction.UNSTOPPABLE)
        }
    }

    fun evaluateComboVox(comboStreak: Int, isBoardWiped: Boolean) {
        evaluateComboProgression(comboStreak, isBoardWiped)
    }

    fun checkGridCriticalWarning(occupiedRatio: Float, movesRemaining: Int) {
        val now = SystemClock.elapsedRealtime()
        if (occupiedRatio >= 0.85f || (movesRemaining in 1..2)) {
            if (now - lastCriticalWarningTimestamp > 12000L) {
                lastCriticalWarningTimestamp = now
                playVox(VoxAction.GRID_CRITICAL)
            }
        }
    }

    // --- SFX Playback Engine ---

    fun playSfx(
        type: SfxType,
        overridePitch: Float = 1.0f,
        pitchJitter: Float = 0.0f,
        priority: Int = 1,
        volumeMultiplier: Float = 1.0f
    ) {
        if (isSfxMuted || soundPool == null) return
        val index = type.ordinal
        if (index >= sfxSoundIds.size) {
            Log.w(TAG, "SfxType $type (index $index) out of bounds. Skipping playback.")
            return
        }
        val soundId = sfxSoundIds[index]
        if (soundId == 0) return

        val finalPitch = if (pitchJitter > 0f) {
            overridePitch + (Math.random().toFloat() * pitchJitter * 2 - pitchJitter)
        } else {
            overridePitch
        }.coerceIn(0.5f, 2.0f)

        // Integrated dynamic VOX ducking factor:
        val finalVolume = (sfxVolume * volumeMultiplier * sfxVolumeScale * activeVoxDuckingFactor).coerceIn(0f, 1f)
        soundPool?.play(soundId, finalVolume, finalVolume, priority, 0, finalPitch)
    }

    fun playPlacementSound(isSpecial: Boolean = false) {
        if (isSpecial) {
            playSfx(SfxType.SPECIAL_BLOCK_PLACE, overridePitch = 1.0f, volumeMultiplier = 1.0f)
            triggerHaptic(HapticType.CLICK)
            return
        }

        val placeType = if (placeAlternateFlag) SfxType.BLOCK_PLACE_2 else SfxType.BLOCK_PLACE_3
        placeAlternateFlag = !placeAlternateFlag

        playSfx(placeType, pitchJitter = 0.04f, volumeMultiplier = 1.0f)
        playSfx(SfxType.TILE_DROP, volumeMultiplier = 0.9f)
        triggerHaptic(HapticType.LIGHT_TICK)
    }

    fun playSnapTick() {
        // Left intentionally empty to keep dragging silent
    }

    fun playLineClear(themeKey: String?, totalLines: Int, comboStreak: Int) {
        val clearSfx = when (themeKey?.lowercase()) {
            "solar", "solar_flare", "fire" -> SfxType.CLEAR_FIRE
            "midnight", "glass", "midnight_glass" -> SfxType.CLEAR_GLASS
            "void", "voidborn", "singularity", "matter" -> SfxType.CLEAR_VOID
            else -> SfxType.CLEAR_DIGITAL
        }

        // Musical Audio Ladder (C-E-G-B-C scale mapping)
        val pitch = when (comboStreak) {
            1 -> 1.00f // C4
            2 -> 1.26f // E4
            3 -> 1.50f // G4
            4 -> 1.88f // B4
            5 -> 2.00f // C5
            else -> 1.0f + (comboStreak * 0.1f).coerceAtMost(1.0f)
        }
        playSfx(clearSfx, overridePitch = pitch, priority = 2)

        if (totalLines >= 3) {
            playSfx(SfxType.LIGHTNING_STRIKE, priority = 3)
            triggerHaptic(HapticType.SURGE_EXPLOSION)
        } else {
            triggerHaptic(HapticType.HEAVY_IMPACT)
        }

        if (comboStreak == 5) {
            playSfx(SfxType.OVERDRIVE_ACTIVATE, priority = 3)
        } else if (comboStreak >= 8) {
            playSfx(SfxType.MEGA_BLITZ, priority = 4)
        } else if (comboStreak > 1) {
            playSfx(SfxType.LASER_SWEEP, priority = 1)
        }
    }

    // --- Sensory Haptics ---

    fun triggerHaptic(type: HapticType) {
        if (isHapticsMuted) return
        val vib = vibrator?.takeIf { it.hasVibrator() } ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = when (type) {
                HapticType.LIGHT_TICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                HapticType.CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                HapticType.HEAVY_IMPACT -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                HapticType.DOUBLE_CRACK -> VibrationEffect.createWaveform(longArrayOf(0, 20, 35, 30), intArrayOf(0, 180, 0, 255), -1)
                HapticType.SURGE_EXPLOSION -> VibrationEffect.createWaveform(longArrayOf(0, 30, 25, 60), intArrayOf(0, 200, 0, 255), -1)
            }
            vib.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            when (type) {
                HapticType.LIGHT_TICK -> vib.vibrate(10)
                HapticType.CLICK -> vib.vibrate(25)
                HapticType.HEAVY_IMPACT -> vib.vibrate(50)
                HapticType.DOUBLE_CRACK -> vib.vibrate(longArrayOf(0, 20, 35, 30), -1)
                HapticType.SURGE_EXPLOSION -> vib.vibrate(longArrayOf(0, 30, 25, 60), -1)
            }
        }
    }

    // --- Preview Methods ---

    fun playVoxPreview(volume: Float) {
        voxVolumeScale = volume
        isVoxMuted = volume <= 0f
        if (volume > 0f) {
            val packIndex = activeVoicePack.ordinal
            val actionIndex = VoxAction.OVERDRIVE.ordinal
            
            if (packIndex < voxSoundMatrix.size && actionIndex < voxSoundMatrix[packIndex].size) {
                var soundId = voxSoundMatrix[packIndex][actionIndex]
                if (soundId == 0) soundId = voxSoundMatrix[VoicePackId.DEFAULT.ordinal][actionIndex]
                
                if (soundId != 0) {
                    soundPool?.play(soundId, volume, volume, 3, 0, 1.0f)
                }
            }
        }
    }

    fun playSfxPreview(volume: Float) {
        sfxVolumeScale = volume
        isSfxMuted = volume <= 0f
        if (volume > 0f) {
            val soundId = sfxSoundIds[SfxType.SNAP_TICK.ordinal]
            if (soundId != 0) {
                soundPool?.play(soundId, volume, volume, 2, 0, 1.0f)
            }
        }
    }

    fun updateBgmVolume(volume: Float) {
        bgmVolumeScale = volume
        BgmManager.setMuted(volume <= 0f)
        BgmManager.setMasterVolume(volume)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        sfxSoundIds.fill(0)
        voxSoundMatrix.forEach { it.fill(0) }
        activeVoxStreamId = 0
    }
}
