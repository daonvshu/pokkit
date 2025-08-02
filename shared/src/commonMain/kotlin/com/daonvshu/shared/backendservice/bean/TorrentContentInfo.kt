package com.daonvshu.shared.backendservice.bean

data class TorrentInfoPathData(
    val path: String,
    val size: Long,
)

data class TorrentContentInfo(
    val srcName: String,
    val linkUrl: String,
    val linkName: String,
    val torrentInfoHash: String,
    val torrentContent: String,
    val invalid: Boolean,
    val invalidType: Int,
    val errorString: String,
    val filePaths: List<TorrentInfoPathData>,
)
