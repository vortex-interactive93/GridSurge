package com.example.gridsurge.ui.store

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.R
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.ui.components.StarVaultPill
import com.example.gridsurge.audio.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.example.gridsurge.billing.BillingManager

@Composable
fun CyberStoreScreen(
    profileManager: PlayerProfileManager,
    onBack: () -> Unit
) {
    val stars by profileManager.starCurrency.collectAsState()
    val isNoAdsPurchased by profileManager.isNoAdsPurchased.collectAsState()
    
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        BgmManager.playTrack(context, BgmTrack.ARMORY_VAULT)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0C14))) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.bg_main_hub),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(Color(0xCC0A0C14)))

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = {
                    SfxManager.playSfx(SfxType.UI_CONFIRM)
                    onBack()
                }) {
                    Text("< BACK", color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                Text("STAR VAULT", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                
                StarVaultPill(stars = stars, onClick = {})
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Hero Card: No-Ads Protocol Bundle
                item {
                    val activity = context as? Activity
                    StoreBundleCard(
                        imageRes = R.drawable.card_store_no_ads_bundle,
                        title = "NO-ADS PROTOCOL BUNDLE",
                        content = "PERMANENT NO-ADS + 1,000 ★ + 5 REVIVES",
                        price = if (isNoAdsPurchased) "ACTIVE ✓" else "$3.99",
                        isPurchased = isNoAdsPurchased,
                        imageSize = 130.dp,
                        onPurchase = {
                            if (!isNoAdsPurchased) {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                if (activity != null) {
                                    BillingManager.launchPurchaseFlow(activity, BillingManager.SKU_NO_ADS_BUNDLE)
                                } else {
                                    profileManager.purchaseNoAdsBundle()
                                }
                            }
                        }
                    )
                }

                item {
                    val activity = context as? Activity
                    StoreBundleCard(
                        imageRes = R.drawable.card_store_starter_bundle,
                        title = "STARTER CRATE",
                        content = "★ 500 STARS + 3 REVIVES",
                        price = "$0.99",
                        imageSize = 110.dp,
                        onPurchase = {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            if (activity != null) {
                                BillingManager.launchPurchaseFlow(activity, BillingManager.SKU_STARTER_CRATE)
                            } else {
                                profileManager.addStarCurrency(500)
                            }
                        }
                    )
                }

                item {
                    val activity = context as? Activity
                    StoreBundleCard(
                        imageRes = R.drawable.card_store_hyper_bundle,
                        title = "HYPER VAULT CHEST",
                        content = "★ 1,500 STARS + VOIDBORN UNLOCK",
                        price = "$4.99",
                        imageSize = 120.dp,
                        onPurchase = {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            if (activity != null) {
                                BillingManager.launchPurchaseFlow(activity, BillingManager.SKU_HYPER_VAULT)
                            } else {
                                profileManager.addStarCurrency(1500)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StoreBundleCard(
    imageRes: Int,
    title: String,
    content: String,
    price: String,
    imageSize: Dp,
    isPurchased: Boolean = false,
    onPurchase: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(0.92f),
        color = Color(0xFF1E2230).copy(alpha = 0.85f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = if (isPurchased) 2.dp else 1.dp,
            color = if (isPurchased) Color(0xFF00E676) else Color(0xFF00E5FF).copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.size(imageSize),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp, textAlign = TextAlign.Center)
            Text(content, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
            
            Spacer(modifier = Modifier.height(18.dp))
            
            Button(
                onClick = onPurchase,
                enabled = !isPurchased,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPurchased) Color(0x3300E676) else Color(0xFF00E5FF),
                    disabledContainerColor = Color(0x3300E676)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = price,
                    color = if (isPurchased) Color(0xFF00E676) else Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }
    }
}
