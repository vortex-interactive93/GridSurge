package com.example.gridsurge.ui.dialogs

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.ui.CyberChamferShape
import com.example.gridsurge.ui.Screen
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily

import androidx.annotation.DrawableRes
import com.example.gridsurge.R

data class ModeSelectionSpec(
    val screen: Screen,
    val title: String,
    val category: String,
    val subtitle: String,
    val description: String,
    val accentColor: Color,
    @DrawableRes val watermarkRes: Int,
    val badgeText: String? = null,
    val isLiveEvent: Boolean = false
)

object ModeSelectionCatalog {
    val ALL_MODES = listOf(
        ModeSelectionSpec(
            screen = Screen.GAME_CLASSIC,
            title = "CLASSIC SURGE",
            category = "CORE ARENA",
            subtitle = "ENDLESS SPATIAL SURVIVAL",
            description = "Spatial survival with 50/50 seed layouts & high-voltage bounty clears.",
            accentColor = Color(0xFF00E5FF),
            watermarkRes = R.drawable.ic_watermark_classic
        ),
        ModeSelectionSpec(
            screen = Screen.GAME_ADVENTURE,
            title = "SECTOR CAMPAIGN",
            category = "CORE ARENA",
            subtitle = "TACTICAL PROGRESSION",
            description = "27 handcrafted sectors, Neural Relics, and Sector Overlord boss encounters.",
            accentColor = Color(0xFFFFB300),
            watermarkRes = R.drawable.ic_watermark_campaign,
            badgeText = "SECTOR 01"
        ),
        ModeSelectionSpec(
            screen = Screen.DAILY_GLITCH,
            title = "DAILY GLITCH",
            category = "HIGH-STAKES COMPETITIVE",
            subtitle = "CORRUPTED 24H SEED",
            description = "Global competitive seed with volatile anomalies for international leaderboards.",
            accentColor = Color(0xFF00FF66),
            watermarkRes = R.drawable.ic_watermark_glitch,
            badgeText = "LIVE 24H",
            isLiveEvent = true
        ),
        ModeSelectionSpec(
            screen = Screen.TIME_BLITZ,
            title = "TIME BLITZ",
            category = "HIGH-STAKES COMPETITIVE",
            subtitle = "90-SECOND SPRINT",
            description = "Velocity score rush with 100% Fever Overdrive (2X) and Time Surge refunds.",
            accentColor = Color(0xFFD500F9),
            watermarkRes = R.drawable.ic_watermark_blitz,
            badgeText = "SPEED"
        ),
        ModeSelectionSpec(
            screen = Screen.BLITZ_CLASH,
            title = "BLITZ CLASH",
            category = "HIGH-STAKES COMPETITIVE",
            subtitle = "1V1 GHOST DUEL",
            description = "75-second asynchronous PvP match against operative ghost replays with EMP jammers.",
            accentColor = Color(0xFFFF1744),
            watermarkRes = R.drawable.ic_watermark_clash,
            badgeText = "PVP 1V1"
        )
    )
}

@Composable
fun ModeSelectionDrawer(
    selectedScreen: Screen,
    onSelectMode: (ModeSelectionSpec) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD9040710))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.76f)
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xF50D1526), Color(0xFD060A14))
                    )
                )
                .border(
                    1.5.dp,
                    Color(0xFF00E5FF).copy(alpha = 0.5f),
                    RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
                )
                .clickable(enabled = false) {}
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tactical Drag Handle & Header
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF1E2D44))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SELECT MISSION PROTOCOL",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x22101826))
                                .border(1.dp, Color(0xFF1E2D44), RoundedCornerShape(6.dp))
                                .clickable { onDismiss() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "✕ CLOSE",
                                color = Color(0xFF78909C),
                                fontSize = 9.sp,
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Mode Cards List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(ModeSelectionCatalog.ALL_MODES, key = { it.title }) { spec ->
                        val isSelected = spec.screen == selectedScreen

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CyberChamferShape)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            if (isSelected) spec.accentColor.copy(alpha = 0.22f) else Color(0x33101522),
                                            Color(0xDD0D111A)
                                        )
                                    )
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) spec.accentColor else spec.accentColor.copy(alpha = 0.3f),
                                    shape = CyberChamferShape
                                )
                                .clickable {
                                    SfxManager.playSfx(SfxType.MODE_LOCK_IN, volume = 0.95f, pitchRate = 1.05f)
                                    onSelectMode(spec)
                                }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = spec.category,
                                            color = spec.accentColor,
                                            fontSize = 9.sp,
                                            fontFamily = ChakraPetchFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.8.sp
                                        )

                                        if (spec.badgeText != null) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(spec.accentColor.copy(alpha = 0.2f))
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = spec.badgeText,
                                                    color = spec.accentColor,
                                                    fontSize = 8.sp,
                                                    fontFamily = ChakraPetchFontFamily,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = spec.title,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontFamily = OrbitronFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )

                                    Text(
                                        text = spec.description,
                                        color = Color(0xFF8FA3BF),
                                        fontSize = 11.sp,
                                        fontFamily = ChakraPetchFontFamily,
                                        lineHeight = 14.sp
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(spec.accentColor)
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE ✓",
                                            color = Color(0xFF040812),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = ChakraPetchFontFamily
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}