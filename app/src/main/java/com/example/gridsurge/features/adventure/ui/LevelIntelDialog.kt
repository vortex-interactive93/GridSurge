package com.example.gridsurge.features.adventure.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.R
import com.example.gridsurge.features.adventure.model.AdventureLevelBlueprint
import com.example.gridsurge.features.adventure.model.ObjectiveType
import com.example.gridsurge.features.adventure.model.SectorCoreType
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.features.adventure.data.AdventureSectorRegistry
import com.example.gridsurge.ui.CyberChamferShape

data class IntelBriefingData(
    @DrawableRes val iconRes: Int,
    val unitDesignation: String,
    val tacticalSummary: String
)

@Composable
fun LevelIntelDialog(
    blueprint: AdventureLevelBlueprint,
    record: com.example.gridsurge.features.adventure.model.LevelProgressRecord? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val stageNum = blueprint.levelNumber
    val benchmark = AdventureSectorRegistry.getBenchmark(stageNum)

    // Dynamic Intel Generation based on Stage Objective & Core Type
    val intel = resolveStageIntel(blueprint)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
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
                    .fillMaxWidth(0.94f)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF0A1322), Color(0xFF030712))
                        ),
                        shape = CyberChamferShape
                    )
                    .border(1.5.dp, Color(0xFF00E5FF), CyberChamferShape)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Sector & Stage Code
                Text(
                    text = "STAGE %02d // SEC-%02d.%d".format(stageNum, blueprint.sectorId, stageNum),
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                // Stage Name Title
                Text(
                    text = blueprint.stageName,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                // Target Intel Unit Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF07101C))
                        .border(1.dp, Color(0xFF16253B), RoundedCornerShape(6.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF030812), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = intel.iconRes),
                            contentDescription = intel.unitDesignation,
                            modifier = Modifier.size(32.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = intel.unitDesignation,
                            color = Color(0xFF00FF66),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = intel.tacticalSummary,
                            color = Color(0xFF8FA3BF),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Primary Directive Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF07101C))
                        .border(1.dp, Color(0xFF1A2A40), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "PRIMARY DIRECTIVE",
                        color = Color(0xFF5C8599),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = blueprint.directive,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tri-Directive Mastery Protocol Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF07101C))
                        .border(1.dp, Color(0xFF1A2A40), RoundedCornerShape(6.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "TRI-DIRECTIVE MASTERY PROTOCOL",
                        color = Color(0xFF5C8599),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    BenchmarkRow("★ 1", "Complete Primary: ${blueprint.objective.title}")
                    BenchmarkRow("★ 2", "Efficiency: ≤${benchmark.moveBudgetStar2} Moves or <${benchmark.timeLimitSecStar2}s")
                    BenchmarkRow("★ 3", benchmark.masteryFeat.description)
                }

                // Previous Record Stars (If Stage Cleared Previously)
                record?.let { rec ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SECTOR RECORD",
                            color = Color(0xFF5C8599),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 1..3) {
                                Text(
                                    text = if (i <= rec.starsEarned) "★" else "☆",
                                    color = if (i <= rec.starsEarned) Color(0xFFFFD700) else Color(0xFF263859),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ABORT / BACK TO MAP
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x22FF0055))
                            .border(1.dp, Color(0xFFFF0055), RoundedCornerShape(4.dp))
                            .clickable {
                                SfxManager.playSfx(SfxType.MODAL_WHOOSH)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "< ABORT",
                            color = Color(0xFFFF0055),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // ENGAGE PROTOCOL
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF00E5FF))
                            .clickable {
                                SfxManager.playSfx(SfxType.MODAL_WHOOSH)
                                onConfirm()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ENGAGE PROTOCOL",
                            color = Color(0xFF030712),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BenchmarkRow(starText: String, desc: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = starText,
            color = Color(0xFFFFD700),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black
        )
        Text(
            text = desc,
            color = Color(0xFFB0BEC5),
            fontSize = 9.5.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

private fun resolveStageIntel(blueprint: AdventureLevelBlueprint): IntelBriefingData {
    val objectiveType = blueprint.objective.type
    return when {
        blueprint.levelNumber % 9 == 0 -> {
            when (blueprint.sectorId) {
                2 -> IntelBriefingData(
                    iconRes = R.drawable.sector_2_block,
                    unitDesignation = "SOLAR COLOSSUS // APEX",
                    tacticalSummary = "Phase 1: Neutralize 4 Thermal Pylons. Phase 2: Strike the central Solar Core before thermal venting."
                )
                3 -> IntelBriefingData(
                    iconRes = R.drawable.sector_3_block_with_lock,
                    unitDesignation = "CRIMSON APEX // GUARDIAN",
                    tacticalSummary = "Phase 1: Unlock and destroy 4 Cipher Shield Pylons. Phase 2: Strike the central Apex Core before it locks the matrix."
                )
                4 -> IntelBriefingData(
                    iconRes = R.drawable.sector_4_block,
                    unitDesignation = "BIO-COLOSSUS // APEX",
                    tacticalSummary = "Phase 1: Destroy 4 Bio-Relay Pylons. Phase 2: Strike the central Bio-Core before toxic sludge covers the grid."
                )
                5 -> IntelBriefingData(
                    iconRes = R.drawable.sector_5_block,
                    unitDesignation = "EVENT HORIZON // FINAL APEX",
                    tacticalSummary = "Phase 1: Destroy 4 Gravitational Relay Pylons. Phase 2: Strike the central Event Horizon Core to collapse the void matrix."
                )
                else -> IntelBriefingData(
                    iconRes = R.drawable.sector_1_block,
                    unitDesignation = "NEON GUARDIAN // APEX",
                    tacticalSummary = "Phase 1: Destroy 4 Shield Pylons to collapse forcefield. Phase 2: Direct-hit central Apex Core."
                )
            }
        }
        blueprint.sectorId == 2 -> {
            IntelBriefingData(
                iconRes = R.drawable.sector_2_block,
                unitDesignation = "AMBER FURNACE CRUCIBLE",
                tacticalSummary = "Thermal Core with 10-turn countdown (+2 turns per hit). Clear lines to trigger Cardinal Lasers before it transmutes into slag."
            )
        }
        blueprint.sectorId == 3 -> {
            IntelBriefingData(
                iconRes = R.drawable.sector_3_block_with_lock,
                unitDesignation = "CRIMSON CIPHER",
                tacticalSummary = "Sustain a 2x Surge Streak to shatter the Cipher lock and make the core vulnerable to damage."
            )
        }
        blueprint.sectorId == 4 -> {
            IntelBriefingData(
                iconRes = R.drawable.sector_4_block,
                unitDesignation = "EMERALD BIO-CONDUIT",
                tacticalSummary = "Volatile core that secretes toxic slime into adjacent cells if not purged quickly."
            )
        }
        blueprint.sectorId == 5 -> {
            IntelBriefingData(
                iconRes = R.drawable.sector_5_block,
                unitDesignation = "EVENT HORIZON GYRO",
                tacticalSummary = "Void core that exerts a gravitational pull, warping surrounding block coordinates."
            )
        }
        objectiveType == ObjectiveType.CHROMA_SYNTHESIS -> {
            IntelBriefingData(
                iconRes = R.drawable.sector_1_block,
                unitDesignation = "CIRCUIT CONDUIT NODE",
                tacticalSummary = "Synthesize and clear required matrix block conduits to stabilize energy distribution."
            )
        }
        objectiveType == ObjectiveType.SURGE_STREAK_TARGET -> {
            IntelBriefingData(
                iconRes = R.drawable.sector_1_block,
                unitDesignation = "MOMENTUM HARMONIC",
                tacticalSummary = "Chain consecutive line clears to sustain Surge Streak without exhausting combo buffer grace moves."
            )
        }
        objectiveType == ObjectiveType.LINE_CLEANSE -> {
            IntelBriefingData(
                iconRes = R.drawable.sector_1_block,
                unitDesignation = "CONDUIT PURGE LINE",
                tacticalSummary = "Execute full horizontal and vertical line sweeps across the matrix to cleanse sector conduits."
            )
        }
        else -> {
            IntelBriefingData(
                iconRes = R.drawable.sector_1_block,
                unitDesignation = "CHRONO REACTOR CORE",
                tacticalSummary = "Standard guardian core configuration detected. Strike row/column twice to trigger 3x3 burst."
            )
        }
    }
}
