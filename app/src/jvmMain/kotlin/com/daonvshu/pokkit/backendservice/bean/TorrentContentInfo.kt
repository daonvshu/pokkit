@file:Suppress("PROVIDED_RUNTIME_TOO_LOW", "INLINE_CLASSES_NOT_SUPPORTED")
package com.daonvshu.pokkit.backendservice.bean

import kotlinx.serialization.Serializable

@Serializable
data class TorrentInfoPathData(
    val path: String,
    val size: Long,
)

@Serializable
data class TorrentContentInfo(
    val srcName: String,
    val linkUrl: String,
    val linkName: String,
    val torrentInfoHash: String,
    val torrentContent: String,
    val invalid: Boolean,
    val invalidType: Int,
    val errorString: String,
    val filePaths: List<TorrentInfoPathData>,
)
