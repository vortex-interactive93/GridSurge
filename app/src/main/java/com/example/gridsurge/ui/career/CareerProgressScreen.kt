package com.example.gridsurge.ui.career

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.R

data class Achievement(
    val title: String,
    val description: String,
    val progress: Float, // 0.0 to 1.0
    val badgeRes: Int
)

@Composable
fun CareerProgressScreen(
    onBack: () -> Unit
) {
    val achievements = listOf(
        Achievement("Grid Purger", "Clear 10,000 total lines", 0.45f, R.drawable.logo_grid_surge), // Fallback badge
        Achievement("Combo Master", "Reach a 15x combo in Classic", 0.8f, R.drawable.logo_grid_surge),
        Achievement("Singularity Walker", "Complete all Sector 05 levels", 0.1f, R.drawable.logo_grid_surge)
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
                Text("CAREER LOG", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(achievements) { achievement ->
                    AchievementItem(achievement)
                }
            }
        }
    }
}

@Composable
fun AchievementItem(achievement: Achievement) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(Color(0xFF0F1524), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF1B2A4A), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge Slot
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = achievement.badgeRes),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(achievement.title.uppercase(), color = Color(0xFFFFB300), fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text(achievement.description, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress Gauge
                Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(3.dp))) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(achievement.progress)
                            .fillMaxHeight()
                            .background(Color(0xFFFFB300), RoundedCornerShape(3.dp))
                    )
                }
                Text("${(achievement.progress * 100).toInt()}% SYNCED", color = Color(0xFFFFB300), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {},
                enabled = achievement.progress >= 1f,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFB300),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("CLAIM", fontWeight = FontWeight.Black, fontSize = 10.sp)
            }
        }
    }
}
