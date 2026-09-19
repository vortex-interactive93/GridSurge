package com.example.gridsurge.ui.career

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.audio.BgmManager
import com.example.gridsurge.audio.BgmTrack
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.career.data.CareerCatalog
import com.example.gridsurge.career.model.CareerDirective
import com.example.gridsurge.career.model.DirectiveCategory
import com.example.gridsurge.career.model.MilestoneStatus
import com.example.gridsurge.career.model.OperativeClearance
import com.example.gridsurge.career.model.OperativeDossierState
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.ui.Screen
import com.example.gridsurge.ui.career.components.CinematicOperativeCard
import com.example.gridsurge.ui.career.components.LiveCombatTelemetryMatrix
import com.example.gridsurge.ui.career.components.TacticalCategoryDock
import com.example.gridsurge.ui.components.StarVaultPill
import com.example.gridsurge.ui.operations.components.DirectiveCard
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun CareerProgressScreen(
    profileManager: PlayerProfileManager? = null,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val stars = profileManager?.starCurrency?.collectAsState()?.value ?: 0
    val callsign = profileManager?.callsign?.collectAsState()?.value ?: "OPERATIVE"
    val highScore = profileManager?.highScore?.collectAsState()?.value ?: 0
    val totalRuns = profileManager?.totalRuns?.collectAsState()?.value ?: 0
    val totalLines = profileManager?.totalLinesCleared?.collectAsState()?.value ?: 0
    val maxCombo = profileManager?.maxCombo?.collectAsState()?.value ?: 0

    val dossierState = remember(callsign, highScore, totalRuns, totalLines, maxCombo) {
        val totalXp = (totalRuns * 50L + highScore / 50L)
        val level = ((totalXp / 500L).toInt() + 1).coerceAtLeast(1)
        val xpInLevel = totalXp % 500L

        val clearance = when {
            level >= 5 -> OperativeClearance.OVERLORD
            level >= 4 -> OperativeClearance.VANGUARD
            level >= 3 -> OperativeClearance.SPECIALIST
            level >= 2 -> OperativeClearance.OPERATIVE
            else -> OperativeClearance.RECRUIT
        }

        val grade = when {
            highScore >= 50000 -> "S-TIER"
            highScore >= 25000 -> "A-TIER"
            highScore >= 10000 -> "B-TIER"
            totalRuns > 0 -> "C-TIER"
            else -> "UNRANKED"
        }

        OperativeDossierState(
            callsign = callsign,
            uidTag = "GS-${abs(callsign.hashCode()) % 1000}-ALPHA",
            clearance = clearance,
            currentLevel = level,
            currentXp = xpInLevel,
            xpForNextLevel = 500L,
            lifetimeSorties = totalRuns,
            apexHighScore = highScore.toLong(),
            totalGridClears = totalLines,
            maxComboMultiplier = maxCombo,
            combatEfficiencyGrade = grade
        )
    }

    var selectedCategory by remember { mutableStateOf(DirectiveCategory.ALL) }

    val milestones = remember(totalRuns, highScore, totalLines, maxCombo) {
        listOf(
            CareerDirective(
                id = "car_sorties_100",
                title = "CENTURION SORTIES",
                description = "Complete 100 tactical arena deployments.",
                category = DirectiveCategory.COMBAT,
                currentProgress = minOf(totalRuns, 100),
                targetProgress = 100,
                rewardStars = 200,
                status = if (totalRuns >= 100) MilestoneStatus.READY_TO_CLAIM else MilestoneStatus.IN_PROGRESS
            ),
            CareerDirective(
                id = "car_score_50k",
                title = "HIGH-VOLTAGE APEX",
                description = "Achieve a single-run score exceeding 50,000 PTS.",
                category = DirectiveCategory.COMBAT,
                currentProgress = minOf(highScore, 50000),
                targetProgress = 50000,
                rewardStars = 500,
                status = if (highScore >= 50000) MilestoneStatus.READY_TO_CLAIM else MilestoneStatus.IN_PROGRESS
            ),
            CareerDirective(
                id = "car_grid_clears_100",
                title = "PURGE MASTER",
                description = "Perform 100 total full-screen grid decontamination sweeps.",
                category = DirectiveCategory.TACTICAL,
                currentProgress = minOf(totalLines, 100),
                targetProgress = 100,
                rewardStars = 300,
                status = if (totalLines >= 100) MilestoneStatus.READY_TO_CLAIM else MilestoneStatus.IN_PROGRESS
            ),
            CareerDirective(
                id = "car_combo_10x",
                title = "FEVER OVERLOAD",
                description = "Reach an active 10.0x Overdrive combo streak.",
                category = DirectiveCategory.TACTICAL,
                currentProgress = minOf(maxCombo, 10),
                targetProgress = 10,
                rewardStars = 400,
                status = if (maxCombo >= 10) MilestoneStatus.READY_TO_CLAIM else MilestoneStatus.IN_PROGRESS
            )
        )
    }

    // Stagger Cascade Orchestrator
    var isLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        SfxManager.playSfx(SfxType.MODE_DRAWER_OPEN, volume = 0.65f, pitchRate = 1.15f)
        delay(30)
        isLoaded = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040711))
    ) {
        // Perspective Ambient Grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val lines = 16
            for (i in 0..lines) {
                val y = (h / lines) * i
                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = 0.025f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp)
        ) {
            // ==================== 1. TOP TELEMETRY ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC0D1424))
                        .border(1.dp, Color(0xFF1E2D44), RoundedCornerShape(8.dp))
                        .clickable {
                            SfxManager.playSfx(SfxType.UI_BACK)
                            onBack()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "HUB",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CAREER VAULT",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "HARDWARE & OPERATIVE TELEMETRY",
                        color = Color(0xFF00E5FF),
                        fontSize = 8.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                StarVaultPill(
                    stars = stars,
                    onClick = {
                        SfxManager.playSfx(SfxType.UI_CONFIRM)
                    }
                )
            }

            // ==================== 2. DOSSIER STREAM ====================
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Cascading ID Card (Stage 1: Spring Inset)
                item {
                    val cardOffset by animateFloatAsState(
                        targetValue = if (isLoaded) 0f else 40f,
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
                        label = "cardOffset"
                    )
                    val cardAlpha by animateFloatAsState(
                        targetValue = if (isLoaded) 1f else 0f,
                        animationSpec = tween(400, easing = LinearOutSlowInEasing),
                        label = "cardAlpha"
                    )

                    CinematicOperativeCard(
                        state = dossierState,
                        modifier = Modifier.graphicsLayer {
                            translationY = cardOffset
                            alpha = cardAlpha
                        }
                    )
                }

                // Cascading Telemetry 2x2 Matrix (Stage 2: +60ms Delay)
                item {
                    val teleOffset by animateFloatAsState(
                        targetValue = if (isLoaded) 0f else 50f,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
                        label = "teleOffset"
                    )
                    val teleAlpha by animateFloatAsState(
                        targetValue = if (isLoaded) 1f else 0f,
                        animationSpec = tween(450, delayMillis = 60, easing = LinearOutSlowInEasing),
                        label = "teleAlpha"
                    )

                    LiveCombatTelemetryMatrix(
                        state = dossierState,
                        modifier = Modifier.graphicsLayer {
                            translationY = teleOffset
                            alpha = teleAlpha
                        }
                    )
                }

                // Sliding Tactical Category Filter Dock
                item {
                    TacticalCategoryDock(
                        selectedCategory = selectedCategory,
                        onSelectCategory = { selectedCategory = it }
                    )
                }

                // Filtered Directives Stream with Cinematic Crossfade & Slide
                item {
                    AnimatedContent(
                        targetState = selectedCategory,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing)) +
                                    slideInVertically(
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                        initialOffsetY = { 18 }
                                    )).togetherWith(
                                fadeOut(animationSpec = tween(140, easing = FastOutLinearInEasing))
                            )
                        },
                        label = "directiveFilterTransition"
                    ) { currentCategory ->
                        val filtered = remember(currentCategory, milestones) {
                            milestones.filter {
                                currentCategory == DirectiveCategory.ALL || it.category == currentCategory
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            filtered.forEach { directive ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xDD090F1B))
                                        .border(1.dp, Color(0xFF182638), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = directive.title,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontFamily = OrbitronFontFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = directive.description,
                                            color = Color(0xFF8FA3BF),
                                            fontSize = 10.sp,
                                            fontFamily = ChakraPetchFontFamily
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "PROGRESS: ${directive.currentProgress} / ${directive.targetProgress}",
                                                color = Color(0xFF00E5FF),
                                                fontSize = 9.sp,
                                                fontFamily = OrbitronFontFamily
                                            )
                                            Text(
                                                text = "+${directive.rewardStars} ★",
                                                color = Color(0xFFFFB300),
                                                fontSize = 10.sp,
                                                fontFamily = OrbitronFontFamily,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
