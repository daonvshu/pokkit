package com.daonvshu.shared.backendservice

import com.daonvshu.protocol.codec.CodecType
import com.daonvshu.protocol.codec.annotations.Type
import com.daonvshu.shared.backendservice.bean.TorrentContentInfo
import com.daonvshu.shared.backendservice.bean.TorrentDisplayInfo
import com.daonvshu.shared.backendservice.bean.TorrentDownloadInfo
import kotlinx.serialization.Serializable

@Serializable
@Type(id = 100, codec = CodecType.JSON)
data class TorrentContentFetchRequest(
    val requestId: Long,
    val torrentSrcNames: List<String>,
    val torrentUrls: List<String>
)

enum class SpecialIntCommand(val value: Int) {
    TORRENT_CONTENT_FETCH_CANCEL(101),
    TORRENT_STATUS_REFRESH_REQUEST(206),
    GLOBAL_SPEED_LIMIT_REQUEST(401),
    TRACKER_LIST_REQUEST(404),
}

@Serializable
@Type(id = 200, codec = CodecType.JSON)
data class TorrentContentFetchProgressUpdate(
    val requestId: Long,
    val finishedCount: Int,
    val totalCount: Int
)

@Serializable
@Type(id = 201, codec = CodecType.JSON)
data class TorrentContentFetchResult(
    val requestId: Long,
    val data: List<TorrentContentInfo>,
)

@Serializable
@Type(id = 202, codec = CodecType.JSON)
data class RequestOpenDir(
    val paths: List<String>
)

@Serializable
@Type(id = 203, codec = CodecType.JSON)
data class TorrentDownloadRequest(
    val savePath: String,
    val data: List<TorrentDownloadInfo>
)

@Serializable
@Type(id = 204, codec = CodecType.JSON)
data class TorrentPauseOrResumeRequest(
    val isPause: Boolean,
    val isAll: Boolean,
    val torrentHash: List<String>
)

@Serializable
@Type(id = 205, codec = CodecType.JSON)
data class TorrentRemoveRequest(
    val removeSrcFile: Boolean,
    val torrentHash: List<String>
)

@Serializable
@Type(id = 207, codec = CodecType.JSON)
data class TorrentContentFetch2Request(
    val requestId: Long,
    val type: Int, //0: torrent file 1: magnet url
    val target: String, //torrent file path or magnet url
)

@Serializable
@Type(id = 301, codec = CodecType.JSON)
data class TorrentStatusList(
    val status: List<TorrentDisplayInfo>
)

@Serializable
@Type(id = 302, codec = CodecType.JSON)
data class TorrentSpeedUpdated(
    val downloadSpeed: String,
    val uploadSpeed: String
)

@Serializable
@Type(id = 402, codec = CodecType.JSON)
data class GlobalSpeedLimitFeedback(
    val download: Int,
    val upload: Int
)

@Serializable
@Type(id = 403, codec = CodecType.JSON)
data class GlobalSpeedLimitUpdate(
    val download: Int,
    val upload: Int
)

@Serializable
@Type(id = 405, codec = CodecType.JSON)
data class TrackerListFeedback(
    val enabled: Boolean,
    val trackers: String,
)

@Serializable
@Type(id = 406, codec = CodecType.JSON)
data class TrackerListUpdateRequest(
    val enable: Boolean,
    val trackers: String,
)