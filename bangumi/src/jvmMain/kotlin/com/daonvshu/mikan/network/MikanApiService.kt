package com.daonvshu.mikan.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface MikanApiService {
    @GET("Home/BangumiCoverFlowByDayOfWeek")
    suspend fun getDataBySeason(@Query("year") year: Int, @Query("seasonStr") season: String): ResponseBody

    @Streaming
    @GET
    suspend fun getImage(@Url url: String): ResponseBody

    @GET("Home/Bangumi/{id}")
    suspend fun getDetailPage(@Path("id") mikanId: Int): String
}