package com.example.gridsurge.game.gesture

import android.graphics.Rect
import android.os.Build
import android.view.View
import androidx.core.view.ViewCompat

object SystemGestureHelper {

    private val exclusionRects = ArrayList<Rect>(2)

    /**
     * Prevents system edge-swipe navigation gestures from interfering with
     * block dragging near the left and right borders of the screen.
     */
    fun updateExclusionZones(view: View, gridBoundsPx: Rect, dockBoundsPx: Rect) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            exclusionRects.clear()
            exclusionRects.add(gridBoundsPx)
            exclusionRects.add(dockBoundsPx)
            ViewCompat.setSystemGestureExclusionRects(view, exclusionRects)
        }
    }
}
