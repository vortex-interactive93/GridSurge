package com.example.gridsurge.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.features.adventure.model.NeuralAugment
import com.example.gridsurge.ui.CyberChamferShape
import kotlinx.coroutines.delay

@Composable
fun ActiveAugmentsLoadoutRow(
    activeAugments: List<NeuralAugment>,
    modifier: Modifier = Modifier
) {
    if (activeAugments.isEmpty()) return

    var selectedAugmentForTooltip by remember { mutableStateOf<NeuralAugment?>(null) }

    LaunchedEffect(selectedAugmentForTooltip) {
        if (selectedAugmentForTooltip != null) {
            delay(3200)
            selectedAugmentForTooltip = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Row of Chips (Default View)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            activeAugments.forEach { augment ->
                val borderColor = Color(augment.rarity.colorHex)

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0A1325), Color(0xFF030712))
                            )
                        )
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable { selectedAugmentForTooltip = augment }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Image(
                        painter = painterResource(id = augment.iconRes),
                        contentDescription = augment.title,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = augment.title,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Tooltip Banner Overlay (Floating Overlay over Chips - zero height shift)
        AnimatedVisibility(
            visible = selectedAugmentForTooltip != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            selectedAugmentForTooltip?.let { aug ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CyberChamferShape)
                        .background(Color(0xF506101E))
                        .border(1.dp, Color(aug.rarity.colorHex), CyberChamferShape)
                        .clickable { selectedAugmentForTooltip = null }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${aug.title}: ${aug.description}",
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
