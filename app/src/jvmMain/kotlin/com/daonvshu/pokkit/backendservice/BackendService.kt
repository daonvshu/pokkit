@file:Suppress("PROVIDED_RUNTIME_TOO_LOW", "INLINE_CLASSES_NOT_SUPPORTED")
package com.daonvshu.pokkit.backendservice

import com.daonvshu.protocol.codec.CodecType
import com.daonvshu.protocol.codec.annotations.Type
import com.daonvshu.pokkit.settings.AppSettings
import com.daonvshu.shared.utils.LogCollector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.io.Closeable
import java.io.RandomAccessFile
import kotlin.String

@Serializable
@Type(id = 0, codec = CodecType.JSON)
data class IdentifyAuthRequest(
    val role: String = "Pokkit/ReadChannel"
)

@Serializable
@Type(id = 1, codec = CodecType.JSON)
data class ProxyInfoSync(
    val enabled: Boolean,
    val proxyAddress: String,
    val proxyPort: Int
)

inline fun<reified T : Any> T.sendToBackend() {
    BackendService.sendRaw(BackendService.dataHandler.codec.encode<T>(this))
}

fun SpecialIntCommand.sendToBackend() {
    BackendService.sendRaw(BackendService.dataHandler.codec.encodeId(this.value))
}

object BackendService : Closeable {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var readPipe: RandomAccessFile? = null
    private var writePipe: RandomAccessFile? = null
    private var readJob: Job? = null

    private const val PIPE_NAME = "pokkit_backend_pipe"

    val dataHandler = CommandDataHandler()

    fun sendRaw(data: ByteArray) {
        coroutineScope.launch {
            try {
                writePipe?.write(data)
            } catch (e: Exception) {
                e.printStackTrace()
                println("write data fail!")
            }
        }
    }

    fun updateProxyInfo() {
        ProxyInfoSync(
            enabled = AppSettings.settings.general.proxyEnabled,
            proxyAddress = AppSettings.settings.general.proxyAddress,
            proxyPort = AppSettings.settings.general.proxyPort,
        ).sendToBackend()
    }

    fun tryCreatePipeIfNeeded(): Boolean {
        if (readPipe != null && writePipe != null) return true
        try {
            println("try create connection to backend service...")
            readPipe = RandomAccessFile("""\\.\pipe\$PIPE_NAME""", "rw")
            writePipe = RandomAccessFile("""\\.\pipe\$PIPE_NAME""", "rw")

            readPipe?.write(dataHandler.codec.encode(IdentifyAuthRequest()))
            readJob?.cancel()
            readJob = coroutineScope.launch {
                try {
                    val buffer = ByteArray(256)
                    while (isActive) {
                        val bytesRead = readPipe?.read(buffer) ?: -1
                        if (bytesRead == -1) {
                            LogCollector.addLog("backend service disconnected!")
                            break
                        }
                        dataHandler.handle(buffer.copyOfRange(0, bytesRead))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("read exception!")
                }
                readPipe = null
            }
            if (readPipe != null) {
                updateProxyInfo()
                enableTrackOnFirstLoad()
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("backend service not found!")
            BackendDataObserver.backendServiceConnectError.value = "本地服务无法连接!"
        }
        return false
    }

    private fun enableTrackOnFirstLoad() {
        if (AppSettings.settings.general.trackReset) {
            //send tracks to backend
            val trackers = """
                udp://tracker.opentrackr.org:1337/announce
                udp://open.demonii.com:1337/announce
                udp://open.stealth.si:80/announce
                udp://explodie.org:6969/announce
                udp://exodus.desync.com:6969/announce
                udp://tracker.srv00.com:6969/announce
                udp://tracker.ololosh.space:6969/announce
                udp://isk.richardsw.club:6969/announce
                udp://hificode.in:6969/announce
                udp://glotorrents.pw:6969/announce
                http://share.hkg-fansub.info:80/announce.php
                udp://ttk2.nbaonlineservice.com:6969/announce
                udp://tracker.zupix.online:6969/announce
                udp://tracker.valete.tf:9999/announce
                udp://tracker.tryhackx.org:6969/announce
                udp://tracker.torrust-demo.com:6969/announce
                udp://tracker.therarbg.to:6969/announce
                udp://tracker.theoks.net:6969/announce
                udp://tracker.skillindia.site:6969/announce
                udp://tracker.plx.im:6969/announce
                http://open.acgtracker.com:1096/announce
                udp://tracker4.itzmx.com:2710/announce
                udp://tracker3.itzmx.com:6961/announce
                udp://tracker2.itzmx.com:6961/announce
                udp://tracker1.itzmx.com:8080/announce
                http://tracker4.itzmx.com:2710/announce
                http://tracker3.itzmx.com:6961/announce
                http://tracker2.itzmx.com:6961/announce
                http://tracker1.itzmx.com:8080/announce
                udp://tracker.publicbt.com:80/announce
                udp://tracker.torrent.eu.org:451/announc
            """.trimIndent()

            TrackerListUpdateRequest(
                enable = true,
                trackers = trackers
            ).sendToBackend()

            AppSettings.settings.general.trackReset = false
            AppSettings.save()
        }
    }

    override fun close() {
        try {
            readPipe?.close()
        } catch (_: Exception) {
        }
        try {
            writePipe?.close()
        } catch (_: Exception) {
        }
        readPipe = null
        writePipe = null
        readJob?.cancel()
    }
}