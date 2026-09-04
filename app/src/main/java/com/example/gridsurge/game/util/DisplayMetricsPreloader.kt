package com.example.gridsurge.game.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.gridsurge.R

object DisplayMetricsPreloader {
    var density: Float = 1f
        private set
    var screenWidth: Int = 0
        private set
    var screenHeight: Int = 0
        private set
    
    var prewarmedCellSize: Int = 0
        private set
    var prewarmedRivalPin: Bitmap? = null
        private set

    fun prewarm(context: Context) {
        val dm = context.resources.displayMetrics
        density = dm.density
        screenWidth = dm.widthPixels
        screenHeight = dm.heightPixels

        // Heuristic calculation matching GridSurgeGameView.onSizeChanged logic
        val horizontalMargin = 16f * density
        val topMargin = 85f * density
        val availableWidth = screenWidth - horizontalMargin * 2f
        val availableHeight = screenHeight - topMargin - (150f * density)

        val cellSpacing = 3.5f * density
        val sizeFromWidth = (availableWidth - (cellSpacing * 9)) / 8f
        val sizeFromHeight = (availableHeight - (cellSpacing * 9)) / 8f
        
        val cellSize = minOf(sizeFromWidth, sizeFromHeight).coerceAtLeast(10f * density)
        prewarmedCellSize = cellSize.toInt()

        // Pre-scale Rival Pin
        try {
            val raw = BitmapFactory.decodeResource(context.resources, R.drawable.ic_rival_ghost)
            if (raw != null) {
                val targetSize = maxOf(1, (24f * density).toInt())
                prewarmedRivalPin = Bitmap.createScaledBitmap(raw, targetSize, targetSize, true)
            }
        } catch (_: Exception) {}
    }
}
