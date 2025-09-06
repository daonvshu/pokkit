package com.daonvshu.pokkit.bangumi.network

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

@Serializable
data class BgmTvItem(
    val summary: String,
    val eps: Int,
)

interface BgmTvApiService {
    @GET("subject/{id}?responseGroup=small")
    suspend fun getBangumiDetail(@Path("id") bangumiId: Int): BgmTvItem
}