package com.example.gridsurge.meta

import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter

object ColorFilterCache {
    private val filterCache = HashMap<Int, ColorFilter>()

    // Pre-allocated reusable matrix for initialization
    private val colorMatrix = ColorMatrix()

    fun getOrCreateHueFilter(hueDegrees: Float): ColorFilter {
        val key = hueDegrees.toInt()
        return filterCache.getOrPut(key) {
            colorMatrix.reset()
            // Rotate RGB channels along the color vector
            setHue(colorMatrix, hueDegrees)
            ColorMatrixColorFilter(colorMatrix)
        }
    }

    private fun setHue(matrix: ColorMatrix, degrees: Float) {
        val rad = Math.toRadians(degrees.toDouble())
        val cos = Math.cos(rad).toFloat()
        val sin = Math.sin(rad).toFloat()
        
        // Lum values for perceptual brightness preservation
        val lumR = 0.213f
        val lumG = 0.715f
        val lumB = 0.072f

        matrix.set(floatArrayOf(
            lumR + cos * (1 - lumR) + sin * (-lumR), lumG + cos * (-lumG) + sin * (-lumG), lumB + cos * (-lumB) + sin * (1 - lumB), 0f, 0f,
            lumR + cos * (-lumR) + sin * (0.143f), lumG + cos * (1 - lumG) + sin * (0.140f), lumB + cos * (-lumB) + sin * (-0.283f), 0f, 0f,
            lumR + cos * (-lumR) + sin * (-(1 - lumR)), lumG + cos * (-lumG) + sin * (lumG), lumB + cos * (1 - lumB) + sin * (lumB), 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
    }
}
