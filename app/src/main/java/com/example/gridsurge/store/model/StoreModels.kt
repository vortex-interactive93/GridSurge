package com.example.gridsurge.store.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.gridsurge.R

enum class StoreCategory(val title: String) {
    FEATURED("FEATURED"),
    STAR_PACKS("STAR PACKS"),
    VIP_ACCESS("VIP ACCESS")
}

enum class CurrencyGraphicType(@DrawableRes val iconRes: Int) {
    HEX_CELL(R.drawable.ic_store_data_drop),          // Tier 1: Single data cell
    DUAL_CONDUIT(R.drawable.ic_store_recharge),      // Tier 2: Twin capacitors
    TRI_REACTOR(R.drawable.ic_store_capital),       // Tier 3: High-yield triangular core
    QUANTUM_TESSERACT(R.drawable.ic_store_quantum), // Tier 4: Dimensional hypercube
    APEX_COFFER(R.drawable.ic_store_overlord)        // Tier 5: Overlord vault matrix
}

enum class PackTier(
    val tierLabel: String,
    val bonusPercent: Int,
    val accentColor: Color,
    val graphicType: CurrencyGraphicType,
    val cellCount: Int = 1, // Backward compatibility property
    val buttonTextColor: Color = Color(0xFF040711)
) {
    FREE_RATION("DAILY RATION", 0, Color(0xFF00FF66), CurrencyGraphicType.HEX_CELL, 1, Color(0xFF040711)),
    ENTRY("DATA DROP", 0, Color(0xFF00E5FF), CurrencyGraphicType.HEX_CELL, 1, Color(0xFF040711)),
    TACTICAL("RECHARGE", 15, Color(0xFF00E5FF), CurrencyGraphicType.DUAL_CONDUIT, 2, Color(0xFF040711)),
    SURGE("CAPITAL SURGE", 30, Color(0xFFFFB300), CurrencyGraphicType.TRI_REACTOR, 3, Color(0xFF040711)),
    QUANTUM("QUANTUM CACHE", 45, Color(0xFFD500F9), CurrencyGraphicType.QUANTUM_TESSERACT, 4, Color.White),
    OVERLORD("NEURAL OVERLORD", 65, Color(0xFFFF1744), CurrencyGraphicType.APEX_COFFER, 5, Color.White)
}

data class StoreProduct(
    val id: String,
    val title: String,
    val subtitle: String,
    val starAmount: Long,
    val bonusStarAmount: Long = 0L,
    val priceFormatted: String,
    val priceMicros: Long,
    val tier: PackTier,
    @DrawableRes val iconRes: Int = R.drawable.ic_store_data_drop,
    val opticalScale: Float = 1.0f,
    val isHeroBundle: Boolean = false,
    val includesNoAds: Boolean = false,
    val isOneTimePurchase: Boolean = false
)

data class StoreUiState(
    val selectedCategory: StoreCategory = StoreCategory.FEATURED,
    val isFreeRationClaimedToday: Boolean = false,
    val freeRationResetTimeHms: String = "07:42:15",
    val isNoAdsPurchased: Boolean = false,
    val isPurchasing: Boolean = false
)
