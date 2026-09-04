package com.example.gridsurge.ui.leaderboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.R
import java.util.Locale

data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val score: Int
)

@Composable
fun LeaderboardScreen(
    onBack: () -> Unit
) {
    val entries = listOf(
        LeaderboardEntry(1, "Vortex_Zero", 1250400),
        LeaderboardEntry(2, "CyberGhost", 1120000),
        LeaderboardEntry(3, "NeonPulse", 980500),
        LeaderboardEntry(4, "GridMaster", 850000),
        LeaderboardEntry(5, "QuantumShift", 720000),
        LeaderboardEntry(6, "SolarStriker", 650000),
        LeaderboardEntry(7, "VoidRunner", 580000),
        LeaderboardEntry(8, "BinaryBeast", 510000)
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0C14))) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("< HUB", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("GLOBAL RANKINGS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
            }

            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(entries) { entry ->
                    LeaderboardRow(entry)
                }
            }

            // Footer Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFF0F1524))
                    .border(BorderStroke(1.dp, Color(0xFF1B2A4A)))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("YOUR RANK", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("#42", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BEST SCORE", color = Color.Gray, fontSize = 10.sp)
                        Text(
                            String.format(Locale.getDefault(), "%,d", 342000), 
                            color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace
                        )
                    }

                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SYNC DATA", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(entry: LeaderboardEntry) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color(0xFF0F1524), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF1B2A4A), RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${entry.rank}",
                color = if (entry.rank <= 3) Color(0xFFFFB300) else Color.Gray,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(40.dp)
            )
            
            // Avatar Placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = entry.username,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = String.format(Locale.getDefault(), "%,d", entry.score),
                color = Color(0xFF00E5FF),
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp
            )
        }
    }
}
