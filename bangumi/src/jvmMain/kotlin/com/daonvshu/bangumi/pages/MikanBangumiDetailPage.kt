package com.daonvshu.bangumi.pages

import TreeView
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daonvshu.bangumi.BangumiSharedVm
import com.daonvshu.shared.components.FlowRowGroup
import com.daonvshu.shared.components.HSpacer
import com.daonvshu.shared.components.IconButtonSize
import com.daonvshu.shared.components.ImageLoadingIndicator
import com.daonvshu.shared.components.NormalCheckbox
import com.daonvshu.shared.components.ShapeIconButton
import com.daonvshu.shared.components.StandardButton
import com.daonvshu.shared.components.TabNavBar
import com.daonvshu.shared.components.VSpacer
import com.daonvshu.shared.generated.resources.Res
import com.daonvshu.shared.generated.resources.ic_back
import com.daonvshu.shared.generated.resources.ic_download
import com.daonvshu.shared.generated.resources.ic_modify
import com.daonvshu.shared.generated.resources.ic_refresh
import com.daonvshu.shared.generated.resources.ic_type_info
import com.daonvshu.shared.generated.resources.ic_type_play
import com.daonvshu.shared.utils.PrimaryColors
import com.daonvshu.shared.utils.friendlySize
import io.github.mataku.middleellipsistext.MiddleEllipsisText
import org.jetbrains.compose.resources.painterResource
import java.io.File
import javax.swing.JFileChooser

@Composable
fun MikanBangumiDetailPage(sharedVm: BangumiSharedVm) {
    val vm = viewModel { MikanBangumiDetailPageVm(sharedVm.detailBangumiItem) }

    LaunchedEffect(vm) {
        vm.loadImage(sharedVm.detailBangumiItem.thumbnail)
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
            val image by vm.imageCache.collectAsStateWithLifecycle()
            if (image != null) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    bitmap = image!!,
                    contentDescription = null,
                )
            } else {
                ImageLoadingIndicator()
            }
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
                                label = item.description
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
        Dialog(onDismissRequest = {
            vm.showDownloadDialog.value = false
        }) {
            CompositionLocalProvider(
                LocalTextStyle provides LocalTextStyle.current.copy(
                    fontSize = 14.sp, color = PrimaryColors.Text_Normal
                )
            ) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(PrimaryColors.White),
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(PrimaryColors.Bangumi_Body),
                        ) {
                            Text(
                                "下载",
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(horizontal = 16.dp),
                            )
                        }

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            val downloadSizeAll by vm.downloadSizeAll.collectAsStateWithLifecycle()
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text("总大小：")
                                Text(downloadSizeAll.friendlySize())
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("保存路径：")
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PrimaryColors.Bangumi_Body),
                                ) {
                                    Text(
                                        "D:\\Bangumi",
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .fillMaxWidth()
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val chooser = JFileChooser()
                                        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                                        chooser.isAcceptAllFileFilterUsed = false
                                        //chooser.currentDirectory = File("")
                                        val result = chooser.showOpenDialog(null)
                                        if (result == JFileChooser.APPROVE_OPTION) {
                                            println(chooser.selectedFile.absolutePath)
                                        }
                                    }
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painterResource(Res.drawable.ic_modify),
                                            modifier = Modifier,
                                            contentDescription = "",
                                            tint = PrimaryColors.Icon_Button,
                                        )

                                        Text("更改")
                                    }
                                }
                            }

                            NormalCheckbox(
                                checked = true,
                                onCheckedChange = {

                                },
                                label = "以番剧名自动创建目录"
                            )

                            NormalCheckbox(
                                checked = true,
                                onCheckedChange = {

                                },
                                label = "仅下载种子文件"
                            )

                            Box(
                                modifier = Modifier
                                    .height(300.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryColors.Bangumi_Body),
                            ) {
                                val isFetching by vm.isTorrentFetching.collectAsStateWithLifecycle()
                                if (isFetching) {
                                    val fetchProgress by vm.torrentFetchProgress.collectAsStateWithLifecycle()
                                    Text(fetchProgress)
                                } else {
                                    val root by vm.torrentFetchedData.collectAsStateWithLifecycle()
                                    if (root != null) {
                                        TreeView(
                                            nodes = listOf(root!!),
                                            iconHint = PrimaryColors.Icon_Button
                                        ) {
                                            vm.reloadSelectedSize()
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                StandardButton("下载", color = PrimaryColors.Button_Normal_Primary) {
                                    vm.startDownloadSelectedTorrents()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
