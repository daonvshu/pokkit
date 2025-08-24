package com.daonvshu.bangumi.network

import com.daonvshu.shared.settings.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Proxy

object BgmTvApi {
    const val HOST = "https://api.bgm.tv/"

    @Volatile
    var apiService: BgmTvApiService = createApiService(AppSettings.getProxy())
        private set

    init {
        CoroutineScope(Dispatchers.IO).launch {
            AppSettings.proxyFlow.collect { proxy ->
                apiService = createApiService(proxy)
            }
        }
    }

    private fun createApiService(proxy: Proxy?): BgmTvApiService {
        val client = OkHttpClient.Builder()
            .proxy(proxy)
            .build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(HOST)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BgmTvApiService::class.java)
    }
}