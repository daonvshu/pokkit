package com.daonvshu.mikan

import androidx.lifecycle.ViewModel
import com.daonvshu.shared.database.schema.MikanDataRecord
import kotlinx.coroutines.flow.MutableStateFlow

class BangumiSharedVm : ViewModel() {

    val navHost = MutableStateFlow("")

    var detailBangumiItem = MikanDataRecord()
}