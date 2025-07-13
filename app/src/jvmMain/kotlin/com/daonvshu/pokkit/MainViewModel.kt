package com.daonvshu.pokkit

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daonvshu.shared.backendservice.BackendDataObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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

    val showServiceError = MutableStateFlow(false)

    init {
        BackendDataObserver.backendServiceConnectError.onEach { error ->
            if (error.isNotEmpty()) {
                showServiceError.value = true
            }
        }.launchIn(viewModelScope)
    }
}