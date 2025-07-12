package com.daonvshu.shared.backendservice

import com.daonvshu.protocol.codec.ProtocolCodecEngine

class CommandDataHandler {
    val codec = ProtocolCodecEngine()

    init {
        codec.frameDeclare("H(FAFE)S2CV(CRC16)E(FE)")
        codec.setVerifyFlags("SC")
        codec.registerCallback(this)

        codec.registerType<IdentifyAuthRequest>()
        codec.registerType<TorrentContentFetchRequest>()
    }

    fun handle(data: ByteArray) {
        codec.appendBuffer(data)
        println("read buffer: ${data.toString(Charsets.UTF_8)}")
    }

}