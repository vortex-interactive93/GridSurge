package com.example.gridsurge.features.adventure.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.features.adventure.model.NeuralAugment
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.ui.CyberChamferShape

@Composable
fun NeuralAugmentDraftDialog(
    options: List<NeuralAugment>,
    onAugmentSelected: (NeuralAugment) -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .background(
                        brush = Brush.verticalGradient(listOf(Color(0xFF0A1322), Color(0xFF030712))),
                        shape = CyberChamferShape
                    )
                    .border(1.5.dp, Color(0xFF00E5FF), CyberChamferShape)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MATRIX SYNCHRONIZED",
                    color = Color(0xFF00FF66),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "SELECT NEURAL OVERCLOCK",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Choose 1 system augment to install for remainder of this Sector run",
                    color = Color(0xFF5C8599),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // 3 Card Vertical Column
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    options.forEach { augment ->
                        val cardBorderColor = Color(augment.rarity.colorHex)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF07111E))
                                .border(1.2.dp, cardBorderColor.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                .clickable {
                                    SfxManager.playSfx(SfxType.OVERDRIVE_ACTIVATE)
                                    onAugmentSelected(augment)
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(Color(0xFF030812), RoundedCornerShape(6.dp))
                                        .border(1.dp, cardBorderColor, RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = augment.iconRes),
                                        contentDescription = augment.title,
                                        modifier = Modifier.size(34.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = augment.title,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = augment.rarity.name,
                                            color = cardBorderColor,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = augment.description,
                                        color = Color(0xFFB0BEC5),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
