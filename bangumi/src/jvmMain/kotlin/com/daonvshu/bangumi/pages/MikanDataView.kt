package com.daonvshu.bangumi.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daonvshu.bangumi.BangumiSharedVm
import com.daonvshu.shared.components.FlowRowGroup
import com.daonvshu.shared.components.ImageLoadingIndicator
import com.daonvshu.shared.components.TabNavBar
import com.daonvshu.shared.generated.resources.Res
import com.daonvshu.shared.generated.resources.ic_error_image
import org.jetbrains.compose.resources.painterResource
import java.util.Calendar

@Composable
fun MikanDataView(sharedVm: BangumiSharedVm) {
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
        BangumiItemView(vm, sharedVm)
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
    Row(modifier = Modifier.fillMaxWidth()) {
        FlowRowGroup(
            modifier = Modifier.weight(1f),
            title = "季度：",
            items = listOf("冬季", "春季", "夏季", "秋季"),
            selectedIndex = selectedSeason,
        ) { index, _ ->
            vm.filterSeason.value = index
        }

        val favoriteFilter by vm.favoriteFilter.collectAsStateWithLifecycle()
        Text(
            modifier = Modifier.clickable {
                vm.favoriteFilter.value = !favoriteFilter
                vm.reloadWeekData()
            },
            text = if (favoriteFilter) "\u2764\uFE0E" else "\uD83E\uDD0D"
        )
    }
}

@Composable
fun WeekFilter(vm: MikanDataViewVm) {
    val selectedIndex by vm.weekDayFilter.collectAsStateWithLifecycle()
    val days = listOf("周日", "周一", "周二", "周三", "周四", "周五", "周六", "MV", "OVA")

    TabNavBar(
        titles = days,
        selectedIndex = selectedIndex,
        normalColor = Color(0xFF6B4D36),
        selectedColor = Color(0xFF22A9C3),
    ) {
        vm.weekDayFilter.value = it
    }
}

@Composable
fun BangumiItemView(vm: MikanDataViewVm, sharedVm: BangumiSharedVm) {
    val weekData by vm.weekSeasonData.collectAsStateWithLifecycle()

    val gridState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = gridState)

    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        flingBehavior = flingBehavior
    ) {
        items(weekData) { item ->
            val state = if (item.thumbnail.isEmpty()) {
                MikanDataViewVm.ImageLoadState.ERROR
            } else {
                LaunchedEffect(item.thumbnail) {
                    vm.loadImage(item.thumbnail)
                }
                vm.imageLoadState[item.thumbnail] ?: MikanDataViewVm.ImageLoadState.LOADING
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clickable(item.link.isNotEmpty()) {
                                sharedVm.detailBangumiItem = item
                                sharedVm.navHost.value = "detail"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when (state) {
                            MikanDataViewVm.ImageLoadState.LOADING -> {
                                ImageLoadingIndicator()
                            }

                            MikanDataViewVm.ImageLoadState.ERROR -> {
                                Image(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    painter = painterResource(Res.drawable.ic_error_image),
                                    contentDescription = "error",
                                )
                            }

                            MikanDataViewVm.ImageLoadState.FINISHED -> {
                                Image(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    bitmap = vm.imageCache[item.thumbnail]!!,
                                    contentDescription = "thumbnail",
                                    contentScale = ContentScale.Crop,
                                    colorFilter = if (item.link.isEmpty())
                                        ColorFilter.tint(Color(0xFFD9D9D9), blendMode = BlendMode.Color)
                                    else null
                                )
                            }
                        }
                    }
                    Text(
                        text = buildAnnotatedString {
                            withLink(
                                LinkAnnotation.Clickable(
                                    "favorite",
                                    linkInteractionListener = {
                                        vm.changeFavorite(item)
                                    }
                                )
                            ) {
                                append(if (item.favorite) "\u2764\uFE0E" else "\uD83E\uDD0D")
                            }
                            append(item.title)
                        },
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color(0xFF6B4D36),
                    )
                }
            }
        }
    }
}