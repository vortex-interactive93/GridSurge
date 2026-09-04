package com.example.gridsurge.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.example.gridsurge.R
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.ui.CyberChamferShape
import java.util.Locale

data class MilestoneStep(
    val tierLevel: Int,
    val targetAmount: Long,
    val starReward: Int
)

data class ProgressiveAchievementChain(
    val chainId: String,
    val titlePrefix: String,
    val directiveTemplate: String,
    val accentColor: Color,
    val milestones: List<MilestoneStep>
)

data class AchievementCategorySpec(
    val categoryId: String,
    val title: String,
    val accentColor: Color,
    val chains: List<ProgressiveAchievementChain>
)

object ProgressiveAchievementRegistry {

    // Category 1: CORE SURGE & CLASSIC
    val LINE_BREAKER = ProgressiveAchievementChain(
        chainId = "chain_lines",
        titlePrefix = "LINE BREAKER",
        directiveTemplate = "Clear %,d lines total across matches",
        accentColor = Color(0xFF00E5FF),
        milestones = listOf(
            MilestoneStep(1, 100L, 100),
            MilestoneStep(2, 500L, 200),
            MilestoneStep(3, 1000L, 350),
            MilestoneStep(4, 2500L, 500),
            MilestoneStep(5, 5000L, 750),
            MilestoneStep(6, 10000L, 1000),
            MilestoneStep(7, 50000L, 2500)
        )
    )

    val COMBO_MATRIX = ProgressiveAchievementChain(
        chainId = "chain_combo",
        titlePrefix = "COMBO MATRIX",
        directiveTemplate = "Trigger a %dx Overdrive Combo streak",
        accentColor = Color(0xFFFFD600),
        milestones = listOf(
            MilestoneStep(1, 3L, 100),
            MilestoneStep(2, 5L, 150),
            MilestoneStep(3, 8L, 250),
            MilestoneStep(4, 10L, 400),
            MilestoneStep(5, 12L, 600),
            MilestoneStep(6, 15L, 1000)
        )
    )

    val SURGE_RUNNER = ProgressiveAchievementChain(
        chainId = "chain_runs",
        titlePrefix = "SURGE RUNNER",
        directiveTemplate = "Complete %,d matches across all modes",
        accentColor = Color(0xFF00E676),
        milestones = listOf(
            MilestoneStep(1, 1L, 50),
            MilestoneStep(2, 5L, 100),
            MilestoneStep(3, 20L, 200),
            MilestoneStep(4, 50L, 350),
            MilestoneStep(5, 100L, 500),
            MilestoneStep(6, 500L, 1200)
        )
    )

    val HIGH_SCORE = ProgressiveAchievementChain(
        chainId = "chain_score",
        titlePrefix = "HIGH SCORE OPERATIVE",
        directiveTemplate = "Achieve a score of %,d+ pts in a single match",
        accentColor = Color(0xFFD500F9),
        milestones = listOf(
            MilestoneStep(1, 5000L, 100),
            MilestoneStep(2, 15000L, 250),
            MilestoneStep(3, 30000L, 400),
            MilestoneStep(4, 50000L, 600),
            MilestoneStep(5, 100000L, 1200)
        )
    )

    // Category 2: ADVENTURE CAMPAIGN
    val SECTOR_CONQUEROR = ProgressiveAchievementChain(
        chainId = "chain_sectors",
        titlePrefix = "SECTOR CONQUEROR",
        directiveTemplate = "Clear Sector %d in Adventure Mode",
        accentColor = Color(0xFFFFB300),
        milestones = listOf(
            MilestoneStep(1, 1L, 200),
            MilestoneStep(2, 2L, 350),
            MilestoneStep(3, 3L, 500),
            MilestoneStep(4, 4L, 750),
            MilestoneStep(5, 5L, 1000)
        )
    )

    val PERFECT_SECTOR = ProgressiveAchievementChain(
        chainId = "chain_perfect_stars",
        titlePrefix = "PERFECT SECTOR",
        directiveTemplate = "Earn 3 Stars on %,d Adventure Levels",
        accentColor = Color(0xFFFFD600),
        milestones = listOf(
            MilestoneStep(1, 3L, 150),
            MilestoneStep(2, 9L, 300),
            MilestoneStep(3, 18L, 500),
            MilestoneStep(4, 27L, 1000)
        )
    )

    val RELIC_MASTER = ProgressiveAchievementChain(
        chainId = "chain_relics",
        titlePrefix = "RELIC MASTER",
        directiveTemplate = "Win %,d Adventure Levels using a Relic Ability",
        accentColor = Color(0xFF00E5FF),
        milestones = listOf(
            MilestoneStep(1, 1L, 100),
            MilestoneStep(2, 5L, 200),
            MilestoneStep(3, 15L, 400),
            MilestoneStep(4, 30L, 750)
        )
    )

    // Category 3: TIME BLITZ & SPEED
    val FEVER_OVERCHARGE = ProgressiveAchievementChain(
        chainId = "chain_fever",
        titlePrefix = "FEVER OVERCHARGE",
        directiveTemplate = "Activate 2X Fever Mode %,d times in Time Blitz",
        accentColor = Color(0xFFD500F9),
        milestones = listOf(
            MilestoneStep(1, 1L, 100),
            MilestoneStep(2, 3L, 200),
            MilestoneStep(3, 10L, 450),
            MilestoneStep(4, 25L, 800)
        )
    )

    val SPEED_DEMON = ProgressiveAchievementChain(
        chainId = "chain_blitz_score",
        titlePrefix = "SPEED DEMON",
        directiveTemplate = "Score %,d+ pts in a single Time Blitz match",
        accentColor = Color(0xFFD500F9),
        milestones = listOf(
            MilestoneStep(1, 10000L, 150),
            MilestoneStep(2, 25000L, 350),
            MilestoneStep(3, 50000L, 750)
        )
    )

    // Category 4: BLITZ CLASH 1V1 PVP
    val CLASH_VICTOR = ProgressiveAchievementChain(
        chainId = "chain_pvp_wins",
        titlePrefix = "CLASH VICTOR",
        directiveTemplate = "Win %,d Blitz Clash PvP Duels against Rivals",
        accentColor = Color(0xFFFF1744),
        milestones = listOf(
            MilestoneStep(1, 1L, 150),
            MilestoneStep(2, 5L, 300),
            MilestoneStep(3, 20L, 600),
            MilestoneStep(4, 50L, 1200)
        )
    )

    val RATING_CLIMBER = ProgressiveAchievementChain(
        chainId = "chain_rating_points",
        titlePrefix = "RATING CLIMBER",
        directiveTemplate = "Reach %,d Rating Points in Blitz Clash PvP",
        accentColor = Color(0xFFFF1744),
        milestones = listOf(
            MilestoneStep(1, 300L, 200),
            MilestoneStep(2, 800L, 400),
            MilestoneStep(3, 1500L, 750),
            MilestoneStep(4, 3000L, 1500)
        )
    )

    // Category 5: DAILY GLITCH & EVENTS
    val GLITCH_SURVIVOR = ProgressiveAchievementChain(
        chainId = "chain_glitch_seeds",
        titlePrefix = "GLITCH SURVIVOR",
        directiveTemplate = "Complete %,d Daily Glitch Seed challenges",
        accentColor = Color(0xFF00E676),
        milestones = listOf(
            MilestoneStep(1, 1L, 150),
            MilestoneStep(2, 5L, 300),
            MilestoneStep(3, 15L, 600),
            MilestoneStep(4, 30L, 1000)
        )
    )

    val CATEGORIES = listOf(
        AchievementCategorySpec(
            categoryId = "cat_core",
            title = "CORE SURGE & CLASSIC",
            accentColor = Color(0xFF00E5FF),
            chains = listOf(LINE_BREAKER, COMBO_MATRIX, SURGE_RUNNER, HIGH_SCORE)
        ),
        AchievementCategorySpec(
            categoryId = "cat_adventure",
            title = "ADVENTURE CAMPAIGN",
            accentColor = Color(0xFFFFB300),
            chains = listOf(SECTOR_CONQUEROR, PERFECT_SECTOR, RELIC_MASTER)
        ),
        AchievementCategorySpec(
            categoryId = "cat_blitz",
            title = "TIME BLITZ & SPEED",
            accentColor = Color(0xFFD500F9),
            chains = listOf(FEVER_OVERCHARGE, SPEED_DEMON)
        ),
        AchievementCategorySpec(
            categoryId = "cat_pvp",
            title = "BLITZ CLASH 1V1 PVP",
            accentColor = Color(0xFFFF1744),
            chains = listOf(CLASH_VICTOR, RATING_CLIMBER)
        ),
        AchievementCategorySpec(
            categoryId = "cat_events",
            title = "DAILY GLITCH & EVENTS",
            accentColor = Color(0xFF00E676),
            chains = listOf(GLITCH_SURVIVOR)
        )
    )
}

data class ActiveChainDisplayState(
    val chain: ProgressiveAchievementChain,
    val activeTierLevel: Int,
    val title: String,
    val directive: String,
    val currentProgress: Long,
    val targetAmount: Long,
    val starReward: Int,
    val isCompleted: Boolean,
    val isMaxedOut: Boolean
)

@Composable
fun TierAchievementsScreen(
    profileManager: PlayerProfileManager,
    onBackToHub: () -> Unit
) {
    val currentStarBalance by profileManager.starCurrency.collectAsState()
    val claimedChainTiers by profileManager.claimedChainTiers.collectAsState()

    val totalLines by profileManager.totalLinesCleared.collectAsState()
    val maxCombo by profileManager.maxCombo.collectAsState()
    val totalRuns by profileManager.totalRuns.collectAsState()
    val highScore by profileManager.highScore.collectAsState()
    val highestSector by profileManager.highestSectorCleared.collectAsState()
    val ratingPoints by profileManager.ratingPoints.collectAsState()

    var expandedCategoryIds by remember {
        mutableStateOf(setOf("cat_core", "cat_adventure", "cat_blitz", "cat_pvp", "cat_events"))
    }

    var claimingChainId by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF03060E))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x2200E5FF))
                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(6.dp))
                        .clickable {
                            SfxManager.playSfx(SfxType.MODAL_WHOOSH)
                            onBackToHub()
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("< HUB", color = Color(0xFF00E5FF), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "NEURAL ACHIEVEMENTS",
                    color = Color(0xFF5C8599),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .background(Color(0x22162238), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF263859), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("★ $currentStarBalance", color = Color(0xFFFFD700), fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hero Summary Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(CyberChamferShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xF00A1424), Color(0x3300E5FF))
                        )
                    )
                    .border(1.dp, Color(0xFF00E5FF), CyberChamferShape)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_medal_star_gold),
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        contentScale = ContentScale.Fit
                    )
                    Column {
                        Text(
                            text = "PROGRESSIVE REWARD CHAINS",
                            color = Color(0xFF00E5FF),
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "MILESTONE TRACKER",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expandable Categories Feed
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(ProgressiveAchievementRegistry.CATEGORIES, key = { it.categoryId }) { category ->
                    val isExpanded = expandedCategoryIds.contains(category.categoryId)

                    // Calculate Category Card States
                    val cardStates = category.chains.map { chain ->
                        val claimedTier = claimedChainTiers[chain.chainId] ?: 0
                        val nextTierLevel = claimedTier + 1
                        val milestone = chain.milestones.find { it.tierLevel == nextTierLevel }

                        val rawStat = when (chain.chainId) {
                            "chain_lines" -> totalLines.toLong()
                            "chain_combo" -> maxCombo.toLong()
                            "chain_runs" -> totalRuns.toLong()
                            "chain_score" -> highScore.toLong()
                            "chain_sectors" -> highestSector.toLong()
                            "chain_rating_points" -> ratingPoints.toLong()
                            else -> 0L
                        }

                        if (milestone != null) {
                            val formattedDirective = formatDirective(chain.directiveTemplate, milestone.targetAmount)

                            ActiveChainDisplayState(
                                chain = chain,
                                activeTierLevel = nextTierLevel,
                                title = "${chain.titlePrefix} ${getRomanNumeral(nextTierLevel)}",
                                directive = formattedDirective,
                                currentProgress = rawStat,
                                targetAmount = milestone.targetAmount,
                                starReward = milestone.starReward,
                                isCompleted = rawStat >= milestone.targetAmount,
                                isMaxedOut = false
                            )
                        } else {
                            val lastMilestone = chain.milestones.last()
                            ActiveChainDisplayState(
                                chain = chain,
                                activeTierLevel = lastMilestone.tierLevel,
                                title = "${chain.titlePrefix} MASTER",
                                directive = "ALL TIERS COMPLETED",
                                currentProgress = lastMilestone.targetAmount,
                                targetAmount = lastMilestone.targetAmount,
                                starReward = 0,
                                isCompleted = true,
                                isMaxedOut = true
                            )
                        }
                    }

                    val completedInCat = cardStates.count { it.isCompleted || it.isMaxedOut }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Collapsible Category Header Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x33101522))
                                .border(1.dp, category.accentColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .clickable {
                                    SfxManager.playSfx(SfxType.SNAP_TICK)
                                    expandedCategoryIds = if (isExpanded) {
                                        expandedCategoryIds - category.categoryId
                                    } else {
                                        expandedCategoryIds + category.categoryId
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = category.title,
                                        color = category.accentColor,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(category.accentColor.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$completedInCat/${cardStates.size}",
                                            color = category.accentColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Text(
                                    text = if (isExpanded) "▲ COLLAPSE" else "▼ EXPAND",
                                    color = Color(0xFF78909C),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Expanded Cards
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                cardStates.forEach { cardState ->
                                    val progressPct = (cardState.currentProgress.toFloat() / cardState.targetAmount.coerceAtLeast(1L)).coerceIn(0f, 1f)
                                    val animatedProgress by animateFloatAsState(targetValue = progressPct, label = "chainProgress")
                                    val isClaimingThis = claimingChainId == cardState.chain.chainId

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(CyberChamferShape)
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    listOf(Color(0xF00A1424), Color(0xDD040812))
                                                )
                                            )
                                            .border(
                                                width = if (cardState.isCompleted && !cardState.isMaxedOut) 1.5.dp else 1.dp,
                                                color = if (cardState.isCompleted && !cardState.isMaxedOut) cardState.chain.accentColor else cardState.chain.accentColor.copy(alpha = 0.35f),
                                                shape = CyberChamferShape
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = cardState.title,
                                                        color = Color.White,
                                                        fontSize = 13.5.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(3.dp))
                                                    Text(
                                                        text = cardState.directive,
                                                        color = Color(0xFFB0BEC5),
                                                        fontSize = 11.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                if (cardState.isMaxedOut) {
                                                    Box(
                                                        modifier = Modifier
                                                            .height(30.dp)
                                                            .background(Color(0x2200E676), RoundedCornerShape(4.dp))
                                                            .border(1.dp, Color(0xFF00E676), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 8.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "MAXED OUT ✓",
                                                            color = Color(0xFF00E676),
                                                            fontSize = 9.sp,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                } else if (cardState.isCompleted) {
                                                    Box(
                                                        modifier = Modifier
                                                            .height(32.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(cardState.chain.accentColor)
                                                            .clickable(enabled = !isClaimingThis) {
                                                                claimingChainId = cardState.chain.chainId
                                                                SfxManager.playSfx(SfxType.LEVEL_COMPLETE)
                                                                profileManager.claimChainTier(
                                                                    chainId = cardState.chain.chainId,
                                                                    tierLevel = cardState.activeTierLevel,
                                                                    starReward = cardState.starReward
                                                                )
                                                                claimingChainId = null
                                                            }
                                                            .padding(horizontal = 10.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = if (isClaimingThis) "CLAIMING..." else "CLAIM +${cardState.starReward} ★",
                                                            color = Color(0xFF03060E),
                                                            fontSize = 10.5.sp,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontWeight = FontWeight.Black
                                                        )
                                                    }
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .height(30.dp)
                                                            .background(Color(0x22162238), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 8.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "+${cardState.starReward} ★",
                                                            color = Color(0xFFFFD700),
                                                            fontSize = 10.5.sp,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(4.5.dp)
                                                        .background(Color(0xFF0E1A2E), RoundedCornerShape(2.5.dp))
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(animatedProgress)
                                                            .fillMaxHeight()
                                                            .background(
                                                                brush = Brush.horizontalGradient(
                                                                    listOf(cardState.chain.accentColor.copy(alpha = 0.6f), cardState.chain.accentColor)
                                                                ),
                                                                shape = RoundedCornerShape(2.5.dp)
                                                            )
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "${cardState.currentProgress} / ${cardState.targetAmount}",
                                                    color = Color(0xFF90A4AE),
                                                    fontSize = 9.5.sp,
                                                    fontFamily = FontFamily.Monospace,
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
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

private fun getRomanNumeral(level: Int): String {
    return when (level) {
        1 -> "I"
        2 -> "II"
        3 -> "III"
        4 -> "IV"
        5 -> "V"
        6 -> "VI"
        7 -> "VII"
        8 -> "VIII"
        9 -> "IX"
        10 -> "X"
        else -> "$level"
    }
}

private fun formatDirective(template: String, amount: Long): String {
    val formatted = try {
        String.format(Locale.US, template, amount)
    } catch (_: Exception) {
        template
    }
    return if (amount == 1L) {
        formatted.replace(" times", " time")
                 .replace(" Levels", " Level")
                 .replace(" Duels", " Duel")
                 .replace(" matches", " match")
                 .replace(" seeds", " seed")
    } else formatted
}
