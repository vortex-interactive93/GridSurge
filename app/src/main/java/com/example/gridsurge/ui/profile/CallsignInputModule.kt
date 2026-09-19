package com.example.gridsurge.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.R
import com.example.gridsurge.meta.engine.CallsignValidator
import com.example.gridsurge.meta.model.CallsignValidationState
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(FlowPreview::class)
@Composable
fun CallsignInputModule(
    initialCallsign: String,
    onCallsignConfirmed: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var rawText by remember { mutableStateOf(initialCallsign) }
    var validationState by remember { mutableStateOf<CallsignValidationState>(CallsignValidationState.Idle) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        snapshotFlow { rawText }
            .debounce(350)
            .distinctUntilChanged()
            .collectLatest { query ->
                if (query.isBlank()) {
                    validationState = CallsignValidationState.Idle
                } else {
                    val local = CallsignValidator.validateLocally(query)
                    if (local is CallsignValidationState.Invalid) {
                        validationState = local
                    } else {
                        validationState = CallsignValidationState.CheckingAvailability
                        validationState = CallsignValidator.checkAvailabilityOnBackend(query)
                    }
                }
            }
    }

    val stateColor = when (validationState) {
        is CallsignValidationState.ValidAvailable -> Color(0xFF00FF66)
        is CallsignValidationState.Invalid -> Color(0xFFFF5252)
        is CallsignValidationState.CheckingAvailability -> Color(0xFFFFD600)
        CallsignValidationState.Idle -> Color(0xFF00E5FF)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "OPERATIVE CALLSIGN IDENTIFIER",
            color = Color(0xFF6B82A6),
            fontSize = 10.sp,
            fontFamily = ChakraPetchFontFamily,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        OutlinedTextField(
            value = rawText,
            onValueChange = { input ->
                if (input.length <= 16 && !input.contains(" ")) {
                    rawText = input
                }
            },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontFamily = ChakraPetchFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (validationState is CallsignValidationState.ValidAvailable) {
                        onCallsignConfirmed(rawText)
                    }
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = stateColor,
                unfocusedBorderColor = stateColor.copy(alpha = 0.5f),
                focusedContainerColor = Color(0x33101A2B),
                unfocusedContainerColor = Color(0x22101A2B)
            ),
            trailingIcon = {
                when (validationState) {
                    is CallsignValidationState.CheckingAvailability -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFFFFD600),
                            strokeWidth = 2.dp
                        )
                    }
                    is CallsignValidationState.ValidAvailable -> {
                        Text("✓", color = Color(0xFF00FF66), fontWeight = FontWeight.Bold)
                    }
                    is CallsignValidationState.Invalid -> {
                        Text("✕", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                    }
                    CallsignValidationState.Idle -> {}
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(
            visible = validationState !is CallsignValidationState.Idle,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val message = when (val state = validationState) {
                is CallsignValidationState.ValidAvailable -> "CALLSIGN AVAILABLE // UNLOCKED"
                is CallsignValidationState.CheckingAvailability -> "QUERYING SECURE REGISTRY..."
                is CallsignValidationState.Invalid -> state.reason
                CallsignValidationState.Idle -> ""
            }
            Text(
                text = message,
                color = stateColor,
                fontSize = 10.sp,
                fontFamily = ChakraPetchFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
