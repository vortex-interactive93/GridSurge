package com.example.gridsurge.ui.career.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.career.model.OperativeDossierState
import com.example.gridsurge.ui.components.TacticalRollingCounter
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily

@Composable
fun LiveCombatTelemetryMatrix(
    state: OperativeDossierState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LIFETIME COMBAT TELEMETRY",
                color = Color(0xFF00E5FF),
                fontSize = 9.sp,
                fontFamily = ChakraPetchFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "STATUS: SYNCHRONIZED",
                color = Color(0xFF556980),
                fontSize = 8.sp,
                fontFamily = ChakraPetchFontFamily
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RollingMetricTile(
                label = "TOTAL SORTIES",
                targetValue = state.lifetimeSorties.toLong(),
                telemetry = if (state.lifetimeSorties == 0) "0% DEPLOYMENT" else "COMBAT READY",
                accentColor = Color(0xFF00E5FF),
                modifier = Modifier.weight(1f)
            )
            RollingMetricTile(
                label = "APEX SCORE",
                targetValue = state.apexHighScore,
                telemetry = if (state.apexHighScore == 0L) "UNRANKED" else "RECORD LOGGED",
                accentColor = Color(0xFFFFB300),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RollingMetricTile(
                label = "GRID CLEARS",
                targetValue = state.totalGridClears.toLong(),
                telemetry = if (state.totalGridClears == 0) "BOUNTY LEVEL 0" else "BOUNTY LEVEL ${(state.totalGridClears / 25) + 1}",
                accentColor = Color(0xFF00FF66),
                modifier = Modifier.weight(1f)
            )
            RollingMetricTile(
                label = "MAX COMBO",
                targetValue = state.maxComboMultiplier.toLong(),
                prefix = "x",
                telemetry = if (state.maxComboMultiplier == 0) "OVERDRIVE INACTIVE" else "OVERDRIVE ${state.maxComboMultiplier}.0X",
                accentColor = Color(0xFFFF1744),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RollingMetricTile(
    label: String,
    targetValue: Long,
    prefix: String = "",
    telemetry: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(68.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xDD090F1B))
            .border(1.dp, Color(0xFF182638), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    color = Color(0xFF8FA3BF),
                    fontSize = 8.sp,
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = telemetry,
                    color = accentColor,
                    fontSize = 7.sp,
                    fontFamily = ChakraPetchFontFamily
                )
            }

            TacticalRollingCounter(
                targetValue = targetValue,
                prefix = prefix,
                fontSize = 18.sp,
                color = Color.White,
                durationMs = 950
            )
        }
    }
}
