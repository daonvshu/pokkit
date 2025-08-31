package com.daonvshu.pokkit.backendservice

import kotlinx.coroutines.flow.MutableStateFlow

object BackendDataObserver {

    val backendServiceConnectError = MutableStateFlow("")

    val torrentContentFetchProgressUpdate = MutableStateFlow<TorrentContentFetchProgressUpdate?>(null)

    val torrentContentFetchResult = MutableStateFlow<TorrentContentFetchResult?>(null)

    val torrentStatusList = MutableStateFlow<TorrentStatusList?>(null)

    val torrentSpeedUpdated = MutableStateFlow<TorrentSpeedUpdated?>(null)

    val globalSpeedLimit = MutableStateFlow<GlobalSpeedLimitFeedback?>(null)

    val trackerListSetting = MutableStateFlow<TrackerListFeedback?>(null)
}