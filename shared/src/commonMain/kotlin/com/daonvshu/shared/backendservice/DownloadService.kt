package com.daonvshu.shared.backendservice

import com.daonvshu.protocol.codec.CodecType
import com.daonvshu.protocol.codec.annotations.Type
import com.daonvshu.shared.settings.AppSettings

@Type(id = 1, codec = CodecType.JSON)
data class TorrentContentFetchRequest(
    val proxyAddress: String = AppSettings.settings.general.proxyAddress,
    val proxyPort: Int = AppSettings.settings.general.proxyPort,
    val torrentUrls: List<String>
)