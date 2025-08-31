package com.daonvshu.pokkit.backendservice

import com.daonvshu.protocol.codec.ProtocolCodecEngine
import com.daonvshu.protocol.codec.annotations.Subscribe

class CommandDataHandler {
    val codec = ProtocolCodecEngine()

    init {
        codec.frameDeclare("H(FAFE)S4CV(CRC16)E(FE)")
        codec.setVerifyFlags("SC")
        codec.registerCallback(this)

        //request
        codec.registerType<ProxyInfoSync>()
        codec.registerType<IdentifyAuthRequest>()
        codec.registerType<TorrentContentFetchRequest>()
        codec.registerType<TorrentDownloadRequest>()
        codec.registerType<TorrentPauseOrResumeRequest>()
        codec.registerType<TorrentRemoveRequest>()
        codec.registerType<TorrentContentFetch2Request>()
        codec.registerType<GlobalSpeedLimitUpdate>()
        codec.registerType<TrackerListUpdateRequest>()
        //response
        codec.registerType<TorrentContentFetchProgressUpdate>()
        codec.registerType<TorrentContentFetchResult>()
        codec.registerType<RequestOpenDir>()
        codec.registerType<TorrentStatusList>()
        codec.registerType<TorrentSpeedUpdated>()
        codec.registerType<GlobalSpeedLimitFeedback>()
        codec.registerType<TrackerListFeedback>()
    }

    fun handle(data: ByteArray) {
        codec.appendBuffer(data)
    }

    @Subscribe
    fun onTorrentContentFetchProgressUpdateReceived(progress: TorrentContentFetchProgressUpdate) {
        println("progress update: ${progress.finishedCount}/${progress.totalCount}")
        BackendDataObserver.torrentContentFetchProgressUpdate.value = progress
    }

    @Subscribe
    fun onTorrentContentFetchResultReceived(result: TorrentContentFetchResult) {
        BackendDataObserver.torrentContentFetchResult.value = result
    }

    @Subscribe
    fun onTorrentStatusListReceived(result: TorrentStatusList) {
        BackendDataObserver.torrentStatusList.value = result
    }

    @Subscribe
    fun onTorrentSpeedUpdatedReceived(result: TorrentSpeedUpdated) {
        BackendDataObserver.torrentSpeedUpdated.value = result
    }

    @Subscribe
    fun onGlobalSpeedLimitFeedbackReceived(result: GlobalSpeedLimitFeedback) {
        BackendDataObserver.globalSpeedLimit.value = result
    }

    @Subscribe
    fun onTrackerListSettingReceived(result: TrackerListFeedback) {
        BackendDataObserver.trackerListSetting.value = result
    }
}