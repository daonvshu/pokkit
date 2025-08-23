package com.daonvshu.bangumi.pages

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daonvshu.bangumi.dialog.TorrentAddTaskDialog
import com.daonvshu.bangumi.dialog.TorrentDownloadDialog
import com.daonvshu.bangumi.dialog.TorrentRemoveDialog
import com.daonvshu.shared.backendservice.bean.TorrentStateType
import com.daonvshu.shared.components.HSpacer
import com.daonvshu.shared.components.ShapeIconButton
import com.daonvshu.shared.components.VSpacer
import com.daonvshu.shared.generated.resources.*
import com.daonvshu.shared.styles.TextStyleProvider
import com.daonvshu.shared.utils.PrimaryColors

@Composable
fun DownloadOtherPage() {
    val vm = remember { DownloadOtherPageVm() }

    LaunchedEffect(vm) {
        vm.reloadData()
    }

    Scaffold(
        floatingActionButton = {
            ShapeIconButton(Res.drawable.ic_paw, color = PrimaryColors.Icon_Button_Primary) {
                vm.showAddDlg.value = true
            }
        },
        backgroundColor = Color.Transparent
    ) {
        DownloadingList(vm)
    }

    AddTaskDialog(vm)
    DownloadDialog(vm)
}

@Composable
fun AddTaskDialog(vm: DownloadOtherPageVm) {
    val showDlg by vm.showAddDlg.collectAsStateWithLifecycle()
    if (showDlg) {
        TorrentAddTaskDialog { confirm, data ->
            vm.showAddDlg.value = false
            if (confirm) {
                vm.downloadBegin(data!!)
            }
        }
    }
}

@Composable
fun DownloadDialog(vm: DownloadOtherPageVm) {
    val show by vm.showDownloadDialog.collectAsStateWithLifecycle()
    if (show) {
        TorrentDownloadDialog(null, null, vm.currentTorrentRequestId) {
            vm.showDownloadDialog.value = false
            vm.reloadData()
        }
    }
}

@Composable
fun DownloadingList(vm: DownloadOtherPageVm) {
    val displayData by vm.displayData.collectAsStateWithLifecycle()
    displayData?.let {
        LazyColumn {
            items(it) { torrent ->
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
                            HSpacer(4.dp)
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
                                        LinearProgressIndicator(
                                            progress = state!!.progress.toFloat(),
                                            modifier = Modifier.fillMaxWidth().height(4.dp).padding(horizontal = 4.dp),
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
}