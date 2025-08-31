@file:Suppress("PROVIDED_RUNTIME_TOO_LOW", "INLINE_CLASSES_NOT_SUPPORTED")
package com.daonvshu.pokkit.settings.bean

import kotlinx.serialization.Serializable

@Serializable
data class PokkitSetting(
    val general: General = General(),
)
