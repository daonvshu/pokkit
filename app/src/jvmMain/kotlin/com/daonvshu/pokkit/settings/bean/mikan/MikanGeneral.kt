@file:Suppress("PROVIDED_RUNTIME_TOO_LOW", "INLINE_CLASSES_NOT_SUPPORTED")
package com.daonvshu.pokkit.settings.bean.mikan

import kotlinx.serialization.Serializable

@Serializable
data class MikanGeneral(
    var lastUpdateTime: Long = 0,
)
