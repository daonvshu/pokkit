package com.daonvshu.bangumi.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.daonvshu.shared.components.BaseDialog
import com.daonvshu.shared.components.NormalCheckbox
import com.daonvshu.shared.components.StandardButton
import com.daonvshu.shared.settings.AppSettings
import com.daonvshu.shared.utils.PrimaryColors

@Composable
fun TorrentRemoveDialog(
    title: String,
    callback: (Boolean?) -> Unit
) {
    BaseDialog(
        title = title,
        onDismissRequest = {
            callback(null)
        },
        buttons = {
            StandardButton(
                text = "确定",
                color = PrimaryColors.Button_Normal_Primary,
            ) {
                callback(AppSettings.settings.general.torrentDeleteWithSrcFile)
            }
        }
    ) {
        var removeSrcFile by remember { mutableStateOf(AppSettings.settings.general.torrentDeleteWithSrcFile) }
        NormalCheckbox(
            checked = removeSrcFile,
            onCheckedChange = {
                removeSrcFile = it
                AppSettings.settings.general.torrentDeleteWithSrcFile = it
                AppSettings.save()
            },
            label = "删除下载的源文件"
        )
    }
}