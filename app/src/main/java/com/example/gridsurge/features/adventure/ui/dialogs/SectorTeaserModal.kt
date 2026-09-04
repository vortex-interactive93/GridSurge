package com.example.gridsurge.features.adventure.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.ui.CyberActionButton
import com.example.gridsurge.ui.CyberChamferShape

/**
 * High-Tech Classified Transmission Teaser Modal for Soft Launch Content Cap.
 * Displays when a player attempts to access Sectors 4 & 5.
 */
@Composable
fun SectorTeaserModal(
    sectorNumber: Int,
    totalStarsCollected: Int,
    relicsClaimedCount: Int,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true, usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .background(
                        brush = Brush.verticalGradient(listOf(Color(0xFF0D0612), Color(0xFF030208))),
                        shape = CyberChamferShape
                    )
                    .border(1.5.dp, Color(0xFFFF0055), CyberChamferShape)
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Warning Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x33FF0055))
                        .border(1.dp, Color(0xFFFF0055), RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⚠️ TRANSMISSION INTERCEPTED // SECTOR 0$sectorNumber",
                        color = Color(0xFFFF0055),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "QUANTUM CIPHER MATRIX ENCRYPTED",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                // Sci-Fi Narrative Teaser Body
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CyberChamferShape)
                        .background(Color(0xFF0A0512))
                        .border(1.dp, Color(0x66FF0055), CyberChamferShape)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "OPERATIVE: High Command is currently decrypting Sector 04 & 05's Quantum Matrix.\n\nExotic Bio-Slag, Quantum Ciphers, and Apex Overlord Bosses await in the upcoming v1.1 GridSurge Expansion.\n\nMaster your sector gauntlets and max out your 3-star ratings while decryption completes!",
                        color = Color(0xFFE0E6ED),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }

                // Player Stats Snapshot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL STARS", color = Color(0xFF5C8599), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("$totalStarsCollected ★", color = Color(0xFFFFD600), fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }

                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF1B2A42)))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("RELICS UNLOCKED", color = Color(0xFF5C8599), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("$relicsClaimedCount / 3", color = Color(0xFF00E5FF), fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                CyberActionButton(
                    text = "RETURN TO SECTOR MAP ►",
                    primaryColor = Color(0xFF00E5FF),
                    isPrimary = true,
                    onClick = {
                        SfxManager.playSfx(SfxType.UI_CONFIRM)
                        onDismiss()
                    }
                )
            }
        }
    }
}
