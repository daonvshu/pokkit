package com.daonvshu.mikan.pages

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daonvshu.mikan.network.MikanApi
import com.daonvshu.mikan.repository.MikanDataRepository
import com.daonvshu.shared.database.MikanDataRecord
import com.daonvshu.shared.utils.ImageCacheLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import java.net.SocketTimeoutException
import java.time.LocalDate
import java.util.Calendar

class MikanDataViewVm : ViewModel() {

    val filterYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))

    val filterSeason = MutableStateFlow(when(Calendar.getInstance().get(Calendar.MONTH)) {
        0,1,2 -> 0
        3,4,5 -> 1
        6,7,8 -> 2
        9,10,11 -> 3
        else -> 0
    })

    val weekDayFilter = MutableStateFlow(
        LocalDate.now().dayOfWeek.value.let {
            if (it == 7) 0 else it
        }
    )

    private var seasonData = emptyList<MikanDataRecord>()

    val weekSeasonData = MutableStateFlow(emptyList<MikanDataRecord>())

    fun reloadSeasonData() {
        clearImageCache()
        viewModelScope.launch(Dispatchers.IO) {
            println("begin fetch season data...")
            try {
                val records = MikanDataRepository.get().getDataBySeason(filterYear.value, filterSeason.value)
                seasonData = records
                println("fetch season data done...")
                reloadWeekData()
            } catch (e: SocketTimeoutException) {
                println("connect timeout: ${e.message}")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun reloadWeekData() {
        println("reload week data: ${weekDayFilter.value}")
        weekSeasonData.value = seasonData.filter {
            it.dayOfWeek == weekDayFilter.value.toLong()
        }
    }

    enum class ImageLoadState {
        LOADING,
        FINISHED,
        ERROR
    }

    val imageLoadState = mutableStateMapOf<String, ImageLoadState>()
    private val imageLoadJobs = mutableMapOf<String, Job>()

    val imageCache = mutableMapOf<String, ImageBitmap?>()

    fun loadImage(url: String) {
        if (imageLoadJobs.containsKey(url)) {
            return
        }
        val job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageData = try {
                    ImageCacheLoader.getImage(url, "mikan_image", MikanApi.apiService::getImage)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                withContext(Dispatchers.Main) {
                    imageCache[url] = imageData
                    if (imageData != null) {
                        imageLoadState[url] = ImageLoadState.FINISHED
                    } else {
                        imageLoadState[url] = ImageLoadState.ERROR
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    imageLoadState[url] = ImageLoadState.ERROR
                }
            }
        }
        imageLoadJobs[url] = job
    }

    private fun clearImageCache() {
        imageLoadJobs.forEach { t, u ->
            u.cancel()
        }
        imageLoadJobs.clear()
        imageCache.clear()
        imageLoadState.clear()
    }
}