package com.example.gridsurge.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun CyberVolumeSlider(
    label: String,
    currentVolume: Float, // 0.0f .. 1.0f
    onVolumeChanged: (Float) -> Unit,
    onPreviewTriggered: (Float) -> Unit,
    accentColor: Color = Color(0xFF00E5FF)
) {
    var sliderValue by remember(currentVolume) { mutableStateOf(currentVolume) }
    val percentage = (sliderValue * 100).roundToInt()

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "$percentage%",
                color = if (percentage == 0) Color.Gray else accentColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black
            )
        }

        // Stepped slider: 9 intermediate steps = 10% discrete increments
        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                sliderValue = ((newValue * 10).roundToInt() / 10f).coerceIn(0f, 1f)
                onVolumeChanged(sliderValue)
            },
            onValueChangeFinished = {
                onPreviewTriggered(sliderValue)
            },
            valueRange = 0f..1f,
            steps = 9,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = Color(0xFF152238),
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
