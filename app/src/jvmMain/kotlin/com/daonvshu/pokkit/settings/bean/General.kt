@file:Suppress("PROVIDED_RUNTIME_TOO_LOW", "INLINE_CLASSES_NOT_SUPPORTED")
package com.daonvshu.pokkit.settings.bean

import com.daonvshu.pokkit.settings.bean.mikan.MikanGeneral
import kotlinx.serialization.Serializable

@Serializable
data class General(
    var proxyAddress: String = "127.0.0.1",
    var proxyPort: Int = 7890,
    var proxyEnabled: Boolean = true,

    var bangumiLastSavePath: String = "",
    var autoCreateDir: Boolean = true,
    var torrentDeleteWithSrcFile: Boolean = true,
    var trackReset: Boolean = true,

    val mikan: MikanGeneral = MikanGeneral(),
)