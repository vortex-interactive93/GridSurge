package com.example.gridsurge

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.gridsurge.ads.AdManager
import com.example.gridsurge.audio.BgmManager
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.VoicePackId
import com.example.gridsurge.billing.BillingManager
import com.example.gridsurge.game.util.DisplayMetricsPreloader
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.meta.SkinThemeManager
import com.example.gridsurge.settings.SettingsManager
import com.example.gridsurge.ui.NavigationRoot
import com.example.gridsurge.ui.theme.GridSurgeTheme
import com.example.gridsurge.ui.util.ImmersiveModeHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        installSplashScreen()
        super.onCreate(savedInstanceState)
        ImmersiveModeHelper.enableImmersiveStickyMode(this)
        SkinThemeManager.init(this)
        SfxManager.initialize(this)
        SettingsManager.getInstance(this)
        AdManager.initialize(this)
        BillingManager.initialize(this, PlayerProfileManager(this))
        DisplayMetricsPreloader.prewarm(this)
        
        // Restore user's customized Announcer Pack
        val savedPackName = getSharedPreferences("gridsurge_prefs", MODE_PRIVATE)
            .getString("equipped_voice", VoicePackId.CYBER_AI.name) ?: VoicePackId.CYBER_AI.name
        
        SfxManager.activeVoicePack = runCatching { 
            VoicePackId.valueOf(savedPackName) 
        }.getOrDefault(VoicePackId.CYBER_AI)

        enableEdgeToEdge()
        setContent {
            GridSurgeTheme {
                NavigationRoot()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        BgmManager.pause()
    }

    override fun onResume() {
        super.onResume()
        BgmManager.resume()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            ImmersiveModeHelper.enableImmersiveStickyMode(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BgmManager.release()
        SfxManager.release()
    }
}
