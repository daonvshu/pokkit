package com.daonvshu.pokkit.bangumi.pages

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.daonvshu.pokkit.bangumi.BangumiSharedVm
import com.daonvshu.pokkit.bangumi.dialog.TorrentDownloadDialog
import com.daonvshu.pokkit.bangumi.network.MikanApi
import com.daonvshu.shared.components.*
import com.daonvshu.shared.generated.resources.*
import com.daonvshu.shared.utils.PrimaryColors
import io.github.mataku.middleellipsistext.MiddleEllipsisText
import org.jetbrains.compose.resources.painterResource

@Composable
fun MikanBangumiDetailPage(sharedVm: BangumiSharedVm) {
    val vm = viewModel { MikanBangumiDetailPageVm(sharedVm.detailBangumiItem) }

    LaunchedEffect(vm) {
        vm.updateDetail()
        vm.updateTorrentLinks()
    }

    Column {
        DetailInfoBox(vm, sharedVm)
        DownloadLinkView(vm)
        DownloadDialog(vm)
    }
}

@Composable
fun DetailInfoBox(vm: MikanBangumiDetailPageVm, sharedVm: BangumiSharedVm) {
    Row(
        modifier = Modifier.height(300.dp)
    ) {
        Box(
            modifier = Modifier
                .width(240.dp)
                .fillMaxHeight()
        ) {
            AsyncImage(
                model = MikanApi.HOST + sharedVm.detailBangumiItem.thumbnail,
                modifier = Modifier.fillMaxSize(),
                contentDescription = null,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MiddleEllipsisText(
                    modifier = Modifier.weight(1f),
                    text = vm.data.title,
                    fontSize = 20.sp,
                    color = PrimaryColors.Text_Normal,
                )

                ShapeIconButton(resource = Res.drawable.ic_refresh, color = PrimaryColors.Icon_Button_Primary) {
                    vm.updateDetail(true)
                }

                ShapeIconButton(resource = Res.drawable.ic_back, color = PrimaryColors.Icon_Button_Primary) {
                    sharedVm.navHost.value = "pop"
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val scrollState = rememberScrollState()
                val summary by vm.summary.collectAsStateWithLifecycle()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 4.dp, bottom = 4.dp, end = 8.dp)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = summary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = PrimaryColors.Text_Secondary,
                    )
                }

                VerticalScrollbar(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .width(8.dp),
                    adapter = rememberScrollbarAdapter(scrollState)
                )
            }

            Divider(color = PrimaryColors.GRAY.color(level = 4, alpha = 0.4f), thickness = 1.dp)

            CompositionLocalProvider(
                LocalTextStyle provides LocalTextStyle.current.copy(
                    fontSize = 14.sp, color = PrimaryColors.Text_Normal
                )
            ) {
                val eps by vm.eps.collectAsStateWithLifecycle()
                Row(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("集数：")
                    Text("$eps")
                }

                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text("相关链接：")
                    val items by vm.sites.collectAsStateWithLifecycle()
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val uriHandler = LocalUriHandler.current
                        items.forEachIndexed { index, item ->
                            Box(
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 60.dp)
                                    .height(24.dp)
                                    .clickable(
                                        onClick = {
                                            uriHandler.openUri(item.url)
                                        }
                                    )
                                    .background(
                                        PrimaryColors.Button_Normal.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    when (item.type) {
                                        "onair" -> {
                                            Icon(
                                                modifier = Modifier.height(16.dp),
                                                painter = painterResource(Res.drawable.ic_type_play),
                                                contentDescription = "onair",
                                                tint = PrimaryColors.Icon_Button,
                                            )
                                        }

                                        "resource" -> {
                                            Icon(
                                                modifier = Modifier.height(16.dp),
                                                painter = painterResource(Res.drawable.ic_download),
                                                contentDescription = "resource",
                                                tint = PrimaryColors.Icon_Button,
                                            )
                                        }

                                        "info" -> {
                                            Icon(
                                                modifier = Modifier.height(16.dp),
                                                painter = painterResource(Res.drawable.ic_type_info),
                                                contentDescription = "info",
                                                tint = PrimaryColors.Icon_Button,
                                            )
                                        }
                                    }

                                    Text(item.name)
                                }
                            }
                        }
                    }
                }

                Row {
                    val officialSite by vm.officialSite.collectAsStateWithLifecycle()
                    Text("官方网站：")
                    Text(buildAnnotatedString {
                        withLink(
                            LinkAnnotation.Url(
                                officialSite,
                                TextLinkStyles(style = SpanStyle(color = PrimaryColors.Text_Selected))
                            )
                        ) {
                            append(officialSite)
                        }
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DownloadLinkView(vm: MikanBangumiDetailPageVm) {
    Column(
        modifier = Modifier.padding(top = 12.dp)
    ) {
        val selectedIndex by vm.filterFansubIndex.collectAsStateWithLifecycle()
        val fansubs by vm.selectorDataFansubs.collectAsStateWithLifecycle()

        if (fansubs.isNotEmpty()) {
            TabNavBar(
                titles = fansubs,
                selectedIndex = selectedIndex,
                normalColor = PrimaryColors.Text_Normal,
                selectedColor = PrimaryColors.Text_Selected,
                scrollable = true,
                fontSize = 14.sp,
                iconScale = 0.8f,
            ) {
                vm.filterFansubIndex.value = it
                vm.reloadTorrentLinksByFilter()
            }
        }

        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(
                fontSize = 14.sp, color = PrimaryColors.Text_Normal
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val selectedAll by vm.filterIsAllSelected.collectAsStateWithLifecycle()
                NormalCheckbox(
                    modifier = Modifier.padding(vertical = 4.dp),
                    checked = selectedAll,
                    onCheckedChange = { checked ->
                        vm.filterIsAllSelected.value = checked
                        vm.itemChecked.value = vm.itemChecked.value.map { checked }
                    },
                    label = "全选"
                )

                val gb by vm.filterGb.collectAsStateWithLifecycle()
                NormalCheckbox(
                    checked = gb,
                    onCheckedChange = { checked ->
                        vm.filterGb.value = checked
                        vm.reloadTorrentLinksByFilter()
                    },
                    label = "简体"
                )

                HSpacer()

                ShapeIconButton(resource = Res.drawable.ic_refresh, color = PrimaryColors.Icon_Button_Primary) {
                    vm.updateTorrentLinks(true)
                }

                ShapeIconButton(resource = Res.drawable.ic_download, color = PrimaryColors.Icon_Button_Primary) {
                    vm.showDownloadDialog.value = true
                    vm.beginFetchSelectedLinks()
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val eps by vm.selectorDataEps.collectAsStateWithLifecycle()
                val epsSelected by vm.filterEps.collectAsStateWithLifecycle()
                if (eps.isNotEmpty()) {
                    FlowRowGroup(
                        items = eps,
                        selectedValue = epsSelected,
                        itemWidth = 26.dp,
                        itemHeight = 26.dp,
                        fontSize = 14.sp,
                        padding = 0.dp,
                    ) { index, value ->
                        if (vm.filterEps.value == value) {
                            vm.filterEps.value = -1
                        } else {
                            vm.filterEps.value = value
                        }
                        vm.reloadTorrentLinksByFilter()
                    }
                }
            }

            VSpacer(4.dp)

            val torrentFilteredLinks by vm.torrentFilteredLinks.collectAsStateWithLifecycle()
            val itemChecked by vm.itemChecked.collectAsStateWithLifecycle()

            CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(fontSize = 13.sp)) {
                val listState = rememberLazyListState()
                val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    flingBehavior = flingBehavior,
                ) {
                    itemsIndexed(torrentFilteredLinks) { index, item ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NormalCheckbox(
                                modifier = Modifier.fillMaxWidth(),
                                checked = itemChecked[index],
                                onCheckedChange = {
                                    vm.itemChecked.value = vm.itemChecked.value.mapIndexed { i, it ->
                                        if (index == i) {
                                            !it
                                        } else {
                                            it
                                        }
                                    }
                                    vm.filterIsAllSelected.value = vm.itemChecked.value.all { it }
                                },
                                label = item.description,
                                labelColor = if (vm.downloadedRecords.contains(item.description)) {
                                    PrimaryColors.GRAY.color(4)
                                } else {
                                    Color.Unspecified
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadDialog(vm: MikanBangumiDetailPageVm) {
    val show by vm.showDownloadDialog.collectAsStateWithLifecycle()
    if (show) {
        TorrentDownloadDialog(vm.data, vm.curSelectedFanSub(), vm.currentTorrentRequestId) {
            vm.showDownloadDialog.value = false
            vm.reloadDownloadedRecords {
                vm.reloadTorrentLinksByFilter()
            }
        }
    }
}
