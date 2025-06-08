package com.daonvshu.shared.settings.bean.mikan

import kotlinx.serialization.Serializable

@Serializable
data class MikanGeneral(
    var proxyAddress: String = "127.0.0.1",
    var proxyPort: Int = 7890,
    var proxyEnabled: Boolean = true,

    var lastUpdateTime: Long = 0,
)
