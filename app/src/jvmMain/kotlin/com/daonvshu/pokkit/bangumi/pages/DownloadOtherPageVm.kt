package com.daonvshu.pokkit.bangumi.pages

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daonvshu.pokkit.bangumi.dialog.TorrentAddTaskData
import com.daonvshu.pokkit.bangumi.repository.DownloadDataRepository
import com.daonvshu.pokkit.backendservice.BackendDataObserver
import com.daonvshu.pokkit.backendservice.RequestOpenDir
import com.daonvshu.pokkit.backendservice.SpecialIntCommand
import com.daonvshu.pokkit.backendservice.TorrentContentFetch2Request
import com.daonvshu.pokkit.backendservice.TorrentPauseOrResumeRequest
import com.daonvshu.pokkit.backendservice.TorrentRemoveRequest
import com.daonvshu.pokkit.backendservice.bean.TorrentDisplayInfo
import com.daonvshu.pokkit.backendservice.bean.TorrentDownloadStateType
import com.daonvshu.pokkit.backendservice.sendToBackend
import com.daonvshu.pokkit.database.Databases
import com.daonvshu.pokkit.database.schema.DownloadRecord
import com.daonvshu.shared.utils.LogCollector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class DownloadItemOtherData(
    val record: DownloadRecord,
    val data: MutableState<TorrentDisplayInfo?> = mutableStateOf(null),
    val isDownloading: MutableState<Boolean> = mutableStateOf(false),
    var removed: Boolean = false,
)

class DownloadOtherPageVm : ViewModel() {
    // 显示添加任务弹窗
    val showAddDlg = MutableStateFlow(false)
    // 显示下载对话框
    val showDownloadDialog = MutableStateFlow(false)
    // 种子文件请求id
    var currentTorrentRequestId = 0L
    // 下载状态
    val displayData = MutableStateFlow<List<DownloadItemOtherData>?>(null)

    init {
        BackendDataObserver.torrentStatusList.onEach { statusList ->
            if (statusList == null) {
                return@onEach
            }
            val statusMap = statusList.status.groupBy { it.torrentHash }

            displayData.value?.forEach { data ->
                var status = statusMap[data.record.torrentInfoHash]?.firstOrNull()
                if (status == null) {
                    return@forEach
                }
                val downloadState = TorrentDownloadStateType.of(status.downloadState)
                if (downloadState == TorrentDownloadStateType.Uploading) {
                    if (!data.record.finished) {
                        Databases.downloadRecordService.makeFinished(data.record.id)
                    }
                }
                data.isDownloading.value = downloadState == TorrentDownloadStateType.Downloading
                data.data.value = status
            }
        }.launchIn(viewModelScope)
    }

    fun downloadBegin(data: TorrentAddTaskData) {
        showAddDlg.value = false
        showDownloadDialog.value = true
        currentTorrentRequestId = System.currentTimeMillis()
        TorrentContentFetch2Request(
            requestId = currentTorrentRequestId,
            type = if (data.addByFile) 0 else 1,
            target = if (data.addByFile) data.torrentFile else data.linkUrl
        ).sendToBackend()
    }

    fun reloadData() {
        viewModelScope.launch(Dispatchers.IO) {
            displayData.value = null
            val recordSrc = DownloadDataRepository.get().getBangumiDownloadRecordList()
            val records = recordSrc[-1]
            if (records == null) {
                return@launch
            }

            val curDownloadRecords = mutableListOf<DownloadItemOtherData>()
            records.forEach { record ->
                val status = TorrentDisplayInfo(
                    torrentHash = record.torrentInfoHash,
                    state = 7,
                    downloadState = 0,
                    stateString = "等待",
                    speed = "-",
                    eta = "-",
                    seeds = "-",
                    downloadedSize = "-",
                    totalSize = "-",
                    progress = 0.0,
                    filePath = "",
                    createTime = 0,
                )
                val item = DownloadItemOtherData(record, mutableStateOf(status))
                curDownloadRecords.add(item)
            }
            displayData.value = curDownloadRecords

            LogCollector.addLog("request refresh torrent status")
            BackendDataObserver.torrentStatusList.value = null
            SpecialIntCommand.TORRENT_STATUS_REFRESH_REQUEST.sendToBackend()
        }
    }

    fun requestPause(torrentHash: String) {
        LogCollector.addLog("request torrent pause: $torrentHash")
        TorrentPauseOrResumeRequest(
            isPause = true,
            isAll = false,
            torrentHash = listOf(torrentHash)
        ).sendToBackend()
    }

    fun requestResume(torrentHash: String) {
        LogCollector.addLog("request torrent resume: $torrentHash")
        TorrentPauseOrResumeRequest(
            isPause = false,
            isAll = false,
            torrentHash = listOf(torrentHash)
        ).sendToBackend()
    }

    fun removeTarget(torrent: DownloadItemOtherData, removeSrc: Boolean) {
        TorrentRemoveRequest(
            removeSrcFile = removeSrc,
            torrentHash = listOf(torrent.record.torrentInfoHash)
        ).sendToBackend()

        Databases.downloadRecordService.removeRecord(torrent.record.id)

        val curDownloadRecords = mutableListOf<DownloadItemOtherData>()
        displayData.value?.forEach { data ->
            if (data.record.id != torrent.record.id) {
                curDownloadRecords.add(data)
            }
        }
        displayData.value = curDownloadRecords
    }

    fun openTarget(torrent: DownloadItemOtherData) {
        val fileSavePath = torrent.data.value!!.filePath + "\\" + torrent.record.torrentName
        RequestOpenDir(listOf(fileSavePath)).sendToBackend()
    }
}