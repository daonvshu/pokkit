package com.daonvshu.bangumi.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daonvshu.bangumi.pages.MikanBangumiDetailPageVm
import com.daonvshu.shared.components.BaseDialog
import com.daonvshu.shared.components.NormalCheckbox
import com.daonvshu.shared.components.StandardButton
import com.daonvshu.shared.components.TreeView
import com.daonvshu.shared.database.schema.MikanDataRecord
import com.daonvshu.shared.generated.resources.Res
import com.daonvshu.shared.generated.resources.ic_modify
import com.daonvshu.shared.settings.AppSettings
import com.daonvshu.shared.utils.PrimaryColors
import com.daonvshu.shared.utils.friendlySize
import io.github.mataku.middleellipsistext.MiddleEllipsisText
import org.jetbrains.compose.resources.painterResource
import java.io.File
import javax.swing.JFileChooser

@Composable
fun TorrentDownloadDialog(
    data: MikanDataRecord? = null,
    fansub: String? = null,
    torrentRequestId: Long,
    onDismissRequest: () -> Unit,
) {
    val vm = remember { TorrentDownloadDialogVm(data, fansub, torrentRequestId) }

    BaseDialog(
        title = "下载",
        onDismissRequest = onDismissRequest,
        buttons = {
            val downloadEnabled by vm.downloadEnabled.collectAsStateWithLifecycle()
            StandardButton(
                text = "下载",
                color = PrimaryColors.Button_Normal_Primary,
                enabled = downloadEnabled,
            ) {
                vm.startDownloadSelectedTorrents {
                    onDismissRequest()
                }
            }
        }
    ) {
        val downloadSizeAll by vm.downloadSizeAll.collectAsStateWithLifecycle()
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("总大小：")
            Text(downloadSizeAll.friendlySize())
        }

        val saveDir by vm.saveDir.collectAsStateWithLifecycle()
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
                MiddleEllipsisText(
                    saveDir,
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
                    chooser.currentDirectory = File(saveDir)
                    val result = chooser.showOpenDialog(null)
                    if (result == JFileChooser.APPROVE_OPTION) {
                        vm.updateDownloadDir(chooser.selectedFile.absolutePath)
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

        if (data != null) {
            val autoCreateDir by vm.autoCreateDir.collectAsStateWithLifecycle()
            NormalCheckbox(
                checked = autoCreateDir,
                onCheckedChange = {
                    vm.autoCreateDir.value = it
                    AppSettings.settings.general.autoCreateDir = it
                    AppSettings.save()
                },
                label = "以番剧名自动创建目录"
            )

            val onlyDownloadTorrent by vm.onlyDownloadTorrent.collectAsStateWithLifecycle()
            NormalCheckbox(
                checked = onlyDownloadTorrent,
                onCheckedChange = {
                    vm.onlyDownloadTorrent.value = it
                    vm.reloadLinksCheckable()
                },
                label = "仅下载种子文件"
            )
        }

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
    }
}