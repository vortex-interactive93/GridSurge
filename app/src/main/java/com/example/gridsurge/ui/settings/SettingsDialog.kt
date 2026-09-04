package com.example.gridsurge.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.R
import com.example.gridsurge.audio.HapticType
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.settings.SettingsManager
import com.example.gridsurge.ui.CyberActionButton

private val NeonCyan = Color(0xFF00E5FF)
private val DarkBackdrop = Color(0xDD040711)
private val RowBackdrop = Color(0x66080D1A)
private val RowBorder = Color(0xFF1B2A42)

@Composable
fun SettingsDialog(
    settingsManager: SettingsManager,
    onDismiss: () -> Unit
) {
    val state by settingsManager.settingsState.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackdrop),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .wrapContentHeight()
            ) {
                // Vector Canvas: Cyber Perimeter & Chamfered Frame
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val cut = 22.dp.toPx()

                    val framePath = Path().apply {
                        moveTo(cut, 0f)
                        lineTo(w - cut, 0f)
                        lineTo(w, cut)
                        lineTo(w, h - cut)
                        lineTo(w - cut, h)
                        lineTo(cut, h)
                        lineTo(0f, h - cut)
                        lineTo(0f, cut)
                        close()
                    }

                    drawPath(
                        path = framePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF0C1322), Color(0xFF050811))
                        )
                    )

                    drawPath(
                        path = framePath,
                        color = Color(0xFF1B2A4A),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Corner Accents
                    val cornerLen = 16.dp.toPx()
                    drawLine(NeonCyan, Offset(0f, cut + cornerLen), Offset(0f, cut), 3.dp.toPx())
                    drawLine(NeonCyan, Offset(0f, cut), Offset(cut, 0f), 3.dp.toPx())
                    drawLine(NeonCyan, Offset(cut, 0f), Offset(cut + cornerLen, 0f), 3.dp.toPx())

                    drawLine(NeonCyan, Offset(w, h - cut - cornerLen), Offset(w, h - cut), 3.dp.toPx())
                    drawLine(NeonCyan, Offset(w, h - cut), Offset(w - cut, h), 3.dp.toPx())
                    drawLine(NeonCyan, Offset(w - cut, h), Offset(w - cut - cornerLen, h), 3.dp.toPx())
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Status
                    Text(
                        text = "SYSTEM CONFIGURATION",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "AUDIO & HAPTICS",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. BGM Slider Row
                    CyberVolumeSlider(
                        label = "BACKGROUND MUSIC",
                        currentVolume = state.bgmVolume,
                        onVolumeChanged = { 
                            settingsManager.setBgmVolume(it)
                            SfxManager.updateBgmVolume(it)
                        },
                        onPreviewTriggered = { /* Real-time update already handled */ },
                        accentColor = Color(0xFFFFB300)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. SFX Slider Row
                    CyberVolumeSlider(
                        label = "SOUND EFFECTS",
                        currentVolume = state.sfxVolume,
                        onVolumeChanged = { settingsManager.setSfxVolume(it) },
                        onPreviewTriggered = { SfxManager.playSfxPreview(it) },
                        accentColor = Color(0xFF00E5FF)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. VOX Tactical AI Slider Row
                    CyberVolumeSlider(
                        label = "TACTICAL VOX AI",
                        currentVolume = state.voxVolume,
                        onVolumeChanged = { settingsManager.setVoxVolume(it) },
                        onPreviewTriggered = { SfxManager.playVoxPreview(it) },
                        accentColor = Color(0xFFFF0055)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Haptics Row (Keeping toggle for haptics)
                    SettingsToggleRow(
                        title = "HAPTIC FEEDBACK",
                        subtitle = "Tactile grid snap & surge rumbles",
                        iconRes = R.drawable.ic_settings_haptics,
                        isChecked = state.isHapticsEnabled,
                        onToggle = { settingsManager.toggleHaptics() }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Dismiss Action
                    CyberActionButton(
                        text = "CONFIRM PROTOCOLS",
                        primaryColor = NeonCyan,
                        isPrimary = true,
                        onClick = {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    @DrawableRes iconRes: Int,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(RowBackdrop)
            .border(1.dp, if (isChecked) Color(0x3300E5FF) else RowBorder, RoundedCornerShape(10.dp))
            .clickable {
                SfxManager.playSfx(SfxType.UI_CONFIRM)
                SfxManager.triggerHaptic(HapticType.LIGHT_TICK)
                onToggle()
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Icon Badge
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, if (isChecked) NeonCyan.copy(alpha = 0.6f) else Color(0xFF263859), RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    color = if (isChecked) Color.White else Color(0xFF8A99AD),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = subtitle,
                    color = if (isChecked) Color(0xFF6B7D99) else Color(0xFF4A5568),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Animated Cyber Switch Pill
        CyberSwitch(
            checked = isChecked,
            onCheckedChange = {
                SfxManager.playSfx(SfxType.UI_CONFIRM)
                SfxManager.triggerHaptic(HapticType.LIGHT_TICK)
                onToggle()
            }
        )
    }
}

@Composable
fun CyberSwitch(
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) Color(0x3300E5FF) else Color(0xFF0F1626),
        animationSpec = tween(200),
        label = "trackColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) NeonCyan else Color(0xFF263859),
        animationSpec = tween(200),
        label = "borderColor"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 2.dp,
        animationSpec = tween(200),
        label = "thumbOffset"
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) NeonCyan else Color(0xFF4A5568),
        animationSpec = tween(200),
        label = "thumbColor"
    )

    Box(
        modifier = Modifier
            .width(50.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(trackColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCheckedChange
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(22.dp)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}
