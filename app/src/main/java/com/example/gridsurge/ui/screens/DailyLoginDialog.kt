package com.example.gridsurge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.ui.CyberChamferShape
import com.example.gridsurge.ui.components.TacticalButton

import com.example.gridsurge.ui.daily.DailyUplinkProtocol

data class DailyLoginReward(val day: Int, val starAmount: Int, val rewardLabel: String)

val UPLINK_REWARDS = listOf(
    DailyLoginReward(1, 50, "50 STARS"),
    DailyLoginReward(2, 75, "75 STARS"),
    DailyLoginReward(3, 100, "100 STARS"),
    DailyLoginReward(4, 150, "150 STARS"),
    DailyLoginReward(5, 200, "200 STARS"),
    DailyLoginReward(6, 300, "300 STARS"),
    DailyLoginReward(7, 500, "MATRIX RELIC // 500 ★")
)

@Composable
fun DailyLoginDialog(
    currentDayStreak: Int,
    canClaimToday: Boolean,
    onClaimDay: (DailyLoginReward) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC03060E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .background(Color(0xF00A1424), CyberChamferShape)
                .border(1.5.dp, Color(0xFF00E5FF), CyberChamferShape)
                .padding(20.dp)
        ) {
            Text(
                text = "NEURAL LINK // 7-DAY PROTOCOL",
                color = Color(0xFF00E5FF),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "DAILY UPLINK",
                color = Color.White,
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            DailyUplinkProtocol(
                currentDayStreak = currentDayStreak,
                canClaimToday = canClaimToday,
                onClaimDay = onClaimDay
            )

            Spacer(modifier = Modifier.height(16.dp))

            TacticalButton(
                label = "CLOSE",
                color = Color(0xFF5C8599),
                onClick = onDismiss
            )
        }
    }
}
