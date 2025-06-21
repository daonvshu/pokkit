package com.daonvshu.bangumi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class BangumiMainVm : ViewModel() {

    val menuItemIndex = MutableStateFlow(0)
}