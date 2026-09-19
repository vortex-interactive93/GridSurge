package com.example.gridsurge.ui.dialogs

import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.R
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.meta.engine.OperativeTelemetryEngine
import com.example.gridsurge.meta.engine.RenameEconomyEngine
import com.example.gridsurge.network.SupabaseClientProvider
import com.example.gridsurge.ui.CyberChamferShape
import com.example.gridsurge.ui.profile.CallsignInputModule
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

enum class DossierEditMode {
    VIEWING,
    EDITING_CALLSIGN
}

data class CyberAvatarPreset(
    val id: String,
    val callsignTag: String,
    val archetype: String,
    @DrawableRes val iconRes: Int
)

object CyberAvatarRegistry {
    val PRESETS = listOf(
        CyberAvatarPreset("avatar_asian_male", "KAI", "RECON SPECIALIST", R.drawable.avatar_asian_male),
        CyberAvatarPreset("avatar_asian_female", "LIN", "GRID INFILTRATOR", R.drawable.avatar_asian_female),
        CyberAvatarPreset("avatar_black_male", "MALIK", "HEAVY VANGUARD", R.drawable.avatar_black_male),
        CyberAvatarPreset("avatar_black_female", "NIA", "DATA RUNNER", R.drawable.avatar_black_female),
        CyberAvatarPreset("avatar_caucasian_male", "COLE", "TACTICAL OVERSEER", R.drawable.avatar_caucasian_male),
        CyberAvatarPreset("avatar_caucasian_female", "AVA", "CYBER WARLOCK", R.drawable.avatar_caucasian_female),
        CyberAvatarPreset("avatar_latino_male", "RAMIREZ", "GRID BREAKER", R.drawable.avatar_latino_male),
        CyberAvatarPreset("avatar_latina_female", "ELENA", "NEURAL SLICER", R.drawable.avatar_latina_female),
        CyberAvatarPreset("avatar_middle_eastern_male", "TARIQ", "SECTOR COMMANDER", R.drawable.avatar_middle_eastern_male),
        CyberAvatarPreset("avatar_middle_eastern_female", "AMIRA", "QUANTUM ENGINEER", R.drawable.avatar_middle_eastern_female)
    )

    fun getPresetById(id: String): CyberAvatarPreset {
        return PRESETS.firstOrNull { it.id == id } ?: PRESETS.first()
    }
}

@Composable
fun CyberProfileSetupDialog(
    profileManager: PlayerProfileManager,
    isPvpRequiredNotice: Boolean = false,
    onProfileInitialized: () -> Unit,
    onNavigateToAuth: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val currentCallsign by profileManager.callsign.collectAsState()
    val currentAvatarKey by profileManager.avatarKey.collectAsState()
    val activeTitle by profileManager.activeTitle.collectAsState()
    val unlockedTitles by profileManager.unlockedTitles.collectAsState()
    val starCurrency by profileManager.starCurrency.collectAsState()
    val highScore by profileManager.highScore.collectAsState()
    val savedLinkedEmail by profileManager.linkedEmail.collectAsState()

    var selectedAvatarId by remember { mutableStateOf(currentAvatarKey) }
    var selectedTitle by remember { mutableStateOf(activeTitle) }
    var tentativeCallsign by remember { mutableStateOf(currentCallsign) }
    var editMode by remember { mutableStateOf(DossierEditMode.VIEWING) }

    val lastRenameTime = remember { System.currentTimeMillis() - 1000000L }
    val renameCount = remember { if (currentCallsign.startsWith("OPERATIVE_") || currentCallsign.startsWith("AGENT_")) 0 else 1 }

    val renamePolicy = remember(currentCallsign, starCurrency) {
        RenameEconomyEngine.evaluatePolicy(
            currentCallsign = currentCallsign,
            lastRenameTimestamp = lastRenameTime,
            totalRenameCount = renameCount,
            playerStarCurrency = starCurrency
        )
    }

    val activeAvatar = CyberAvatarRegistry.getPresetById(selectedAvatarId)
    val scope = rememberCoroutineScope()
    val avatarListState = rememberLazyListState()
    val scrollState = rememberScrollState()

    var authenticatedEmail by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (SupabaseClientProvider.isConfigured) {
            try {
                val session = SupabaseClientProvider.client.auth.currentSessionOrNull()
                if (session != null) {
                    authenticatedEmail = session.user?.email
                }
            } catch (_: Exception) {}
        }
    }

    val activeEmail = authenticatedEmail ?: savedLinkedEmail
    val isLinked = activeEmail != null

    val telemetry = remember(highScore, starCurrency, isLinked) {
        OperativeTelemetryEngine.compileTelemetry(highScore, starCurrency, isLinked)
    }

    // Modal Scrim Backdrop (75% black dim for tactical pop)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD903060D))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        DossierViewportHud()

        // MASTER TACTICAL SLATE
        // - wrapContentHeight() ensures the card fits content tightly
        // - widthIn(max = 440.dp) keeps it phone/tablet responsive
        // - heightIn(max = 740.dp) guarantees scrollability if screen is small
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 440.dp)
                .heightIn(max = 740.dp)
                .wrapContentHeight()
                .clip(CyberChamferShape)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xF50D1526), Color(0xFE070B14))
                    )
                )
                .border(1.5.dp, Color(0xFF00E5FF), CyberChamferShape)
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .imePadding()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ==================== 1. TOP HEADER & NETWORK PILL ====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isPvpRequiredNotice) "PVP COMBAT RECON" else "OPERATIVE DOSSIER // RECON",
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "TERMINAL ID #GS-${Math.abs(currentCallsign.hashCode() % 9000 + 1000)}",
                        color = Color(0xFF4C617F),
                        fontSize = 8.sp,
                        fontFamily = ChakraPetchFontFamily
                    )
                }

                if (isLinked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x2200E676))
                            .border(1.dp, Color(0xFF00E676), RoundedCornerShape(6.dp))
                            .clickable(enabled = onNavigateToAuth != null) {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onNavigateToAuth?.invoke()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "✓ LINKED",
                            color = Color(0xFF00E676),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = ChakraPetchFontFamily
                        )
                    }
                } else if (onNavigateToAuth != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x2200E5FF))
                            .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(6.dp))
                            .clickable {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onNavigateToAuth()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🔗 LINK VAULT",
                            color = Color(0xFF00E5FF),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = ChakraPetchFontFamily
                        )
                    }
                }
            }

            // ==================== 2. PRIMARY AVATAR DISPLAY ====================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "${activeAvatar.callsignTag} // ${activeAvatar.archetype}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .size(94.dp)
                        .clip(CyberChamferShape)
                        .background(Color(0x66101726))
                        .border(
                            2.dp,
                            Brush.verticalGradient(listOf(Color(0xFF00E5FF), Color(0x3300E5FF))),
                            CyberChamferShape
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Image(
                        painter = painterResource(id = activeAvatar.iconRes),
                        contentDescription = activeAvatar.callsignTag,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.BottomCenter
                    )
                }
            }

            // ==================== 3. AVATAR SELECTOR ROW ====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0x33141926))
                        .border(1.dp, Color(0xAA00E5FF), CircleShape)
                        .clickable {
                            SfxManager.playSfx(SfxType.SNAP_TICK)
                            val currIndex = CyberAvatarRegistry.PRESETS.indexOfFirst { it.id == selectedAvatarId }
                            val prevIndex = if (currIndex > 0) currIndex - 1 else CyberAvatarRegistry.PRESETS.size - 1
                            selectedAvatarId = CyberAvatarRegistry.PRESETS[prevIndex].id
                            scope.launch { avatarListState.animateScrollToItem(prevIndex) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("‹", color = Color(0xFF00E5FF), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                LazyRow(
                    state = avatarListState,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(CyberAvatarRegistry.PRESETS, key = { it.id }) { preset ->
                        val isSelected = preset.id == selectedAvatarId
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0x4400E5FF) else Color(0x22141926))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF26334D),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    SfxManager.playSfx(SfxType.SNAP_TICK)
                                    selectedAvatarId = preset.id
                                },
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Image(
                                painter = painterResource(id = preset.iconRes),
                                contentDescription = preset.callsignTag,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.BottomCenter
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0x33141926))
                        .border(1.dp, Color(0xAA00E5FF), CircleShape)
                        .clickable {
                            SfxManager.playSfx(SfxType.SNAP_TICK)
                            val currIndex = CyberAvatarRegistry.PRESETS.indexOfFirst { it.id == selectedAvatarId }
                            val nextIndex = (currIndex + 1) % CyberAvatarRegistry.PRESETS.size
                            selectedAvatarId = CyberAvatarRegistry.PRESETS[nextIndex].id
                            scope.launch { avatarListState.animateScrollToItem(nextIndex) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("›", color = Color(0xFF00E5FF), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            // ==================== 4. CALLSIGN DOSSIER MODULE ====================
            AnimatedContent(
                targetState = editMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "DossierCallsignEdit"
            ) { mode ->
                if (mode == DossierEditMode.VIEWING) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x33101A2B))
                            .border(1.dp, Color(0xFF1E2D44), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "OPERATIVE CALLSIGN",
                                    color = Color(0xFF4C617F),
                                    fontSize = 9.sp,
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = currentCallsign,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontFamily = OrbitronFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("🔒", fontSize = 10.sp)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (renamePolicy.canRename) Color(0x2200E5FF) else Color(0x221E2D44))
                                    .border(
                                        1.dp,
                                        if (renamePolicy.canRename) Color(0xFF00E5FF) else Color(0xFF374966),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        SfxManager.playSfx(SfxType.SNAP_TICK)
                                        if (renamePolicy.canRename) {
                                            editMode = DossierEditMode.EDITING_CALLSIGN
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = when {
                                        !renamePolicy.canRename && renamePolicy.cooldownFormatted != null ->
                                            "COOLDOWN: ${renamePolicy.cooldownFormatted}"
                                        renamePolicy.isFree -> "CHANGE [FREE]"
                                        else -> "CHANGE [${renamePolicy.costStars} ⭐]"
                                    },
                                    color = if (renamePolicy.canRename) Color(0xFF00E5FF) else Color(0xFF6B7F99),
                                    fontSize = 10.sp,
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CallsignInputModule(
                            initialCallsign = tentativeCallsign,
                            onCallsignConfirmed = { confirmed ->
                                tentativeCallsign = confirmed
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { editMode = DossierEditMode.VIEWING }) {
                                Text(
                                    text = "CANCEL EDIT",
                                    color = Color(0xFF8FA3BF),
                                    fontSize = 10.sp,
                                    fontFamily = ChakraPetchFontFamily
                                )
                            }
                        }
                    }
                }
            }

            // ==================== 5. ACCOLADES & TITLES ====================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "ACTIVE ACCOLADE // TITLE",
                    color = Color(0xFF4C617F),
                    fontSize = 9.sp,
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
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
                                .background(if (isSelected) Color(0x3300E5FF) else Color(0x22101826))
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E2D44),
                                    RoundedCornerShape(6.dp)
                                )
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
                                fontFamily = ChakraPetchFontFamily
                            )
                        }
                    }
                }
            }

            // ==================== 6. OPERATIVE SERVICE TELEMETRY & CLEARANCE ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x220B1321))
                    .border(1.dp, Color(0xFF1A263B), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OPERATIVE SERVICE TELEMETRY",
                        color = Color(0xFF00E5FF),
                        fontSize = 9.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "NODE STATUS: NOMINAL",
                        color = Color(0xFF00FF66),
                        fontSize = 8.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 2x2 Tactical Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DossierTelemetryStatCell(
                        label = "LIFETIME HIGH SCORE",
                        value = "${telemetry.topScore} PTS",
                        accentColor = Color(0xFFFFD700),
                        modifier = Modifier.weight(1f)
                    )
                    DossierTelemetryStatCell(
                        label = "STAR RESERVES",
                        value = "${telemetry.starReserves} ⭐",
                        accentColor = Color(0xFF00E5FF),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DossierTelemetryStatCell(
                        label = "SECURITY CLEARANCE",
                        value = telemetry.clearanceLevel,
                        accentColor = if (isLinked) Color(0xFF00FF66) else Color(0xFFFF9100),
                        modifier = Modifier.weight(1f)
                    )
                    DossierTelemetryStatCell(
                        label = "MAX COMBO TIER",
                        value = telemetry.maxComboRank,
                        accentColor = Color(0xFFEA80FC),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Segmented Clearance Progress Track (Direct Calculation)
                ClearanceProgressionMeter(stars = starCurrency)
            }

            // Compact spacer: preserves natural separation without runaway stretching
            Spacer(modifier = Modifier.height(4.dp))

            // ==================== 7. MASTER ACTION BUTTONS ====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isPvpRequiredNotice) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x22141926))
                            .border(1.dp, Color(0xFF26334D), RoundedCornerShape(8.dp))
                            .clickable {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DISMISS",
                            color = Color(0xFF78909C),
                            fontSize = 11.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                val hasChanges = selectedAvatarId != currentAvatarKey ||
                        selectedTitle != activeTitle ||
                        (editMode == DossierEditMode.EDITING_CALLSIGN && tentativeCallsign != currentCallsign)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                if (hasChanges) listOf(Color(0xFF00E5FF), Color(0xFF00E676))
                                else listOf(Color(0x3300E5FF), Color(0x3300E676))
                            )
                        )
                        .clickable(enabled = hasChanges) {
                            SfxManager.playSfx(SfxType.PROFILE_CONFIRM)
                            profileManager.saveCyberProfile(
                                callsign = tentativeCallsign.trim(),
                                avatarKey = selectedAvatarId,
                                title = selectedTitle
                            )
                            onProfileInitialized()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "COMMIT DOSSIER // SYNC",
                        color = if (hasChanges) Color(0xFF040812) else Color(0xFF3D5766),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = ChakraPetchFontFamily,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

/**
 * Segmented Hardware-Accelerated Progress Track.
 * Replaces dead space with a tactical star progression gauge.
 */
@Composable
private fun ClearanceProgressionMeter(
    stars: Int,
    modifier: Modifier = Modifier
) {
    val tierTarget = when {
        stars < 100 -> 100
        stars < 250 -> 250
        else -> 500
    }
    val fraction = (stars.toFloat() / tierTarget).coerceIn(0f, 1f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "CLEARANCE PROGRESSION",
                color = Color(0xFF5C708A),
                fontSize = 7.5.sp,
                fontFamily = ChakraPetchFontFamily,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$stars / $tierTarget ⭐",
                color = Color(0xFF00E5FF),
                fontSize = 8.sp,
                fontFamily = ChakraPetchFontFamily,
                fontWeight = FontWeight.Bold
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        ) {
            val w = size.width
            val h = size.height

            // Background track
            drawRect(color = Color(0xFF121B2B), size = Size(w, h))

            // Glowing fill
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF00E5FF), Color(0xFF00FF66))
                ),
                size = Size(w * fraction, h)
            )

            // Segment tick cuts (10 segments)
            val segments = 10
            val segWidth = w / segments
            for (i in 1 until segments) {
                drawLine(
                    color = Color(0xFF080D18),
                    start = Offset(i * segWidth, 0f),
                    end = Offset(i * segWidth, h),
                    strokeWidth = 1.5f
                )
            }
        }
    }
}

@Composable
private fun DossierTelemetryStatCell(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x26101929))
            .border(1.dp, Color(0xFF1F2E47), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                color = Color(0xFF5C708A),
                fontSize = 7.5.sp,
                fontFamily = ChakraPetchFontFamily,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = value,
                color = accentColor,
                fontSize = 11.sp,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DossierViewportHud() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val hudColor = Color(0x0E00E5FF)
        val cornerSize = 36f

        drawLine(hudColor, Offset(16f, 16f), Offset(16f + cornerSize, 16f), strokeWidth = 1.5f)
        drawLine(hudColor, Offset(16f, 16f), Offset(16f, 16f + cornerSize), strokeWidth = 1.5f)

        drawLine(hudColor, Offset(w - 16f, 16f), Offset(w - 16f - cornerSize, 16f), strokeWidth = 1.5f)
        drawLine(hudColor, Offset(w - 16f, 16f), Offset(w - 16f, 16f + cornerSize), strokeWidth = 1.5f)

        drawLine(hudColor, Offset(16f, h - 16f), Offset(16f + cornerSize, h - 16f), strokeWidth = 1.5f)
        drawLine(hudColor, Offset(16f, h - 16f), Offset(16f, h - 16f - cornerSize), strokeWidth = 1.5f)

        drawLine(hudColor, Offset(w - 16f, h - 16f), Offset(w - 16f - cornerSize, h - 16f), strokeWidth = 1.5f)
        drawLine(hudColor, Offset(w - 16f, h - 16f), Offset(w - 16f, h - 16f - cornerSize), strokeWidth = 1.5f)
    }
}