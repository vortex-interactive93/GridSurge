package com.example.gridsurge.game.share

import android.content.Context
import android.content.Intent
import android.graphics.*
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object CyberShareCardGenerator {

    fun generateAndShareClashCard(
        context: Context,
        playerScore: Long,
        rivalScore: Long,
        maxCombo: Int,
        linesCleared: Int,
        isVictory: Boolean
    ) {
        val width = 1080
        val height = 1350 // 4:5 Social Media Portrait Ratio
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background Cyber Canvas
        val bgPaint = Paint().apply { color = Color.parseColor("#050B14") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Outer Neon Grid Border
        val borderPaint = Paint().apply {
            color = if (isVictory) Color.parseColor("#00E5FF") else Color.parseColor("#FF0055")
            style = Paint.Style.STROKE
            strokeWidth = 12f
        }
        canvas.drawRoundRect(24f, 24f, width - 24f, height - 24f, 32f, 32f, borderPaint)

        // 3. Header Title
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 64f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("GRID SURGE // BLITZ CLASH", width / 2f, 160f, titlePaint)

        val bannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isVictory) Color.parseColor("#00E5FF") else Color.parseColor("#FF0055")
            textSize = 82f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(if (isVictory) "CLASH VICTORY" else "RIVAL OVERCLOCKED", width / 2f, 270f, bannerPaint)

        // 4. Head-to-Head Score Cards
        val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00E5FF")
            textSize = 96f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val formattedScore = String.format(java.util.Locale.US, "%,d", playerScore)
        canvas.drawText(formattedScore, width * 0.28f, 520f, scorePaint)

        val rivalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF0055")
            textSize = 96f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val formattedRival = String.format(java.util.Locale.US, "%,d", rivalScore)
        canvas.drawText(formattedRival, width * 0.72f, 520f, rivalPaint)

        val vsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GRAY
            textSize = 48f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("VS", width / 2f, 510f, vsPaint)

        // 5. Stat Badges
        val statLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8090A0")
            textSize = 40f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("MAX SURGE STREAK: x$maxCombo", width / 2f, 780f, statLabelPaint)
        canvas.drawText("LINES CLEARED: $linesCleared", width / 2f, 860f, statLabelPaint)

        // 6. Save and Dispatch Native Share Intent
        try {
            val cachePath = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File(cachePath, "grid_surge_clash_card.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, "I just scored $playerScore pts in Grid Surge 1v1 Blitz Clash! Can you beat me?")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Neural Battle Dossier"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
