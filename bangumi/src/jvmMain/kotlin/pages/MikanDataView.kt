package pages

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import components.FlowRowGroup
import components.TabNavBar
import java.util.Calendar

@Composable
fun MikanDataView() {
    val vm = viewModel { MikanDataViewVm() }
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
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