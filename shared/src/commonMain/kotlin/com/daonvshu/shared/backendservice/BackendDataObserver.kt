package com.daonvshu.shared.backendservice

import kotlinx.coroutines.flow.MutableStateFlow

object BackendDataObserver {

    val backendServiceConnectError = MutableStateFlow("")

    val torrentContentFetchProgressUpdate = MutableStateFlow<TorrentContentFetchProgressUpdate?>(null)

    val torrentContentFetchResult = MutableStateFlow<TorrentContentFetchResult?>(null)

    val torrentStatusList = MutableStateFlow<TorrentStatusList?>(null)

    val torrentSpeedUpdated = MutableStateFlow<TorrentSpeedUpdated?>(null)
}