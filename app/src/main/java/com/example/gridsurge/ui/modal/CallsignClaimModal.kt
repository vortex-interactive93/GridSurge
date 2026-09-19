package com.example.gridsurge.ui.modal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.gridsurge.R
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.meta.engine.CallsignValidator
import com.example.gridsurge.meta.model.CallsignValidationState
import com.example.gridsurge.meta.model.ClaimTriggerReason
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(FlowPreview::class)
@Composable
fun CallsignClaimModal(
    suggestedCallsign: String,
    triggerReason: ClaimTriggerReason,
    onCallsignConfirmed: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var rawInput by remember { mutableStateOf(suggestedCallsign) }
    var validationState by remember { mutableStateOf<CallsignValidationState>(CallsignValidationState.Idle) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        snapshotFlow { rawInput }
            .debounce(300)
            .distinctUntilChanged()
            .collectLatest { query ->
                val local = CallsignValidator.validateLocally(query)
                if (local is CallsignValidationState.Invalid) {
                    validationState = local
                } else {
                    validationState = CallsignValidationState.CheckingAvailability
                    validationState = CallsignValidator.checkAvailabilityOnBackend(query)
                }
            }
    }

    val isValid = validationState is CallsignValidationState.ValidAvailable

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0C111C))
                .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = when (triggerReason) {
                        ClaimTriggerReason.FIRST_HIGH_SCORE -> "NEW RECORD DETECTED"
                        ClaimTriggerReason.GOOGLE_ACCOUNT_LINKED -> "IDENTITY UPLINK VERIFIED"
                        else -> "ESTABLISH CALLSIGN"
                    },
                    color = Color(0xFF00E5FF),
                    fontSize = 13.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Text(
                    text = when (triggerReason) {
                        ClaimTriggerReason.FIRST_HIGH_SCORE ->
                            "You achieved a competitive high score! Register your permanent operative handle to submit to the Global Grid."
                        ClaimTriggerReason.GOOGLE_ACCOUNT_LINKED ->
                            "Your Google identity is secured. Confirm your public leaderboard callsign."
                        else ->
                            "Choose a unique operative identifier (3–16 characters, alphanumeric)."
                    },
                    color = Color(0xFF8FA3BF),
                    fontSize = 11.sp,
                    fontFamily = ChakraPetchFontFamily,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )

                OutlinedTextField(
                    value = rawInput,
                    onValueChange = { input ->
                        if (input.length <= 16 && !input.contains(" ")) {
                            rawInput = input
                        }
                    },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (isValid) {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onCallsignConfirmed(rawInput)
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isValid) Color(0xFF00FF66) else Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF1E283D),
                        focusedContainerColor = Color(0x33101A2B),
                        unfocusedContainerColor = Color(0x22101A2B)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (val state = validationState) {
                        is CallsignValidationState.CheckingAvailability -> {
                            Text(
                                text = "CHECKING GRID REGISTRY...",
                                color = Color(0xFFFFD600),
                                fontSize = 10.sp,
                                fontFamily = ChakraPetchFontFamily
                            )
                        }
                        is CallsignValidationState.ValidAvailable -> {
                            Text(
                                text = "✓ CALLSIGN AVAILABLE",
                                color = Color(0xFF00FF66),
                                fontSize = 10.sp,
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        is CallsignValidationState.Invalid -> {
                            Text(
                                text = state.reason,
                                color = Color(0xFFFF5252),
                                fontSize = 10.sp,
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        CallsignValidationState.Idle -> {}
                    }
                }

                val canSave = (rawInput == suggestedCallsign) || (validationState is CallsignValidationState.ValidAvailable)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x22141926))
                            .border(1.dp, Color(0xFF26334D), RoundedCornerShape(8.dp))
                            .clickable {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onDismissRequest()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "KEEP $suggestedCallsign",
                            color = Color(0xFF78909C),
                            fontSize = 10.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (canSave) Color(0xFF00E5FF) else Color(0x3322314D))
                            .clickable(enabled = canSave) {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onCallsignConfirmed(rawInput)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CONFIRM & SAVE",
                            color = if (canSave) Color(0xFF06090F) else Color(0xFF4A5D78),
                            fontSize = 11.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
