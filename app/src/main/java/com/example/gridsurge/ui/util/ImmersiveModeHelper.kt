package com.example.gridsurge.ui.util

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object ImmersiveModeHelper {

    /**
     * Hides system status bar, action bar, and navigation bar.
     * Swiping from the edge shows transient bars that auto-hide after 2 seconds.
     */
    fun enableImmersiveStickyMode(activity: Activity) {
        val window = activity.window

        // 1. Extend game canvas behind notches and cutouts
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        // 2. Hide Status & Navigation Bars with Sticky Transient Swipe Behavior
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // 3. Keep screen awake during gameplay
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * Ensures immersive mode persists when the user switches apps or unlocks the phone.
     */
    @Suppress("DEPRECATION")
    fun attachWindowFocusObserver(activity: Activity) {
        activity.window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                enableImmersiveStickyMode(activity)
            }
        }
    }
}
