package com.example.gridsurge.armory.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import com.example.gridsurge.armory.model.ArmoryMatrixConfig
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.R
import com.example.gridsurge.armory.data.ArmoryCatalog
import com.example.gridsurge.armory.model.ArmoryItem
import com.example.gridsurge.armory.model.ArmoryTab
import com.example.gridsurge.armory.model.ItemRarity
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.audio.VoxAction
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.ui.Screen
import com.example.gridsurge.ui.components.StarVaultPill
import com.example.gridsurge.ui.modifiers.cyberBorderGlow
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CyberArmoryScreen(
    profileManager: PlayerProfileManager,
    onNavigate: (Screen) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val stars by profileManager.starCurrency.collectAsState()
    val equippedSkinId by profileManager.equippedBlockSkinId.collectAsState()

    var activeTab by remember { mutableStateOf(ArmoryTab.BLOCK_SKINS) }
    var selectedItem by remember {
        mutableStateOf<ArmoryItem>(
            ArmoryCatalog.BLOCK_SKINS.firstOrNull { it.id == equippedSkinId } ?: ArmoryCatalog.BLOCK_SKINS[0]
        )
    }

    val unlockedItemIds by profileManager.unlockedItemIds.collectAsState()
    val equippedVoxId by profileManager.equippedVoxPackId.collectAsState()

    var isAuditioningVox by remember { mutableStateOf(false) }

    val activeAccentColor = selectedItem.rarity.primaryColor

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040711))
    ) {
        // Perspective Ambient Horizon Lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val step = h / 16f
            for (i in 0..16) {
                val y = i * step
                drawLine(
                    color = activeAccentColor.copy(alpha = 0.035f),
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
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ==================== 1. TOP TELEMETRY ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
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
                            onNavigate(Screen.MAIN_MENU)
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
                        text = "CYBER ARMORY",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "HARDWARE & NEURAL CALIBRATION",
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
                        onNavigate(Screen.STORE)
                    }
                )
            }

            // ==================== 2. SECTOR TAB SELECTOR ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xCC0A101C))
                    .border(1.dp, Color(0xFF1B2A40), RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(ArmoryTab.BLOCK_SKINS, ArmoryTab.VOX_COMMS).forEach { tab ->
                    val isSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.16f) else Color.Transparent
                            )
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) Color(0xFF00E5FF) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (!isSelected) {
                                    SfxManager.playSfx(SfxType.CAROUSEL_SNAP)
                                    activeTab = tab
                                    selectedItem = if (tab == ArmoryTab.BLOCK_SKINS) {
                                        ArmoryCatalog.BLOCK_SKINS.firstOrNull { it.id == equippedSkinId } ?: ArmoryCatalog.BLOCK_SKINS[0]
                                    } else {
                                        ArmoryCatalog.VOX_COMMS.firstOrNull { it.id == equippedVoxId } ?: ArmoryCatalog.VOX_COMMS[0]
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF8FA3BF),
                            fontSize = 11.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            // ==================== 3. HOLOGRAPHIC STAGE VIEWPORT (EXPANDED) ====================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(205.dp)
                    .cyberBorderGlow(
                        colors = listOf(selectedItem.rarity.primaryColor, Color.Transparent),
                        cornerRadius = 14.dp
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xF20E1626), Color(0xFD050912))
                        )
                    )
                    .border(1.2.dp, selectedItem.rarity.primaryColor.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Projection Core (4x4 Simulation Grid or Audio Spectrum)
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xCC060B14))
                            .border(1.dp, selectedItem.rarity.primaryColor.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedItem is ArmoryItem.BlockSkinItem) {
                            val skinItem = selectedItem as ArmoryItem.BlockSkinItem
                            TacticalMatrixSimulation(
                                glowColor = skinItem.glowColor,
                                skinRes = skinItem.skinRes,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (selectedItem is ArmoryItem.VoxAnnouncerItem) {
                            NeuralVoiceSpectrum(
                                isAuditioning = isAuditioningVox,
                                accentColor = selectedItem.rarity.primaryColor,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Right Telemetry Details
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(selectedItem.rarity.primaryColor.copy(alpha = 0.2f))
                                    .border(0.5.dp, selectedItem.rarity.primaryColor, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = selectedItem.rarity.label,
                                    color = selectedItem.rarity.primaryColor,
                                    fontSize = 8.sp,
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            if (selectedItem is ArmoryItem.VoxAnnouncerItem) {
                                Text(
                                    text = (selectedItem as ArmoryItem.VoxAnnouncerItem).frequencyBand,
                                    color = Color(0xFF8FA3BF),
                                    fontSize = 8.sp,
                                    fontFamily = ChakraPetchFontFamily
                                )
                            }
                        }

                        Text(
                            text = selectedItem.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = selectedItem.callsign,
                            color = selectedItem.rarity.primaryColor,
                            fontSize = 9.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )

                        Text(
                            text = selectedItem.description,
                            color = Color(0xFF8FA3BF),
                            fontSize = 10.sp,
                            fontFamily = ChakraPetchFontFamily,
                            lineHeight = 13.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (selectedItem is ArmoryItem.VoxAnnouncerItem) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isAuditioningVox) selectedItem.rarity.primaryColor else Color(0xFF132034)
                                    )
                                    .border(
                                        0.8.dp,
                                        if (isAuditioningVox) Color.White else selectedItem.rarity.primaryColor,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        coroutineScope.launch {
                                            isAuditioningVox = true
                                            SfxManager.auditionVoxPack(selectedItem.id, VoxAction.OVERDRIVE)
                                            delay(2400)
                                            isAuditioningVox = false
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isAuditioningVox) "● AUDITIONING..." else "▶ TEST AUDIO FEED",
                                    color = if (isAuditioningVox) Color(0xFF040711) else Color.White,
                                    fontSize = 9.sp,
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ==================== 4. HARDWARE SPECIFICATION GRID ====================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 6.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val currentItems = if (activeTab == ArmoryTab.BLOCK_SKINS) {
                        ArmoryCatalog.BLOCK_SKINS
                    } else {
                        ArmoryCatalog.VOX_COMMS
                    }

                    items(currentItems, key = { it.id }) { item ->
                        val isEquipped = if (item is ArmoryItem.BlockSkinItem) {
                            item.id == equippedSkinId
                        } else {
                            item.id == equippedVoxId
                        }

                        val isUnlocked = unlockedItemIds.contains(item.id) || item.priceStars == 0
                        val isSelected = item.id == selectedItem.id

                        ArmoryGridCard(
                            item = item,
                            isSelected = isSelected,
                            isEquipped = isEquipped,
                            isUnlocked = isUnlocked,
                            onClick = {
                                SfxManager.playSfx(SfxType.CAROUSEL_SNAP)
                                selectedItem = item
                            }
                        )
                    }
                }
            }

            // ==================== 5. OPERATIVE TELEMETRY STATUS STRIP ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x990C121E))
                    .border(0.8.dp, Color(0xFF1B283A), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SECTOR ARMORY // STATUS: ONLINE",
                    color = Color(0xFF8FA3BF),
                    fontSize = 9.sp,
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (activeTab == ArmoryTab.BLOCK_SKINS) {
                        val unlockedCount = ArmoryCatalog.BLOCK_SKINS.count { unlockedItemIds.contains(it.id) || it.priceStars == 0 }
                        "UNLOCKED: $unlockedCount/${ArmoryCatalog.BLOCK_SKINS.size}"
                    } else {
                        val unlockedCount = ArmoryCatalog.VOX_COMMS.count { unlockedItemIds.contains(it.id) || it.priceStars == 0 }
                        "UNLOCKED: $unlockedCount/${ArmoryCatalog.VOX_COMMS.size}"
                    },
                    color = Color(0xFF00E5FF),
                    fontSize = 9.sp,
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }

            // ==================== 6. ENGAGEMENT ACTION FOOTER ====================
            val isSelectedItemUnlocked = unlockedItemIds.contains(selectedItem.id) || selectedItem.priceStars == 0
            val isSelectedItemEquipped = if (selectedItem is ArmoryItem.BlockSkinItem) {
                selectedItem.id == equippedSkinId
            } else {
                selectedItem.id == equippedVoxId
            }

            ArmoryActionFooter(
                item = selectedItem,
                currentStars = stars.toLong(),
                isUnlocked = isSelectedItemUnlocked,
                isEquipped = isSelectedItemEquipped,
                onEquip = {
                    SfxManager.playSfx(SfxType.MODE_LOCK_IN)
                    if (selectedItem is ArmoryItem.BlockSkinItem) {
                        profileManager.equipSkin(selectedItem.id)
                    } else {
                        profileManager.equipVox(selectedItem.id)
                    }
                },
                onUnlock = {
                    if (stars >= selectedItem.priceStars) {
                        SfxManager.playSfx(SfxType.LEVEL_COMPLETE)
                        coroutineScope.launch {
                            profileManager.unlockItem(selectedItem.id, selectedItem.priceStars)
                            if (selectedItem is ArmoryItem.BlockSkinItem) {
                                profileManager.equipSkin(selectedItem.id)
                            } else {
                                profileManager.equipVox(selectedItem.id)
                            }
                        }
                    } else {
                        SfxManager.playSfx(SfxType.UI_CONFIRM)
                        onNavigate(Screen.STORE)
                    }
                }
            )
        }
    }
}

/**
 * Tactical 4x4 Mini-Grid Puzzle Simulation on GPU Canvas.
 */
@Composable
private fun TacticalMatrixSimulation(
    glowColor: Color,
    @DrawableRes skinRes: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "matrixPulse")

    // Primary Piece Pulse (Active Hero Piece)
    val activePulse by infiniteTransition.animateFloat(
        initialValue = 0.80f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "activePulse"
    )

    // Secondary Settled Piece Pulse (Out of Phase)
    val settledPulse by infiniteTransition.animateFloat(
        initialValue = 0.50f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "settledPulse"
    )

    val skinPainter = painterResource(id = skinRes)
    val simulationCells = remember { ArmoryMatrixConfig.SIMULATION_CELLS }

    Canvas(modifier = modifier.fillMaxSize().padding(10.dp)) {
        val w = size.width
        val cols = 4
        val gap = 4.dp.toPx()
        val cellSize = (w - (gap * (cols - 1))) / cols

        // 1. Draw 4x4 Empty Grid Chassis
        for (r in 0 until cols) {
            for (c in 0 until cols) {
                val x = c * (cellSize + gap)
                val y = r * (cellSize + gap)

                drawRoundRect(
                    color = Color(0xFF0C1422),
                    topLeft = Offset(x, y),
                    size = Size(cellSize, cellSize),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        // 2. Draw Active Polyomino Cells with Skin Artwork & Piece Boundaries
        simulationCells.forEach { cell ->
            val x = cell.col * (cellSize + gap)
            val y = cell.row * (cellSize + gap)
            val isHeroPiece = cell.pieceId == 1
            val pulse = if (isHeroPiece) activePulse else settledPulse
            val cellColor = if (isHeroPiece) glowColor else glowColor.copy(alpha = 0.75f)

            // Cell Obsidian Substrate
            drawRoundRect(
                color = Color(0xFF070D18),
                topLeft = Offset(x, y),
                size = Size(cellSize, cellSize),
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
            )

            // Dynamic Radial Core Glow
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        cellColor.copy(alpha = 0.45f * pulse),
                        Color.Transparent
                    ),
                    center = Offset(x + cellSize / 2f, y + cellSize / 2f),
                    radius = cellSize * 0.75f
                ),
                topLeft = Offset(x, y),
                size = Size(cellSize, cellSize),
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
            )

            // STAMP THE ACTUAL SKIN TEXTURE INSIDE THE CELL
            val texturePadding = cellSize * 0.08f
            val textureSize = cellSize - (texturePadding * 2f)
            translate(left = x + texturePadding, top = y + texturePadding) {
                with(skinPainter) {
                    draw(
                        size = Size(textureSize, textureSize),
                        alpha = if (isHeroPiece) 1.0f * pulse else 0.75f * pulse
                    )
                }
            }

            // High-Refraction Inner Bevel Highlight (Top/Left Chamfer)
            drawLine(
                color = Color.White.copy(alpha = if (isHeroPiece) 0.40f * pulse else 0.15f),
                start = Offset(x + 3.dp.toPx(), y + 3.dp.toPx()),
                end = Offset(x + cellSize - 3.dp.toPx(), y + 3.dp.toPx()),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = if (isHeroPiece) 0.40f * pulse else 0.15f),
                start = Offset(x + 3.dp.toPx(), y + 3.dp.toPx()),
                end = Offset(x + 3.dp.toPx(), y + cellSize - 3.dp.toPx()),
                strokeWidth = 1.5.dp.toPx()
            )

            // Outer Neon Border Frame
            drawRoundRect(
                color = cellColor.copy(alpha = if (isHeroPiece) 0.95f * pulse else 0.55f * pulse),
                topLeft = Offset(x, y),
                size = Size(cellSize, cellSize),
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                style = Stroke(width = if (isHeroPiece) 1.8.dp.toPx() else 1.2.dp.toPx())
            )
        }
    }
}

/**
 * High-Density Neural Audio Spectrum Analyzer for Vox Comms.
 */
@Composable
private fun NeuralVoiceSpectrum(
    isAuditioning: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voiceFreq")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isAuditioning) 350 else 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            val w = size.width
            val h = size.height
            val centerY = h / 2f
            val bars = 24
            val barWidth = (w / bars) * 0.65f
            val step = w / bars

            if (isAuditioning) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(w / 2f, h / 2f),
                        radius = (w * 0.55f) * pulseScale
                    )
                )
            }

            for (i in 0 until bars) {
                val normX = i.toFloat() / bars
                val window = sin(normX * 3.1415f)
                val wave = if (isAuditioning) {
                    sin(normX * 16f + phase) * 0.45f + cos(normX * 28f - phase * 1.5f) * 0.4f + 0.35f
                } else {
                    sin(normX * 6f + phase) * 0.15f + 0.1f
                }

                val barH = (h * 0.85f * wave * window).coerceIn(4f, h * 0.95f)
                val x = i * step + (step - barWidth) / 2f

                val topColor = if (isAuditioning) Color.White else accentColor
                val bottomColor = accentColor.copy(alpha = if (isAuditioning) 0.6f else 0.25f)

                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(topColor, bottomColor),
                        startY = centerY - barH / 2f,
                        endY = centerY + barH / 2f
                    ),
                    start = Offset(x, centerY - barH / 2f),
                    end = Offset(x, centerY + barH / 2f),
                    strokeWidth = barWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Calibrated Armory Grid Item Card (125dp Height).
 */
@Composable
private fun ArmoryGridCard(
    item: ArmoryItem,
    isSelected: Boolean,
    isEquipped: Boolean,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(125.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    Brush.verticalGradient(
                        listOf(item.rarity.primaryColor.copy(alpha = 0.12f), Color(0xCC090E1A))
                    )
                } else {
                    SolidColor(Color(0xCC090E1A))
                }
            )
            .border(
                width = if (isSelected) 1.5.dp else 0.8.dp,
                color = when {
                    isSelected -> item.rarity.primaryColor
                    isEquipped -> Color(0xFF00E5FF)
                    else -> Color(0xFF1B283C)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Asset Glyph Slot
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF060A14))
                        .border(0.8.dp, item.rarity.primaryColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (item is ArmoryItem.BlockSkinItem) {
                        Image(
                            painter = painterResource(id = item.skinRes),
                            contentDescription = item.name,
                            modifier = Modifier
                                .size(30.dp)
                                .graphicsLayer { blendMode = BlendMode.Screen }
                        )
                    } else {
                        // Neural Frequency Icon for Vox
                        Canvas(modifier = Modifier.size(24.dp)) {
                            val bars = 4
                            val sp = size.width / bars
                            for (b in 0 until bars) {
                                val bh = size.height * (0.35f + 0.18f * b)
                                drawLine(
                                    color = item.rarity.primaryColor,
                                    start = Offset(b * sp + 3f, (size.height - bh) / 2f),
                                    end = Offset(b * sp + 3f, (size.height + bh) / 2f),
                                    strokeWidth = 2.5f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }

                // Status Tag / Price Tag
                when {
                    isEquipped -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                                .border(0.5.dp, Color(0xFF00E5FF), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "EQUIPPED",
                                color = Color(0xFF00E5FF),
                                fontSize = 8.sp,
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    !isUnlocked -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x99121B2A))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "${item.priceStars} ★",
                                color = Color(0xFFFFB300),
                                fontSize = 9.sp,
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = "UNLOCKED",
                            color = Color(0xFF8FA3BF),
                            fontSize = 8.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.callsign,
                    color = item.rarity.primaryColor,
                    fontSize = 8.sp,
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Adaptive Action Footer with Cost & State Interpolation.
 */
@Composable
private fun ArmoryActionFooter(
    item: ArmoryItem,
    currentStars: Long,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    onEquip: () -> Unit,
    onUnlock: () -> Unit
) {
    val canAfford = currentStars >= item.priceStars
    val buttonColor = when {
        isEquipped -> Color(0xFF1A2638)
        isUnlocked -> Color(0xFF00E5FF)
        canAfford -> Color(0xFFFFB300)
        else -> Color(0xFFFF1744)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isEquipped) Color(0x99101826) else buttonColor
            )
            .border(
                width = 1.dp,
                color = if (isEquipped) Color(0xFF1E2F46) else buttonColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !isEquipped) {
                if (isUnlocked) onEquip() else onUnlock()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when {
                isEquipped -> "EQUIPPED"
                isUnlocked -> "▶ EQUIP PROTOCOL"
                canAfford -> "▶ UNLOCK FOR ${item.priceStars} ★"
                else -> "INSUFFICIENT STARS (${item.priceStars - currentStars} ★ NEEDED)"
            },
            color = if (isEquipped) Color(0xFF8FA3BF) else Color(0xFF050811),
            fontSize = 13.sp,
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}
