package com.example.gridsurge.meta

import android.content.Context
import android.graphics.*
import com.example.gridsurge.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SkinThemeManager {

    private val skinCache = HashMap<String, Bitmap>()
    private const val SKIN_SIZE = 256

    // Pre-allocated paints for procedural fallback
    private val bevelHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = (0.35f * 255).toInt()
        style = Paint.Style.STROKE
    }

    private val neonBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val bevelPath = Path()

    fun init(context: Context) {
        val density = context.resources.displayMetrics.density
        bevelHighlightPaint.strokeWidth = 2f * density
        neonBorderPaint.strokeWidth = 1.5f * density

        CoroutineScope(Dispatchers.IO).launch {
            ThemeCatalog.THEMES.forEach { theme ->
                loadAndCacheBitmap(context, theme.id, theme.blockSkinRes)
            }
            
            // Cache Surge Core bitmaps
            loadAndCacheBitmap(context, "core_intact", R.drawable.skin_core_block)
            loadAndCacheBitmap(context, "core_cracked", R.drawable.skin_core_cracked)
            
            // Cache Special Catalysts
            loadAndCacheBitmap(context, "catalyst_cross", R.drawable.skin_catalyst_cross_block)
            loadAndCacheBitmap(context, "catalyst_supernova", R.drawable.skin_catalyst_supernova_block)
        }
    }

    fun getCoreBitmap(isCracked: Boolean): Bitmap? {
        val key = if (isCracked) "core_cracked" else "core_intact"
        return synchronized(skinCache) {
            skinCache[key]
        }
    }

    private fun loadAndCacheBitmap(context: Context, themeId: String, resId: Int) {
        try {
            val options = BitmapFactory.Options().apply {
                inScaled = true
                inMutable = false
            }
            val original = BitmapFactory.decodeResource(context.resources, resId, options) ?: return
            val scaled = Bitmap.createScaledBitmap(original, SKIN_SIZE, SKIN_SIZE, true)
            synchronized(skinCache) {
                skinCache[themeId] = scaled
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getThemeBitmap(themeId: String): Bitmap? {
        return synchronized(skinCache) {
            skinCache[themeId]
        }
    }

    fun drawCell(
        canvas: Canvas,
        dstRect: RectF,
        cornerRadius: Float,
        paint: Paint,
        themeId: String,
        fallbackColor: Int,
        alpha: Int = 255,
        colorFilter: ColorFilter? = null
    ) {
        val bitmap = getThemeBitmap(themeId)
        
        if (bitmap != null) {
            val originalAlpha = paint.alpha
            val originalFilter = paint.colorFilter
            paint.alpha = alpha
            paint.colorFilter = colorFilter
            canvas.drawBitmap(bitmap, null, dstRect, paint)
            paint.alpha = originalAlpha
            paint.colorFilter = originalFilter
        } else {
            // Procedural Fallback
            val originalAlpha = alpha
            
            // 1. Base Fill
            fillPaint.color = fallbackColor
            fillPaint.alpha = originalAlpha
            canvas.drawRoundRect(dstRect, cornerRadius, cornerRadius, fillPaint)

            // 2. Top-left Specular Bevel Highlight
            canvas.save()
            canvas.clipRect(dstRect)
            
            bevelHighlightPaint.alpha = (0.35f * originalAlpha).toInt()
            bevelPath.reset()
            bevelPath.moveTo(dstRect.left + cornerRadius, dstRect.top)
            bevelPath.lineTo(dstRect.right - cornerRadius, dstRect.top)
            bevelPath.moveTo(dstRect.left, dstRect.bottom - cornerRadius)
            bevelPath.lineTo(dstRect.left, dstRect.top + cornerRadius)
            canvas.drawPath(bevelPath, bevelHighlightPaint)
            
            canvas.restore()

            // 3. Neon Border Stroke
            neonBorderPaint.color = fallbackColor
            neonBorderPaint.alpha = originalAlpha
            canvas.drawRoundRect(dstRect, cornerRadius, cornerRadius, neonBorderPaint)
        }
    }
}
