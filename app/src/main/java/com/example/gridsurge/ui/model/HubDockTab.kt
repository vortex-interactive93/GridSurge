package com.example.gridsurge.ui.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.gridsurge.ui.Screen

data class HubDockTab(
    val id: String,
    val label: String,
    val targetScreen: Screen,
    @DrawableRes val iconRes: Int,
    val badgeCount: Int = 0,
    val accentColor: Color
)
