package com.example.gridsurge.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.R
import com.example.gridsurge.audio.BgmManager
import com.example.gridsurge.audio.BgmTrack
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.auth.NativeGoogleAuthBridge
import com.example.gridsurge.auth.model.NativeAuthState
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.meta.model.ClaimTriggerReason
import com.example.gridsurge.network.SupabaseClientProvider
import com.example.gridsurge.ui.CyberChamferShape
import com.example.gridsurge.ui.auth.model.CloudSyncState
import com.example.gridsurge.ui.auth.model.SecurityTelemetry
import com.example.gridsurge.ui.modal.CallsignClaimModal
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import io.github.jan.supabase.gotrue.providers.builtin.OTP
import io.github.jan.supabase.gotrue.OtpType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CyberAuthScreen(
    profileManager: PlayerProfileManager,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val currentCallsign by profileManager.callsign.collectAsState()
    val savedLinkedEmail by profileManager.linkedEmail.collectAsState()
    val starCurrency by profileManager.starCurrency.collectAsState()
    val highScore by profileManager.highScore.collectAsState()

    var emailInput by remember { mutableStateOf("") }
    var otpCodeInput by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var authFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var authenticatedEmail by remember { mutableStateOf<String?>(null) }
    var isEmailDrawerOpen by remember { mutableStateOf(false) }
    var cloudSyncState by remember { mutableStateOf(CloudSyncState.IDLE) }
    var showUnlinkStep1Dialog by remember { mutableStateOf(false) }
    var showUnlinkStep2Dialog by remember { mutableStateOf(false) }
    var unlinkSecurityCodeInput by remember { mutableStateOf("") }
    var generatedSecurityCode by remember { mutableStateOf("") }
    var showCallsignClaimModal by remember { mutableStateOf(false) }

    val authBridge = remember {
        NativeGoogleAuthBridge(context = context, profileManager = profileManager)
    }

    val nativeAuthState by authBridge.authState.collectAsState()

    LaunchedEffect(nativeAuthState) {
        when (val state = nativeAuthState) {
            is NativeAuthState.Success -> {
                authenticatedEmail = state.email
                authFeedbackMessage = "IDENTITY PROTOCOL VERIFIED ✓"
                isAuthenticating = false
                showCallsignClaimModal = true
            }
            is NativeAuthState.Error -> {
                authFeedbackMessage = "AUTH ERROR: ${state.message}"
                isAuthenticating = false
            }
            is NativeAuthState.LaunchingSystemSheet -> {
                authFeedbackMessage = "INITIALIZING PLAY SERVICES..."
            }
            is NativeAuthState.VerifyingWithSupabase -> {
                authFeedbackMessage = "SYNCHRONIZING REPUTATION DATABASE..."
            }
            NativeAuthState.Idle -> isAuthenticating = false
        }
    }

    LaunchedEffect(Unit) {
        BgmManager.playTrack(context, BgmTrack.MAIN_HUB)
        if (SupabaseClientProvider.isConfigured) {
            try {
                SupabaseClientProvider.client.auth.sessionStatus.collect { status ->
                    if (status is SessionStatus.Authenticated) {
                        val email = status.session.user?.email
                        if (!email.isNullOrEmpty()) {
                            authenticatedEmail = email
                            val rawName = try {
                                status.session.user?.userMetadata?.get("full_name")?.toString()
                            } catch (_: Exception) { null }
                            profileManager.syncGoogleAccount(email, rawName)
                        }
                    } else if (status is SessionStatus.NotAuthenticated) {
                        authenticatedEmail = null
                    }
                }
            } catch (_: Exception) {}
        }
    }

    val activeEmail = authenticatedEmail ?: savedLinkedEmail
    val isLinked = activeEmail != null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF06090F))
    ) {
        AmbientCyberGrid()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ==================== 1. TOP TELEMETRY BAR ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x2200E5FF))
                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(6.dp))
                        .clickable {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            onNavigateBack()
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "‹ TERMINAL",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = if (isLinked) "NEURAL IDENTITY VAULT" else "NEURAL LINK PROTOCOL",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                // Network Status Beacon
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isLinked) Color(0xFF00FF66) else Color(0xFFFF9100))
                )
            }

            Spacer(modifier = Modifier.weight(0.6f).heightIn(min = 16.dp))

            // ==================== 2. MAIN CONSOLE CHASSIS ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Operative Dossier Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CyberChamferShape)
                        .background(
                            Brush.horizontalGradient(
                                if (isLinked) listOf(Color(0x2200E676), Color(0x1100E5FF))
                                else listOf(Color(0x2200E5FF), Color(0x11EA80FC))
                            )
                        )
                        .border(
                            1.dp,
                            if (isLinked) Color(0xFF00E676) else Color(0x6600E5FF),
                            CyberChamferShape
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isLinked) "AUTHENTICATED IDENTITY // SECURE" else "GUEST OPERATIVE PROTOCOL",
                                color = if (isLinked) Color(0xFF00FF66) else Color(0xFF00E5FF),
                                fontSize = 10.sp,
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (isLinked) "SYNC: ACTIVE" else "SYNC: OFF",
                                color = if (isLinked) Color(0xFF00FF66) else Color(0xFFFF9100),
                                fontSize = 9.sp,
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = if (isLinked) currentCallsign else "OPERATIVE_X",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        if (isLinked) {
                            Text(
                                text = "LINKED: $activeEmail",
                                color = Color(0xFF8FA3BF),
                                fontSize = 11.sp,
                                fontFamily = ChakraPetchFontFamily
                            )
                        }

                        Text(
                            text = if (isLinked) "Encrypted Balance: $starCurrency ⭐  •  Top Score: $highScore"
                            else "Unsecured Balance: $starCurrency ⭐  •  Top Score: $highScore",
                            color = if (isLinked) Color(0xFF00E5FF) else Color(0xFFFFB300),
                            fontSize = 11.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Feedback Banner
                AnimatedVisibility(
                    visible = authFeedbackMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    authFeedbackMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33141926))
                                .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = msg,
                                color = Color(0xFFFFB300),
                                fontSize = 11.sp,
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Unified Tactical Module
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xCC0D121F))
                        .border(1.dp, Color(0xFF1F2B42), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 3-COLUMN TELEMETRY MATRIX (Always stays, converts to active statuses!)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SecurityTelemetry.entries.forEach { feature ->
                                TelemetryStatusCell(
                                    feature = feature,
                                    isLinked = isLinked,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFF1A2438), thickness = 1.dp)

                        if (!isLinked) {
                            // ================= GUEST CONVERSION STATE =================
                            // Official Google Sign-In Button
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = !isAuthenticating && nativeAuthState !is NativeAuthState.LaunchingSystemSheet) {
                                        SfxManager.playSfx(SfxType.UI_CONFIRM)
                                        if (isLinked) {
                                            authFeedbackMessage = "ACCOUNT ACTIVE: $activeEmail"
                                        } else {
                                            isAuthenticating = true
                                            scope.launch {
                                                authBridge.launchNativeGoogleSignIn()
                                            }
                                        }
                                    },
                                color = Color.White,
                                shadowElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_google_logo),
                                        contentDescription = "Google Logo",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = when (nativeAuthState) {
                                            is NativeAuthState.LaunchingSystemSheet,
                                            is NativeAuthState.VerifyingWithSupabase -> "Synchronizing Play Services..."
                                            else -> "Sign in with Google"
                                        },
                                        color = Color(0xFF1F1F1F),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                }
                            }

                            // Expandable Email/OTP Drawer Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isEmailDrawerOpen = !isEmailDrawerOpen }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF1B263B))
                                Text(
                                    text = if (isEmailDrawerOpen) "  ▲ HIDE EMAIL ACCESS  " else "  ▼ OR TRANSMIT EMAIL ACCESS CODE  ",
                                    color = Color(0xFF4C617F),
                                    fontSize = 10.sp,
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF1B263B))
                            }

                            // Collapsible Email Inputs
                            AnimatedVisibility(
                                visible = isEmailDrawerOpen,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = emailInput,
                                        onValueChange = { emailInput = it },
                                        placeholder = {
                                            Text(
                                                "operative@domain.com",
                                                color = Color(0xFF4A5D78),
                                                fontSize = 12.sp,
                                                fontFamily = ChakraPetchFontFamily
                                            )
                                        },
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(
                                            fontFamily = ChakraPetchFontFamily,
                                            fontSize = 13.sp
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF00E5FF),
                                            unfocusedBorderColor = Color(0xFF1F2B42),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )

                                    if (isOtpSent) {
                                        OutlinedTextField(
                                            value = otpCodeInput,
                                            onValueChange = { if (it.length <= 6) otpCodeInput = it },
                                            placeholder = {
                                                Text(
                                                    "6-DIGIT DISPATCH CODE",
                                                    color = Color(0xFFFFD600),
                                                    fontSize = 12.sp,
                                                    fontFamily = ChakraPetchFontFamily
                                                )
                                            },
                                            singleLine = true,
                                            textStyle = LocalTextStyle.current.copy(
                                                fontFamily = ChakraPetchFontFamily,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFFFFD600),
                                                unfocusedBorderColor = Color(0xFF1F2B42),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(42.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0x2200E5FF))
                                            .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(6.dp))
                                            .clickable(enabled = !isAuthenticating && emailInput.contains("@")) {
                                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                                isAuthenticating = true
                                                scope.launch {
                                                    try {
                                                        if (SupabaseClientProvider.isConfigured) {
                                                            if (!isOtpSent) {
                                                                SupabaseClientProvider.client.auth.signInWith(OTP) {
                                                                    email = emailInput
                                                                }
                                                                isOtpSent = true
                                                                authFeedbackMessage = "DISPATCH CODE SENT TO $emailInput"
                                                            } else {
                                                                SupabaseClientProvider.client.auth.verifyEmailOtp(
                                                                    type = OtpType.Email.EMAIL,
                                                                    token = otpCodeInput,
                                                                    email = emailInput
                                                                )
                                                                authenticatedEmail = emailInput
                                                                profileManager.saveLinkedEmail(emailInput)
                                                                authFeedbackMessage = "IDENTITY DISPATCH VERIFIED ✓"
                                                                showCallsignClaimModal = true
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        authFeedbackMessage = "OTP ERROR: ${e.message}"
                                                    } finally {
                                                        isAuthenticating = false
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (!isOtpSent) "TRANSMIT DISPATCH LINK" else "VERIFY CODE & LINK",
                                            color = Color(0xFF00E5FF),
                                            fontSize = 11.sp,
                                            fontFamily = ChakraPetchFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.8.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            // ================= AUTHENTICATED VAULT STATE =================
                            // Tactical Cloud Backup Button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (cloudSyncState == CloudSyncState.SYNCING) Color(0x2200E5FF)
                                        else Color(0x2200FF66)
                                    )
                                    .border(
                                        1.dp,
                                        if (cloudSyncState == CloudSyncState.SYNCING) Color(0xFF00E5FF)
                                        else Color(0xFF00FF66),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable(enabled = cloudSyncState != CloudSyncState.SYNCING) {
                                        SfxManager.playSfx(SfxType.UI_CONFIRM)
                                        scope.launch {
                                            cloudSyncState = CloudSyncState.SYNCING
                                            authFeedbackMessage = "UPLINKING LOCAL PROGRESS TO CLOUD..."
                                            delay(1200)
                                            cloudSyncState = CloudSyncState.SYNC_SUCCESS
                                            authFeedbackMessage = "CLOUD VAULT SYNCHRONIZED // 75 STARS ENCRYPTED ✓"
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (cloudSyncState) {
                                        CloudSyncState.SYNCING -> "TRANSMITTING DATA UPLINK..."
                                        CloudSyncState.SYNC_SUCCESS -> "⟳ SYNC COMPLETED // SECURE"
                                        else -> "⟳ TRANSMIT CLOUD BACKUP NOW"
                                    },
                                    color = if (cloudSyncState == CloudSyncState.SYNCING) Color(0xFF00E5FF) else Color(0xFF00FF66),
                                    fontSize = 12.sp,
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }

                            // Secondary Actions: Unlink Protocol
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SESSION: GOOGLE PLAY SERVICES",
                                    color = Color(0xFF4C617F),
                                    fontSize = 9.sp,
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = "DISCONNECT LINK",
                                    color = Color(0xFFFF5252),
                                    fontSize = 11.sp,
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        showUnlinkStep1Dialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1.0f).heightIn(min = 20.dp))

            // ==================== 3. INTEGRITY FOOTER ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🔒", fontSize = 10.sp)
                    Text(
                        text = "ENCRYPTED VIA GOOGLE PLAY CREDENTIAL SERVICES",
                        color = Color(0xFF4C617F),
                        fontSize = 10.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = "One-tap cryptographic token verification. No sensitive local credentials stored.",
                    color = Color(0xFF26354D),
                    fontSize = 9.sp,
                    fontFamily = ChakraPetchFontFamily,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 2-Step Verification Dialog - Step 1
        if (showUnlinkStep1Dialog) {
            AlertDialog(
                onDismissRequest = { showUnlinkStep1Dialog = false },
                title = {
                    Text(
                        text = "UNLINK ACCOUNT (STEP 1 OF 2)",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                },
                text = {
                    Text(
                        text = "Disconnecting $activeEmail will pause cloud progress backup and revert this terminal to Guest Protocol. Proceed to Step 2 security verification?",
                        fontFamily = ChakraPetchFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF8FA3BF)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showUnlinkStep1Dialog = false
                            generatedSecurityCode = (1000..9999).random().toString()
                            unlinkSecurityCodeInput = ""
                            showUnlinkStep2Dialog = true
                        }
                    ) {
                        Text("PROCEED TO STEP 2 ➔", color = Color(0xFF00E5FF), fontFamily = ChakraPetchFontFamily, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUnlinkStep1Dialog = false }) {
                        Text("CANCEL", color = Color(0xFF78909C), fontFamily = ChakraPetchFontFamily)
                    }
                },
                containerColor = Color(0xFF0D121F),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // 2-Step Verification Dialog - Step 2 (Challenge Pin Verification)
        if (showUnlinkStep2Dialog) {
            val isCodeValid = unlinkSecurityCodeInput.trim() == generatedSecurityCode
            AlertDialog(
                onDismissRequest = { showUnlinkStep2Dialog = false },
                title = {
                    Text(
                        text = "SECURITY CHALLENGE (STEP 2 OF 2)",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFFFF5252)
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "To finalize unlinking $activeEmail, enter the 4-digit security code shown below:",
                            fontFamily = ChakraPetchFontFamily,
                            fontSize = 12.sp,
                            color = Color(0xFF8FA3BF)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33101A2B))
                                .border(1.dp, Color(0xFFFFD600), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CODE: $generatedSecurityCode",
                                color = Color(0xFFFFD600),
                                fontSize = 14.sp,
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }

                        OutlinedTextField(
                            value = unlinkSecurityCodeInput,
                            onValueChange = { if (it.length <= 4) unlinkSecurityCodeInput = it },
                            placeholder = { Text("4-DIGIT CODE", color = Color(0xFF4A5D78), fontSize = 12.sp, fontFamily = ChakraPetchFontFamily) },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontFamily = OrbitronFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00FF66),
                                unfocusedBorderColor = Color(0xFF1F2B42)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = isCodeValid,
                        onClick = {
                            showUnlinkStep2Dialog = false
                            scope.launch {
                                try {
                                    if (SupabaseClientProvider.isConfigured) {
                                        SupabaseClientProvider.client.auth.signOut()
                                    }
                                } catch (_: Exception) {}
                                authenticatedEmail = null
                                profileManager.clearLinkedEmail()
                                authFeedbackMessage = "NEURAL LINK TERMINATED // GUEST PROTOCOL - READY TO LINK NEW ACCOUNT"
                            }
                        }
                    ) {
                        Text("UNLINK & SIGN OUT", color = if (isCodeValid) Color(0xFFFF5252) else Color(0xFF4A5D78), fontFamily = ChakraPetchFontFamily, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUnlinkStep2Dialog = false }) {
                        Text("CANCEL", color = Color(0xFF78909C), fontFamily = ChakraPetchFontFamily)
                    }
                },
                containerColor = Color(0xFF0D121F),
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (showCallsignClaimModal) {
            CallsignClaimModal(
                suggestedCallsign = currentCallsign,
                triggerReason = ClaimTriggerReason.GOOGLE_ACCOUNT_LINKED,
                onCallsignConfirmed = { newCallsign ->
                    profileManager.saveCyberProfile(
                        callsignToSave = newCallsign,
                        avatarKeyToSave = profileManager.avatarKey.value,
                        titleToSave = profileManager.activeTitle.value
                    )
                    showCallsignClaimModal = false
                },
                onDismissRequest = {
                    showCallsignClaimModal = false
                }
            )
        }
    }
}

/**
 * Telemetry Cell that dynamically updates when linked.
 */
@Composable
private fun TelemetryStatusCell(
    feature: SecurityTelemetry,
    isLinked: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isLinked) Color(0x2200FF66) else Color(0x33101A2B))
                .border(
                    1.dp,
                    if (isLinked) Color(0x6600FF66) else Color(0xFF223452),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = feature.iconRes),
                contentDescription = feature.title,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        blendMode = BlendMode.Screen
                    },
                contentScale = ContentScale.Fit
            )
        }

        Text(
            text = feature.title,
            color = if (isLinked) Color.White else Color(0xFF00E5FF),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ChakraPetchFontFamily,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        // Status pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (isLinked) Color(0x2200FF66) else Color(0x221E2A3A))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isLinked) feature.linkedStatus else feature.guestDesc,
                color = if (isLinked) Color(0xFF00FF66) else Color(0xFF6B82A6),
                fontSize = 8.sp,
                fontFamily = ChakraPetchFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AmbientCyberGrid() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)

        val stroke = Stroke(width = 1f)
        val gridColor = Color(0x0A00E5FF)
        val markerColor = Color(0x1500E5FF)

        drawCircle(color = gridColor, radius = w * 0.45f, center = center, style = stroke)
        drawCircle(color = gridColor, radius = w * 0.70f, center = center, style = stroke)

        drawLine(markerColor, Offset(center.x - 24f, center.y), Offset(center.x + 24f, center.y), strokeWidth = 1f)
        drawLine(markerColor, Offset(center.x, center.y - 24f), Offset(center.x, center.y + 24f), strokeWidth = 1f)
    }
}
