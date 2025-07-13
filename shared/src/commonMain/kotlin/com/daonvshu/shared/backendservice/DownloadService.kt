package com.daonvshu.shared.backendservice

import com.daonvshu.protocol.codec.CodecType
import com.daonvshu.protocol.codec.annotations.Type
import com.daonvshu.shared.backendservice.bean.TorrentContentInfo

@Type(id = 100, codec = CodecType.JSON)
data class TorrentContentFetchRequest(
    val requestId: Long,
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