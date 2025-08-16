package com.daonvshu.bangumi.pages

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daonvshu.bangumi.BangumiSharedVm
import com.daonvshu.bangumi.repository.DownloadDataRepository
import com.daonvshu.shared.backendservice.BackendDataObserver
import com.daonvshu.shared.backendservice.RequestOpenDir
import com.daonvshu.shared.backendservice.SpecialIntCommand
import com.daonvshu.shared.backendservice.TorrentPauseOrResumeRequest
import com.daonvshu.shared.backendservice.TorrentRemoveRequest
import com.daonvshu.shared.backendservice.bean.TorrentDisplayInfo
import com.daonvshu.shared.backendservice.bean.TorrentDownloadStateType
import com.daonvshu.shared.backendservice.sendToBackend
import com.daonvshu.shared.components.TreeNode
import com.daonvshu.shared.database.Databases
import com.daonvshu.shared.database.schema.DownloadRecord
import com.daonvshu.shared.utils.LogCollector
import com.daonvshu.shared.utils.toValidSystemName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.io.File
import java.util.Base64

data class DownloadSrcRecordList(
    val id: Int,
    val title: String,
    val records: List<DownloadRecord>
)

data class DownloadItemData(
    val record: DownloadRecord,
    val data: MutableState<TorrentDisplayInfo?> = mutableStateOf(null),
    val isDownloading: MutableState<Boolean> = mutableStateOf(false),
    var removed: Boolean = false,
)

class DownloadListPageVm(private val sharedVm: BangumiSharedVm): ViewModel() {

    val srcData = mutableListOf<DownloadSrcRecordList>()
    val displayData = MutableStateFlow<TreeNode<DownloadItemData>?>(null)

    init {
        BackendDataObserver.torrentStatusList.onEach { statusList ->
            if (statusList == null) {
                return@onEach
            }
            val statusMap = statusList.status.groupBy { it.torrentHash }

            var isAnyRemoved = false
            displayData.value?.children?.forEach { parentNode ->
                parentNode.children.forEach childLoop@{ childNode ->
                    val childData = childNode.data!!
                    var status = statusMap[childData.record.torrentInfoHash]?.firstOrNull()
                    if (status == null) {
                        return@childLoop
                    }
                    val downloadState = TorrentDownloadStateType.of(status.downloadState)
                    if (downloadState == TorrentDownloadStateType.Uploading) {
                        if (!childData.record.finished) {
                            Databases.downloadRecordService.makeFinished(childData.record.id)
                        }
                        if (sharedVm.showOnlyDownloading) {
                            childData.removed = true
                            isAnyRemoved = true
                        }
                    }
                    childData.isDownloading.value = downloadState == TorrentDownloadStateType.Downloading
                    childData.data.value = status
                }
            }
            if (isAnyRemoved) {
                val oldData = displayData.value!!
                val rootNode = TreeNode<DownloadItemData>("/", null)
                rootNode.children.addAll(oldData.children)
                rootNode.children.forEach { parentNode ->
                    parentNode.children.removeIf { it.data!!.removed }
                }
                rootNode.children.removeIf { it.children.isEmpty() }
                displayData.value = rootNode
            }

        }.launchIn(viewModelScope)
    }

    fun reloadData() {
        viewModelScope.launch(Dispatchers.IO) {
            srcData.clear()
            displayData.value = null
            val records = DownloadDataRepository.get().getBangumiDownloadRecordList()
            val ids = records.keys.sorted()
            ids.forEach { id ->
                val recordList = records[id]!!
                if (recordList.isEmpty()) {
                    return@forEach
                }
                srcData.add(DownloadSrcRecordList(
                    id = id,
                    title = recordList.first().title,
                    records = recordList
                ))
            }
            LogCollector.addLog("load record size: ${records.size}")
            LogCollector.addLog("show only downloading: ${sharedVm.showOnlyDownloading}")
            LogCollector.addLog("show extra downloading: ${sharedVm.showExtraDownloading}")
            LogCollector.addLog("target download id: ${sharedVm.targetDownloadId}")

            val root = TreeNode<DownloadItemData>("/", null)
            srcData.forEach srcLoop@{ srcRecordList ->
                if (sharedVm.targetDownloadId.value != -1) {
                    if (sharedVm.targetDownloadId.value != srcRecordList.id) {
                        return@srcLoop
                    }
                }
                val groupNode = TreeNode(srcRecordList.title, DownloadItemData(record = DownloadRecord.empty().copy(
                    id = srcRecordList.id
                )), isExpanded = mutableStateOf(true))
                srcRecordList.records.forEach recordLoop@{ record ->
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
                    if (sharedVm.showOnlyDownloading) {
                        if (record.finished) {
                            return@recordLoop
                        }
                    }
                    val node = TreeNode("", DownloadItemData(record, mutableStateOf(status)))
                    groupNode.children.add(node)
                }
                if (groupNode.children.isNotEmpty()) {
                    root.children.add(groupNode)
                }
            }
            displayData.value = root

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

    fun requestPauseAll() {
        LogCollector.addLog("request torrent pause all")
        TorrentPauseOrResumeRequest(
            isPause = true,
            isAll = true,
            torrentHash = emptyList()
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

    fun requestResumeAll() {
        LogCollector.addLog("request torrent resume all")
        TorrentPauseOrResumeRequest(
            isPause = false,
            isAll = true,
            torrentHash = emptyList()
        ).sendToBackend()
    }

    fun playTarget(torrent: DownloadItemData) {
        val fileSaveDir = torrent.data.value!!.filePath
        Desktop.getDesktop().open(File(fileSaveDir + "\\" + torrent.record.torrentName))
    }

    fun removeTarget(torrent: DownloadItemData, removeSrc: Boolean) {
        TorrentRemoveRequest(
            removeSrcFile = removeSrc,
            torrentHash = listOf(torrent.record.torrentInfoHash)
        ).sendToBackend()

        Databases.downloadRecordService.removeRecord(torrent.record.id)

        displayData.value = displayData.value?.deepCopy(true) { node ->
            node.data?.record?.id != torrent.record.id
        }
    }

    fun removeAll(node: TreeNode<DownloadItemData>, removeSrc: Boolean) {
        val childRecords = node.children.map { it.data!!.record }
        TorrentRemoveRequest(
            removeSrcFile = removeSrc,
            torrentHash = childRecords.map { it.torrentInfoHash }
        ).sendToBackend()

        val recordIds = childRecords.map { it.id }
        Databases.downloadRecordService.removeRecords(recordIds)

        displayData.value = displayData.value?.deepCopy(true) { node ->
            !recordIds.contains(node.data?.record?.id)
        }
    }

    fun exportTorrent(torrent: DownloadItemData) {
        val fileSavePath = torrent.data.value!!.filePath + "\\" + torrent.record.torrentSrcName.toValidSystemName() + ".torrent"
        val file = File(fileSavePath)
        try {
            file.writeBytes(Base64.getDecoder().decode(torrent.record.torrentData))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        RequestOpenDir(listOf(fileSavePath)).sendToBackend()
    }

    fun openTarget(torrent: DownloadItemData) {
        val fileSavePath = torrent.data.value!!.filePath + "\\" + torrent.record.torrentName
        RequestOpenDir(listOf(fileSavePath)).sendToBackend()
    }
}