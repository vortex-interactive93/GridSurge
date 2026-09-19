package com.example.gridsurge.ui.career.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.career.model.DirectiveCategory
import com.example.gridsurge.ui.theme.OrbitronFontFamily

@Composable
fun TacticalCategoryDock(
    selectedCategory: DirectiveCategory,
    onSelectCategory: (DirectiveCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = remember { DirectiveCategory.entries }
    val selectedIndex = categories.indexOf(selectedCategory)

    // Calculate horizontal bias (-1.0f for leftmost tab, +1.0f for rightmost tab)
    val targetBias = if (categories.size > 1) {
        -1f + (2f * selectedIndex / (categories.size - 1))
    } else 0f

    val animatedBias by animateFloatAsState(
        targetValue = targetBias,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tabBias"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xCC0A101C))
            .border(1.dp, Color(0xFF1B2A40), RoundedCornerShape(8.dp))
            .padding(3.dp)
    ) {
        // Smooth Sliding Selection Indicator Pill
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(1f / categories.size)
                .align(BiasAlignment(horizontalBias = animatedBias, verticalBias = 0f))
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF00E5FF).copy(alpha = 0.16f))
                .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(6.dp))
        )

        // Tab Text Row
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEach { category ->
                val isSelected = category == selectedCategory
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isSelected) {
                                SfxManager.playSfx(SfxType.CAROUSEL_SNAP, volume = 0.6f, pitchRate = 1.1f)
                                onSelectCategory(category)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.title,
                        color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF8FA3BF),
                        fontSize = 9.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
