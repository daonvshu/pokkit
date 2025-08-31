package com.daonvshu.bangumi.network

import com.daonvshu.shared.settings.AppSettings
import com.daonvshu.shared.utils.LogCollector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.net.Proxy

object MikanApi {

    const val HOST = "https://mikanani.me/"

    @Volatile
    var apiService: MikanApiService = createApiService(AppSettings.getProxy())
        private set

    init {
        CoroutineScope(Dispatchers.IO).launch {
            AppSettings.proxyFlow.collect { proxy ->
                apiService = createApiService(proxy)
                LogCollector.addLog("mikan api proxy reload: $proxy")
            }
        }
    }

    private fun createApiService(proxy: Proxy?): MikanApiService {
        val client = OkHttpClient.Builder()
            .proxy(proxy)
            .build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(HOST)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .addConverterFactory(
                Json.asConverterFactory("application/json; charset=UTF8".toMediaType())
            )
            .build()
            .create(MikanApiService::class.java)
    }
}