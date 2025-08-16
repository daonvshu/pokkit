package com.daonvshu.bangumi

import androidx.lifecycle.ViewModel
import com.daonvshu.shared.database.schema.MikanDataRecord
import kotlinx.coroutines.flow.MutableStateFlow

class BangumiSharedVm : ViewModel() {

    val navHost = MutableStateFlow("")

    var detailBangumiItem = MikanDataRecord()

    var showOnlyDownloading = true
    var showExtraDownloading = false
    val targetDownloadId = MutableStateFlow(-1)
}