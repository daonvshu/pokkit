package com.daonvshu.pokkit.bangumi.pages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daonvshu.pokkit.bangumi.repository.DownloadDataRepository
import com.daonvshu.pokkit.bangumi.repository.MikanDataRepository
import com.daonvshu.pokkit.backendservice.TorrentContentFetchRequest
import com.daonvshu.pokkit.backendservice.sendToBackend
import com.daonvshu.pokkit.database.schema.MikanDataRecord
import com.daonvshu.pokkit.database.schema.MikanTorrentLinkCache
import com.daonvshu.shared.utils.LogCollector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class TorrentLinkData(
    val fansub: String,
    val torrent: MutableList<MikanTorrentLinkCache>
)

data class TorrentNodeData(
    val srcName: String,
    val linkUrl: String,
    val linkName: String,
    val torrentInfoHash: String,
    val torrentContent: String,
    val filePath: String,
    val filePathIndex: Int,
    val itemSize: Long,
    var ignored: Boolean = false, //仅用于下载时使用
)

class MikanBangumiDetailPageVm(var data: MikanDataRecord): ViewModel() {
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
    // 当前已下载的记录列表（链接名）
    var downloadedRecords = emptySet<String>()
    // 字幕组下载数
    var fansubDownloadCount = mapOf<String, Int>()

    // 是否显示下载对话框
    val showDownloadDialog = MutableStateFlow(false)
    // 种子文件请求id
    var currentTorrentRequestId = 0L

    data class SiteInfo(
        val name: String,
        val url: String,
        val type: String,
    )

    val sites = MutableStateFlow(parseSites(data.sites))

    fun updateDetail(reload: Boolean = false) {
        if (!reload && data.bindBangumiId != -1) {
            LogCollector.addLog("fetch detail from cache.")
            refreshUi()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (MikanDataRepository.get().updateDetail(data)) {
                    val newData = MikanDataRepository.get().getDataById(data.mikanId)
                    if (newData != null) {
                        data = newData
                        refreshUi()
                        LogCollector.addLog("fetch detail finished.")
                    } else {
                        LogCollector.addLog("update detail info fail, cannot load cache!")
                    }
                } else {
                    LogCollector.addLog("update detail info fail, cannot find detail info!")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LogCollector.addLog("fetch detail fail with exception!")
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
                reloadDownloadedRecords {
                    //sort by fansubs
                    torrentLinkCaches.sortByDescending { fansubDownloadCount[it.fansub] ?: 0 }
                    selectorDataFansubs.value = torrentLinkCaches.map { it.fansub }
                    if (fansubs.isNotEmpty()) {
                        filterFansubIndex.value = 0
                    }
                    filterEps.value = -1
                    reloadTorrentLinksByFilter()
                }
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

    fun reloadDownloadedRecords(finished: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val records = DownloadDataRepository.get().getBangumiDownloadRecord(data.mikanId)
                downloadedRecords = records.map { it.torrentSrcName }.toSet()
                fansubDownloadCount = records.filter {
                    it.fansub.isNotEmpty()
                }.groupBy {
                    it.fansub
                }.mapValues {
                    it.value.size
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LogCollector.addLog("reload downloaded records fail!")
            } finally {
                finished?.invoke()
            }
        }
    }

    fun curSelectedFanSub(): String? {
        if (selectorDataFansubs.value.isEmpty()) {
            return null
        }
        return selectorDataFansubs.value[filterFansubIndex.value]
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
}