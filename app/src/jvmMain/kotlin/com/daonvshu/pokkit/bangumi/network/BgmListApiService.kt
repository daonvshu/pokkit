@file:Suppress("PROVIDED_RUNTIME_TOO_LOW", "INLINE_CLASSES_NOT_SUPPORTED")
package com.daonvshu.pokkit.bangumi.network

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

@Serializable
data class BgmListItemSite(
    val site: String,
    val id: String,
)

@Serializable
data class BgmListItem(
    val officialSite: String,
    val sites: List<BgmListItemSite>
)

@Serializable
data class BgmListItems(
    val items: List<BgmListItem>
)

@Serializable
data class SiteMetaPlatform(
    val title: String,
    val urlTemplate: String,
    val regions: List<String>? = null,
    val type: String
)

interface BgmListApiService {
    @GET("api/v1/bangumi/archive/{year}q{season}")
    suspend fun getBangumiList(@Path("year") year: Int, @Path("season") season: Int): BgmListItems

    @GET("api/v1/bangumi/site")
    suspend fun getSiteMeta(): Map<String, SiteMetaPlatform>
}