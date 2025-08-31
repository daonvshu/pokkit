package com.daonvshu.pokkit.bangumi.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daonvshu.pokkit.bangumi.pages.TorrentNodeData
import com.daonvshu.pokkit.bangumi.repository.DownloadDataRepository
import com.daonvshu.pokkit.backendservice.BackendDataObserver
import com.daonvshu.pokkit.backendservice.RequestOpenDir
import com.daonvshu.pokkit.backendservice.TorrentContentFetchResult
import com.daonvshu.pokkit.backendservice.TorrentDownloadRequest
import com.daonvshu.pokkit.backendservice.bean.TorrentDownloadInfo
import com.daonvshu.pokkit.backendservice.bean.TorrentDownloadPath
import com.daonvshu.pokkit.backendservice.sendToBackend
import com.daonvshu.shared.components.CheckState
import com.daonvshu.shared.components.TreeNode
import com.daonvshu.pokkit.database.Databases
import com.daonvshu.pokkit.database.schema.DownloadRecord
import com.daonvshu.pokkit.database.schema.MikanDataRecord
import com.daonvshu.pokkit.settings.AppSettings
import com.daonvshu.shared.utils.LogCollector
import com.daonvshu.shared.utils.dir
import com.daonvshu.shared.utils.friendlySize
import com.daonvshu.shared.utils.toValidSystemName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.util.Base64

class TorrentDownloadDialogVm(var data: MikanDataRecord?, val fanSub: String?, torrentRequestId: Long): ViewModel() {
    // 种子文件请求进度
    val torrentFetchProgress = MutableStateFlow("")
    // 种子请求中
    val isTorrentFetching = MutableStateFlow(true)
    // 种子信息
    val torrentFetchedData = MutableStateFlow<TreeNode<TorrentNodeData>?>(null)
    // 下载总大小
    val downloadSizeAll = MutableStateFlow(0L)
    // 保存位置
    val saveDir = MutableStateFlow("")
    // 自动创建目录
    val autoCreateDir = MutableStateFlow(true)
    // 仅下载种子文件
    val onlyDownloadTorrent = MutableStateFlow(false)
    // 是否可以点击下载
    val downloadEnabled = MutableStateFlow(false)
    // 当前已下载的记录列表（链接名）
    var downloadedRecords = emptySet<String>()

    init {
        val lastSaveDir = DownloadDataRepository.get().getBangumiLastSaveDir(data?.mikanId ?: -1)
        saveDir.value = lastSaveDir.first
        autoCreateDir.value = lastSaveDir.second

        BackendDataObserver.torrentContentFetchProgressUpdate.onEach { data ->
            if (data != null) {
                if (data.requestId == torrentRequestId) {
                    torrentFetchProgress.value = "下载中：${data.finishedCount}/${data.totalCount}"
                }
            }
        }.launchIn(viewModelScope)

        BackendDataObserver.torrentContentFetchResult.onEach { data ->
            if (data != null) {
                if (data.requestId == torrentRequestId) {
                    isTorrentFetching.value = false
                    BackendDataObserver.torrentContentFetchResult.value = null
                    reloadDownloadedRecords {
                        parseTorrentLinks(data)
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun updateDownloadDir(dir: String) {
        saveDir.value = dir
        AppSettings.settings.general.bangumiLastSavePath = dir
        AppSettings.save()
        downloadEnabled.value = downloadSizeAll.value > 0 && saveDir.value.isNotEmpty()
        println(dir)
    }

    fun startDownloadSelectedTorrents(onSuccess: () -> Unit) {
        val nodes = torrentFetchedData.value?.children
        if (nodes == null) {
            return
        }

        val nodeData = getAllNodeData(nodes)
        if (nodeData.isEmpty() || nodeData.all { it.ignored }) {
            return
        }
        //println(torrents.map { it.srcName + ":" + it.linkUrl + ":" + it.filePath })
        val torrentGroup = nodeData
            .groupBy { it.linkUrl }
            .filter { (_, list) -> list.isNotEmpty() && list.any { !it.ignored } }

        val subSaveDir = if (data != null) {
            saveDir.value.dir(autoCreateDir.value, data!!.title)
        } else {
            saveDir.value.dir(false)
        }
        if (onlyDownloadTorrent.value) {
            val saveFiles = mutableListOf<String>()
            torrentGroup.forEach { (_, torrents) ->
                val saveFile = File(subSaveDir, torrents.first().srcName.toValidSystemName() + ".torrent")
                try {
                    saveFile.writeBytes(Base64.getDecoder().decode(torrents.first().torrentContent))
                    saveFiles.add(saveFile.absolutePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (saveFiles.isNotEmpty()) {
                RequestOpenDir(saveFiles).sendToBackend()
            }
        } else {
            val torrentInfo = mutableListOf<TorrentDownloadInfo>()
            torrentGroup.forEach { (_, torrents) ->
                Databases.downloadRecordService.createRecord(
                    DownloadRecord(
                        linkedMikanId = data?.mikanId ?: -1,
                        title = data?.title ?: "",
                        thumbnail = data?.thumbnail ?: "",
                        torrentInfoHash = torrents.first().torrentInfoHash,
                        torrentData = torrents.first().torrentContent,
                        torrentSrcName = torrents.first().srcName,
                        torrentName = torrents.first().linkName,
                        fansub = fanSub ?: "",
                        saveDir = subSaveDir.absolutePath,
                        autoCreateDir = autoCreateDir.value,
                        finished = false
                    )
                )
                torrentInfo.add(TorrentDownloadInfo(
                    content = torrents.first().torrentContent,
                    paths = torrents.map { path ->
                        TorrentDownloadPath(
                            path = path.filePath,
                            ignored = path.ignored
                        )
                    }
                ))
            }
            TorrentDownloadRequest(
                savePath = subSaveDir.absolutePath,
                data = torrentInfo
            ).sendToBackend()
        }
        onSuccess()
    }

    fun reloadSelectedSize() {
        downloadSizeAll.value = torrentFetchedData.value?.let { root ->
            getAllNodeData(root.children).filter { !it.ignored }.sumOf { it.itemSize }
        } ?: 0L

        downloadEnabled.value = downloadSizeAll.value > 0 && saveDir.value.isNotEmpty()
    }

    fun reloadDownloadedRecords(finished: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val records = DownloadDataRepository.get().getBangumiAllDownloadRecords()
                downloadedRecords = records.map { it.torrentInfoHash }.toSet()
            } catch (e: Exception) {
                e.printStackTrace()
                LogCollector.addLog("reload downloaded records fail!")
            } finally {
                finished?.invoke()
            }
        }
    }

    fun reloadLinksCheckable() {
        torrentFetchedData.value = torrentFetchedData.value?.deepCopy(true, nodeProducer = { node ->
            if (!onlyDownloadTorrent.value && downloadedRecords.contains(node.data?.torrentInfoHash)) {
                node.checkable = false
                node.checkState.value = CheckState.Unchecked
            } else {
                node.checkable = true
            }
            node
        })
        reloadSelectedSize()
    }

    private fun parseTorrentLinks(data: TorrentContentFetchResult) {
        val root = TreeNode<TorrentNodeData>("/", null)
        data.data.forEach { content ->
            content.filePaths.forEach { path ->
                var folders = path.path.split("\\").drop(1)
                if (folders.isEmpty()) {
                    folders = listOf(path.path)
                }
                var current = root

                for ((index, name) in folders.withIndex()) {
                    val isFile = index == folders.lastIndex
                    val displayName = if (isFile) "$name (${path.size.friendlySize()})" else name

                    val existing = current.children.find { it.label == displayName }
                    if (existing != null) {
                        current = existing
                        continue
                    }

                    val newNode = TreeNode(
                        label = displayName,
                        data = if (isFile) TorrentNodeData(
                            srcName = content.srcName,
                            linkUrl = content.linkUrl,
                            linkName = content.linkName,
                            torrentInfoHash = content.torrentInfoHash,
                            torrentContent = content.torrentContent,
                            filePath = path.path,
                            itemSize = path.size
                        ) else null
                    )
                    if (!onlyDownloadTorrent.value && downloadedRecords.contains(content.torrentInfoHash)) {
                        newNode.checkable = false
                        newNode.checkState.value = CheckState.Unchecked
                    }
                    current.children.add(newNode)
                    newNode.parent = current
                    current = newNode
                }
            }
        }
        torrentFetchedData.value = root
        reloadSelectedSize()
    }

    private fun getAllNodeData(children: List<TreeNode<TorrentNodeData>>): List<TorrentNodeData> {
        val result = mutableListOf<TorrentNodeData>()
        fun dfs(node: TreeNode<TorrentNodeData>) {
            if (node.children.isNotEmpty()) {
                node.children.forEach { dfs(it) }
            } else {
                node.data?.let {
                    it.ignored = node.checkState.value != CheckState.Checked
                    result.add(it)
                }
            }
        }

        children.forEach { dfs(it) }
        return result
    }
}