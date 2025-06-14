package com.daonvshu.shared.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import okhttp3.ResponseBody
import java.io.File
import java.security.MessageDigest
import javax.imageio.ImageIO

object ImageCacheLoader {
    /**
     * 从本地缓存或网络中获取图片
     * @param url 图片地址
     * @return 图片
     * @throws okio.IOException 网络请求错误
     */
    suspend fun getImage(url: String, saveDir: String, serviceProvider: suspend (String) -> ResponseBody): ImageBitmap? {
        val file = File(".cache/$saveDir/${md5(url)}.jpg").apply {
            if (!parentFile.exists()) {
                parentFile.mkdirs()
            }
        }
        if (file.exists()) {
            println("load image $url from cache...")
            return file.inputStream().use { ImageIO.read(it)?.toComposeImageBitmap() }
        }
        println("request image $url...")
        val response = serviceProvider(url)
        file.outputStream().use {
            it.write(response.bytes())
            return file.inputStream().use { stream -> ImageIO.read(stream)?.toComposeImageBitmap() }
        }
    }

    private fun md5(str: String): String {
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(str.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}