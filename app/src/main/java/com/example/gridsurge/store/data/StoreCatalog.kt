package com.example.gridsurge.store.data

import com.example.gridsurge.R
import com.example.gridsurge.store.model.PackTier
import com.example.gridsurge.store.model.StoreProduct

object StoreCatalog {
    val HERO_BUNDLE = StoreProduct(
        id = "bundle_neural_bypass",
        title = "NEURAL BYPASS // STAR SURGE",
        subtitle = "PERMANENT NO-ADS + 2,500 ★ INJECTION",
        starAmount = 2500L,
        bonusStarAmount = 1000L,
        priceFormatted = "$4.99",
        priceMicros = 4990000L,
        tier = PackTier.SURGE,
        iconRes = R.drawable.ic_store_capital,
        opticalScale = 1.0f,
        isHeroBundle = true,
        includesNoAds = true,
        isOneTimePurchase = true
    )

    val STANDARD_GRID_PACKS = listOf(
        StoreProduct(
            id = "pack_stars_500",
            title = "DATA DROP",
            subtitle = "TACTICAL CAPACITOR",
            starAmount = 500L,
            priceFormatted = "$0.99",
            priceMicros = 990000L,
            tier = PackTier.ENTRY,
            iconRes = R.drawable.ic_store_data_drop,
            opticalScale = 0.95f
        ),
        StoreProduct(
            id = "pack_stars_1200",
            title = "RECHARGE",
            subtitle = "DUAL CELL ARRAY",
            starAmount = 1200L,
            bonusStarAmount = 150L,
            priceFormatted = "$1.99",
            priceMicros = 1990000L,
            tier = PackTier.TACTICAL,
            iconRes = R.drawable.ic_store_recharge,
            opticalScale = 1.20f
        ),
        StoreProduct(
            id = "pack_stars_6000",
            title = "CAPITAL SURGE",
            subtitle = "SUBCRITICAL CORE",
            starAmount = 6000L,
            bonusStarAmount = 1200L,
            priceFormatted = "$9.99",
            priceMicros = 9990000L,
            tier = PackTier.SURGE,
            iconRes = R.drawable.ic_store_capital,
            opticalScale = 1.00f
        ),
        StoreProduct(
            id = "pack_stars_15000",
            title = "QUANTUM CACHE",
            subtitle = "SUBSPACE HYPERCUBE",
            starAmount = 15000L,
            bonusStarAmount = 4500L,
            priceFormatted = "$19.99",
            priceMicros = 19990000L,
            tier = PackTier.QUANTUM,
            iconRes = R.drawable.ic_store_quantum,
            opticalScale = 1.15f
        )
    )

    val OVERLORD_APEX_PACK = StoreProduct(
        id = "pack_stars_40000",
        title = "NEURAL OVERLORD",
        subtitle = "EVENT-HORIZON CAPITAL VAULT",
        starAmount = 40000L,
        bonusStarAmount = 15000L,
        priceFormatted = "$49.99",
        priceMicros = 49990000L,
        tier = PackTier.OVERLORD,
        iconRes = R.drawable.ic_store_overlord,
        opticalScale = 1.05f
    )

    val STAR_PACKS = STANDARD_GRID_PACKS + OVERLORD_APEX_PACK
}
