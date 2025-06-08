package com.daonvshu.mikan.pages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daonvshu.mikan.repository.MikanDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okio.IOException
import java.net.SocketTimeoutException
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
        (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
    )

    fun reloadSeasonData() {
        viewModelScope.launch(Dispatchers.IO) {
            println("begin fetch season data...")
            try {
                val records = MikanDataRepository.get().getDataBySeason(filterYear.value, filterSeason.value)
                println(records)
                println("fetch season data done...")
            } catch (e: SocketTimeoutException) {
                println("connect timeout: ${e.message}")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun reloadWeekData() {
        println("Reload week data")
    }
}