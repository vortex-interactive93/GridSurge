package com.example.gridsurge.ui.clash.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.R

enum class OperatorFeatBadge(
    val badgeId: String,
    val title: String,
    @DrawableRes val drawableRes: Int,
    val badgeColor: Color,
    val description: String
) {
    DECA_SURGE("DECA_SURGE", "10x Surge Combo", R.drawable.deca_surge_sigil, Color(0xFFFFD600), "Reach a 10.0x Overdrive combo streak in live combat."),
    GRID_NULLIFIER("GRID_NULLIFIER", "Quad Line Clear", R.drawable.grid_nuillifier_sigil, Color(0xFF00E5FF), "Clear 4 lines simultaneously in a single placement."),
    CLUTCH_HERO("CLUTCH_HERO", "Last 5s Win", R.drawable.clutch_hero_sigil, Color(0xFFFF0055), "Secure a comeback victory with ≤ 5.0 seconds remaining."),
    FOUNDER("FOUNDER", "Founder Alpha", R.drawable.founder_sigil, Color(0xFFE0AAFF), "Account tagged with exclusive Founder Alpha status."),
    APEX_PREDATOR("APEX_PREDATOR", "Apex Predator", R.drawable.apex_predator_sigil, Color(0xFFFF9E00), "Achieve and maintain a 10-match win streak in Blitz Clash."),
    MATRIX_CLEANSE("MATRIX_CLEANSE", "Matrix Cleanse", R.drawable.matrix_cleanse_sigil, Color(0xFF00E676), "Completely clear all active blocks off the grid (Perfect Clear)."),
    FLAWLESS_VICTORY("FLAWLESS_VICTORY", "Flawless Victory", R.drawable.flawless_victory_sigil, Color(0xFFE2E8F0), "Win a match without taking any incoming rival line attacks."),
    JAMMER_DEFUSE("JAMMER_DEFUSE", "Jammer Defuse", R.drawable.jammer_defuse_sigil, Color(0xFFFF5722), "Clear a row or column currently locked by a Stasis Jammer."),
    REDLINE_SURVIVOR("REDLINE_SURVIVOR", "Redline Survivor", R.drawable.redline_survivor_sigl, Color(0xFFFF1744), "Win a match after board fill exceeded 85% capacity."),
    KINETIC_BREACH("KINETIC_BREACH", "Kinetic Breach", R.drawable.kinetic_breach_sigil, Color(0xFFD500F9), "Trigger a 3-stage or higher cascade clear from one placement."),
    INTERCEPTOR("INTERCEPTOR", "Interceptor", R.drawable.interceptor_sigil, Color(0xFF2979FF), "Break a rival's 5x+ combo streak with an attack line or jammer."),
    HYPER_THROUGHPUT("HYPER_THROUGHPUT", "Hyper Throughput", R.drawable.hyper_throughput_sigil, Color(0xFFAEEA00), "Place ≥ 35 blocks and clear ≥ 8 lines in under 45 seconds.");

    companion object {
        fun fromId(id: String): OperatorFeatBadge {
            return entries.find { it.badgeId.equals(id, ignoreCase = true) } ?: DECA_SURGE
        }
    }
}

@Composable
fun FeatSigilIcon(
    badge: OperatorFeatBadge,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = badge.drawableRes),
        contentDescription = badge.title,
        modifier = modifier.graphicsLayer {
            blendMode = BlendMode.Screen
        }
    )
}

@Composable
fun OverflowSafeFeatRibbon(
    badge: OperatorFeatBadge,
    modifier: Modifier = Modifier
) {
    val titleLen = badge.title.length
    val fontSize = when {
        titleLen > 12 -> 8.5.sp
        titleLen > 10 -> 9.5.sp
        else -> 10.5.sp
    }
    val letterSpacing = when {
        titleLen > 12 -> (-0.4).sp
        titleLen > 10 -> (-0.2).sp
        else -> 0.1.sp
    }
    val endPadding = when {
        titleLen > 12 -> 3.dp
        titleLen > 10 -> 4.dp
        else -> 6.dp
    }

    Surface(
        shape = CutCornerShape(topStart = 3.dp, bottomEnd = 3.dp),
        color = Color(0xF0080E18),
        border = BorderStroke(1.dp, badge.badgeColor.copy(alpha = 0.65f)),
        modifier = modifier.height(26.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = endPadding)
        ) {
            // Left illuminated accent bar
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .background(badge.badgeColor)
            )

            Spacer(modifier = Modifier.width(3.dp))

            // Micro-Sigil Icon
            FeatSigilIcon(
                badge = badge,
                modifier = Modifier.size(14.dp)
            )

            Spacer(modifier = Modifier.width(3.dp))

            // Title with dynamic sizing to prevent wrapping
            Text(
                text = badge.title.uppercase(),
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = letterSpacing,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

data class OperatorCardData(
    val callsign: String,
    val level: Int,
    val tierTitle: String,
    val mmr: Int,
    val winLossRatio: String,
    val equippedBadges: List<OperatorFeatBadge>,
    val isLocalPlayer: Boolean,
    val avatarResId: Int = R.drawable.avatar_caucasian_male,
    val rankCrestResId: Int = R.drawable.ic_rank_crest_gold
)

@Composable
fun TacticalOperatorCard(
    data: OperatorCardData,
    modifier: Modifier = Modifier
) {
    val accentColor = if (data.isLocalPlayer) Color(0xFF00E5FF) else Color(0xFFFF0055)
    val cardShape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp)

    Surface(
        shape = cardShape,
        color = Color(0xF5060A12),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.8f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background tactical watermark crest (96dp low opacity behind card body)
            Image(
                painter = painterResource(id = data.rankCrestResId),
                contentDescription = null,
                alpha = 0.08f,
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Top Header Row: Avatar, Identity, Stats, Crest
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Operator Avatar Box with Bottom-End Level Pip
                    Box(
                        modifier = Modifier.size(48.dp)
                    ) {
                        Surface(
                            shape = CutCornerShape(6.dp),
                            color = Color(0xFF0D1524),
                            border = BorderStroke(1.5.dp, accentColor),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Image(
                                painter = painterResource(id = data.avatarResId),
                                contentDescription = "${data.callsign} Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Bottom-End Level Pip
                        Surface(
                            shape = CutCornerShape(2.dp),
                            color = Color(0xFF04060A),
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 3.dp, y = 3.dp)
                        ) {
                            Text(
                                text = "${data.level}",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Player Name & Rank Telemetry
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = data.callsign.uppercase(),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${data.tierTitle} //${data.mmr} MMR",
                            color = accentColor.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Rank Crest Graphic
                    Image(
                        painter = painterResource(id = data.rankCrestResId),
                        contentDescription = data.tierTitle,
                        modifier = Modifier.size(30.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // W/L Badge Pill
                    Surface(
                        shape = CutCornerShape(3.dp),
                        color = Color(0x33000000),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = data.winLossRatio,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Row: 3 Equippable Feat Ribbons (Overflow Safe)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    data.equippedBadges.take(3).forEach { badge ->
                        OverflowSafeFeatRibbon(
                            badge = badge,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    repeat((3 - data.equippedBadges.take(3).size).coerceAtLeast(0)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                                .clip(CutCornerShape(topStart = 3.dp, bottomEnd = 3.dp))
                                .background(Color(0x33000000))
                                .border(1.dp, Color(0x22FFFFFF), CutCornerShape(topStart = 3.dp, bottomEnd = 3.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "EMPTY",
                                color = Color.DarkGray,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
