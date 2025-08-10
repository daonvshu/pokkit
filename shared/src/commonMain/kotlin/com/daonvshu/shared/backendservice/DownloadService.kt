package com.daonvshu.shared.backendservice

import com.daonvshu.protocol.codec.CodecType
import com.daonvshu.protocol.codec.annotations.Type
import com.daonvshu.shared.backendservice.bean.TorrentContentInfo
import com.daonvshu.shared.backendservice.bean.TorrentDisplayInfo
import com.daonvshu.shared.backendservice.bean.TorrentDownloadInfo

@Type(id = 100, codec = CodecType.JSON)
data class TorrentContentFetchRequest(
    val requestId: Long,
    val torrentSrcNames: List<String>,
    val torrentUrls: List<String>
)

enum class SpecialIntCommand(val value: Int) {
    TORRENT_CONTENT_FETCH_CANCEL(101),
}

@Type(id = 200, codec = CodecType.JSON)
data class TorrentContentFetchProgressUpdate(
    val requestId: Long,
    val finishedCount: Int,
    val totalCount: Int
)

@Type(id = 201, codec = CodecType.JSON)
data class TorrentContentFetchResult(
    val requestId: Long,
    val data: List<TorrentContentInfo>,
)

@Type(id = 202, codec = CodecType.JSON)
data class RequestOpenDir(
    val paths: List<String>
)

@Type(id = 203, codec = CodecType.JSON)
data class TorrentDownloadRequest(
    val savePath: String,
    val data: List<TorrentDownloadInfo>
)

@Type(id = 204, codec = CodecType.JSON)
data class TorrentStatusList(
    val status: List<TorrentDisplayInfo>
)

@Type(id = 205, codec = CodecType.JSON)
data class TorrentPauseOrResumeRequest(
    val isPause: Boolean,
    val isAll: Boolean,
    val torrentHash: List<String>
)