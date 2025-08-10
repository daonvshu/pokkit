package com.daonvshu.bangumi.pages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daonvshu.bangumi.repository.DownloadDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class BangumiDownloadRecordViewData(
    val bindId: Int,
    val title: String,
    val thumbnail: String,
    val finishedCount: Int,
    val downloadingCount: Int,
)

class DownloadBangumiPageVm : ViewModel() {

    val recordList = MutableStateFlow<List<BangumiDownloadRecordViewData>>(emptyList())

    fun reloadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val records = DownloadDataRepository.get().getBangumiDownloadRecordList()
            recordList.value = records.map { (_, data) ->
                BangumiDownloadRecordViewData(
                    bindId = data.first().linkedMikanId,
                    title = data.first().title,
                    thumbnail = data.first().thumbnail,
                    finishedCount = data.count { it.finished },
                    downloadingCount = data.count { !it.finished },
                )
            }.sortedBy {
                it.bindId
            }
        }
    }
}