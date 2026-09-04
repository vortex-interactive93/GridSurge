package com.example.gridsurge.armory.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.R
import com.example.gridsurge.armory.model.ArmoryCategory
import com.example.gridsurge.armory.model.ArmoryItem
import com.example.gridsurge.armory.model.ArmoryUserState
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.ui.CyberActionButton
import com.example.gridsurge.ui.CyberChamferShape
import java.util.Locale

private val NeonCyan = Color(0xFF00E5FF)
private val NeonGold = Color(0xFFFFD600)
private val NeonRed = Color(0xFFFF0055)
private val DarkVoidBackdrop = Color(0xFF03060E)
private val CardBackground = Color(0x66080E1A)
private val CardBorder = Color(0xFF1B2A42)

@Composable
fun CyberArmoryScreen(
    viewModel: ArmoryViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoidBackdrop)
    ) {
        // Subtle Background Grid Lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 40.dp.toPx()
            for (x in 0..(size.width / step).toInt()) {
                drawLine(
                    color = Color(0x0D00E5FF),
                    start = Offset(x * step, 0f),
                    end = Offset(x * step, size.height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(size.height / step).toInt()) {
                drawLine(
                    color = Color(0x0D00E5FF),
                    start = Offset(0f, y * step),
                    end = Offset(size.width, y * step),
                    strokeWidth = 1f
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 20.dp)
        ) {
            // Top HUD Bar: Back Button, Title, Star Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(CardBackground, CyberChamferShape)
                        .border(1.dp, CardBorder, CyberChamferShape)
                        .clickable {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            onNavigateBack()
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "< BACK",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CYBER ARMORY",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "HARDWARE CUSTOMIZATION",
                        color = NeonCyan,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Currency Pill
                Box(
                    modifier = Modifier
                        .background(Color(0x33FFD600), CyberChamferShape)
                        .border(1.dp, NeonGold, CyberChamferShape)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "★",
                            color = NeonGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = String.format(Locale.US, "%,d", uiState.userState.starsBalance),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Tabs (BLOCK SKINS | VOX ANNOUNCERS)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ArmoryCategory.entries.filter { it != ArmoryCategory.CHASSIS_FRAMES }.forEach { cat ->
                    val isSelected = uiState.selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) Color(0x3300E5FF) else CardBackground,
                                CyberChamferShape
                            )
                            .border(
                                1.dp,
                                if (isSelected) NeonCyan else CardBorder,
                                CyberChamferShape
                            )
                            .clickable { viewModel.selectCategory(cat) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat.title,
                            color = if (isSelected) NeonCyan else Color(0xFF7E8B9B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hero Inspection Viewport (Active Selection Preview)
            HeroPreviewViewport(
                item = uiState.selectedItem,
                userState = uiState.userState
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Feedback / Status Banner
            AnimatedVisibility(
                visible = uiState.feedbackMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = uiState.feedbackMessage ?: "",
                    color = if (uiState.feedbackMessage?.contains("DENIED") == true) NeonRed else NeonGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Catalog Grid View
            val categoryItems = viewModel.getItemsForCategory(uiState.selectedCategory)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categoryItems) { item ->
                    ArmoryCatalogCard(
                        item = item,
                        isSelected = uiState.selectedItem.id == item.id,
                        isUnlocked = uiState.userState.unlockedItemIds.contains(item.id),
                        isEquipped = when (item.category) {
                            ArmoryCategory.BLOCK_SKINS -> uiState.userState.equippedBlockSkinId == item.id
                            ArmoryCategory.VOX_PACKS -> uiState.userState.equippedVoxPackId == item.id
                            else -> false
                        },
                        onSelect = { viewModel.selectItem(item) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Action Button (EQUIP | PURCHASE | EQUIPPED)
            val selectedItem = uiState.selectedItem
            val isUnlocked = uiState.userState.unlockedItemIds.contains(selectedItem.id)
            val isEquipped = when (selectedItem.category) {
                ArmoryCategory.BLOCK_SKINS -> uiState.userState.equippedBlockSkinId == selectedItem.id
                ArmoryCategory.VOX_PACKS -> uiState.userState.equippedVoxPackId == selectedItem.id
                else -> false
            }

            val (buttonText, buttonColor, isPrimary) = when {
                isEquipped -> Triple("EQUIPPED", Color(0xFF475569), false)
                isUnlocked -> Triple("EQUIP ${selectedItem.title}", NeonCyan, true)
                else -> Triple("UNLOCK FOR ${selectedItem.priceStars} ★", NeonGold, true)
            }

            CyberActionButton(
                text = buttonText,
                primaryColor = buttonColor,
                isPrimary = isPrimary,
                onClick = { viewModel.onPrimaryActionClicked() }
            )
        }
    }
}

@Composable
private fun HeroPreviewViewport(
    item: ArmoryItem,
    userState: ArmoryUserState
) {
    val isEquipped = when (item.category) {
        ArmoryCategory.BLOCK_SKINS -> userState.equippedBlockSkinId == item.id
        ArmoryCategory.VOX_PACKS -> userState.equippedVoxPackId == item.id
        else -> false
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF091222), Color(0xFF040810))
                ),
                shape = CyberChamferShape
            )
            .border(1.dp, if (isEquipped) NeonCyan else CardBorder, CyberChamferShape)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preview Asset Graphic (High-Definition 3D Asset Preview)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(Color(0x44000000), CyberChamferShape)
                    .border(1.dp, if (isEquipped) NeonCyan else Color(0x3300E5FF), CyberChamferShape),
                contentAlignment = Alignment.Center
            ) {
                if (item.category == ArmoryCategory.VOX_PACKS) {
                    AudioWaveformGlyph(color = if (isEquipped) NeonCyan else NeonGold)
                } else {
                    Image(
                        painter = painterResource(id = item.previewDrawableRes),
                        contentDescription = item.title,
                        modifier = Modifier.size(72.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Item Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    if (isEquipped) {
                        Text(
                            text = "[ACTIVE]",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = item.subtitle,
                    color = NeonGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.description,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ArmoryCatalogCard(
    item: ArmoryItem,
    isSelected: Boolean,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = when {
        isEquipped -> NeonCyan
        isSelected -> NeonGold
        else -> CardBorder
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .background(if (isSelected) Color(0x1A00E5FF) else CardBackground, CyberChamferShape)
            .border(1.5.dp, borderColor, CyberChamferShape)
            .clickable { onSelect() }
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
                // Item Thumbnail
                Image(
                    painter = painterResource(id = item.previewDrawableRes),
                    contentDescription = item.title,
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Fit
                )

                // Status Tag
                when {
                    isEquipped -> {
                        Text(
                            text = "EQUIPPED",
                            color = NeonCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    isUnlocked -> {
                        Text(
                            text = "UNLOCKED",
                            color = Color(0xFF00FF66),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    else -> {
                        Text(
                            text = "${item.priceStars} ★",
                            color = NeonGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Column {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = item.subtitle,
                    color = Color(0xFF64748B),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun AudioWaveformGlyph(color: Color) {
    Canvas(modifier = Modifier.size(48.dp)) {
        val w = size.width
        val h = size.height
        val barCount = 7
        val barWidth = 3.dp.toPx()
        val spacing = (w - (barCount * barWidth)) / (barCount - 1)

        val heights = floatArrayOf(0.3f, 0.6f, 0.9f, 1.0f, 0.75f, 0.5f, 0.25f)
        for (i in 0 until barCount) {
            val barH = h * heights[i]
            val x = i * (barWidth + spacing)
            val y = (h - barH) / 2f
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
            )
        }
    }
}
