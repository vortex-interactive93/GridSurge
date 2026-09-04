package com.example.gridsurge.features.adventure.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

object RelicImageSlicer {

    /**
     * Slices an Android drawable resource into a 3x3 array (9 fragments total)
     * indexed from row 0..2, col 0..2 (fragment index: r * 3 + c).
     */
    fun sliceRelicDrawable(
        context: Context,
        drawableResId: Int,
        rows: Int = 3,
        cols: Int = 3
    ): List<Bitmap> {
        val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
        val sourceBitmap = BitmapFactory.decodeResource(context.resources, drawableResId, options)
            ?: return emptyList()

        val fragments = ArrayList<Bitmap>(rows * cols)

        val totalW = sourceBitmap.width
        val totalH = sourceBitmap.height

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val startX = (c * totalW) / cols
                val endX = ((c + 1) * totalW) / cols
                val startY = (r * totalH) / rows
                val endY = ((r + 1) * totalH) / rows

                val sliceW = endX - startX
                val sliceH = endY - startY

                val slice = Bitmap.createBitmap(sourceBitmap, startX, startY, sliceW, sliceH)
                fragments.add(slice)
            }
        }

        return fragments
    }
}
