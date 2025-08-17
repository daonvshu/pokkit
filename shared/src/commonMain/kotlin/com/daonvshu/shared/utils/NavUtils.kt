package com.daonvshu.shared.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun rememberNavHostController(
    navHost: MutableStateFlow<String>
): NavHostController {
    val navController = rememberNavController()
    LaunchedEffect(navHost) {
        navHost.collect {
            if (it.isNotEmpty()) {
                if (it == "pop") {
                    navController.popBackStack()
                } else {
                    navController.navigate(it)
                }
            }
        }
    }
    return navController
}