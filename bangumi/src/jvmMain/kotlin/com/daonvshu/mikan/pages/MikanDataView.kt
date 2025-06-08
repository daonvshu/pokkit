package com.daonvshu.mikan.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daonvshu.shared.components.FlowRowGroup
import com.daonvshu.shared.components.TabNavBar
import java.util.Calendar

@Composable
fun MikanDataView() {
    val vm = viewModel { MikanDataViewVm() }
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val year by vm.filterYear.collectAsStateWithLifecycle()
        val season by vm.filterSeason.collectAsStateWithLifecycle()
        LaunchedEffect(year, season) {
            vm.reloadSeasonData()
        }

        val weekDay by vm.weekDayFilter.collectAsStateWithLifecycle()
        LaunchedEffect(weekDay) {
            vm.reloadWeekData()
        }

        YearFilter(vm)
        SeasonFilter(vm)
        WeekFilter(vm)
    }
}

@Composable
fun YearFilter(vm: MikanDataViewVm) {
    val selectedYear by vm.filterYear.collectAsStateWithLifecycle()
    FlowRowGroup(
        title = "年份：",
        items = (2013 .. Calendar.getInstance().get(Calendar.YEAR)).reversed().toList(),
        selectedValue = selectedYear,
    ) { _, year ->
        vm.filterYear.value = year
    }
}

@Composable
fun SeasonFilter(vm: MikanDataViewVm) {
    val selectedSeason by vm.filterSeason.collectAsStateWithLifecycle()
    FlowRowGroup(
        title = "季度：",
        items = listOf("冬季", "春季", "夏季", "秋季"),
        selectedIndex = selectedSeason,
    ) { index, _ ->
        vm.filterSeason.value = index
    }
}

@Composable
fun WeekFilter(vm: MikanDataViewVm) {
    val selectedIndex by vm.weekDayFilter.collectAsStateWithLifecycle()
    val days = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    TabNavBar(
        titles = days,
        selectedIndex = selectedIndex,
        normalColor = Color(0xFF6B4D36),
        selectedColor = Color(0xFF22A9C3),
    ) {
        vm.weekDayFilter.value = it
    }
 }