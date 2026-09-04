package com.example.gridsurge.game.render

import android.content.Context
import android.graphics.*
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.example.gridsurge.R
import com.example.gridsurge.game.model.SpecialBlockType
import com.example.gridsurge.game.model.CoreKind
import com.example.gridsurge.game.model.CoreIntegrity
import com.example.gridsurge.theme.ThemeNormalizer

class BlockTextureCache(private val context: Context) {

    private val density = context.resources.displayMetrics.density
    private var currentBoardCellPx = 0
    private var currentDockCellPx = 0

    // Raw Decoded Source Bitmaps
    private val rawSkinMap = mutableMapOf<String, Bitmap>()
    
    // Scaled Render Cache
    private val scaledBoardCache = mutableMapOf<String, Bitmap>()
    private val scaledDockCache = mutableMapOf<String, Bitmap>()

    // Relic & Special Bitmaps
    private var scaledCatalystBoard: Bitmap? = null
    private var scaledCatalystDock: Bitmap? = null
    private var scaledWarpBoard: Bitmap? = null
    private var scaledWarpDock: Bitmap? = null
    private var scaledNovaCoreBoard: Bitmap? = null
    private var scaledNovaCoreDock: Bitmap? = null
    private var scaledCircuitBoard: Bitmap? = null
    private var scaledCircuitDock: Bitmap? = null
    private var scaledRelic1: Bitmap? = null
    private var scaledRelic2: Bitmap? = null
    private var scaledRelic3Locked: Bitmap? = null
    private var scaledRelic3Unlocked: Bitmap? = null
    private var scaledRelic4: Bitmap? = null
    private var scaledRelic5: Bitmap? = null
    private var scaledCoreIntactBoard: Bitmap? = null
    private var scaledCoreCrackedBoard: Bitmap? = null
    private var scaledInfectedBoard: Bitmap? = null
    private var scaledInfectedDock: Bitmap? = null

    // Augment Icon Cache
    private val augmentIconCache = mutableMapOf<Int, Bitmap>()

    // Booster Icons & Emitter Pods
    var icEmpStrike: Bitmap? = null
        private set
    var icReroll: Bitmap? = null
        private set
    var icOverdrive: Bitmap? = null
        private set
    var podLeft: Bitmap? = null
        private set
    var podCenter: Bitmap? = null
        private set
    var podRight: Bitmap? = null
        private set

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    val filterPaint: Paint get() = bitmapPaint

    // Luminance ColorFilter Dictionary for High-Fidelity Tinting
    private val luminanceFilterMap = mutableMapOf<Int, ColorMatrixColorFilter>()

    private val rainbowRimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
    }
    private val rainbowGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.5f * density
        maskFilter = BlurMaskFilter(3.5f * density, BlurMaskFilter.Blur.NORMAL)
    }

    init {
        loadRawBitmaps()
        loadBoosterIcons()
    }

    private fun loadRawBitmaps() {
        val options = BitmapFactory.Options().apply {
            inScaled = false
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        fun safeDecode(key: String, @DrawableRes resId: Int) {
            try {
                BitmapFactory.decodeResource(context.resources, resId, options)?.let {
                    rawSkinMap[key] = it
                }
            } catch (e: Exception) {
                android.util.Log.e("BlockTextureCache", "Failed to decode skin: $key", e)
            }
        }

        fun safeDecodeVariant(key: String, drawableName: String, fallbackResId: Int) {
            val resId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
            val targetResId = if (resId != 0) resId else fallbackResId
            safeDecode(key, targetResId)
        }

        safeDecode(ThemeNormalizer.GLASS, R.drawable.skin_midnight_glass_cyan)
        safeDecode(ThemeNormalizer.CYBER, R.drawable.skin_cyber_void_cyan)
        safeDecode(ThemeNormalizer.SOLAR, R.drawable.skin_solar_flare_cyan)
        safeDecode(ThemeNormalizer.VOID, R.drawable.skin_voidborn_purple)
        safeDecode(ThemeNormalizer.HYPERCUBE, R.drawable.skin_hypercube_prism_cyan)
        safeDecode(ThemeNormalizer.QUANTUM, R.drawable.skin_quantum_matrix_cyan)

        // Custom 3D Hypercube Color Variants
        safeDecode("hypercube_yellow", R.drawable.skin_hypercube_prism_yellow)
        safeDecode("hypercube_orange", R.drawable.skin_hypercube_prism_orange)
        safeDecodeVariant("hypercube_green", "skin_hypercube_prism_green", R.drawable.skin_hypercube_prism_cyan)
        safeDecodeVariant("hypercube_green", "skin_hypercube_prism_greebn", R.drawable.skin_hypercube_prism_cyan)
        safeDecode("hypercube_purple", R.drawable.skin_hypercube_prism_purple)
        safeDecode("hypercube_red", R.drawable.skin_hypercube_prism_red)
        safeDecode("hypercube_blue", R.drawable.skin_hypercube_prism_blue)
        safeDecode("hypercube_cyan", R.drawable.skin_hypercube_prism_cyan)

        // Custom 3D Quantum Matrix Color Variants
        safeDecode("quantum_yellow", R.drawable.skin_quantum_matrix_yellow)
        safeDecode("quantum_orange", R.drawable.skin_quantum_matrix_orange)
        safeDecodeVariant("quantum_green", "skin_quantum_matrix_green", R.drawable.skin_quantum_matrix_cyan)
        safeDecode("quantum_purple", R.drawable.skin_quantum_matrix_purple)
        safeDecode("quantum_red", R.drawable.skin_quantum_matrix_red)
        safeDecode("quantum_blue", R.drawable.skin_quantum_matrix_blue)
        safeDecode("quantum_cyan", R.drawable.skin_quantum_matrix_cyan)

        // Custom 3D Voidborn Color Variants
        safeDecode("void_yellow", R.drawable.skin_voidborn_yellow)
        safeDecode("void_orange", R.drawable.skin_voidborn_orange)
        safeDecode("void_green", R.drawable.skin_voidborn_green)
        safeDecode("void_purple", R.drawable.skin_voidborn_purple)
        safeDecode("void_pink", R.drawable.skin_voidborn_purple)
        safeDecode("void_red", R.drawable.skin_voidborn_red)
        safeDecode("void_blue", R.drawable.skin_voidborn_blue)
        safeDecode("void_cyan", R.drawable.skin_voidborn_cyan)

        // Custom 3D Midnight Glass Color Variants
        safeDecode("glass_yellow", R.drawable.skin_midnight_glass_yellow)
        safeDecode("glass_orange", R.drawable.skin_midnight_glass_orange)
        safeDecode("glass_green", R.drawable.skin_midnight_glass_green)
        safeDecode("glass_purple", R.drawable.skin_midnight_glass_purple)
        safeDecode("glass_pink", R.drawable.skin_midnight_glass_cyan)
        safeDecode("glass_red", R.drawable.skin_midnight_glass_red)
        safeDecode("glass_blue", R.drawable.skin_midnight_glass_blue)
        safeDecode("glass_cyan", R.drawable.skin_midnight_glass_cyan)

        // Custom 3D Cyber Void Color Variants
        safeDecode("cyber_yellow", R.drawable.skin_cyber_void_yellow)
        safeDecode("cyber_orange", R.drawable.skin_cyber_void_orange)
        safeDecode("cyber_green", R.drawable.skin_cyber_void_green)
        safeDecode("cyber_purple", R.drawable.skin_cyber_void_purple)
        safeDecode("cyber_pink", R.drawable.skin_cyber_void_cyan)
        safeDecode("cyber_red", R.drawable.skin_cyber_void_red)
        safeDecode("cyber_blue", R.drawable.skin_cyber_void_blue)
        safeDecode("cyber_cyan", R.drawable.skin_cyber_void_cyan)

        // Custom 3D Solar Flare Color Variants
        safeDecode("solar_yellow", R.drawable.skin_solar_flare_yellow)
        safeDecode("solar_orange", R.drawable.skin_solar_flare_orange)
        safeDecode("solar_green", R.drawable.skin_solar_flare_green)
        safeDecode("solar_purple", R.drawable.skin_solar_flare_purple)
        safeDecode("solar_pink", R.drawable.skin_solar_flare_cyan)
        safeDecode("solar_red", R.drawable.skin_solar_flare_red)
        safeDecode("solar_blue", R.drawable.skin_solar_flare_blue)
        safeDecode("solar_cyan", R.drawable.skin_solar_flare_cyan)

        // Custom 3D Cyber Void Color Variants
        safeDecodeVariant("cyber_yellow", "skin_cyber_void_yellow", R.drawable.skin_midnight_glass_cyan)
        safeDecodeVariant("cyber_orange", "skin_cyber_void_orange", R.drawable.skin_midnight_glass_cyan)
        safeDecodeVariant("cyber_green", "skin_cyber_void_green", R.drawable.skin_midnight_glass_cyan)
        safeDecodeVariant("cyber_purple", "skin_cyber_void_purple", R.drawable.skin_midnight_glass_cyan)
        safeDecodeVariant("cyber_pink", "skin_cyber_void_pink", R.drawable.skin_midnight_glass_cyan)
        safeDecodeVariant("cyber_red", "skin_cyber_void_red", R.drawable.skin_midnight_glass_cyan)
        safeDecodeVariant("cyber_blue", "skin_cyber_void_blue", R.drawable.skin_midnight_glass_cyan)
        safeDecodeVariant("cyber_cyan", "skin_cyber_void_cyan", R.drawable.skin_midnight_glass_cyan)

        // Custom 3D Solar Flare Color Variants
        safeDecodeVariant("solar_yellow", "skin_solar_flare_yellow", R.drawable.skin_midnight_glass_cyan)
        safeDecodeVariant("solar_orange", "skin_solar_flare_orange", R.drawable.skin_midnight_glass_cyan)
        safeDecodeVariant("solar_green", "skin_solar_flare_green", R.drawable.skin_midnight_glass_cyan)
        safeDecodeVariant("solar_purple", "skin_solar_flare_purple", R.drawable.skin_midnight_glass_cyan)
        safeDecodeVariant("solar_pink", "skin_solar_flare_pink", R.drawable.skin_midnight_glass_cyan)
        safeDecodeVariant("solar_red", "skin_solar_flare_red", R.drawable.skin_midnight_glass_cyan)
        safeDecodeVariant("solar_blue", "skin_solar_flare_blue", R.drawable.skin_midnight_glass_cyan)
        safeDecodeVariant("solar_cyan", "skin_solar_flare_cyan", R.drawable.skin_midnight_glass_cyan)
    }

    private fun loadBoosterIcons() {
        val options = BitmapFactory.Options().apply { inScaled = false }
        icEmpStrike = BitmapFactory.decodeResource(context.resources, R.drawable.ic_skill_emp_strike, options)
        icReroll = BitmapFactory.decodeResource(context.resources, R.drawable.ic_skill_reroll, options)
        icOverdrive = BitmapFactory.decodeResource(context.resources, R.drawable.ic_skill_overdrive, options)

        try {
            podLeft = BitmapFactory.decodeResource(context.resources, R.drawable.emitter_pod_left, options)
            podCenter = BitmapFactory.decodeResource(context.resources, R.drawable.emitter_pod_center, options)
            podRight = BitmapFactory.decodeResource(context.resources, R.drawable.emitter_pod_right, options)
        } catch (e: Exception) {
            Log.e("BlockTextureCache", "Failed to decode emitter pods", e)
        }
    }

    private fun getLuminanceFilter(colorInt: Int): ColorMatrixColorFilter {
        return luminanceFilterMap.getOrPut(colorInt) {
            val r = Color.red(colorInt) / 255f
            val g = Color.green(colorInt) / 255f
            val b = Color.blue(colorInt) / 255f

            // Rec. 709 Luminance Matrix:
            // Converts texture color to luminance, then tints it with target neon color.
            val matrix = ColorMatrix(
                floatArrayOf(
                    0.2126f * r, 0.7152f * r, 0.0722f * r, 0f, 0f,
                    0.2126f * g, 0.7152f * g, 0.0722f * g, 0f, 0f,
                    0.2126f * b, 0.7152f * b, 0.0722f * b, 0f, 0f,
                    0f,          0f,          0f,          1f, 0f
                )
            )
            ColorMatrixColorFilter(matrix)
        }
    }

    fun refreshCache(cellSizePx: Int, themeKey: String) {
        if (cellSizePx <= 0) return
        if (currentBoardCellPx == cellSizePx && scaledBoardCache.isNotEmpty()) return

        currentBoardCellPx = cellSizePx
        currentDockCellPx = (cellSizePx * 0.58f).toInt()
        
        scaledBoardCache.values.forEach { if (!it.isRecycled) it.recycle() }
        scaledDockCache.values.forEach { if (!it.isRecycled) it.recycle() }
        scaledBoardCache.clear()
        scaledDockCache.clear()

        rawSkinMap.forEach { (key, raw) ->
            scaledBoardCache[key] = Bitmap.createScaledBitmap(raw, currentBoardCellPx, currentBoardCellPx, true)
            scaledDockCache[key] = Bitmap.createScaledBitmap(raw, currentDockCellPx, currentDockCellPx, true)
        }

        // Pre-scale special/relic assets
        val options = BitmapFactory.Options().apply { inScaled = false }
        fun loadAndScale(resId: Int, size: Int): Bitmap? {
            return try {
                BitmapFactory.decodeResource(context.resources, resId, options)?.let {
                    val scaled = Bitmap.createScaledBitmap(it, size, size, true)
                    if (scaled != it) it.recycle()
                    scaled
                }
            } catch (_: Exception) { null }
        }

        scaledCatalystBoard?.recycle()
        scaledWarpBoard?.recycle()
        scaledCircuitBoard?.recycle()
        scaledRelic1?.recycle()
        scaledRelic2?.recycle()
        scaledRelic3Locked?.recycle()
        scaledRelic3Unlocked?.recycle()
        scaledRelic4?.recycle()
        scaledRelic5?.recycle()
        scaledCoreIntactBoard?.recycle()
        scaledCoreCrackedBoard?.recycle()
        scaledInfectedBoard?.recycle()

        scaledCatalystBoard = loadAndScale(R.drawable.skin_catalyst_cross_block, currentBoardCellPx)
        scaledWarpBoard = loadAndScale(R.drawable.skin_warp_block, currentBoardCellPx)
        scaledNovaCoreBoard = loadAndScale(R.drawable.hud_nova_core_supercharged, currentBoardCellPx)
        scaledCircuitBoard = loadAndScale(R.drawable.overlay_circuit_rim, currentBoardCellPx)
        scaledRelic1 = loadAndScale(R.drawable.relic_sec01_frag_0, currentBoardCellPx)
        scaledRelic2 = loadAndScale(R.drawable.relic_sec02_frag_0, currentBoardCellPx)
        scaledRelic3Locked = loadAndScale(R.drawable.relic_sec03_frag_0, currentBoardCellPx)
        scaledRelic3Unlocked = loadAndScale(R.drawable.relic_sec03_frag_0, currentBoardCellPx)
        scaledRelic4 = loadAndScale(R.drawable.relic_sec04_frag_0, currentBoardCellPx)
        scaledRelic5 = loadAndScale(R.drawable.relic_sec05_frag_0, currentBoardCellPx)
        scaledCoreIntactBoard = loadAndScale(R.drawable.skin_core_block, currentBoardCellPx)
        scaledCoreCrackedBoard = loadAndScale(R.drawable.skin_core_cracked, currentBoardCellPx)
        scaledInfectedBoard = loadAndScale(R.drawable.skin_infected_block, currentBoardCellPx)

        scaledCatalystDock?.recycle()
        scaledWarpDock?.recycle()
        scaledNovaCoreDock?.recycle()
        scaledCircuitDock?.recycle()
        scaledInfectedDock?.recycle()

        scaledCatalystDock = scaledCatalystBoard?.let { Bitmap.createScaledBitmap(it, currentDockCellPx, currentDockCellPx, true) }
        scaledWarpDock = scaledWarpBoard?.let { Bitmap.createScaledBitmap(it, currentDockCellPx, currentDockCellPx, true) }
        scaledNovaCoreDock = scaledNovaCoreBoard?.let { Bitmap.createScaledBitmap(it, currentDockCellPx, currentDockCellPx, true) }
        scaledCircuitDock = scaledCircuitBoard?.let { Bitmap.createScaledBitmap(it, currentDockCellPx, currentDockCellPx, true) }
        scaledInfectedDock = scaledInfectedBoard?.let { Bitmap.createScaledBitmap(it, currentDockCellPx, currentDockCellPx, true) }
    }

    private fun getHypercubeColorKey(tintColor: Int): String {
        if (tintColor == 0) return "hypercube_cyan"

        val r = Color.red(tintColor)
        val g = Color.green(tintColor)
        val b = Color.blue(tintColor)

        return when {
            // Orange (#FF9100 / #FF6D00)
            r > 200 && g in 90..155 && b < 100 -> "hypercube_orange"
            // Yellow (#FFD600)
            r > 200 && g > 160 && b < 120 -> "hypercube_yellow"
            // Green (#00FF66)
            r < 100 && g > 200 && b < 180 -> "hypercube_green"
            // Red / Crimson (#FF1744 / #FF0055)
            r > 200 && g < 80 && b < 100 -> "hypercube_red"
            // Pink / Rose (#FF4081)
            r > 200 && g < 100 && b in 100..180 -> "hypercube_pink"
            // Purple / Magenta (#EA80FC / #E040FB)
            r > 150 && g < 180 && b > 200 -> "hypercube_purple"
            // Blue (#2979FF)
            r < 100 && g < 180 && b > 200 -> "hypercube_blue"
            // Cyan / Default (#00E5FF)
            else -> "hypercube_cyan"
        }
    }

    fun drawCell(
        canvas: Canvas,
        rect: RectF,
        themeKey: String,
        cellValue: Int,
        tintColor: Int = 0,
        specialType: SpecialBlockType = SpecialBlockType.NONE,
        isDock: Boolean = false,
        alpha: Int = 255,
        now: Long = 0L
    ) {
        bitmapPaint.alpha = alpha
        
        // 1. Special Block Overrides
        when (specialType) {
            SpecialBlockType.CATALYST_CROSSHAIR -> {
                val bitmap = if (isDock) scaledCatalystDock else scaledCatalystBoard
                bitmap?.let { 
                    bitmapPaint.colorFilter = null
                    canvas.drawBitmap(it, null, rect, bitmapPaint) 
                }
                return
            }
            SpecialBlockType.QUANTUM_WARP_VORTEX -> {
                val bitmap = if (isDock) scaledWarpDock else scaledWarpBoard
                bitmap?.let { 
                    bitmapPaint.colorFilter = null
                    canvas.drawBitmap(it, null, rect, bitmapPaint) 
                }
                return
            }
            SpecialBlockType.NOVA_CORE_EXPLOSION -> {
                val bitmap = if (isDock) scaledNovaCoreDock else scaledNovaCoreBoard
                bitmap?.let { 
                    bitmapPaint.colorFilter = null
                    canvas.drawBitmap(it, null, rect, bitmapPaint) 
                }
                return
            }
            SpecialBlockType.CIRCUIT_CONDUIT -> {
                val bitmap = if (isDock) scaledCircuitDock else scaledCircuitBoard
                bitmap?.let { 
                    bitmapPaint.colorFilter = null
                    canvas.drawBitmap(it, null, rect, bitmapPaint)
                    drawRainbowFrame(canvas, rect, now)
                }
                return
            }
            else -> {}
        }

        // 2. Infected Hazard
        if (cellValue == 9) {
            val bitmap = if (isDock) scaledInfectedDock else scaledInfectedBoard
            bitmap?.let { 
                bitmapPaint.colorFilter = null
                canvas.drawBitmap(it, null, rect, bitmapPaint) 
            }
            return
        }

        // 3. Themed Textured Block with Custom Variant or Luminance Tinting
        val cache = if (isDock) scaledDockCache else scaledBoardCache
        val canonical = ThemeNormalizer.normalize(themeKey)
        val effectiveKey = when (canonical) {
            ThemeNormalizer.HYPERCUBE -> getHypercubeColorKey(tintColor)
            ThemeNormalizer.QUANTUM -> getQuantumColorKey(tintColor)
            ThemeNormalizer.VOID -> getVoidbornColorKey(tintColor)
            ThemeNormalizer.GLASS -> getGlassColorKey(tintColor)
            ThemeNormalizer.CYBER -> getCyberColorKey(tintColor)
            ThemeNormalizer.SOLAR -> getSolarColorKey(tintColor)
            else -> canonical
        }

        val bitmap = cache[effectiveKey] ?: cache[canonical] ?: cache[ThemeNormalizer.GLASS] ?: return

        if (canonical == ThemeNormalizer.HYPERCUBE || canonical == ThemeNormalizer.QUANTUM || canonical == ThemeNormalizer.VOID || canonical == ThemeNormalizer.GLASS || canonical == ThemeNormalizer.CYBER || canonical == ThemeNormalizer.SOLAR) {
            bitmapPaint.colorFilter = null
        } else if (tintColor != 0) {
            bitmapPaint.colorFilter = getLuminanceFilter(tintColor)
        } else {
            bitmapPaint.colorFilter = null
        }

        canvas.drawBitmap(bitmap, null, rect, bitmapPaint)
        bitmapPaint.colorFilter = null
        bitmapPaint.alpha = 255
    }

    private fun getCyberColorKey(tintColor: Int): String {
        if (tintColor == 0) return "cyber_green"

        val r = Color.red(tintColor)
        val g = Color.green(tintColor)
        val b = Color.blue(tintColor)

        return when {
            r > 200 && g in 90..155 && b < 100 -> "cyber_orange"
            r > 200 && g > 160 && b < 120 -> "cyber_yellow"
            r < 100 && g > 200 && b < 180 -> "cyber_green"
            r > 200 && g < 80 && b < 100 -> "cyber_red"
            r > 200 && g < 100 && b in 100..180 -> "cyber_pink"
            r > 150 && g < 180 && b > 200 -> "cyber_purple"
            r < 100 && g < 180 && b > 200 -> "cyber_blue"
            else -> "cyber_cyan"
        }
    }

    private fun getSolarColorKey(tintColor: Int): String {
        if (tintColor == 0) return "solar_orange"

        val r = Color.red(tintColor)
        val g = Color.green(tintColor)
        val b = Color.blue(tintColor)

        return when {
            r > 200 && g in 90..155 && b < 100 -> "solar_orange"
            r > 200 && g > 160 && b < 120 -> "solar_yellow"
            r < 100 && g > 200 && b < 180 -> "solar_green"
            r > 200 && g < 80 && b < 100 -> "solar_red"
            r > 200 && g < 100 && b in 100..180 -> "solar_pink"
            r > 150 && g < 180 && b > 200 -> "solar_purple"
            r < 100 && g < 180 && b > 200 -> "solar_blue"
            else -> "solar_cyan"
        }
    }

    private fun getGlassColorKey(tintColor: Int): String {
        if (tintColor == 0) return "glass_cyan"

        val r = Color.red(tintColor)
        val g = Color.green(tintColor)
        val b = Color.blue(tintColor)

        return when {
            r > 200 && g in 90..155 && b < 100 -> "glass_orange"
            r > 200 && g > 160 && b < 120 -> "glass_yellow"
            r < 100 && g > 200 && b < 180 -> "glass_green"
            r > 200 && g < 80 && b < 100 -> "glass_red"
            r > 200 && g < 100 && b in 100..180 -> "glass_pink"
            r > 150 && g < 180 && b > 200 -> "glass_purple"
            r < 100 && g < 180 && b > 200 -> "glass_blue"
            else -> "glass_cyan"
        }
    }

    private fun getVoidbornColorKey(tintColor: Int): String {
        if (tintColor == 0) return "void_purple"

        val r = Color.red(tintColor)
        val g = Color.green(tintColor)
        val b = Color.blue(tintColor)

        return when {
            r > 200 && g in 90..155 && b < 100 -> "void_orange"
            r > 200 && g > 160 && b < 120 -> "void_yellow"
            r < 100 && g > 200 && b < 180 -> "void_green"
            r > 200 && g < 80 && b < 100 -> "void_red"
            r > 200 && g < 100 && b in 100..180 -> "void_pink"
            r > 150 && g < 180 && b > 200 -> "void_purple"
            r < 100 && g < 160 && b > 200 -> "void_blue"
            else -> "void_cyan"
        }
    }

    private fun getQuantumColorKey(tintColor: Int): String {
        if (tintColor == 0) return "quantum_cyan"

        val r = Color.red(tintColor)
        val g = Color.green(tintColor)
        val b = Color.blue(tintColor)

        return when {
            r > 200 && g in 90..155 && b < 100 -> "quantum_orange"
            r > 200 && g > 160 && b < 120 -> "quantum_yellow"
            r < 100 && g > 200 && b < 180 -> "quantum_green"
            r > 200 && g < 80 && b < 100 -> "quantum_red"
            r > 200 && g < 100 && b in 100..180 -> "quantum_pink"
            r > 150 && g < 180 && b > 200 -> "quantum_purple"
            r < 100 && g < 180 && b > 200 -> "quantum_blue"
            else -> "quantum_cyan"
        }
    }

    fun drawCellWithMatrix(
        canvas: Canvas,
        matrix: Matrix,
        themeKey: String,
        tintColor: Int,
        alpha: Int,
        specialType: SpecialBlockType = SpecialBlockType.NONE,
        now: Long = 0L
    ) {
        val canonical = ThemeNormalizer.normalize(themeKey)
        val effectiveKey = when (canonical) {
            ThemeNormalizer.HYPERCUBE -> getHypercubeColorKey(tintColor)
            ThemeNormalizer.QUANTUM -> getQuantumColorKey(tintColor)
            ThemeNormalizer.VOID -> getVoidbornColorKey(tintColor)
            ThemeNormalizer.GLASS -> getGlassColorKey(tintColor)
            ThemeNormalizer.CYBER -> getCyberColorKey(tintColor)
            ThemeNormalizer.SOLAR -> getSolarColorKey(tintColor)
            else -> canonical
        }

        val bitmap = when (specialType) {
            SpecialBlockType.CATALYST_CROSSHAIR -> scaledCatalystBoard
            SpecialBlockType.QUANTUM_WARP_VORTEX -> scaledWarpBoard
            SpecialBlockType.CIRCUIT_CONDUIT -> scaledCircuitBoard
            else -> scaledBoardCache[effectiveKey] ?: scaledBoardCache[canonical] ?: scaledBoardCache[ThemeNormalizer.GLASS]
        } ?: return

        bitmapPaint.alpha = alpha
        if (canonical == ThemeNormalizer.HYPERCUBE || canonical == ThemeNormalizer.QUANTUM || canonical == ThemeNormalizer.VOID || canonical == ThemeNormalizer.GLASS || canonical == ThemeNormalizer.CYBER || canonical == ThemeNormalizer.SOLAR) {
            bitmapPaint.colorFilter = null
        } else if (specialType == SpecialBlockType.NONE && tintColor != 0) {
            bitmapPaint.colorFilter = getLuminanceFilter(tintColor)
        } else {
            bitmapPaint.colorFilter = null
        }

        canvas.drawBitmap(bitmap, matrix, bitmapPaint)
        
        if (specialType == SpecialBlockType.CIRCUIT_CONDUIT) {
            val rect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
            val saveCount = canvas.save()
            canvas.concat(matrix)
            drawRainbowFrame(canvas, rect, now)
            canvas.restoreToCount(saveCount)
        }

        bitmapPaint.colorFilter = null
        bitmapPaint.alpha = 255
    }

    fun drawAdventureCore(
        canvas: Canvas,
        rect: RectF,
        coreKind: CoreKind,
        coreIntegrity: CoreIntegrity?,
        isInvulnerable: Boolean
    ) {
        val bitmap = when (coreKind) {
            CoreKind.CYAN_REACTOR -> scaledRelic1
            CoreKind.AMBER_FURNACE, CoreKind.THERMAL_CATALYST -> scaledRelic2
            CoreKind.CRIMSON_CIPHER_LOCKED -> scaledRelic3Locked
            CoreKind.CRIMSON_CIPHER_EXPOSED -> scaledRelic3Unlocked
            CoreKind.EMERALD_CONDUIT -> scaledRelic4
            CoreKind.PURPLE_SINGULARITY -> scaledRelic5
            else -> if (coreIntegrity == CoreIntegrity.CRACKED) scaledCoreCrackedBoard else scaledCoreIntactBoard
        } ?: return

        bitmapPaint.colorFilter = null
        canvas.drawBitmap(bitmap, null, rect, bitmapPaint)
    }

    fun getAugmentIcon(@DrawableRes resId: Int, targetSizePx: Int): Bitmap? {
        return augmentIconCache.getOrPut(resId) {
            val options = BitmapFactory.Options().apply { inScaled = false }
            val src = BitmapFactory.decodeResource(context.resources, resId, options) ?: return null
            val scaled = Bitmap.createScaledBitmap(src, targetSizePx, targetSizePx, true)
            if (scaled != src) src.recycle()
            scaled
        }
    }

    private fun drawRainbowFrame(canvas: Canvas, rect: RectF, now: Long) {
        val hue = (now % 2000L) / 2000f * 360f
        val color = Color.HSVToColor(floatArrayOf(hue, 0.9f, 1.0f))
        rainbowGlowPaint.color = color
        rainbowGlowPaint.alpha = 140
        canvas.drawRoundRect(rect, 4f * density, 4f * density, rainbowGlowPaint)
        rainbowRimPaint.color = color
        rainbowRimPaint.alpha = 255
        canvas.drawRoundRect(rect, 4f * density, 4f * density, rainbowRimPaint)
    }

    fun prepareDimensions(cellSize: Int, dockCellSize: Int) {
        refreshCache(cellSize, "glass")
    }

    fun recycle() {
        scaledBoardCache.values.forEach { if (!it.isRecycled) it.recycle() }
        scaledDockCache.values.forEach { if (!it.isRecycled) it.recycle() }
        scaledBoardCache.clear()
        scaledDockCache.clear()
    }
}
