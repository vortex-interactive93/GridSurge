package com.example.gridsurge.features.adventure.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.ui.CyberActionButton
import com.example.gridsurge.R
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.features.adventure.model.AugmentRarity
import com.example.gridsurge.features.adventure.model.AugmentType
import com.example.gridsurge.features.adventure.model.NeuralAugment
import com.example.gridsurge.ui.CyberChamferShape

data class SkillIntelDetail(
    val augment: NeuralAugment,
    val tacticalGuideText: String
)

object SkillIntelRegistry {
    val ALL_SKILLS = listOf(
        SkillIntelDetail(
            augment = NeuralAugment(
                id = "aug_buffer",
                type = AugmentType.BUFFER_OPTIMIZER,
                title = "BUFFER OPTIMIZER",
                description = "+2 Combo Grace Moves (Buffer pips never decay on 1st miss).",
                rarity = AugmentRarity.COMMON,
                iconRes = R.drawable.ic_aug_buffer_optimizer,
                minSectorRequired = 1
            ),
            tacticalGuideText = "Gives you 2 extra grace pips so your combo streak won't break when you drop a block without clearing a line. Keeps streak multipliers active longer!"
        ),
        SkillIntelDetail(
            augment = NeuralAugment(
                id = "aug_cavity",
                type = AugmentType.CAVITY_COMPRESSOR,
                title = "CAVITY COMPRESSOR",
                description = "Spawns smaller pieces when the board is getting full.",
                rarity = AugmentRarity.COMMON,
                iconRes = R.drawable.ic_aug_cavity_compressor,
                minSectorRequired = 1
            ),
            tacticalGuideText = "When your board is 70%+ full, the game stops giving you giant 5-block pieces and gives you small 1x1, 2x1, and L-shapes so you can easily fill tight gaps and avoid Game Over!"
        ),
        SkillIntelDetail(
            augment = NeuralAugment(
                id = "aug_molten",
                type = AugmentType.MOLTEN_HARVEST,
                title = "MOLTEN HARVEST",
                description = "Multi-line clears grant +2.5x score multiplier.",
                rarity = AugmentRarity.RARE,
                iconRes = R.drawable.ic_aug_molten_harvest,
                minSectorRequired = 1
            ),
            tacticalGuideText = "Clearing 2, 3, or 4 lines simultaneously multiplies all points earned on that move by 2.5x. Essential for hitting 3-star high score targets!"
        ),
        SkillIntelDetail(
            augment = NeuralAugment(
                id = "aug_kinetic",
                type = AugmentType.KINETIC_BURST,
                title = "KINETIC BURST",
                description = "Clearing 2+ lines deals 1 direct damage to all active cores.",
                rarity = AugmentRarity.RARE,
                iconRes = R.drawable.ic_aug_chrono_siphon,
                minSectorRequired = 1
            ),
            tacticalGuideText = "Executing a double or triple line clear sends a shockwave across the grid, automatically damaging every active Core Reactor by 1 hit point regardless of location."
        ),
        SkillIntelDetail(
            augment = NeuralAugment(
                id = "aug_cardinal",
                type = AugmentType.CARDINAL_OVERCLOCK,
                title = "CARDINAL OVERCLOCK",
                description = "Line clears fire secondary perpendicular lasers across the board.",
                rarity = AugmentRarity.LEGENDARY,
                iconRes = R.drawable.ic_aug_cardinal_overclock,
                minSectorRequired = 1
            ),
            tacticalGuideText = "Clearing horizontal rows fires vertical lasers down center columns (3 & 4), and clearing vertical columns fires horizontal lasers across center rows (3 & 4)."
        ),
        SkillIntelDetail(
            augment = NeuralAugment(
                id = "aug_warp",
                type = AugmentType.WARP_INJECTOR,
                title = "WARP INJECTOR",
                description = "+50% Phase Resonance energy charge rate on line clears.",
                rarity = AugmentRarity.LEGENDARY,
                iconRes = R.drawable.ic_aug_warp_injector,
                minSectorRequired = 1
            ),
            tacticalGuideText = "Fills your Phase Resonance meter 50% faster on every line clear, allowing you to charge and deploy the 3x3 Nova Core Supercharged ability twice as often!"
        ),
        SkillIntelDetail(
            augment = NeuralAugment(
                id = "aug_corrosion",
                type = AugmentType.CORROSION_SHIELD,
                title = "CORROSION SHIELD",
                description = "Hazard slag and toxic slime cannot spread to adjacent cells.",
                rarity = AugmentRarity.LEGENDARY,
                iconRes = R.drawable.ic_aug_corrosion_shield,
                minSectorRequired = 3
            ),
            tacticalGuideText = "Provides complete immunity to hazard slag and toxic bio-slime spreading in Sectors 2, 4, and Boss stages, locking hazards strictly in place."
        )
    )
}

@Composable
fun NeuralCyberwareIntelDialog(
    activeAugmentIds: Set<String> = emptySet(),
    onDismiss: () -> Unit,
    onResetSectorRun: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
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
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .background(
                        brush = Brush.verticalGradient(listOf(Color(0xFF0A1322), Color(0xFF030712))),
                        shape = CyberChamferShape
                    )
                    .border(1.5.dp, Color(0xFF00E5FF), CyberChamferShape)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SYSTEM SKILLS INTEL",
                            color = Color(0xFF00E5FF),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "NEURAL CYBERWARE MANUAL",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x33FF0055))
                            .border(1.dp, Color(0xFFFF0055), RoundedCornerShape(6.dp))
                            .clickable {
                                SfxManager.playSfx(SfxType.MODAL_WHOOSH)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = "Draft 1 of 3 System Augments after completing Stage 3 and Stage 6 of any Sector run.",
                    color = Color(0xFF5C8599),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                // Scrollable Cards List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(SkillIntelRegistry.ALL_SKILLS) { item ->
                        val augment = item.augment
                        val cardBorderColor = Color(augment.rarity.colorHex)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF07111E))
                                .border(1.2.dp, cardBorderColor.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(Color(0xFF030812), RoundedCornerShape(6.dp))
                                            .border(1.dp, cardBorderColor, RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = augment.iconRes),
                                            contentDescription = augment.title,
                                            modifier = Modifier.size(36.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        val isInstalled = augment.id in activeAugmentIds || activeAugmentIds.any { it.contains(augment.id, ignoreCase = true) }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f, fill = false),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = augment.title,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Black,
                                                    maxLines = 1
                                                )
                                                if (isInstalled) {
                                                    Text(
                                                        text = "⚡ ACTIVE IN RUN",
                                                        color = Color(0xFF00FF66),
                                                        fontSize = 8.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = augment.rarity.name,
                                                color = cardBorderColor,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = augment.description,
                                            color = Color(0xFF00FF66),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 12.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Detailed Tactical Guide Box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0x3300E5FF), RoundedCornerShape(4.dp))
                                        .border(1.dp, Color(0x6600E5FF), RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "💡 TACTICAL INTEL: ${item.tacticalGuideText}",
                                        color = Color(0xFFB0BEC5),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                if (onResetSectorRun != null && activeAugmentIds.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    CyberActionButton(
                        text = "RESET RUN & AUGMENTS ↺",
                        primaryColor = Color(0xFFFF0055),
                        isPrimary = true,
                        onClick = {
                            SfxManager.playSfx(SfxType.MODAL_WHOOSH)
                            onResetSectorRun()
                        }
                    )
                }
            }
        }
    }
}
