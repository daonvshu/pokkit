package com.daonvshu.shared.settings

import com.daonvshu.shared.settings.bean.PokkitSetting
import kotlinx.serialization.json.Json
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy

object AppSettings {
    private val configFile = File(".data/config.json").apply {
        parentFile.let {
            if (!it.exists()) {
                it.mkdirs()
            }
        }
    }
    private val json = Json { prettyPrint = true }

    val settings: PokkitSetting = load()

    private fun load(): PokkitSetting {
        return if (configFile.exists()) {
            configFile.readText().let { json.decodeFromString(it) }
        } else {
            PokkitSetting()
        }
    }

    fun save() {
        configFile.writeText(json.encodeToString(settings))
    }

    fun getProxy(): Proxy? {
        //get proxy from config
        val mikanSetting = settings.general
        if (!mikanSetting.proxyEnabled) {
            return null
        }
        if (mikanSetting.proxyAddress.isEmpty() || mikanSetting.proxyPort <= 0) {
            return null
        }
        return Proxy(Proxy.Type.HTTP, InetSocketAddress(mikanSetting.proxyAddress, mikanSetting.proxyPort))
    }
}