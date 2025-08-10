package com.daonvshu.bangumi.pages

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class DownloadPageVm : ViewModel() {
    val typeIndex = MutableStateFlow(0)
    val navHost = MutableStateFlow("")
}