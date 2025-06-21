package com.daonvshu.bangumi.network

import retrofit2.http.GET
import retrofit2.http.Path

data class BgmTvItem(
    val summary: String,
    val eps: Int,
)

interface BgmTvApiService {
    @GET("subject/{id}?responseGroup=small")
    suspend fun getBangumiDetail(@Path("id") bangumiId: Int): BgmTvItem
}