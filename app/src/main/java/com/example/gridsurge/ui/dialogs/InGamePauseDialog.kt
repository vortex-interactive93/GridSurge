package com.example.gridsurge.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.features.adventure.model.NeuralAugment
import com.example.gridsurge.features.adventure.model.RelicAbilityType
import com.example.gridsurge.ui.CyberActionButton
import com.example.gridsurge.ui.CyberChamferShape

@Composable
fun InGamePauseDialog(
    onResumeClicked: () -> Unit,
    onRestartClicked: () -> Unit,
    onResetSectorRunClicked: (() -> Unit)? = null,
    onSettingsClicked: () -> Unit,
    onQuitClicked: () -> Unit,
    stageTitle: String? = null,
    stageDirective: String? = null,
    benchmarkInfo: String? = null,
    masteryFeatInfo: String? = null,
    activeAugments: List<NeuralAugment> = emptyList(),
    activeRelicAbility: RelicAbilityType? = null
) {
    var showResetConfirmation by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onResumeClicked,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xD903060C)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .background(Color(0xF0080D1A), CyberChamferShape)
                    .border(1.5.dp, Color(0xFF00E5FF), CyberChamferShape)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "SYSTEM PAUSED",
                    color = Color(0xFF00E5FF),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )

                // Stage Objective & 3-Star Goals Card (Adventure Mode)
                if (!stageTitle.isNullOrEmpty() && !stageDirective.isNullOrEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF071220), CyberChamferShape)
                            .border(1.dp, Color(0x6600E5FF), CyberChamferShape)
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DIRECTIVE: $stageTitle",
                            color = Color(0xFF00E5FF),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = stageDirective,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(text = "★1: Complete Primary Objective", color = Color(0xFFFFD600), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            if (!benchmarkInfo.isNullOrEmpty()) {
                                Text(text = "★2: Efficiency ($benchmarkInfo)", color = Color(0xFFFFD600), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                            if (!masteryFeatInfo.isNullOrEmpty()) {
                                Text(text = "★3: Mastery ($masteryFeatInfo)", color = Color(0xFFFFD600), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                // Installed System Skills Section
                if (activeAugments.isNotEmpty() || (activeRelicAbility != null && activeRelicAbility != RelicAbilityType.NONE)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF071220), CyberChamferShape)
                            .border(1.dp, Color(0x6600E5FF), CyberChamferShape)
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "INSTALLED SYSTEM SKILLS",
                            color = Color(0xFF00E5FF),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        // Relic Active Ability
                        if (activeRelicAbility != null && activeRelicAbility != RelicAbilityType.NONE) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0A182A))
                                    .border(1.dp, Color(activeRelicAbility.colorHex), RoundedCornerShape(6.dp))
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = activeRelicAbility.iconRes),
                                    contentDescription = activeRelicAbility.title,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "RELIC: ${activeRelicAbility.title}",
                                        color = Color(activeRelicAbility.colorHex),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = activeRelicAbility.calloutText,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Drafted Run Augments
                        activeAugments.forEach { aug ->
                            val augColor = Color(aug.rarity.colorHex)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0A182A))
                                    .border(1.dp, augColor, RoundedCornerShape(6.dp))
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = aug.iconRes),
                                    contentDescription = aug.title,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = aug.title,
                                            color = augColor,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = aug.rarity.name,
                                            color = augColor,
                                            fontSize = 8.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = aug.description,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                CyberActionButton(
                    text = "RESUME",
                    primaryColor = Color(0xFF00E5FF),
                    isPrimary = true,
                    onClick = onResumeClicked
                )

                CyberActionButton(
                    text = "SETTINGS",
                    primaryColor = Color(0xFF8A99AD),
                    isPrimary = false,
                    onClick = onSettingsClicked
                )

                CyberActionButton(
                    text = "RESTART MATCH",
                    primaryColor = Color(0xFFFFD600),
                    isPrimary = false,
                    onClick = onRestartClicked
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onResetSectorRunClicked != null) {
                        Box(modifier = Modifier.weight(1f)) {
                            CyberActionButton(
                                text = "RESTART SECTOR",
                                primaryColor = Color(0xFFFF0055),
                                isPrimary = false,
                                onClick = { showResetConfirmation = true }
                            )
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        CyberActionButton(
                            text = "ABORT TO HUB",
                            primaryColor = Color(0xFFFF0055),
                            isPrimary = false,
                            onClick = onQuitClicked
                        )
                    }
                }
            }

            // Two-step verification overlay for RESTART SECTOR
            if (showResetConfirmation && onResetSectorRunClicked != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xF203060C)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(Color(0xF00F050B), CyberChamferShape)
                            .border(1.5.dp, Color(0xFFFF0055), CyberChamferShape)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "⚠️ CONFIRM RESET",
                            color = Color(0xFFFF0055),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "This will purge all installed Neural Augments & active progress, restarting from Stage 1 of this Sector.",
                            color = Color(0xFFCFD8DC),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                CyberActionButton(
                                    text = "CANCEL",
                                    primaryColor = Color(0xFF00E5FF),
                                    isPrimary = true,
                                    onClick = { showResetConfirmation = false }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                CyberActionButton(
                                    text = "PURGE & RESET",
                                    primaryColor = Color(0xFFFF0055),
                                    isPrimary = false,
                                    onClick = {
                                        showResetConfirmation = false
                                        onResetSectorRunClicked()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
