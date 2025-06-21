package com.daonvshu.bangumi.network

import com.daonvshu.shared.settings.AppSettings
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BgmListApi {
    const val HOST = "https://bgmlist.com/"

    val apiService: BgmListApiService by lazy {
        val client = OkHttpClient.Builder()
            .proxy(AppSettings.getProxy())
            .build()

        Retrofit.Builder()
            .client(client)
            .baseUrl(HOST)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BgmListApiService::class.java)
    }
}