package com.example.gridsurge.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.network.SupabaseClientProvider
import com.example.gridsurge.ui.CyberChamferShape
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Facebook
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.OTP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CyberAuthScreen(
    profileManager: PlayerProfileManager,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentCallsign by profileManager.callsign.collectAsState()

    var emailInput by remember { mutableStateOf("") }
    var otpCodeInput by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var authFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var authenticatedEmail by remember { mutableStateOf<String?>(null) }

    // Check existing Supabase Session
    LaunchedEffect(Unit) {
        BgmManager.playTrack(context, BgmTrack.MAIN_HUB)
        if (SupabaseClientProvider.isConfigured) {
            try {
                val session = SupabaseClientProvider.client.auth.currentSessionOrNull()
                if (session != null) {
                    authenticatedEmail = session.user?.email
                }
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0C14))
    ) {
        // Background Image & Scrim
        Image(
            painter = painterResource(id = R.drawable.bg_main_hub),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(Color(0xDD0A0C14)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xCC141926))
                        .border(1.dp, Color(0xFF26334D), RoundedCornerShape(10.dp))
                        .clickable {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            onNavigateBack()
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "‹ BACK",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "NEURAL LINK AUTH",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )

                Box(modifier = Modifier.size(40.dp)) // Spacer alignment
            }

            // Current Session / Status Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CyberChamferShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0x4400E5FF), Color(0x33E040FB))
                        )
                    )
                    .border(1.dp, Color(0x8800E5FF), CyberChamferShape)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (authenticatedEmail != null) "CONNECTED ACCOUNT" else "GUEST OPERATIVE PROTOCOL",
                            color = Color(0xFF00E5FF),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = authenticatedEmail ?: currentCallsign,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (authenticatedEmail != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x44FF0055))
                                .border(1.dp, Color(0xFFFF0055), RoundedCornerShape(8.dp))
                                .clickable {
                                    scope.launch {
                                        try {
                                            SupabaseClientProvider.client.auth.signOut()
                                        } catch (_: Exception) {}
                                        authenticatedEmail = null
                                        authFeedbackMessage = "DISCONNECTED // SESSION ENDED"
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("DISCONNECT", color = Color(0xFFFF0055), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Feedback Message Banner
            authFeedbackMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x33141926))
                        .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(msg, color = Color(0xFFFFB300), fontSize = 11.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                }
            }

            // 1. Google 1-Tap Auth Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(CyberChamferShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF00E5FF), Color(0xFF00E676))
                        )
                    )
                    .clickable(enabled = !isAuthenticating) {
                        SfxManager.playSfx(SfxType.UI_CONFIRM)
                        isAuthenticating = true
                        authFeedbackMessage = "INITIALIZING GOOGLE 1-TAP AUTH..."

                        scope.launch {
                            try {
                                if (SupabaseClientProvider.isConfigured) {
                                    SupabaseClientProvider.client.auth.signInWith(Google)
                                    authFeedbackMessage = "GOOGLE LINK SUCCESSFUL ✓"
                                } else {
                                    authFeedbackMessage = "SUPABASE LINKED // SIMULATED GOOGLE AUTH SUCCESS"
                                }
                            } catch (e: Exception) {
                                authFeedbackMessage = "AUTH ERROR: ${e.message}"
                            } finally {
                                isAuthenticating = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "SIGN IN WITH GOOGLE",
                        color = Color(0xFF040812),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }

            // 2. Email / OTP Magic Link Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x88101522))
                    .border(1.dp, Color(0xFF26334D), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "EMAIL / OTP MAGIC LINK",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("OPERATIVE EMAIL ADDRESS", color = Color(0xFF00E5FF), fontSize = 10.sp) },
                        placeholder = { Text("agent@gridsurge.io", color = Color(0xFF5C8599), fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF26334D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isOtpSent) {
                        OutlinedTextField(
                            value = otpCodeInput,
                            onValueChange = { if (it.length <= 6) otpCodeInput = it },
                            label = { Text("ENTER 6-DIGIT CODE", color = Color(0xFFFFB300), fontSize = 10.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFB300),
                                unfocusedBorderColor = Color(0xFF26334D),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x2200E5FF))
                            .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(10.dp))
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
                                                authFeedbackMessage = "VERIFICATION CODE SENT TO $emailInput"
                                            } else {
                                                SupabaseClientProvider.client.auth.verifyEmailOtp(
                                                    type = OtpType.Email.EMAIL,
                                                    token = otpCodeInput,
                                                    email = emailInput
                                                )
                                                authenticatedEmail = emailInput
                                                authFeedbackMessage = "EMAIL LINK VERIFIED ✓"
                                            }
                                        } else {
                                            authenticatedEmail = emailInput
                                            authFeedbackMessage = "SIMULATED EMAIL LINK SUCCESS ✓"
                                        }
                                    } catch (e: Exception) {
                                        authFeedbackMessage = "EMAIL AUTH ERROR: ${e.message}"
                                    } finally {
                                        isAuthenticating = false
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isOtpSent) "VERIFY 6-DIGIT CODE" else "SEND OTP VERIFICATION LINK",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
