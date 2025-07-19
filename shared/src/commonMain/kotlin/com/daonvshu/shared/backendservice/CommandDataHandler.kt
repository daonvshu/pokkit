package com.daonvshu.shared.backendservice

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
        //response
        codec.registerType<TorrentContentFetchProgressUpdate>()
        codec.registerType<TorrentContentFetchResult>()
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
}