package com.example.gridsurge.ui.store

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.audio.BgmManager
import com.example.gridsurge.audio.BgmTrack
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.store.data.StoreCatalog
import com.example.gridsurge.store.model.StoreCategory
import com.example.gridsurge.store.model.StoreProduct
import com.example.gridsurge.ui.Screen
import com.example.gridsurge.ui.components.StarVaultPill
import com.example.gridsurge.ui.modifiers.cyberBorderGlow
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CyberStoreScreen(
    profileManager: PlayerProfileManager,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val stars by profileManager.starCurrency.collectAsState()
    val isNoAdsPurchased by profileManager.isNoAdsPurchased.collectAsState()

    var selectedCategory by remember { mutableStateOf(StoreCategory.FEATURED) }
    var isFreeRationClaimed by remember { mutableStateOf(false) }

    // Split: 4 standard grid packs, 1 Overlord apex pack
    val standardGridPacks = remember { StoreCatalog.STANDARD_GRID_PACKS }
    val overlordPack = remember { StoreCatalog.OVERLORD_APEX_PACK }

    LaunchedEffect(Unit) {
        BgmManager.playTrack(context, BgmTrack.ARMORY_VAULT)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040711))
    ) {
        // Perspective Ambient Commerce Grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val lines = 14
            for (i in 0..lines) {
                val y = (h / lines) * i
                drawLine(
                    color = Color(0xFFFFB300).copy(alpha = 0.035f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp)
        ) {
            // ==================== 1. TOP COMMAND TELEMETRY ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC0D1424))
                        .border(1.dp, Color(0xFF1E2D44), RoundedCornerShape(8.dp))
                        .clickable {
                            SfxManager.playSfx(SfxType.UI_BACK)
                            onBack()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "HUB",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "STAR VAULT",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "NEURAL EXCHANGE & CAPITAL RESERVES",
                        color = Color(0xFFFFB300),
                        fontSize = 8.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                StarVaultPill(stars = stars, onClick = {})
            }

            // ==================== 2. FIXED CATEGORY SELECTOR ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xCC0A101C))
                    .border(1.dp, Color(0xFF1B2A40), RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StoreCategory.entries.forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) Color(0xFFFFB300).copy(alpha = 0.16f) else Color.Transparent
                            )
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) Color(0xFFFFB300) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (!isSelected) {
                                    SfxManager.playSfx(SfxType.CAROUSEL_SNAP)
                                    selectedCategory = category
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.title,
                            color = if (isSelected) Color(0xFFFFB300) else Color(0xFF8FA3BF),
                            fontSize = 10.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
            }

            // ==================== 3. COMMERCE STREAM (SCROLLABLE) ====================
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 2.dp, bottom = 16.dp)
            ) {
                when (selectedCategory) {
                    StoreCategory.FEATURED -> {
                        item {
                            FreeDailyRationCard(
                                isClaimed = isFreeRationClaimed,
                                onClaim = {
                                    SfxManager.playSfx(SfxType.LEVEL_COMPLETE)
                                    coroutineScope.launch {
                                        profileManager.addStars(50)
                                        isFreeRationClaimed = true
                                    }
                                }
                            )
                        }

                        if (!isNoAdsPurchased) {
                            item {
                                HeroBundleCard(
                                    product = StoreCatalog.HERO_BUNDLE,
                                    onPurchase = {
                                        SfxManager.playSfx(SfxType.LEVEL_COMPLETE)
                                        coroutineScope.launch {
                                            profileManager.addStars(StoreCatalog.HERO_BUNDLE.starAmount + StoreCatalog.HERO_BUNDLE.bonusStarAmount)
                                            profileManager.setNoAdsPurchased(true)
                                        }
                                    }
                                )
                            }
                        }

                        val chunkedStandard = standardGridPacks.chunked(2)
                        items(chunkedStandard) { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pair.forEach { pack ->
                                    CurrencyPackCard(
                                        product = pack,
                                        modifier = Modifier.weight(1f),
                                        onPurchase = {
                                            SfxManager.playSfx(SfxType.LEVEL_COMPLETE)
                                            coroutineScope.launch {
                                                profileManager.addStars(pack.starAmount + pack.bonusStarAmount)
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        item {
                            OverlordApexCard(
                                product = overlordPack,
                                onPurchase = {
                                    SfxManager.playSfx(SfxType.LEVEL_COMPLETE)
                                    coroutineScope.launch {
                                        profileManager.addStars(overlordPack.starAmount + overlordPack.bonusStarAmount)
                                    }
                                }
                            )
                        }
                    }

                    StoreCategory.STAR_PACKS -> {
                        item {
                            FreeDailyRationCard(
                                isClaimed = isFreeRationClaimed,
                                onClaim = {
                                    SfxManager.playSfx(SfxType.LEVEL_COMPLETE)
                                    coroutineScope.launch {
                                        profileManager.addStars(50)
                                        isFreeRationClaimed = true
                                    }
                                }
                            )
                        }

                        val chunkedStandard = standardGridPacks.chunked(2)
                        items(chunkedStandard) { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pair.forEach { pack ->
                                    CurrencyPackCard(
                                        product = pack,
                                        modifier = Modifier.weight(1f),
                                        onPurchase = {
                                            SfxManager.playSfx(SfxType.LEVEL_COMPLETE)
                                            coroutineScope.launch {
                                                profileManager.addStars(pack.starAmount + pack.bonusStarAmount)
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        item {
                            OverlordApexCard(
                                product = overlordPack,
                                onPurchase = {
                                    SfxManager.playSfx(SfxType.LEVEL_COMPLETE)
                                    coroutineScope.launch {
                                        profileManager.addStars(overlordPack.starAmount + overlordPack.bonusStarAmount)
                                    }
                                }
                            )
                        }
                    }

                    StoreCategory.VIP_ACCESS -> {
                        if (!isNoAdsPurchased) {
                            item {
                                HeroBundleCard(
                                    product = StoreCatalog.HERO_BUNDLE,
                                    onPurchase = {
                                        SfxManager.playSfx(SfxType.LEVEL_COMPLETE)
                                        coroutineScope.launch {
                                            profileManager.addStars(StoreCatalog.HERO_BUNDLE.starAmount + StoreCatalog.HERO_BUNDLE.bonusStarAmount)
                                            profileManager.setNoAdsPurchased(true)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // RESTORE & LEGAL FOOTER
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "RESTORE PURCHASES",
                            color = Color(0xFF00E5FF),
                            fontSize = 10.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { SfxManager.playSfx(SfxType.UI_CONFIRM) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        Text(
                            text = "Neural transactions are processed securely via Google Play Billing.\nAll star acquisitions are immediate and persistent across neural syncs.",
                            color = Color(0xFF556980),
                            fontSize = 8.sp,
                            fontFamily = ChakraPetchFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Currency Pack Card with Optical Scaling & Dynamic Text Contrast.
 */
@Composable
private fun CurrencyPackCard(
    product: StoreProduct,
    modifier: Modifier = Modifier,
    onPurchase: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "packScale"
    )

    Box(
        modifier = modifier
            .height(188.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xCC090F1B))
            .border(1.dp, product.tier.accentColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onPurchase() }
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.tier.tierLabel,
                    color = product.tier.accentColor,
                    fontSize = 8.sp,
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Black
                )

                if (product.bonusStarAmount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(product.tier.accentColor.copy(alpha = 0.2f))
                            .border(0.5.dp, product.tier.accentColor, RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "+${product.tier.bonusPercent}%",
                            color = product.tier.accentColor,
                            fontSize = 7.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Asset Presenter with Optical Scale Normalization
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = product.iconRes),
                    contentDescription = product.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = product.opticalScale
                            scaleY = product.opticalScale
                            blendMode = BlendMode.Screen
                        },
                    contentScale = ContentScale.Fit
                )
            }

            // Star Counter
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = "${product.starAmount} ★",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black
                )
                if (product.bonusStarAmount > 0) {
                    Text(
                        text = "+${product.bonusStarAmount} BONUS",
                        color = product.tier.accentColor,
                        fontSize = 8.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    // Keeps vertical baseline identical across both cards in the row
                    Text(
                        text = "STANDARD YIELD",
                        color = Color(0xFF4A5B73),
                        fontSize = 8.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Purchase Button with Tier-Specific Text Luminance
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(product.tier.accentColor)
                    .border(0.8.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.priceFormatted,
                    color = product.tier.buttonTextColor,
                    fontSize = 11.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

/**
 * Free Daily Ration Card.
 */
@Composable
private fun FreeDailyRationCard(
    isClaimed: Boolean,
    onClaim: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xCC09121E))
            .border(
                1.dp,
                if (isClaimed) Color(0xFF1B2A3D) else Color(0xFF00FF66).copy(alpha = 0.8f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isClaimed) Color(0xFF14202E) else Color(0x3300FF66))
                        .border(1.dp, if (isClaimed) Color(0xFF24364D) else Color(0xFF00FF66), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isClaimed) Icons.Default.Check else Icons.Default.Star,
                        contentDescription = null,
                        tint = if (isClaimed) Color(0xFF8FA3BF) else Color(0xFF00FF66),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "DAILY RATION // 50 ★",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        if (!isClaimed) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x3300FF66))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "FREE",
                                    color = Color(0xFF00FF66),
                                    fontSize = 8.sp,
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                    Text(
                        text = if (isClaimed) "RECHARGING // NEXT DROP AT 00:00 UTC" else "OPERATIVE SUBSIDY READY TO CLAIM",
                        color = Color(0xFF8FA3BF),
                        fontSize = 9.sp,
                        fontFamily = ChakraPetchFontFamily
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isClaimed) Color(0xFF14202E) else Color(0xFF00FF66))
                    .clickable(enabled = !isClaimed) { onClaim() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isClaimed) "CLAIMED" else "CLAIM",
                    color = if (isClaimed) Color(0xFF556980) else Color(0xFF040711),
                    fontSize = 11.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

/**
 * Hero Bundle Offer.
 */
@Composable
private fun HeroBundleCard(
    product: StoreProduct,
    onPurchase: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "heroGleam")
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .cyberBorderGlow(
                colors = listOf(Color(0xFFFFB300), Color(0xFFFF1744)),
                cornerRadius = 14.dp
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xF21C1408), Color(0xFD0A0B14))
                )
            )
            .border(1.5.dp, Color(0xFFFFB300), RoundedCornerShape(14.dp))
            .clickable { onPurchase() }
            .padding(14.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val sheenX = w * sheenProgress
            val sheenWidth = w * 0.35f

            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    start = Offset(sheenX, 0f),
                    end = Offset(sheenX + sheenWidth, h)
                ),
                size = Size(w, h)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFF1744))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ONE-TIME OFFER",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFFB300).copy(alpha = 0.2f))
                            .border(0.5.dp, Color(0xFFFFB300), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SAVE 65%",
                            color = Color(0xFFFFB300),
                            fontSize = 8.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = product.priceFormatted,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black
                )
            }

            Column {
                Text(
                    text = product.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = product.subtitle,
                    color = Color(0xFFFFB300),
                    fontSize = 10.sp,
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            Text(
                text = "Permanently disables interstitial ads across all modes and provides an immediate capital injection of 2,500 Stars + 1,000 Bonus Stars.",
                color = Color(0xFF8FA3BF),
                fontSize = 9.sp,
                fontFamily = ChakraPetchFontFamily,
                lineHeight = 12.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFFB300), Color(0xFFFF8F00))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▶ AUTHORIZE PURCHASE // ${product.priceFormatted}",
                    color = Color(0xFF040711),
                    fontSize = 11.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

/**
 * Full-Width Overlord Apex Anchor Card.
 */
@Composable
private fun OverlordApexCard(
    product: StoreProduct,
    onPurchase: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "overlordScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "overlordFx")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val laserPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "laserPhase"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .cyberBorderGlow(
                colors = listOf(Color(0xFFFF1744), Color(0xFFFFB300)),
                cornerRadius = 14.dp
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xF21C0A10), Color(0xFD090F1B))
                )
            )
            .border(1.5.dp, Color(0xFFFF1744).copy(alpha = 0.85f), RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onPurchase() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xDD080B14))
                    .border(1.dp, Color(0xFFFF1744).copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = size.width * 0.65f

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF1744).copy(alpha = pulseAlpha),
                                Color(0xFFFFB300).copy(alpha = pulseAlpha * 0.4f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = r
                        ),
                        radius = r,
                        center = Offset(cx, cy)
                    )

                    for (i in 0..3) {
                        val angle = (i * 1.5708f) + (sin(laserPhase + i) * 0.12f)
                        val endX = cx + (r * 1.2f) * cos(angle)
                        val endY = cy + (r * 1.2f) * sin(angle)
                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFFFB300).copy(alpha = 0.6f), Color.Transparent),
                                start = Offset(cx, cy),
                                end = Offset(endX, endY)
                            ),
                            start = Offset(cx, cy),
                            end = Offset(endX, endY),
                            strokeWidth = 3f
                        )
                    }
                }

                Image(
                    painter = painterResource(id = product.iconRes),
                    contentDescription = product.title,
                    modifier = Modifier
                        .size(90.dp)
                        .graphicsLayer {
                            scaleX = product.opticalScale
                            scaleY = product.opticalScale
                            blendMode = BlendMode.Screen
                        },
                    contentScale = ContentScale.Fit
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFF1744).copy(alpha = 0.2f))
                            .border(0.5.dp, Color(0xFFFF1744), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "MAX VALUE // +65% BONUS",
                            color = Color(0xFFFF1744),
                            fontSize = 8.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Text(
                    text = product.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${product.starAmount} ★",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "+${product.bonusStarAmount} BONUS",
                        color = Color(0xFFFF1744),
                        fontSize = 9.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFF1744), Color(0xFFFF5252))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶ ACQUIRE // ${product.priceFormatted}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
