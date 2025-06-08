package com.daonvshu.shared.settings

import com.daonvshu.shared.settings.bean.PokkitSetting
import kotlinx.serialization.json.Json
import java.io.File

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
}