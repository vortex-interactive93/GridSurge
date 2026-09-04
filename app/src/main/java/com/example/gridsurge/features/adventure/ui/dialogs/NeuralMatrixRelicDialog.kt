package com.example.gridsurge.features.adventure.ui.dialogs

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.ui.CyberActionButton
import com.example.gridsurge.ui.CyberChamferShape

// --- Updated Data Model with 9 Pre-Sliced Fragment Resource IDs ---

data class RelicSpec(
    val id: String,
    val sectorNumber: Int,
    val name: String,
    val codeName: String,
    val description: String,
    val totalFragments: Int = 9,
    val starsPerFragment: Int = 3, // 27 stars total for 9 fragments
    val requiredStarsForBoss: Int = 18, // 2/3 of sector stars
    val fragmentDrawables: List<Int>, // Size 9: indices 0..8 mapped Top-Left to Bottom-Right
    val rewardTitle: String = "CONDUIT OPERATIVE",
    val rewardBadgeRes: Int = 0,
    val rewardStars: Int = 100
)

data class NeuralMatrixState(
    val relic: RelicSpec,
    val totalStarsEarnedInSector: Int,
    val totalStarsInSector: Int = 18,
    val completedStagesCount: Int = 0,
    val totalStagesInSector: Int = 9,
    val isBossDefeated: Boolean = false,
    val isRewardClaimed: Boolean = false
) {
    val unlockedFragmentsCount: Int
        get() = (totalStarsEarnedInSector / relic.starsPerFragment).coerceAtMost(relic.totalFragments)

    val isMatrixComplete: Boolean
        get() = unlockedFragmentsCount >= relic.totalFragments

    val isBossUnlocked: Boolean
        get() = totalStarsEarnedInSector >= relic.requiredStarsForBoss
}

// --- Main Matrix UI Dialog ---

@Composable
fun NeuralMatrixRelicDialog(
    matrixState: NeuralMatrixState,
    isEquipped: Boolean = false,
    onDismiss: () -> Unit,
    onEngageBoss: () -> Unit,
    onClaimReward: () -> Unit,
    onEquipRelic: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.78f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF091424), Color(0xFF03070E))
                        ),
                        shape = CyberChamferShape
                    )
                    .border(1.5.dp, Color(0xFF00E5FF), CyberChamferShape)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Protocol Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SECTOR 0${matrixState.relic.sectorNumber} // RELIC MATRIX",
                            color = Color(0xFF00E5FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = matrixState.relic.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "✕",
                        color = Color(0xFF5C8599),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Star Progress & Integrity Meter
                MatrixStarTracker(matrixState = matrixState)

                Spacer(modifier = Modifier.height(18.dp))

                // 3x3 Holographic Fragment Reconstruction Grid
                Box(
                    modifier = Modifier
                        .size(270.dp)
                        .background(Color(0xFF050B14), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF16253B), RoundedCornerShape(10.dp))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RelicFragmentGrid(matrixState = matrixState)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tactical Directive / Status Footer
                MatrixStatusBanner(matrixState = matrixState)

                Spacer(modifier = Modifier.height(16.dp))

                // Action Gateway
                when {
                    matrixState.isMatrixComplete && !matrixState.isRewardClaimed -> {
                        CyberActionButton(
                            text = "CLAIM RELIC HARDWARE ►",
                            primaryColor = Color(0xFF00FF66),
                            isPrimary = true,
                            onClick = {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onClaimReward()
                            }
                        )
                    }
                    matrixState.isRewardClaimed -> {
                        if (isEquipped) {
                            CyberActionButton(
                                text = "EQUIPPED [ACTIVE] ✓",
                                primaryColor = Color(0xFF00E5FF),
                                isPrimary = false,
                                onClick = onDismiss
                            )
                        } else {
                            CyberActionButton(
                                text = "EQUIP ${matrixState.relic.name} ►",
                                primaryColor = Color(0xFFFFD600),
                                isPrimary = true,
                                onClick = {
                                    SfxManager.playSfx(SfxType.UI_CONFIRM)
                                    SfxManager.playSfx(SfxType.OVERDRIVE_ACTIVATE)
                                    onEquipRelic()
                                }
                            )
                        }
                    }
                    matrixState.isBossUnlocked && !matrixState.isBossDefeated -> {
                        CyberActionButton(
                            text = "ENGAGE GUARDIAN BOSS ►",
                            primaryColor = Color(0xFFFF0055),
                            isPrimary = true,
                            onClick = {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onEngageBoss()
                            }
                        )
                    }
                    else -> {
                        CyberActionButton(
                            text = "RETURN TO CONDUIT",
                            primaryColor = Color(0xFF5C8599),
                            isPrimary = false,
                            onClick = onDismiss
                        )
                    }
                }
            }
        }
    }
}

// --- 3x3 Fragment Matrix Grid ---

@Composable
fun RelicFragmentGrid(matrixState: NeuralMatrixState) {
    val totalCols = 3
    val totalRows = 3

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (r in 0 until totalRows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (c in 0 until totalCols) {
                    val fragmentIndex = r * totalCols + c
                    val isUnlocked = fragmentIndex < matrixState.unlockedFragmentsCount
                    val requiredStars = (fragmentIndex + 1) * matrixState.relic.starsPerFragment
                    val drawableRes = matrixState.relic.fragmentDrawables.getOrElse(fragmentIndex) { 0 }

                    RelicFragmentCell(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        isUnlocked = isUnlocked,
                        requiredStars = requiredStars,
                        drawableRes = drawableRes
                    )
                }
            }
        }
    }
}

// --- Individual Fragment Cell with Reveal Animation ---

@Composable
fun RelicFragmentCell(
    modifier: Modifier,
    isUnlocked: Boolean,
    requiredStars: Int,
    @DrawableRes drawableRes: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanline")
    val scanlineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanlineProgress"
    )

    val cellScale by animateFloatAsState(
        targetValue = if (isUnlocked) 1f else 0.94f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "cellScale"
    )

    Box(
        modifier = modifier
            .scale(cellScale)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isUnlocked) Color(0xFF0A1422) else Color(0x80040810))
            .border(
                width = if (isUnlocked) 1.dp else 0.5.dp,
                color = if (isUnlocked) Color(0xFF00E5FF) else Color(0xFF152238),
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isUnlocked,
            transitionSpec = {
                (fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.85f))
                    .togetherWith(fadeOut(animationSpec = tween(200)))
            },
            label = "fragmentReveal"
        ) { unlocked ->
            if (unlocked && drawableRes != 0) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = "Relic Fragment",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Subtle cyan neon border overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(
                            color = Color(0x3300E5FF),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Holographic scanlines & reticle
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val step = 14.dp.toPx()
                        val offsetPx = scanlineProgress * step

                        for (x in -size.height.toInt()..size.width.toInt() step step.toInt()) {
                            drawLine(
                                color = Color(0x0A00E5FF),
                                start = Offset(x.toFloat() + offsetPx, 0f),
                                end = Offset(x.toFloat() + offsetPx + size.height, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val len = 4.dp.toPx()
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        drawLine(Color(0x2B5C8599), Offset(cx - len, cy), Offset(cx + len, cy), 1.dp.toPx())
                        drawLine(Color(0x2B5C8599), Offset(cx, cy - len), Offset(cx, cy + len), 1.dp.toPx())
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🔒",
                            fontSize = 11.sp
                        )
                        Text(
                            text = "$requiredStars★",
                            color = Color(0xFF5C8599),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// --- Star Tracker Bar ---

@Composable
fun MatrixStarTracker(matrixState: NeuralMatrixState) {
    val totalSectorStars = 27 // Standardized for Sector 01 (9 stages x 3 stars)
    val progress = (matrixState.totalStarsEarnedInSector.toFloat() / totalSectorStars).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NEURAL SYNC PROGRESS",
                color = Color(0xFF5C8599),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${matrixState.unlockedFragmentsCount}/9 NODES (${matrixState.totalStarsEarnedInSector}/$totalSectorStars ★)",
                color = Color(0xFFFFD600),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFF00FF66),
            trackColor = Color(0xFF101B2B)
        )
    }
}

// --- Status Banner ---

@Composable
fun MatrixStatusBanner(matrixState: NeuralMatrixState) {
    val (statusColor, title, subtitle) = when {
        matrixState.isMatrixComplete && matrixState.isBossDefeated -> Triple(
            Color(0xFF00FF66),
            "RELIC SYNCHRONIZED",
            "Guardian core assimilated. Module ready for deployment in Cyber Armory."
        )
        matrixState.isMatrixComplete -> Triple(
            Color(0xFF00FF66),
            "RELIC BLUEPRINT FULLY DECODED",
            "Hardware ready for sync. Claim module to equip in Cyber Armory."
        )
        matrixState.isBossUnlocked && !matrixState.isBossDefeated -> Triple(
            Color(0xFFFF0055),
            "GUARDIAN FIREWALL DESTABILIZED",
            "Sufficient sector stars acquired. Defeat Boss Core to slot final shard."
        )
        matrixState.isBossUnlocked && matrixState.isBossDefeated -> Triple(
            Color(0xFF00FF66),
            "BOSS CORE NEUTRALIZED",
            "Collect remaining stars to finalize relic reconstruction."
        )
        else -> {
            val starsToNext = matrixState.relic.starsPerFragment - (matrixState.totalStarsEarnedInSector % matrixState.relic.starsPerFragment)
            Triple(
                Color(0xFF00E5FF),
                "RECONSTRUCTING DATA SCHEMATICS",
                "Earn $starsToNext more ★ in Sector 0${matrixState.relic.sectorNumber} to unlock next fragment."
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x1A00E5FF), RoundedCornerShape(8.dp))
            .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = statusColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
        Text(
            text = subtitle,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
