package com.daonvshu.pokkit

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

data class MenuItem(
    val label: String,
    val color: Color,
)

class MainViewModel : ViewModel() {
    val selectedMenuIndex = MutableStateFlow(0)

    val menuItems = listOf(
        MenuItem("Bangumi", Color(0xFFFF639C)),
        MenuItem("Pixiv", Color(0xFF8BACFF)),
        MenuItem("Exit", Color(0xFF00A6ED)),
    )
}