package com.daonvshu.mikan.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface MikanApiService {
    @GET("Home/BangumiCoverFlowByDayOfWeek")
    suspend fun getDataBySeason(@Query("year") year: Int, @Query("seasonStr") season: String): ResponseBody
}