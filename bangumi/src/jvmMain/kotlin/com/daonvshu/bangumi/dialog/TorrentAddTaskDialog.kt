package com.daonvshu.bangumi.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daonvshu.shared.components.BaseDialog
import com.daonvshu.shared.components.CustomTextArea
import com.daonvshu.shared.components.HSpacer
import com.daonvshu.shared.components.InputColors
import com.daonvshu.shared.components.StandardButton
import com.daonvshu.shared.components.VSpacer
import com.daonvshu.shared.generated.resources.Res
import com.daonvshu.shared.generated.resources.ic_modify
import com.daonvshu.shared.utils.PrimaryColors
import io.github.mataku.middleellipsistext.MiddleEllipsisText
import org.jetbrains.compose.resources.painterResource
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TorrentAddTaskDialog(
    onDismissRequest: (Boolean, TorrentAddTaskData?) -> Unit
) {
    val vm = remember { TorrentAddTaskDialogVm() }
    BaseDialog(
        title = "添加任务",
        onDismissRequest = {
            onDismissRequest(false, null)
        },
        buttons = {
            StandardButton(
                text = "确定",
                color = PrimaryColors.Button_Normal_Primary,
            ) {
                onDismissRequest(true, TorrentAddTaskData(
                    vm.torrentFile.value,
                    vm.linkUrl.value,
                    vm.addByFile.value
                ))
            }
        }
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
            val addByFile by vm.addByFile.collectAsStateWithLifecycle()
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    modifier = Modifier.size(20.dp),
                    selected = addByFile,
                    onClick = {
                        vm.addByFile.value = true
                    }
                )
                HSpacer(8.dp)
                Text(text = "Torrent文件")
            }

            VSpacer(8.dp)
            DialogAddByFileRow(vm)

            VSpacer(16.dp)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    modifier = Modifier.size(20.dp),
                    selected = !addByFile,
                    onClick = {
                        vm.addByFile.value = false
                    }
                )
                HSpacer(8.dp)
                Text(text = "URL链接")
            }

            VSpacer(8.dp)
            DialogAddByUrl(vm)
        }
    }
}

@Composable
fun DialogAddByFileRow(vm: TorrentAddTaskDialogVm) {
    val torrentFile by vm.torrentFile.collectAsStateWithLifecycle()
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("选择文件：")
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(PrimaryColors.Bangumi_Body),
        ) {
            MiddleEllipsisText(
                torrentFile,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .fillMaxWidth()
            )
        }

        IconButton(
            onClick = {
                val chooser = JFileChooser()
                chooser.fileSelectionMode = JFileChooser.FILES_ONLY
                chooser.isAcceptAllFileFilterUsed = false
                chooser.fileFilter = FileNameExtensionFilter("种子文件（*.torrent）", "torrent")
                val result = chooser.showOpenDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) {
                    vm.torrentFile.value = chooser.selectedFile.absolutePath
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
}

@Composable
fun DialogAddByUrl(vm: TorrentAddTaskDialogVm) {
    val url by vm.linkUrl.collectAsStateWithLifecycle()
    CustomTextArea(
        modifier = Modifier.fillMaxWidth().height(148.dp),
        value = url,
        onValueChange = {
            vm.linkUrl.value = it
        },
        hint = "输入磁力链接",
        colors = InputColors().copy(
            borderInFocus = PrimaryColors.Bangumi_Primary
        )
    )
}