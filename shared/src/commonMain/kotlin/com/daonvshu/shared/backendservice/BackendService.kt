package com.daonvshu.shared.backendservice

import com.daonvshu.protocol.codec.CodecType
import com.daonvshu.protocol.codec.annotations.Type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.Closeable
import java.io.FileNotFoundException
import java.io.RandomAccessFile

@Type(id = 0, codec = CodecType.JSON)
data class IdentifyAuthRequest(
    val role: String = "Pokkit/ReadChannel"
)

inline fun<reified T : Any> T.sendToBackend() {
    BackendService.sendRaw(BackendService.dataHandler.codec.encode<T>(this))
}

object BackendService : Closeable {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var readPipe: RandomAccessFile? = null
    private var writePipe: RandomAccessFile? = null
    private var readJob: Job? = null

    private const val PIPE_NAME = "pokkit_backend_pipe"

    val dataHandler = CommandDataHandler()

    fun sendRaw(data: ByteArray) {
        tryCreatePipeIfNeeded()
        coroutineScope.launch {
            try {
                writePipe?.write(data)
            } catch (e: Exception) {
                e.printStackTrace()
                println("write data fail!")
            }
        }
    }

    private fun tryCreatePipeIfNeeded() {
        if (readPipe != null && writePipe != null) return
        try {
            readPipe = RandomAccessFile("""\\.\pipe\$PIPE_NAME""", "rw")
            writePipe = RandomAccessFile("""\\.\pipe\$PIPE_NAME""", "rw")

            readJob?.cancel()
            readJob = coroutineScope.launch {
                try {
                    readPipe?.write(dataHandler.codec.encode(IdentifyAuthRequest()))
                    val buffer = ByteArray(256)
                    while (isActive) {
                        val bytesRead = readPipe?.read(buffer) ?: -1
                        if (bytesRead == -1) {
                            println("pipe exit!")
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
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            throw BackendServiceException("服务连接失败！")
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