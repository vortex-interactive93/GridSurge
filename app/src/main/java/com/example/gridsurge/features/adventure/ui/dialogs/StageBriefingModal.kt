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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.features.adventure.model.AdventureLevelBlueprint
import com.example.gridsurge.features.adventure.model.StageStarBenchmark
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.ui.CyberChamferShape

@Composable
fun StageBriefingModal(
    blueprint: AdventureLevelBlueprint,
    benchmark: StageStarBenchmark,
    onEngage: () -> Unit,
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
                        brush = Brush.verticalGradient(listOf(Color(0xFF0A1322), Color(0xFF030712))),
                        shape = CyberChamferShape
                    )
                    .border(1.5.dp, Color(0xFF00E5FF), CyberChamferShape)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STAGE 0${blueprint.levelNumber} // SEC-0${blueprint.sectorId}.${blueprint.levelNumber}",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x22FF0055))
                            .border(1.dp, Color(0xFFFF0055), RoundedCornerShape(14.dp))
                            .clickable {
                                SfxManager.playSfx(SfxType.MODAL_WHOOSH)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = Color(0xFFFF0055), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = blueprint.stageName,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                // Primary Directive Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF07101C))
                        .border(1.dp, Color(0xFF1A2A40), RoundedCornerShape(6.dp))
                        .padding(12.dp)
                ) {
                    Text("PRIMARY DIRECTIVE", color = Color(0xFF5C8599), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text(blueprint.directive, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mastery Benchmarks Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF07101C))
                        .border(1.dp, Color(0xFF1A2A40), RoundedCornerShape(6.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("TRI-DIRECTIVE MASTERY PROTOCOL", color = Color(0xFF5C8599), fontSize = 9.sp, fontFamily = FontFamily.Monospace)

                    BenchmarkRow("★ 1", "Complete Primary Directive: ${blueprint.objective.title}")
                    BenchmarkRow("★ 2", "Efficiency: ≤${benchmark.moveBudgetStar2} Moves or <${benchmark.timeLimitSecStar2}s")
                    BenchmarkRow("★ 3", benchmark.masteryFeat.description)
                }

                Spacer(modifier = Modifier.height(18.dp))

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
                                onEngage()
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(starText, color = Color(0xFFFFD700), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
        Text(desc, color = Color(0xFFB0BEC5), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}
