package com.daonvshu.bangumi.pages

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daonvshu.bangumi.network.MikanApi
import com.daonvshu.bangumi.repository.MikanDataRepository
import com.daonvshu.shared.database.schema.MikanDataRecord
import com.daonvshu.shared.database.schema.MikanTorrentLinkCache
import com.daonvshu.shared.utils.ImageCacheLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TorrentLinkData(
    val fansub: String,
    val torrent: MutableList<MikanTorrentLinkCache>
)

class MikanBangumiDetailPageVm(var data: MikanDataRecord): ViewModel() {

    val imageCache = MutableStateFlow<ImageBitmap?>(null)

    val summary = MutableStateFlow(data.summary)

    val officialSite = MutableStateFlow(data.officialSite)

    val eps = MutableStateFlow(data.eps)

    var torrentLinkCaches = mutableListOf<TorrentLinkData>()

    val torrentFilteredLinks = MutableStateFlow(emptyList<MikanTorrentLinkCache>())

    val itemChecked = MutableStateFlow(emptyList<Boolean>())

    val selectorDataFansubs = MutableStateFlow(emptyList<String>())

    val filterFansubIndex = MutableStateFlow(-1)

    val filterGb = MutableStateFlow(false)

    val selectorDataEps = MutableStateFlow(emptyList<Int>())

    val filterEps = MutableStateFlow(-1)

    val filterIsAllSelected = MutableStateFlow(false)

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
            refreshUi()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (MikanDataRepository.get().updateDetail(data)) {
                    MikanDataRepository.get().getDataById(data.mikanId)?.let {
                        data = it
                        refreshUi()
                        println("fetch detail finished.")
                    }
                } else {
                    println("update detail info fail!")
                }
            } catch (e: Exception) {
                e.printStackTrace()
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