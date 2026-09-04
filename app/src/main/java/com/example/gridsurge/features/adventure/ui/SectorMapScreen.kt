package com.example.gridsurge.features.adventure.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.features.adventure.model.*
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.features.adventure.data.AdventureSectorRegistry
import com.example.gridsurge.ui.CyberActionButton
import com.example.gridsurge.ui.CyberChamferShape
import com.example.gridsurge.features.adventure.ui.dialogs.*
import com.example.gridsurge.features.adventure.data.RelicCatalog
import java.util.Locale

private val NeonCyan = Color(0xFF00E5FF)
private val NeonGold = Color(0xFFFFD600)
private val NeonRed = Color(0xFFFF0055)
private val CardBorder = Color(0xFF1B2A42)

@Composable
fun SectorMapScreen(
    currentSector: SectorSpec,
    allSectors: List<SectorSpec>,
    progressMap: Map<Int, LevelProgressRecord>,
    totalStarsCollected: Int,
    claimedRelicReward: RelicSpec?,
    isRelicClaimed: Boolean,
    equippedRelicName: String = "NONE",
    activeAugmentIds: Set<String> = emptySet(),
    onLevelSelected: (LevelNodeSpec) -> Unit,
    onClaimRelic: (RelicSpec) -> Unit,
    onSectorChanged: (Int) -> Unit,
    onDismissReward: () -> Unit,
    onEquipRelic: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onResetSectorRun: (() -> Unit)? = null,
    showMatrixOnArrival: Boolean = false
) {
    var showMatrixDialog by remember { mutableStateOf(showMatrixOnArrival) }
    var showSkillIntelDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showTeaserModalForSector by remember { mutableStateOf<Int?>(null) }
    var selectedLevelForBriefing by remember { mutableStateOf<LevelNodeSpec?>(null) }
    
    // Calculate stars earned in Sector 01 to check locks
    val sector01Stars = remember(progressMap) {
        val s1 = allSectors.find { it.sectorId == 1 }
        s1?.levels?.sumOf { progressMap[it.levelNumber]?.starsEarned ?: 0 } ?: 0
    }

    val sectorStars = remember(progressMap, currentSector) {
        currentSector.levels.sumOf { progressMap[it.levelNumber]?.starsEarned ?: 0 }
    }

    val currentRelic = remember(currentSector) {
        RelicCatalog.getRelicForSector(currentSector.sectorId)
    }

    val totalSectorPossibleStars = currentSector.levels.size * 3

    val matrixState = remember(currentRelic, sectorStars, isRelicClaimed, totalSectorPossibleStars, progressMap) {
        val completedCount = currentSector.levels.count { progressMap[it.levelNumber]?.isCompleted == true }
        NeuralMatrixState(
            relic = currentRelic,
            totalStarsEarnedInSector = sectorStars,
            totalStarsInSector = totalSectorPossibleStars,
            completedStagesCount = completedCount,
            totalStagesInSector = currentSector.levels.size,
            isBossDefeated = progressMap[currentSector.levels.lastOrNull { it.isBossLevel }?.levelNumber]?.isCompleted ?: false,
            isRewardClaimed = isRelicClaimed
        )
    }

    val listState = rememberLazyListState()

    // Smooth Infinite Animation for Radar Rings and Neon Glow
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "radar"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Sector Thematic Wallpaper Background
        Image(
            painter = painterResource(id = currentSector.backgroundDrawableRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. High-Contrast Darkening Scrim (ensures HUD & nodes pop)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF2030711),
                            Color(0xCC030711),
                            Color(0xF7030711)
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Sector Header Bar with Pagination
            val isBossDefeatedInCurrent = progressMap[currentSector.levels.lastOrNull { it.isBossLevel }?.levelNumber]?.isCompleted == true
            val isNextUnlockedForCurrent = sectorStars >= 18 || isBossDefeatedInCurrent

            SectorTopBar(
                sector = currentSector,
                allSectors = allSectors,
                totalStars = totalStarsCollected,
                isNextUnlocked = isNextUnlockedForCurrent,
                onSectorChanged = onSectorChanged,
                onShowTeaserModal = { teaserSectorId ->
                    showTeaserModalForSector = teaserSectorId
                },
                onBack = onNavigateBack
            )

            // Action Buttons Row (Neural Matrix & Skill Intel)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    CyberActionButton(
                        text = if (isRelicClaimed) "RELIC ACQUIRED" else "MATRIX [${matrixState.unlockedFragmentsCount}/${matrixState.relic.totalFragments}] ►",
                        primaryColor = if (isRelicClaimed) Color(0xFF00FF66) else NeonCyan,
                        isPrimary = false,
                        onClick = { showMatrixDialog = true }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    CyberActionButton(
                        text = "NEURAL INTEL ⚡",
                        primaryColor = Color(0xFFFFD600),
                        isPrimary = false,
                        onClick = { showSkillIntelDialog = true }
                    )
                }
            }

            // Active Sector Skills Loadout Strip
            val activeAugmentList = remember(activeAugmentIds) {
                SkillIntelRegistry.ALL_SKILLS.map { it.augment }.filter { 
                    it.id in activeAugmentIds || activeAugmentIds.any { id -> id.contains(it.id, ignoreCase = true) }
                }
            }

            ActiveAugmentsMapStrip(
                activeAugments = activeAugmentList,
                onIntelClick = { showSkillIntelDialog = true },
                onResetRunClick = if (activeAugmentList.isNotEmpty()) { { showResetConfirmDialog = true } } else null
            )

            // Scrollable Map Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Vertical Node List (Reversed so Stage 1 starts at bottom)
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = true,
                    contentPadding = PaddingValues(top = 40.dp, bottom = 40.dp)
                ) {
                    itemsIndexed(currentSector.levels) { index, levelNode ->
                        val progress = progressMap[levelNode.levelNumber] ?: LevelProgressRecord(levelNode.levelNumber)
                        val nextNode = currentSector.levels.getOrNull(index + 1)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            // Draw locked conduit directly from this node to next node
                            if (nextNode != null) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val startX = levelNode.normalizedX * w
                                    val startY = size.height / 2f
                                    val endX = nextNode.normalizedX * w
                                    val endY = -size.height / 2f

                                    val conduit = Path().apply {
                                        moveTo(startX, startY)
                                        val cp1y = startY - 60.dp.toPx()
                                        val cp2y = endY + 60.dp.toPx()
                                        cubicTo(startX, cp1y, endX, cp2y, endX, endY)
                                    }

                                    // Glow conduit
                                    drawPath(
                                        path = conduit,
                                        color = currentSector.primaryColor.copy(alpha = 0.25f * glowAlpha),
                                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    // Core wire
                                    drawPath(
                                        path = conduit,
                                        color = currentSector.primaryColor.copy(alpha = 0.9f),
                                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                            }

                            // Render Node Button at exact normalizedX
                            StageNodeAnchor(
                                node = levelNode,
                                progress = progress,
                                sectorColor = currentSector.primaryColor,
                                pulseProgress = pulseProgress,
                                glowAlpha = glowAlpha,
                                onClick = {
                                    if (progress.isUnlocked) {
                                        SfxManager.playSfx(SfxType.SNAP_TICK)
                                        selectedLevelForBriefing = levelNode
                                    } else {
                                        SfxManager.playSfx(SfxType.INVALID_MOVE)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Neural Matrix Relic Dialog
        val currentRelicAbility = RelicAbilityType.getRelicForSector(currentSector.sectorId)
        val isCurrentRelicEquipped = (equippedRelicName == currentRelicAbility.name)

        if (showMatrixDialog) {
            NeuralMatrixRelicDialog(
                matrixState = matrixState,
                isEquipped = isCurrentRelicEquipped,
                onDismiss = { showMatrixDialog = false },
                onEngageBoss = {
                    showMatrixDialog = false
                    val bossLevel = currentSector.levels.find { it.isBossLevel }
                    if (bossLevel != null) {
                        onLevelSelected(bossLevel)
                    }
                },
                onClaimReward = {
                    onClaimRelic(currentRelic)
                    showMatrixDialog = false
                },
                onEquipRelic = {
                    onEquipRelic(currentRelicAbility.name)
                    showMatrixDialog = false
                }
            )
        }

        // Level Briefing Dialog
        selectedLevelForBriefing?.let { levelNode ->
            val blueprint = AdventureSectorRegistry.getLevelBlueprint(levelNode.levelNumber)
            val benchmark = AdventureSectorRegistry.getBenchmark(levelNode.levelNumber)
            
            StageBriefingModal(
                blueprint = blueprint,
                benchmark = benchmark,
                onEngage = {
                    selectedLevelForBriefing = null
                    onLevelSelected(levelNode)
                },
                onDismiss = { selectedLevelForBriefing = null }
            )
        }

        // Relic Success Toast
        claimedRelicReward?.let { rewardedRelic ->
            RelicUnlockedSuccessDialog(
                relic = rewardedRelic,
                onDismiss = {
                    onDismissReward()
                }
            )
        }

        // Classified Teaser Modal (Sectors 4 & 5 Soft Launch Cap)
        if (showTeaserModalForSector != null) {
            val relicsCount = progressMap.values.count { it.isCompleted && it.levelNumber % 9 == 0 }
            SectorTeaserModal(
                sectorNumber = showTeaserModalForSector!!,
                totalStarsCollected = totalStarsCollected,
                relicsClaimedCount = relicsCount,
                onDismiss = { showTeaserModalForSector = null }
            )
        }

        // System Skills / Neural Intel Dialog
        if (showSkillIntelDialog) {
            NeuralCyberwareIntelDialog(
                activeAugmentIds = activeAugmentIds,
                onDismiss = { showSkillIntelDialog = false },
                onResetSectorRun = if (activeAugmentIds.isNotEmpty()) {
                    {
                        showSkillIntelDialog = false
                        onResetSectorRun?.invoke()
                    }
                } else null
            )
        }

        // Confirmation Dialog for Resetting Sector Run
        if (showResetConfirmDialog) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showResetConfirmDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(CyberChamferShape)
                        .background(Color(0xF0080D1A))
                        .border(1.5.dp, Color(0xFFFF0055), CyberChamferShape)
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "RESTART SECTOR RUN?",
                            color = Color(0xFFFF0055),
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "This will purge all installed Neural Augments & Relics for this Sector run, allowing you to re-draft new augments.",
                            color = Color(0xFFB0BEC5),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                CyberActionButton(
                                    text = "CANCEL",
                                    primaryColor = Color(0xFF5C8599),
                                    isPrimary = false,
                                    onClick = { showResetConfirmDialog = false }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                CyberActionButton(
                                    text = "RESET RUN ↺",
                                    primaryColor = Color(0xFFFF0055),
                                    isPrimary = true,
                                    onClick = {
                                        showResetConfirmDialog = false
                                        SfxManager.playSfx(SfxType.MODAL_WHOOSH)
                                        onResetSectorRun?.invoke()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveAugmentsMapStrip(
    activeAugments: List<NeuralAugment>,
    onIntelClick: () -> Unit,
    onResetRunClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(CyberChamferShape)
            .background(Color(0xF206101E))
            .border(1.dp, Color(0xFF1B2A42), CyberChamferShape)
            .clickable { onIntelClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.wrapContentWidth()
        ) {
            Text(
                text = "⚡ AUGMENTS",
                color = Color(0xFF00E5FF),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                softWrap = false
            )

            if (onResetRunClick != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x33FF0055))
                        .border(1.dp, Color(0xFFFF0055), RoundedCornerShape(4.dp))
                        .clickable { onResetRunClick() }
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "RESET ↺",
                        color = Color(0xFFFF0055),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            // Slot 1 (Stage 3 Draft)
            val slot1Augment = activeAugments.getOrNull(0)
            Box(modifier = Modifier.weight(1f, fill = false)) {
                if (slot1Augment != null) {
                    AugmentSlotChip(augment = slot1Augment)
                } else {
                    EmptySlotChip(label = "STAGE 3 DRAFT")
                }
            }

            // Slot 2 (Stage 6 Draft)
            val slot2Augment = activeAugments.getOrNull(1)
            Box(modifier = Modifier.weight(1f, fill = false)) {
                if (slot2Augment != null) {
                    AugmentSlotChip(augment = slot2Augment)
                } else {
                    EmptySlotChip(label = "STAGE 6 DRAFT")
                }
            }
        }
    }
}

@Composable
private fun AugmentSlotChip(augment: NeuralAugment) {
    val borderColor = Color(augment.rarity.colorHex)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0A1325))
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Image(
            painter = painterResource(id = augment.iconRes),
            contentDescription = augment.title,
            modifier = Modifier.size(12.dp),
            contentScale = ContentScale.Fit
        )
        Text(
            text = augment.title,
            color = Color.White,
            fontSize = 8.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false
        )
    }
}

@Composable
private fun EmptySlotChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x3306101E))
            .border(1.dp, Color(0x665C8599), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color(0xFF5C8599),
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false
        )
    }
}

@Composable
private fun StageNodeAnchor(
    node: LevelNodeSpec,
    progress: LevelProgressRecord,
    sectorColor: Color,
    pulseProgress: Float,
    glowAlpha: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = BiasAlignment(horizontalBias = node.normalizedX * 2 - 1, verticalBias = 0f)
    ) {
        CyberStageNode(
            node = node,
            progress = progress,
            isCurrentActive = progress.isUnlocked && !progress.isCompleted,
            sectorColor = sectorColor,
            pulseProgress = pulseProgress,
            glowAlpha = glowAlpha,
            onClick = onClick
        )
    }
}

@Composable
private fun CyberStageNode(
    node: LevelNodeSpec,
    progress: LevelProgressRecord,
    isCurrentActive: Boolean,
    sectorColor: Color,
    pulseProgress: Float,
    glowAlpha: Float,
    onClick: () -> Unit
) {
    val nodeSize = if (node.isBossLevel) 66.dp else 54.dp
    val isDraftNode = node.levelInSector == 3 || node.levelInSector == 6

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        if (isDraftNode) {
            Text(
                text = "⚡ DRAFT",
                color = Color(0xFFFFD600),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        Box(
            modifier = Modifier.size(nodeSize + 24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Expanding Active Radar Beacon
            if (isCurrentActive) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxRadius = size.minDimension / 2f
                    val currentRadius = maxRadius * pulseProgress
                    drawCircle(
                        color = sectorColor.copy(alpha = (1f - pulseProgress) * 0.7f),
                        radius = currentRadius,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Main Hexagonal/Chamfered Node Core
            val coreBorderColor = when {
                node.isBossLevel -> if (progress.isCompleted) NeonGold else NeonRed
                progress.isCompleted -> NeonGold
                progress.isUnlocked -> sectorColor.copy(alpha = glowAlpha)
                else -> Color(0xFF1E293B)
            }

            val coreBgColor = when {
                progress.isCompleted -> Color(0x33FFD600)
                progress.isUnlocked -> Color(0x3300E5FF)
                else -> Color(0x80080D1A)
            }

            Box(
                modifier = Modifier
                    .size(nodeSize)
                    .background(coreBgColor, CyberChamferShape)
                    .border(if (node.isBossLevel) 2.5.dp else 1.5.dp, coreBorderColor, CyberChamferShape),
                contentAlignment = Alignment.Center
            ) {
                when {
                    !progress.isUnlocked -> {
                        Text("🔒", fontSize = 14.sp)
                    }
                    node.isBossLevel -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "BOSS",
                                color = if (progress.isCompleted) NeonGold else NeonRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "CORE",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = "${node.levelInSector}",
                            color = if (progress.isCompleted) NeonGold else Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 3-Star Rating Badges
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.offset(y = (-4).dp)
        ) {
            for (s in 1..3) {
                Text(
                    text = "★",
                    color = if (progress.starsEarned >= s) NeonGold else Color(0xFF24344D),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun SectorTopBar(
    sector: SectorSpec,
    allSectors: List<SectorSpec>,
    totalStars: Int,
    isNextUnlocked: Boolean,
    onSectorChanged: (Int) -> Unit,
    onShowTeaserModal: (Int) -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xF2040812))
            .border(1.dp, CardBorder)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: ALWAYS Return to HUB
        Box(
            modifier = Modifier
                .background(Color(0x3300E5FF), CyberChamferShape)
                .border(1.dp, NeonCyan, CyberChamferShape)
                .clickable {
                    SfxManager.playSfx(SfxType.UI_CONFIRM)
                    onBack()
                }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("< HUB", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }

        // Center: Sector Info & Pagination Controls (◄ Sector Title ►)
        val prevSectorId = sector.sectorId - 1
        val nextSectorId = sector.sectorId + 1

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Previous Sector Arrow
            if (prevSectorId >= 1) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x22FFFFFF))
                        .clickable {
                            SfxManager.playSfx(SfxType.SNAP_TICK)
                            onSectorChanged(prevSectorId)
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text("◄", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Sector Name & Subtitle
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = sector.codename, color = sector.primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                Text(text = sector.subtitle, color = Color(0xFF7E8B9B), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
            }

            // Next Sector Arrow
            if (nextSectorId <= allSectors.size) {
                val isTeaserSector = nextSectorId > 3
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isTeaserSector) Color(0x33FFD600) else if (isNextUnlocked) Color(0x22FFFFFF) else Color(0x22FF0055))
                        .clickable {
                            if (isTeaserSector) {
                                SfxManager.playSfx(SfxType.MODAL_WHOOSH)
                                onShowTeaserModal(nextSectorId)
                            } else if (isNextUnlocked) {
                                SfxManager.playSfx(SfxType.SNAP_TICK)
                                onSectorChanged(nextSectorId)
                            } else {
                                SfxManager.playSfx(SfxType.INVALID_MOVE)
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isTeaserSector) "🔒" else "►",
                        color = if (isTeaserSector) Color(0xFFFFD600) else if (isNextUnlocked) Color(0xFF00E5FF) else Color(0xFFFF0055),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Right: Total Stars Badge
        Row(
            modifier = Modifier
                .background(Color(0x33FFD600), CyberChamferShape)
                .border(1.dp, NeonGold, CyberChamferShape)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("★", color = NeonGold, fontSize = 11.sp)
            Text("$totalStars", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        }
    }
}
