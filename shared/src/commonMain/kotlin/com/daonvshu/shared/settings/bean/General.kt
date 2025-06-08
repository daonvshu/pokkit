package com.daonvshu.shared.settings.bean

import com.daonvshu.shared.settings.bean.mikan.MikanGeneral
import kotlinx.serialization.Serializable

@Serializable
data class General(
    val mikan: MikanGeneral = MikanGeneral(),
)