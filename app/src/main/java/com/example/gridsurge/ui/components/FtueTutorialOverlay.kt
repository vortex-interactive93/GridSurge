package com.example.gridsurge.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.ui.CyberChamferShape

@Composable
fun FtueTutorialOverlay(
    isFirstLaunch: Boolean,
    stageIndex: Int,
    onTutorialComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) }

    // Logic to only show tutorial on specific stages
    val shouldShow = isFirstLaunch && when (stageIndex) {
        1 -> step <= 2
        3 -> step <= 1
        else -> false
    }

    if (!shouldShow) return

    val (headline, directive) = when (stageIndex) {
        1 -> when (step) {
            1 -> "DEPLOY MATRIX BLOCKS" to "Drag polyomino shapes onto the 8x8 neural grid."
            else -> "COMPLETE LINES" to "Fill complete rows or columns to clear blocks and stabilize the sector."
        }
        3 -> "NEUTRALIZE CORES" to "Guardian Cores block grid space. Clear lines running through them to crack and destroy them."
        else -> "" to ""
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xCC03060E))
            .clickable {
                SfxManager.playSfx(SfxType.MODAL_WHOOSH)
                val maxSteps = if (stageIndex == 1) 2 else 1
                if (step < maxSteps) step++ else onTutorialComplete()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .background(Color(0xF00A1424), CyberChamferShape)
                .border(1.5.dp, Color(0xFF00E5FF), CyberChamferShape)
                .padding(24.dp)
        ) {
            Text(
                text = "TACTICAL INTEL // 0$step",
                color = Color(0xFF00E5FF),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = headline,
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = directive,
                color = Color(0xFF90A4AE),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(Color(0x2200E5FF), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                val maxSteps = if (stageIndex == 1) 2 else 1
                Text(
                    text = if (step < maxSteps) "PROCEED [TAP]" else "ENGAGE PROTOCOL",
                    color = Color(0xFF00E5FF),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
