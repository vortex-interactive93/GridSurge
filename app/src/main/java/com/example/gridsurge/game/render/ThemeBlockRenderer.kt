package com.example.gridsurge.game.render

import android.graphics.*

enum class ActiveTheme(val themeKey: String) {
    CYBER_NEON("cyber"),
    SOLAR_FLARE("solar"),
    MIDNIGHT_GLASS("glass"),
    TOXIC_SURGE("toxic"),
    VOIDBORN("void"),
    HYPERCUBE_PRISM("hypercube"),
    QUANTUM_MATRIX("quantum")
}

class ThemeBlockRenderer(private val density: Float) {

    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
    }
    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val specularPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        alpha = 80
    }
    private val accentLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * density
    }

    private val tempInsetRect = RectF()
    private val tempFacetPath = Path()

    fun drawThemedBlock(
        canvas: Canvas,
        rect: RectF,
        baseColor: Int,
        theme: ActiveTheme,
        isGhost: Boolean = false
    ) {
        val alphaMultiplier = if (isGhost) 0.35f else 1.0f

        when (theme) {
            ActiveTheme.CYBER_NEON -> drawCyberNeon(canvas, rect, baseColor, alphaMultiplier)
            ActiveTheme.SOLAR_FLARE -> drawSolarFlare(canvas, rect, baseColor, alphaMultiplier)
            ActiveTheme.MIDNIGHT_GLASS -> drawMidnightGlass(canvas, rect, baseColor, alphaMultiplier)
            ActiveTheme.TOXIC_SURGE -> drawToxicSurge(canvas, rect, baseColor, alphaMultiplier)
            ActiveTheme.VOIDBORN, ActiveTheme.HYPERCUBE_PRISM, ActiveTheme.QUANTUM_MATRIX -> drawVoidborn(canvas, rect, baseColor, alphaMultiplier)
        }
    }

    private fun drawCyberNeon(canvas: Canvas, r: RectF, color: Int, alpha: Float) {
        basePaint.color = color
        basePaint.alpha = (230 * alpha).toInt()
        canvas.drawRoundRect(r, 6f * density, 6f * density, basePaint)

        borderPaint.color = Color.WHITE
        borderPaint.alpha = (180 * alpha).toInt()
        canvas.drawRoundRect(r, 6f * density, 6f * density, borderPaint)

        // Center Cyber Aperture Glyph
        val inset = r.width() * 0.28f
        tempInsetRect.set(r.left + inset, r.top + inset, r.right - inset, r.bottom - inset)
        corePaint.color = Color.WHITE
        corePaint.alpha = (140 * alpha).toInt()
        canvas.drawRoundRect(tempInsetRect, 3f * density, 3f * density, corePaint)

        // Corner Circuit Notches
        val notch = 4f * density
        canvas.drawLine(r.left, r.top + notch, r.left + notch, r.top, borderPaint)
        canvas.drawLine(r.right - notch, r.bottom, r.right, r.bottom - notch, borderPaint)
    }

    private fun drawSolarFlare(canvas: Canvas, r: RectF, color: Int, alpha: Float) {
        basePaint.color = color
        basePaint.alpha = (240 * alpha).toInt()
        canvas.drawRoundRect(r, 8f * density, 8f * density, basePaint)

        // Radiant Sunburst Core
        val cx = r.centerX()
        val cy = r.centerY()
        val radius = r.width() * 0.32f
        corePaint.color = Color.parseColor("#FFF3B0")
        corePaint.alpha = (210 * alpha).toInt()
        canvas.drawCircle(cx, cy, radius, corePaint)

        // Molten Gold Rim
        borderPaint.color = Color.parseColor("#FFE082")
        borderPaint.alpha = (200 * alpha).toInt()
        canvas.drawRoundRect(r, 8f * density, 8f * density, borderPaint)
    }

    private fun drawMidnightGlass(canvas: Canvas, r: RectF, color: Int, alpha: Float) {
        basePaint.color = color
        basePaint.alpha = (200 * alpha).toInt()
        canvas.drawRoundRect(r, 5f * density, 5f * density, basePaint)

        // 4-Facet Crystal Bevel Path
        tempFacetPath.reset()
        tempFacetPath.moveTo(r.left, r.top)
        tempFacetPath.lineTo(r.centerX(), r.centerY())
        tempFacetPath.lineTo(r.right, r.top)
        tempFacetPath.close()

        specularPaint.alpha = (90 * alpha).toInt()
        canvas.drawPath(tempFacetPath, specularPaint)

        borderPaint.color = Color.parseColor("#80D8FF")
        borderPaint.alpha = (190 * alpha).toInt()
        canvas.drawRoundRect(r, 5f * density, 5f * density, borderPaint)
    }

    private fun drawToxicSurge(canvas: Canvas, r: RectF, color: Int, alpha: Float) {
        basePaint.color = color
        basePaint.alpha = (235 * alpha).toInt()
        canvas.drawRoundRect(r, 6f * density, 6f * density, basePaint)

        // Bio-hazard central reactor bar
        val insetY = r.height() * 0.38f
        val insetX = r.width() * 0.18f
        tempInsetRect.set(r.left + insetX, r.top + insetY, r.right - insetX, r.bottom - insetY)
        corePaint.color = Color.parseColor("#CCFF90")
        corePaint.alpha = (180 * alpha).toInt()
        canvas.drawRoundRect(tempInsetRect, 2f * density, 2f * density, corePaint)

        borderPaint.color = Color.parseColor("#B2FF59")
        borderPaint.alpha = (180 * alpha).toInt()
        canvas.drawRoundRect(r, 6f * density, 6f * density, borderPaint)
    }

    private fun drawVoidborn(canvas: Canvas, r: RectF, color: Int, alpha: Float) {
        basePaint.color = color
        basePaint.alpha = (245 * alpha).toInt()
        canvas.drawRoundRect(r, 7f * density, 7f * density, basePaint)

        // Singularity Event Horizon Ring
        val cx = r.centerX()
        val cy = r.centerY()
        borderPaint.color = Color.parseColor("#EA80FC")
        borderPaint.alpha = (210 * alpha).toInt()
        canvas.drawCircle(cx, cy, r.width() * 0.24f, borderPaint)

        // Obsidian Outer Trim
        accentLinePaint.color = Color.WHITE
        accentLinePaint.alpha = (150 * alpha).toInt()
        canvas.drawRoundRect(r, 7f * density, 7f * density, accentLinePaint)
    }
}
