package com.example.gridsurge.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.honors.data.AccoladeCatalog
import com.example.gridsurge.honors.model.HonorCategory
import com.example.gridsurge.honors.model.PlayerPrestigeProfile
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.ui.Screen
import com.example.gridsurge.ui.components.StarVaultPill
import com.example.gridsurge.ui.honors.components.AccoladeMilestoneCard
import com.example.gridsurge.ui.honors.components.OperativeCallingCard
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily

@Composable
fun OperativeHonorsScreen(
    profileManager: PlayerProfileManager,
    onNavigate: (Screen) -> Unit
) {
    val stars by profileManager.starCurrency.collectAsState()
    val callsign by profileManager.callsign.collectAsState()

    var profile by remember(callsign) {
        mutableStateOf(PlayerPrestigeProfile(callsign = callsign))
    }

    var accolades by remember { mutableStateOf(AccoladeCatalog.OPERATIVE_HONORS) }
    var selectedCategory by remember { mutableStateOf(HonorCategory.BLOCK_DEMOLITION) }

    val equippedBadge = accolades.firstOrNull { it.id == profile.equippedBadgeId }
        ?: accolades.first()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040711))
    ) {
        // Perspective Tactical Background Wireframe
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val lines = 16
            for (i in 0..lines) {
                val y = (h / lines) * i
                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = 0.025f),
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
            // ==================== 1. TOP TELEMETRY ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
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
                            onNavigate(Screen.MAIN_MENU)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF00E5FF),
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
                        text = "OPERATIVE HONORS",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "CALLING CARDS & PRESTIGE EMBLEMS",
                        color = Color(0xFF00E5FF),
                        fontSize = 8.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                StarVaultPill(stars = stars, onClick = { onNavigate(Screen.STORE) })
            }

            // ==================== 2. HONORS FEED ====================
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Active Equipped Calling Card
                item {
                    OperativeCallingCard(
                        profile = profile,
                        equippedBadge = equippedBadge
                    )
                }

                // Category Filter Dock
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC0A101C))
                            .border(1.dp, Color(0xFF1B2A40), RoundedCornerShape(8.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        HonorCategory.entries.forEach { category ->
                            val isSelected = selectedCategory == category
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.16f) else Color.Transparent
                                    )
                                    .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color = if (isSelected) Color(0xFF00E5FF) else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
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
                                    text = when (category) {
                                        HonorCategory.BLOCK_DEMOLITION -> "DEMOLITION"
                                        HonorCategory.SECTOR_CAMPAIGN -> "CAMPAIGN"
                                        HonorCategory.LEADERBOARD_RANK -> "RANK"
                                        HonorCategory.TACTICAL_MASTERY -> "MASTERY"
                                    },
                                    color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF8FA3BF),
                                    fontSize = 9.sp,
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Accolade Cards
                val filteredBadges = accolades.filter { it.category == selectedCategory }
                items(filteredBadges, key = { it.id }) { badge ->
                    AccoladeMilestoneCard(
                        badge = badge,
                        isEquipped = profile.equippedBadgeId == badge.id,
                        onEquip = {
                            SfxManager.playSfx(SfxType.MODE_LOCK_IN)
                            profile = profile.copy(
                                equippedBadgeId = badge.id,
                                equippedBannerId = badge.id
                            )
                        }
                    )
                }
            }
        }
    }
}
