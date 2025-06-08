package com.daonvshu.shared.settings.bean

import kotlinx.serialization.Serializable

@Serializable
data class PokkitSetting(
    val general: General = General(),
)
