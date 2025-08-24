package com.daonvshu.bangumi.network

import com.daonvshu.shared.settings.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Proxy

object BgmListApi {
    const val HOST = "https://bgmlist.com/"

    @Volatile
    var apiService: BgmListApiService = createApiService(AppSettings.getProxy())
        private set

    init {
        CoroutineScope(Dispatchers.IO).launch {
            AppSettings.proxyFlow.collect { proxy ->
                apiService = createApiService(proxy)
            }
        }
    }

    private fun createApiService(proxy: Proxy?): BgmListApiService {
        val client = OkHttpClient.Builder()
            .proxy(proxy)
            .build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(HOST)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BgmListApiService::class.java)
    }
}