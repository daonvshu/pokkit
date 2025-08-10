package com.daonvshu.bangumi.pages

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daonvshu.bangumi.BangumiSharedVm
import com.daonvshu.bangumi.repository.DownloadDataRepository
import com.daonvshu.shared.backendservice.BackendDataObserver
import com.daonvshu.shared.backendservice.BackendService
import com.daonvshu.shared.backendservice.bean.TorrentDisplayInfo
import com.daonvshu.shared.backendservice.bean.TorrentDownloadStateType
import com.daonvshu.shared.components.TreeNode
import com.daonvshu.shared.database.Databases
import com.daonvshu.shared.database.schema.DownloadRecord
import com.daonvshu.shared.utils.LogCollector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class DownloadSrcRecordList(
    val id: Int,
    val title: String,
    val records: List<DownloadRecord>
)

data class DownloadItemData(
    val record: DownloadRecord,
    var data: TorrentDisplayInfo? = null
)

class DownloadListPageVm(private val sharedVm: BangumiSharedVm): ViewModel() {

    val srcData = mutableListOf<DownloadSrcRecordList>()
    val displayData = MutableStateFlow<TreeNode<DownloadItemData>?>(null)

    init {
        BackendService.tryCreatePipeIfNeeded()
        BackendDataObserver.torrentStatusList.onEach { statusList ->
            if (statusList == null) {
                return@onEach
            }
            val statusMap = statusList.status.groupBy { it.torrentHash }

            val oldIsExpanded = mutableMapOf<Int, Boolean>()
            val oldStatus = mutableMapOf<String, TorrentDisplayInfo?>()
            displayData.value?.let {
                it.children.forEach { node ->
                    oldIsExpanded[node.data!!.record.id] = node.isExpanded.value
                    node.children.forEach { child ->
                        if (child.data != null) {
                            oldStatus[child.data!!.record.torrentInfoHash] = child.data!!.data
                        }
                    }
                }
            }

            val root = TreeNode<DownloadItemData>("/", null)
            srcData.forEach srcLoop@{ srcRecordList ->
                if (sharedVm.targetDownloadId != -1) {
                    if (sharedVm.targetDownloadId != srcRecordList.id) {
                        return@srcLoop
                    }
                }
                val groupNode = TreeNode(srcRecordList.title, DownloadItemData(record = DownloadRecord.empty().copy(
                    id = srcRecordList.id
                )), isExpanded = mutableStateOf(oldIsExpanded[srcRecordList.id] ?: true))
                srcRecordList.records.forEach recordLoop@{ record ->
                    var status = statusMap[record.torrentInfoHash]?.firstOrNull()
                    if (status == null) {
                        status = oldStatus[record.torrentInfoHash]
                        if (status == null) {
                            return@recordLoop
                        }
                    }
                    if (TorrentDownloadStateType.of(status.downloadState) == TorrentDownloadStateType.Uploading) {
                        if (!record.finished) {
                            Databases.downloadRecordService.makeFinished(record.id)
                        }
                    }
                    if (sharedVm.showOnlyDownloading) {
                        if (TorrentDownloadStateType.of(status.downloadState) != TorrentDownloadStateType.Downloading) {
                            return@recordLoop
                        }
                    }
                    val node = TreeNode("", DownloadItemData(record, status))
                    groupNode.children.add(node)
                }
                if (groupNode.children.isNotEmpty()) {
                    root.children.add(groupNode)
                }
            }
            displayData.value = root
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
                if (sharedVm.targetDownloadId != -1) {
                    if (sharedVm.targetDownloadId != srcRecordList.id) {
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
                        stateString = "加载中",
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
                    val node = TreeNode("", DownloadItemData(record, status))
                    groupNode.children.add(node)
                }
                if (groupNode.children.isNotEmpty()) {
                    root.children.add(groupNode)
                }
            }
            displayData.value = root
        }
    }
}