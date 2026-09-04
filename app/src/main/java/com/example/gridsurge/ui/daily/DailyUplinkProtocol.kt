package com.example.gridsurge.ui.daily

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.ui.screens.DailyLoginReward
import com.example.gridsurge.ui.screens.UPLINK_REWARDS

@Composable
fun DailyUplinkProtocol(
    currentDayStreak: Int,
    canClaimToday: Boolean,
    onClaimDay: (DailyLoginReward) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(
            items = UPLINK_REWARDS,
            span = { index, _ ->
                if (index == 6) GridItemSpan(3) else GridItemSpan(1)
            }
        ) { index, reward ->
            val dayNum = index + 1
            val isClaimed = dayNum < currentDayStreak || (dayNum == currentDayStreak && !canClaimToday)
            val isCurrent = dayNum == currentDayStreak && canClaimToday
            val isGrandReward = dayNum == 7

            Box(
                modifier = Modifier
                    .height(if (isGrandReward) 64.dp else 84.dp)
                    .background(
                        color = when {
                            isGrandReward && isCurrent -> Color(0x44FFD700)
                            isCurrent -> Color(0x3300E5FF)
                            isClaimed -> Color(0x22162238)
                            else -> Color(0x160D1527)
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = if (isGrandReward && isCurrent) 2.dp else 1.dp,
                        color = when {
                            isGrandReward && isCurrent -> Color(0xFFFFD700)
                            isCurrent -> Color(0xFF00E5FF)
                            isClaimed -> Color(0x3300E5FF)
                            else -> Color(0xFF1E2D4A)
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = isCurrent) {
                        SfxManager.playSfx(SfxType.STAR_REWARD)
                        onClaimDay(reward)
                    }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val labelPrefix = if (isGrandReward) "TERMINAL " else ""
                    Text("${labelPrefix}DAY 0$dayNum", color = Color(0xFF5C8599), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val text = if (isClaimed) "CLAIMED" else if (isGrandReward) "GRAND TERMINAL REWARD // ${reward.starAmount} ★" else "+${reward.starAmount} ★"
                    Text(
                        text = text,
                        color = if (isClaimed) Color(0xFF5C8599) else if (isCurrent) Color(0xFFFFD700) else Color.White,
                        fontSize = if (isGrandReward) 11.sp else 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
