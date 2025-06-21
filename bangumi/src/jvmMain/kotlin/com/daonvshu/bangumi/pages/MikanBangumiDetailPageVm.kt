package com.daonvshu.bangumi.pages

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daonvshu.bangumi.network.MikanApi
import com.daonvshu.bangumi.repository.MikanDataRepository
import com.daonvshu.shared.database.schema.MikanDataRecord
import com.daonvshu.shared.utils.ImageCacheLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MikanBangumiDetailPageVm(var data: MikanDataRecord): ViewModel() {

    val imageCache = MutableStateFlow<ImageBitmap?>(null)

    val summary = MutableStateFlow(data.summary)

    val officialSite = MutableStateFlow(data.officialSite)

    val eps = MutableStateFlow(data.eps)

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