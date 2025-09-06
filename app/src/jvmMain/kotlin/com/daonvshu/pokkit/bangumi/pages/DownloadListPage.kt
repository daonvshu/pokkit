package com.daonvshu.pokkit.bangumi.pages

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.daonvshu.pokkit.bangumi.BangumiSharedVm
import com.daonvshu.pokkit.bangumi.dialog.TorrentRemoveDialog
import com.daonvshu.pokkit.bangumi.network.MikanApi
import com.daonvshu.pokkit.backendservice.bean.TorrentDownloadStateType
import com.daonvshu.pokkit.backendservice.bean.TorrentStateType
import com.daonvshu.shared.components.*
import com.daonvshu.shared.generated.resources.*
import com.daonvshu.shared.styles.TextStyleProvider
import com.daonvshu.shared.utils.PrimaryColors
import org.jetbrains.compose.resources.painterResource

@Composable
fun DownloadListPage(sharedVm: BangumiSharedVm) {
    val vm = viewModel { DownloadListPageVm(sharedVm) }

    LaunchedEffect(sharedVm.targetDownloadId) {
        sharedVm.targetDownloadId.collect {
            vm.reloadData()
        }
    }

    val root by vm.displayData.collectAsStateWithLifecycle()
    if (root != null) {
        TreeView2(
            nodes = root!!.children,
            iconHint = PrimaryColors.Icon_Button,
            parentLayout = { node ->
                var showRemoveDlg by remember { mutableStateOf(false) }
                Row {
                    Text(
                        text = node.label,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = PrimaryColors.Text_Normal,
                    )

                    HSpacer()

                    ShapeIconButton(
                        resource = Res.drawable.ic_resume,
                        color = PrimaryColors.Icon_Button_Primary
                    ) {
                        vm.requestResumeAll(node)
                    }

                    ShapeIconButton(
                        resource = Res.drawable.ic_pause,
                        color = PrimaryColors.Icon_Button_Primary
                    ) {
                        vm.requestPauseAll(node)
                    }

                    ShapeIconButton(
                        resource = Res.drawable.ic_delete,
                        color = PrimaryColors.Icon_Button_Primary
                    ) {
                        showRemoveDlg = true
                    }
                }

                if (showRemoveDlg) {
                    TorrentRemoveDialog("删除所有任务") { removeSrc ->
                        if (removeSrc != null) {
                            vm.removeAll(node, removeSrc)
                        }
                        showRemoveDlg = false
                    }
                }
            }
        ) { node ->
            val torrent = node.data as DownloadItemData
            Column {
                val hoverInteraction = remember { MutableInteractionSource() }
                val isHover by hoverInteraction.collectIsHoveredAsState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryColors.Bangumi_Primary.copy(alpha = 0.05f))
                        .hoverable(hoverInteraction),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = MikanApi.HOST + torrent.record.thumbnail,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentDescription = "thumbnail",
                                contentScale = ContentScale.Crop,
                            )
                        }

                        HSpacer(16.dp)

                        TextStyleProvider(
                            fontSize = 12.sp,
                            color = PrimaryColors.Text_Normal,
                        ) {
                            val isDownloading by torrent.isDownloading
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = torrent.record.torrentName,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = torrent.record.torrentSrcName,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = PrimaryColors.Text_Secondary,
                                )
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val state by torrent.data
                                    val downloadState = TorrentStateType.of(state!!.state)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Canvas(modifier = Modifier.size(8.dp)) {
                                            drawCircle(
                                                color = when (downloadState) {
                                                    TorrentStateType.Downloading,
                                                    TorrentStateType.StalledDownloading,
                                                    TorrentStateType.StalledUploading,
                                                    TorrentStateType.Uploading -> PrimaryColors.Bangumi_Primary

                                                    TorrentStateType.Paused,
                                                    TorrentStateType.Completed,
                                                    TorrentStateType.Queued,
                                                    TorrentStateType.Checking -> PrimaryColors.GRAY.color()

                                                    TorrentStateType.Error -> PrimaryColors.RED.color()
                                                },
                                                radius = 4f
                                            )
                                        }
                                        Text(text = state!!.stateString, modifier = Modifier.width(40.dp))
                                    }

                                    HSpacer(2.dp)
                                    Text(text = state!!.speed, modifier = Modifier.width(80.dp))

                                    if (isDownloading) {
                                        Text(text = "剩余: ${state!!.eta}", modifier = Modifier.width(136.dp))
                                        Text(text = "资源: ${state!!.seeds}", modifier = Modifier.width(108.dp))
                                        Text(text = "${state!!.downloadedSize}/${state!!.totalSize}")
                                    }
                                    HSpacer()

                                    var showRemoveDlg by remember { mutableStateOf(false) }
                                    if (isHover) {
                                        Row {
                                            val downloadType = TorrentDownloadStateType.of(state!!.downloadState)
                                            ShapeIconButton(
                                                resource = Res.drawable.ic_play,
                                                color = when {
                                                    downloadType != TorrentDownloadStateType.Uploading -> PrimaryColors.Icon_Button_Disabled
                                                    torrent.record.played -> PrimaryColors.LIME
                                                    else -> PrimaryColors.Icon_Button_Primary
                                                }
                                            ) {
                                                if (downloadType == TorrentDownloadStateType.Uploading) {
                                                    vm.playTarget(torrent)
                                                }
                                            }

                                            ShapeIconButton(
                                                resource = if (downloadState == TorrentStateType.Paused || downloadState == TorrentStateType.Completed) Res.drawable.ic_resume else Res.drawable.ic_pause,
                                                color = PrimaryColors.Icon_Button_Primary
                                            ) {
                                                if (downloadState == TorrentStateType.Paused || downloadState == TorrentStateType.Completed) {
                                                    vm.requestResume(torrent.record.torrentInfoHash)
                                                } else {
                                                    vm.requestPause(torrent.record.torrentInfoHash)
                                                }
                                            }

                                            ShapeIconButton(
                                                resource = Res.drawable.ic_delete,
                                                color = PrimaryColors.Icon_Button_Primary
                                            ) {
                                                showRemoveDlg = true
                                            }

                                            ShapeIconButton(
                                                resource = Res.drawable.ic_export,
                                                color = PrimaryColors.Icon_Button_Primary
                                            ) {
                                                vm.exportTorrent(torrent)
                                            }

                                            ShapeIconButton(
                                                resource = Res.drawable.ic_open_dir,
                                                color = PrimaryColors.Icon_Button_Primary
                                            ) {
                                                vm.openTarget(torrent)
                                            }
                                        }
                                    }
                                    if (showRemoveDlg) {
                                        TorrentRemoveDialog("删除任务") { removeSrc ->
                                            if (removeSrc != null) {
                                                vm.removeTarget(torrent, removeSrc)
                                            }
                                            showRemoveDlg = false
                                        }
                                    }
                                }
                                if (isDownloading) {
                                    val state by torrent.data
                                    val progress by animateFloatAsState(state!!.progress.toFloat(), animationSpec = tween(800))
                                    LinearProgressIndicator(
                                        progress = progress,
                                        modifier = Modifier.fillMaxWidth().height(4.dp).padding(end = 4.dp),
                                        color = PrimaryColors.Bangumi_Primary,
                                        strokeCap = StrokeCap.Round,
                                    )
                                }
                            }
                        }
                    }
                }
                VSpacer(4.dp)
            }
        }
    }
}

@Composable
fun <T> TreeView2(
    modifier: Modifier = Modifier,
    nodes: List<TreeNode<T>>,
    iconHint: Color = Color.Unspecified,
    parentLayout: @Composable (node: TreeNode<T>) -> Unit,
    childLayout: @Composable (node: TreeNode<T>) -> Unit
) {
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val flatList by remember(nodes) { derivedStateOf { flattenTree(nodes) } }
    Box(modifier = modifier.fillMaxSize()) {
        Row {
            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                modifier = Modifier.fillMaxHeight().weight(1f)
            ) {
                itemsIndexed(flatList, key = { _, item -> item.node.id }) { _, (node, indent) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (node.children.isNotEmpty()) {
                            Icon(
                                painter = painterResource(
                                    if (node.isExpanded.value)
                                        Res.drawable.ic_arrow_down
                                    else
                                        Res.drawable.ic_arrow_right
                                ),
                                contentDescription = null,
                                tint = iconHint,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        node.isExpanded.value = !node.isExpanded.value
                                    }
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clickable(enabled = indent == 0) {
                                    node.isExpanded.value = !node.isExpanded.value
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (indent == 0) {
                                parentLayout(node)
                            } else {
                                childLayout(node)
                            }
                        }
                    }
                }
            }

            // ✅ 纵向滚动条
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(listState),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(8.dp)
            )
        }
    }
}