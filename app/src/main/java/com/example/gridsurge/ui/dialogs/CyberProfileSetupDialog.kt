package com.example.gridsurge.ui.dialogs

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.R
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.meta.util.CallsignValidationResult
import com.example.gridsurge.meta.util.CallsignValidator
import com.example.gridsurge.ui.CyberChamferShape

data class CyberAvatarPreset(
    val id: String,
    val name: String,
    @DrawableRes val iconRes: Int
)

object CyberAvatarRegistry {
    val PRESETS = listOf(
        CyberAvatarPreset("avatar_asian_male", "KAI // RECON", R.drawable.avatar_asian_male),
        CyberAvatarPreset("avatar_asian_female", "LIN // RECON", R.drawable.avatar_asian_female),
        CyberAvatarPreset("avatar_black_male", "MALIK // VANGUARD", R.drawable.avatar_black_male),
        CyberAvatarPreset("avatar_black_female", "NIA // VANGUARD", R.drawable.avatar_black_female),
        CyberAvatarPreset("avatar_caucasian_male", "COLE // TACTICAL", R.drawable.avatar_caucasian_male),
        CyberAvatarPreset("avatar_caucasian_female", "AVA // TACTICAL", R.drawable.avatar_caucasian_female),
        CyberAvatarPreset("avatar_latino_male", "RAMIREZ // INTERCEPTOR", R.drawable.avatar_latino_male),
        CyberAvatarPreset("avatar_latina_female", "ELENA // INTERCEPTOR", R.drawable.avatar_latina_female),
        CyberAvatarPreset("avatar_middle_eastern_male", "TARIQ // COMMANDER", R.drawable.avatar_middle_eastern_male),
        CyberAvatarPreset("avatar_middle_eastern_female", "AMIRA // COMMANDER", R.drawable.avatar_middle_eastern_female),
        CyberAvatarPreset("avatar_cyber_ninja", "CYBER NINJA", R.drawable.ic_rival_ghost)
    )

    fun getPresetById(id: String): CyberAvatarPreset {
        return PRESETS.firstOrNull { it.id == id } ?: PRESETS.first()
    }
}

@Composable
fun CyberProfileSetupDialog(
    profileManager: PlayerProfileManager,
    isPvpRequiredNotice: Boolean = true,
    onProfileInitialized: () -> Unit,
    onNavigateToAuth: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val currentCallsign by profileManager.callsign.collectAsState()
    val currentAvatarKey by profileManager.avatarKey.collectAsState()
    val activeTitle by profileManager.activeTitle.collectAsState()
    val unlockedTitles by profileManager.unlockedTitles.collectAsState()

    var inputCallsign by remember { mutableStateOf(if (currentCallsign == "OPERATIVE_X") "" else currentCallsign) }
    var selectedAvatarId by remember { mutableStateOf(currentAvatarKey) }
    var selectedTitle by remember { mutableStateOf(activeTitle) }

    val activeAvatar = CyberAvatarRegistry.getPresetById(selectedAvatarId)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE040812))
            .clickable { /* Block dismiss outside */ },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(CyberChamferShape)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xF00D1526), Color(0xFE060A14))
                    )
                )
                .border(1.5.dp, Color(0xFF00E5FF), CyberChamferShape)
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Tag
                Text(
                    text = if (isPvpRequiredNotice) "CYBER PROFILE REQUIRED FOR PVP" else "NEURAL PROFILE CUSTOMIZATION",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "INITIALIZE OPERATIVE IDENT",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                // Avatar Preview Bezel (Flush Bottom Alignment, Bigger Scale)
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CyberChamferShape)
                        .background(Color(0x66141926))
                        .border(2.dp, Color(0xFF00E5FF), CyberChamferShape),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Image(
                        painter = painterResource(id = activeAvatar.iconRes),
                        contentDescription = activeAvatar.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.BottomCenter
                    )
                }

                // Avatar Preset Selector Row
                Text(
                    text = "SELECT AVATAR EMBLEM",
                    color = Color(0xFF90A4AE),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(CyberAvatarRegistry.PRESETS, key = { it.id }) { preset ->
                        val isSelected = preset.id == selectedAvatarId
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0x6600E5FF) else Color(0x22141926))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF26334D),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    SfxManager.playSfx(SfxType.SNAP_TICK)
                                    selectedAvatarId = preset.id
                                },
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Image(
                                painter = painterResource(id = preset.iconRes),
                                contentDescription = preset.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.BottomCenter
                            )
                        }
                    }
                }

                // Callsign Input Field with Live Esports Standard Validation
                val validationResult = remember(inputCallsign) { 
                    CallsignValidator.validate(inputCallsign)
                }
                val isValid = validationResult is CallsignValidationResult.Valid

                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = inputCallsign,
                        onValueChange = { if (it.length <= 16) inputCallsign = it },
                        label = { Text("OPERATIVE CALLSIGN", color = Color(0xFF00E5FF), fontSize = 10.sp) },
                        placeholder = { Text("e.g. Cyber_Ninja.99", color = Color(0xFF5C8599), fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isValid) Color(0xFF00E5FF) else Color(0xFFFF0055),
                            unfocusedBorderColor = if (isValid) Color(0xFF26334D) else Color(0xAAFF0055),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Real-Time Validation Feedback Tag
                    val feedbackText = when (validationResult) {
                        is CallsignValidationResult.Valid -> "✓ Callsign Valid & Ready"
                        is CallsignValidationResult.Invalid -> "✕ ${validationResult.reason}"
                    }
                    val feedbackColor = if (isValid) Color(0xFF00E676) else Color(0xFFFF0055)

                    Text(
                        text = feedbackText,
                        color = feedbackColor,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                // Title Selector Dropdown / Chips
                if (unlockedTitles.isNotEmpty()) {
                    Text(
                        text = "ACTIVE TITLE",
                        color = Color(0xFF90A4AE),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(unlockedTitles.toList(), key = { it }) { title ->
                            val isSelected = title == selectedTitle
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color(0x4400E5FF) else Color(0x22141926))
                                    .border(1.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF26334D), RoundedCornerShape(6.dp))
                                    .clickable {
                                        SfxManager.playSfx(SfxType.SNAP_TICK)
                                        selectedTitle = title
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = title,
                                    color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF78909C),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                if (onNavigateToAuth != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x2200E5FF))
                            .border(1.dp, Color(0x8800E5FF), RoundedCornerShape(8.dp))
                            .clickable {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onNavigateToAuth()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LINK GOOGLE / SOCIAL ACCOUNT →",
                            color = Color(0xFF00E5FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!isPvpRequiredNotice) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x22141926))
                                .border(1.dp, Color(0xFF26334D), RoundedCornerShape(8.dp))
                                .clickable {
                                    SfxManager.playSfx(SfxType.UI_CONFIRM)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("CANCEL", color = Color(0xFF78909C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isValid) {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF00E5FF), Color(0xFF00E676))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(Color(0x3326334D), Color(0x3326334D))
                                    )
                                }
                            )
                            .clickable(enabled = isValid) {
                                SfxManager.playSfx(SfxType.LEVEL_COMPLETE)
                                profileManager.saveCyberProfile(inputCallsign.trim(), selectedAvatarId, selectedTitle)
                                onProfileInitialized()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "INITIALIZE PROFILE",
                            color = if (isValid) Color(0xFF040812) else Color(0xFF5C8599),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
