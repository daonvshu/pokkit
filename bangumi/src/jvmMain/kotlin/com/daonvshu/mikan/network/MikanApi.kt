package com.daonvshu.mikan.network

import com.daonvshu.shared.settings.AppSettings
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetSocketAddress
import java.net.Proxy

object MikanApi {

    const val HOST = "https://mikanani.me/"

    val apiService: MikanApiService by lazy {
        val client = OkHttpClient.Builder()
            .proxy(getProxy())
            .build()

        Retrofit.Builder()
            .client(client)
            .baseUrl(HOST)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MikanApiService::class.java)
    }

    private fun getProxy(): Proxy? {
        //get proxy from config
        val mikanSetting = AppSettings.settings.general.mikan
        if (!mikanSetting.proxyEnabled) {
            return null
        }
        if (mikanSetting.proxyAddress.isEmpty() || mikanSetting.proxyPort <= 0) {
            return null
        }
        return Proxy(Proxy.Type.HTTP, InetSocketAddress(mikanSetting.proxyAddress, mikanSetting.proxyPort))
    }
}