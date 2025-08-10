package com.daonvshu.bangumi.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.daonvshu.bangumi.network.MikanApi
import com.daonvshu.shared.utils.PrimaryColors

@Composable
fun DownloadBangumiPage(onItemClicked: (BangumiDownloadRecordViewData) -> Unit) {

    val vm = viewModel{ DownloadBangumiPageVm() }

    LaunchedEffect(vm) {
        vm.reloadData()
    }

    val gridState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = gridState)

    val recordData by vm.recordList.collectAsStateWithLifecycle()
    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        flingBehavior = flingBehavior
    ) {
        items(recordData) { item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clickable {
                                onItemClicked(item)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = MikanApi.HOST + item.thumbnail,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentDescription = "thumbnail",
                            contentScale = ContentScale.Crop,
                        )
                    }
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = PrimaryColors.Text_Normal,
                    )
                }
            }
        }
    }
}