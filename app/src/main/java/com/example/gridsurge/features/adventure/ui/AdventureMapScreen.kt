package com.example.gridsurge.features.adventure.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.features.adventure.model.*
import com.example.gridsurge.meta.CampaignProgressManager
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.ui.components.StarVaultPill
import com.example.gridsurge.R
import com.example.gridsurge.audio.*
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventureMapScreen(
    campaignManager: CampaignProgressManager,
    profileManager: PlayerProfileManager,
    onBack: () -> Unit,
    onStartLevel: (LevelNodeSpec) -> Unit // Updated to use new model
) {
    val totalStars by campaignManager.totalStars.collectAsState()
    val highestUnlocked by campaignManager.highestUnlockedLevel.collectAsState()
    
    var selectedLevel by remember { mutableStateOf<LevelNodeSpec?>(null) }
    
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        BgmManager.playTrack(context, BgmTrack.MAIN_HUB)
    }

    val listState = rememberLazyListState()

    // Simplified for legacy view
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0C14))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top HUD Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    SfxManager.playSfx(SfxType.UI_CONFIRM)
                    onBack()
                }) {
                    Text("< HUB", color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ADVENTURE MAP (LEGACY)", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 2.sp)
                    StarVaultPill(stars = totalStars, onClick = {})
                }
            }
            
            Text("This is an obsolete map view. Use SectorMapScreen instead.", color = Color.Gray, modifier = Modifier.padding(16.dp))
        }
    }
}
