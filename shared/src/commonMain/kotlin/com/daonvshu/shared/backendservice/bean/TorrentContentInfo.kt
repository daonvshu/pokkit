package com.daonvshu.shared.backendservice.bean

data class TorrentInfoPathData(
    val path: String,
    val size: Long,
)

data class TorrentInfoData(
    val filePaths: List<TorrentInfoPathData>,
    val name: String,
    val invalid: Boolean,
    val invalidType: Int,
    val errorString: String,
)

data class TorrentContentInfo(
    val linkUrl: String,
    val linkName: String,
    val torrentContent: String,
    val linkData: TorrentInfoData,
)
