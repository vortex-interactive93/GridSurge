package com.example.gridsurge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.ui.CyberChamferShape
import com.example.gridsurge.features.adventure.model.ObjectiveType
import com.example.gridsurge.core.model.LevelObjectiveFormatter

@Composable
fun MatrixFailureReviveDialog(
    finalScore: Long,
    canRevive: Boolean,
    starBalance: Int,
    objectiveType: ObjectiveType = ObjectiveType.INFECTED_PURGE,
    adventureCoreProgress: Pair<Int, Int>? = null, // (neutralized, total)
    failureSubtitle: String? = null,
    onDeployEmp: () -> Unit,
    onReboot: () -> Unit,
    onAbort: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE603060E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .background(
                    brush = Brush.verticalGradient(listOf(Color(0xF50D1527), Color(0xEE050914))),
                    shape = CyberChamferShape
                )
                .border(1.5.dp, Color(0xFFFF0055), CyberChamferShape)
                .padding(24.dp)
        ) {
            Text(
                text = "CRITICAL FAILURE",
                color = Color(0xFFFF0055),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "MATRIX LOCKED",
                color = Color.White,
                fontSize = 22.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black
            )

            if (failureSubtitle != null) {
                Text(
                    text = failureSubtitle,
                    color = Color(0xFF5C8599),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Adventure Core Progress Indicator
            if (adventureCoreProgress != null) {
                val (neutralized, total) = adventureCoreProgress
                val pct = if (total > 0) (neutralized.toFloat() / total) else 0f
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = LevelObjectiveFormatter.formatFailureProgress(objectiveType, neutralized, total).substringBefore(":"),
                            color = Color(0xFF5C8599),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "$neutralized / $total (${(pct * 100).toInt()}%)",
                            color = Color(0xFF00E5FF),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(6.dp).background(Color(0xFF162238), RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(pct).fillMaxHeight().background(Color(0xFF00E5FF), RoundedCornerShape(3.dp))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // REVIVE OPTION
            if (canRevive) {
                TacticalButton(
                    label = "DEPLOY EMP SURGE [50 ★]",
                    color = Color(0xFF00E5FF),
                    enabled = starBalance >= 50,
                    onClick = onDeployEmp
                )
                Text(
                    text = "Clears center 4x4 matrix to continue",
                    color = Color(0xFF5C8599),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            TacticalButton(
                label = "REBOOT MATRIX",
                color = Color(0xFF00FF66),
                onClick = onReboot
            )

            Spacer(modifier = Modifier.height(10.dp))

            TacticalButton(
                label = "ABORT TO HUB",
                color = Color(0xFF5C8599),
                onClick = onAbort
            )
        }
    }
}
