package com.example.gridsurge.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.R
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.ui.CyberChamferShape
import com.example.gridsurge.ui.Screen

data class ModeSelectionSpec(
    val screen: Screen,
    val title: String,
    val category: String,
    val subtitle: String,
    val description: String,
    val accentColor: Color,
    val badgeText: String? = null,
    val isLiveEvent: Boolean = false
)

object ModeSelectionCatalog {
    val ALL_MODES = listOf(
        ModeSelectionSpec(
            screen = Screen.GAME_CLASSIC,
            title = "CLASSIC MODE",
            category = "CORE ARENA",
            subtitle = "ENDLESS SURGE RUN",
            description = "Deep spatial survival with 50/50 seed layouts & Grid Purge Bounties.",
            accentColor = Color(0xFF00E5FF)
        ),
        ModeSelectionSpec(
            screen = Screen.GAME_ADVENTURE,
            title = "ADVENTURE SECTORS",
            category = "CORE ARENA",
            subtitle = "TACTICAL SECTOR CAMPAIGN",
            description = "27 handcrafted levels, Relic CyberWare, and Sector Boss battles.",
            accentColor = Color(0xFFFFB300),
            badgeText = "SECTOR 01"
        ),
        ModeSelectionSpec(
            screen = Screen.DAILY_GLITCH,
            title = "DAILY GLITCH",
            category = "HIGH-STAKES COMPETITIVE",
            subtitle = "CORRUPTED 24H SEED",
            description = "Compete on today's global seed with glitch catalysts for leaderboard rank.",
            accentColor = Color(0xFF00E676),
            badgeText = "LIVE 24H",
            isLiveEvent = true
        ),
        ModeSelectionSpec(
            screen = Screen.TIME_BLITZ,
            title = "TIME BLITZ",
            category = "HIGH-STAKES COMPETITIVE",
            subtitle = "90S HIGH-VELOCITY SPRINT",
            description = "Rapid score sprint with 100% Fever Overdrive (2X) and Time Refunds.",
            accentColor = Color(0xFFD500F9),
            badgeText = "SPEED"
        ),
        ModeSelectionSpec(
            screen = Screen.BLITZ_CLASH,
            title = "BLITZ CLASH",
            category = "HIGH-STAKES COMPETITIVE",
            subtitle = "1V1 PVP GHOST DUEL",
            description = "75-second PvP duel against real player ghost replays with EMP Stasis Jammers.",
            accentColor = Color(0xFFFF1744),
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
            .background(Color(0xDD040812))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.78f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xF00D1526), Color(0xFE060A14))
                    )
                )
                .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.6f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .clickable { /* Block dismiss click propagation */ }
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Notch & Header Bar
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF263859))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SELECT MISSION PROTOCOL",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0x33141926))
                                .border(1.dp, Color(0xFF26334D), CircleShape)
                                .clickable { onDismiss() }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("✕ CLOSE", color = Color(0xFF78909C), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Modes List Categorized
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
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
                                            if (isSelected) spec.accentColor.copy(alpha = 0.25f) else Color(0x33101522),
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
                                    SfxManager.playSfx(SfxType.UI_CONFIRM)
                                    onSelectMode(spec)
                                }
                                .padding(16.dp)
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
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )

                                        if (spec.badgeText != null) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(spec.accentColor.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = spec.badgeText,
                                                    color = spec.accentColor,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = spec.title,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black
                                    )

                                    Text(
                                        text = spec.description,
                                        color = Color(0xFF90A4AE),
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(spec.accentColor)
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "SELECTED ✓",
                                            color = Color(0xFF040812),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
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
