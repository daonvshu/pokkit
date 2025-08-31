package com.daonvshu.pokkit

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daonvshu.pokkit.backendservice.BackendDataObserver
import com.daonvshu.shared.utils.PrimaryColors
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
        MenuItem("Bangumi", PrimaryColors.Bangumi_Primary),
        MenuItem("Pixiv", PrimaryColors.Pixiv_Primary),
        MenuItem("Exit", PrimaryColors.Black),
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