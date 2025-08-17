package com.daonvshu.bangumi.dialog

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

data class TorrentAddTaskData(
    val torrentFile: String,
    val linkUrl: String,
    val addByFile: Boolean
)

class TorrentAddTaskDialogVm: ViewModel() {
    // 选择的种子文件
    val torrentFile = MutableStateFlow("")
    // 磁力链接地址
    val linkUrl = MutableStateFlow("")
    // 添加任务的方式
    val addByFile = MutableStateFlow(true)
}