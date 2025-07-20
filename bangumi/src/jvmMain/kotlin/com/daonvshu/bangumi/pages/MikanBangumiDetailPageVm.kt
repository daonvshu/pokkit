package com.daonvshu.bangumi.pages

import CheckState
import TreeNode
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daonvshu.bangumi.network.MikanApi
import com.daonvshu.bangumi.repository.MikanDataRepository
import com.daonvshu.shared.backendservice.BackendDataObserver
import com.daonvshu.shared.backendservice.RequestOpenDir
import com.daonvshu.shared.backendservice.TorrentContentFetchRequest
import com.daonvshu.shared.backendservice.TorrentContentFetchResult
import com.daonvshu.shared.backendservice.sendToBackend
import com.daonvshu.shared.database.schema.MikanDataRecord
import com.daonvshu.shared.database.schema.MikanTorrentLinkCache
import com.daonvshu.shared.settings.AppSettings
import com.daonvshu.shared.utils.ImageCacheLoader
import com.daonvshu.shared.utils.LogCollector
import com.daonvshu.shared.utils.friendlySize
import com.daonvshu.shared.utils.toValidSystemName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Base64

data class TorrentLinkData(
    val fansub: String,
    val torrent: MutableList<MikanTorrentLinkCache>
)

data class TorrentNodeData(
    val srcName: String,
    val linkUrl: String,
    val torrentContent: String,
    val filePath: String,
    val itemSize: Long,
)

class MikanBangumiDetailPageVm(var data: MikanDataRecord): ViewModel() {
    // 图片缓存
    val imageCache = MutableStateFlow<ImageBitmap?>(null)
    // 介绍
    val summary = MutableStateFlow(data.summary)
    // 官网
    val officialSite = MutableStateFlow(data.officialSite)
    // 集数
    val eps = MutableStateFlow(data.eps)
    // 当前番剧的下载链接列表
    var torrentLinkCaches = mutableListOf<TorrentLinkData>()
    // 筛选后的下载链接列表
    val torrentFilteredLinks = MutableStateFlow(emptyList<MikanTorrentLinkCache>())
    // 列表中的选项是否选中
    val itemChecked = MutableStateFlow(emptyList<Boolean>())
    // 字幕组筛选列表
    val selectorDataFansubs = MutableStateFlow(emptyList<String>())
    // 当前选择的字幕组
    val filterFansubIndex = MutableStateFlow(-1)
    // 是否只显示简体字幕
    val filterGb = MutableStateFlow(false)
    // 集数筛选列表
    val selectorDataEps = MutableStateFlow(emptyList<Int>())
    // 当前选中的筛选集数
    val filterEps = MutableStateFlow(-1)
    // 列表是否全部选中
    val filterIsAllSelected = MutableStateFlow(false)

    // 是否显示下载对话框
    val showDownloadDialog = MutableStateFlow(false)
    // 种子文件请求id
    var currentTorrentRequestId = 0L
    // 种子文件请求进度
    val torrentFetchProgress = MutableStateFlow("")
    // 种子请求中
    val isTorrentFetching = MutableStateFlow(true)
    // 种子信息
    val torrentFetchedData = MutableStateFlow<TreeNode<TorrentNodeData>?>(null)
    // 下载总大小
    val downloadSizeAll = MutableStateFlow(0L)
    // 保存位置
    val saveDir = MutableStateFlow(AppSettings.settings.general.bangumiLastSavePath)
    // 自动创建目录
    val autoCreateDir = MutableStateFlow(AppSettings.settings.general.autoCreateDir)
    // 仅下载种子文件
    val onlyDownloadTorrent = MutableStateFlow(false)
    // 是否可以点击下载
    val downloadEnabled = MutableStateFlow(false)

    init {
        BackendDataObserver.torrentContentFetchProgressUpdate.onEach { data ->
            if (data != null) {
                if (data.requestId == currentTorrentRequestId) {
                    torrentFetchProgress.value = "下载中：${data.finishedCount}/${data.totalCount}"
                }
            }
        }.launchIn(viewModelScope)

        BackendDataObserver.torrentContentFetchResult.onEach { data ->
            if (data != null) {
                if (data.requestId == currentTorrentRequestId) {
                    isTorrentFetching.value = false
                    parseTorrentLinks(data)
                    BackendDataObserver.torrentContentFetchResult.value = null
                }
            }
        }.launchIn(viewModelScope)
    }

    data class SiteInfo(
        val name: String,
        val url: String,
        val type: String,
    )

    val sites = MutableStateFlow(parseSites(data.sites))

    fun loadImage(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val imageData = try {
                ImageCacheLoader.getImage(url, "mikan_image", MikanApi.apiService::getImage)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            withContext(Dispatchers.Main) {
                imageCache.value = imageData
            }
        }
    }

    fun updateDetail(reload: Boolean = false) {
        if (!reload && data.bindBangumiId != -1) {
            LogCollector.addLog("fetch detail from cache.")
            refreshUi()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (MikanDataRepository.get().updateDetail(data)) {
                    MikanDataRepository.get().getDataById(data.mikanId)?.let {
                        data = it
                        refreshUi()
                        LogCollector.addLog("fetch detail finished.")
                    }
                } else {
                    LogCollector.addLog("update detail info fail!")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LogCollector.addLog("fetch detail fail!")
            }
        }
    }

    fun updateTorrentLinks(reload: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val caches = MikanDataRepository.get().getTorrentLinks(data, reload)
                torrentLinkCaches.clear()
                val fansubs = mutableListOf<String>()
                caches.forEach {
                    val index = fansubs.indexOf(it.fansub)
                    if (index != -1) {
                        torrentLinkCaches[index].torrent.add(it)
                    } else {
                        torrentLinkCaches.add(TorrentLinkData(it.fansub, mutableListOf(it)))
                        fansubs.add(it.fansub)
                    }
                }
                selectorDataFansubs.value = fansubs
                if (fansubs.isNotEmpty()) {
                    filterFansubIndex.value = 0
                }
                filterEps.value = -1
                reloadTorrentLinksByFilter()
            } catch (e: Exception) {
                e.printStackTrace()
                LogCollector.addLog("fetch torrent links fail!")
            }
        }
    }

    fun reloadTorrentLinksByFilter() {
        val eps = mutableListOf<Int>()
        torrentLinkCaches[filterFansubIndex.value].torrent.forEach {
            if (eps.indexOf(it.eps) == -1) {
                eps.add(it.eps)
            }
        }
        eps.removeIf { it == -1 }
        if (eps.size == 1) {
            eps.clear()
        }
        selectorDataEps.value = eps.sorted()
        if (selectorDataEps.value.indexOf(filterEps.value) == -1) {
            filterEps.value = -1
        }

        torrentFilteredLinks.value = torrentLinkCaches[filterFansubIndex.value].torrent.filter {
            (it.gb || !filterGb.value) &&
            (filterEps.value == -1 || it.eps == filterEps.value)
        }

        itemChecked.value = torrentFilteredLinks.value.map { false }
    }

    fun beginFetchSelectedLinks() {
        torrentFetchProgress.value = ""
        isTorrentFetching.value = true
        downloadSelectedLinks()
    }

    fun downloadSelectedLinks() {
        currentTorrentRequestId = System.currentTimeMillis()
        val selectedData = torrentFilteredLinks.value.filterIndexed { index, item ->
            return@filterIndexed itemChecked.value[index]
        }
        TorrentContentFetchRequest(
            requestId = currentTorrentRequestId,
            torrentSrcNames = selectedData.map { it.description },
            torrentUrls = selectedData.map { it.downloadUrl }
        ).sendToBackend()
    }

    fun reloadSelectedSize() {
        downloadSizeAll.value = torrentFetchedData.value?.let { root ->
            getCheckedData(root.children).sumOf { it.itemSize }
        } ?: 0L

        downloadEnabled.value = downloadSizeAll.value > 0 && saveDir.value.isNotEmpty()
    }

    fun updateDownloadDir(dir: String) {
        saveDir.value = dir
        AppSettings.settings.general.bangumiLastSavePath = dir
        AppSettings.save()
        downloadEnabled.value = downloadSizeAll.value > 0 && saveDir.value.isNotEmpty()
        println(dir)
    }

    fun startDownloadSelectedTorrents() {
        val nodes = torrentFetchedData.value?.children
        if (nodes == null) {
            return
        }

        val torrents = getCheckedData(nodes)
        if (torrents.isEmpty()) {
            return
        }
        println(torrents.map { it.srcName + ":" + it.linkUrl + ":" + it.filePath })
        val torrentGroup = torrents.groupBy { it.linkUrl }.filter { (_, list) -> list.isNotEmpty() }
        if (onlyDownloadTorrent.value) {
            val saveDir = File(saveDir.value + if (autoCreateDir.value) "/${data.title.toValidSystemName()}" else "")
            if (!saveDir.exists()) {
                saveDir.mkdirs()
            }
            val saveFiles = mutableListOf<String>()
            torrentGroup.forEach { (_, torrents) ->
                val saveFile = File(saveDir, torrents.first().srcName.toValidSystemName() + ".torrent")
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
            showDownloadDialog.value = false
        }
    }

    private fun refreshUi() {
        summary.value = data.summary
        officialSite.value = data.officialSite
        eps.value = data.eps
        sites.value = parseSites(data.sites)
    }

    private fun parseSites(sites: String): List<SiteInfo> {
        if (sites.isEmpty()) {
            return emptyList()
        }
        val siteList = sites.split(",")
        if (siteList.size % 3 != 0) {
            return emptyList()
        }
        val result = mutableListOf<SiteInfo>()
        for (i in siteList.indices step 3) {
            result.add(SiteInfo(siteList[i].trim(), siteList[i + 1], siteList[i + 2]))
        }
        return result.sortedBy { it.type }
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
                            torrentContent = content.torrentContent,
                            filePath = path.path,
                            itemSize = path.size
                        ) else null
                    )
                    current.children.add(newNode)
                    newNode.parent = current
                    current = newNode
                }
            }
        }
        torrentFetchedData.value = root
        reloadSelectedSize()
    }

    private fun <T> getCheckedData(children: List<TreeNode<T>>): List<T> {
        val result = mutableListOf<T>()
        fun dfs(node: TreeNode<T>) {
            if (node.children.isNotEmpty()) {
                node.children.forEach { dfs(it) }
            } else {
                if (node.checkState.value == CheckState.Checked) {
                    node.data?.let { result.add(it) }
                }
            }
        }

        children.forEach { dfs(it) }
        return result
    }
}