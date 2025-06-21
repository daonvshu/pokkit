package com.daonvshu.bangumi.network

import retrofit2.http.GET
import retrofit2.http.Path

data class BgmListItemSite(
    val site: String,
    val id: String,
)

data class BgmListItem(
    val officialSite: String,
    val sites: List<BgmListItemSite>
)

data class BgmListItems(
    val items: List<BgmListItem>
)

data class SiteMetaPlatform(
    val title: String,
    val urlTemplate: String,
    val regions: List<String>?,
    val type: String
)

interface BgmListApiService {
    @GET("api/v1/bangumi/archive/{year}q{season}")
    suspend fun getBangumiList(@Path("year") year: Int, @Path("season") season: Int): BgmListItems

    @GET("api/v1/bangumi/site")
    suspend fun getSiteMeta(): Map<String, SiteMetaPlatform>
}