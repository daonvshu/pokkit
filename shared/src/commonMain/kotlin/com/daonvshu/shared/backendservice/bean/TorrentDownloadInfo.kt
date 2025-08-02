package com.daonvshu.shared.backendservice.bean

data class TorrentDownloadPath(
    val path: String,
    val ignored: Boolean
)

data class TorrentDownloadInfo(
    val content: String,
    val paths: List<TorrentDownloadPath>,
)