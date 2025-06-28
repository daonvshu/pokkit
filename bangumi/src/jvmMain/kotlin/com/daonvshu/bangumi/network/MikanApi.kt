package com.daonvshu.bangumi.network

import com.daonvshu.shared.settings.AppSettings
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

object MikanApi {

    const val HOST = "https://mikanani.me/"

    val apiService: MikanApiService by lazy {
        val client = OkHttpClient.Builder()
            .proxy(AppSettings.getProxy())
            .build()

        Retrofit.Builder()
            .client(client)
            .baseUrl(HOST)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MikanApiService::class.java)
    }
}