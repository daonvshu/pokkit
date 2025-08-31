package com.daonvshu.pokkit.bangumi.pages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daonvshu.pokkit.bangumi.repository.MikanDataRepository
import com.daonvshu.pokkit.database.schema.MikanDataRecord
import com.daonvshu.shared.utils.LogCollector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okio.IOException
import java.net.SocketTimeoutException
import java.time.LocalDate
import java.util.*

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

    val favoriteFilter = MutableStateFlow(false)

    private var seasonData = emptyList<MikanDataRecord>()

    val weekSeasonData = MutableStateFlow(emptyList<MikanDataRecord>())

    fun reloadSeasonData() {
        viewModelScope.launch(Dispatchers.IO) {
            LogCollector.addLog("begin fetch season data...")
            try {
                val records = MikanDataRepository.get().getDataBySeason(filterYear.value, filterSeason.value)
                seasonData = records
                LogCollector.addLog("fetch season data done...")
                reloadWeekData()
            } catch (e: SocketTimeoutException) {
                LogCollector.addLog("connect timeout: ${e.message}")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun reloadWeekData() {
        LogCollector.addLog("reload week data: ${weekDayFilter.value}")
        weekSeasonData.value = seasonData.filter {
            it.dayOfWeek == weekDayFilter.value && if (favoriteFilter.value) {
                it.favorite
            } else {
                true
            }
        }
    }

    fun changeFavorite(item: MikanDataRecord) {
        val toFavorite = !item.favorite

        val copy: (MikanDataRecord) -> MikanDataRecord = {
            if (it.mikanId == item.mikanId && it.dayOfWeek == item.dayOfWeek) {
                it.copy(favorite = toFavorite)
            } else {
                it
            }
        }
        seasonData = seasonData.map(copy)
        weekSeasonData.value = weekSeasonData.value.map(copy)

        viewModelScope.launch {
            MikanDataRepository.get().changeFavorite(item.copy(favorite = toFavorite))
        }
    }

    enum class ImageLoadState {
        LOADING,
        FINISHED,
        ERROR
    }
}