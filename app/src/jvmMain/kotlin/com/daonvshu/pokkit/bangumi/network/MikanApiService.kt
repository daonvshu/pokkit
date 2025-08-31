package com.daonvshu.pokkit.bangumi.network

import okhttp3.ResponseBody
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

@Root(name = "rss", strict = false)
data class RssFeed(
    @field:Element(name = "channel")
    var channel: Channel? = null
)

@Root(name = "channel", strict = false)
data class Channel(
    @field:Element(name = "title")
    var title: String = "",

    @field:Element(name = "link")
    var link: String = "",

    @field:Element(name = "description", required = false)
    var description: String? = null,

    @field:ElementList(inline = true, entry = "item")
    var items: MutableList<RssItem> = mutableListOf()
)

@Root(name = "item", strict = false)
data class RssItem(
    @field:Element(name = "guid")
    var guid: String = "",

    @field:Element(name = "link")
    var link: String = "",

    @field:Element(name = "title")
    var title: String = "",

    @field:Element(name = "description")
    var description: String = "",

    @field:Element(name = "torrent", required = false)
    var torrent: Torrent? = null,

    @field:Element(name = "enclosure", required = false)
    var enclosure: Enclosure? = null
)

@Root(name = "torrent", strict = false)
data class Torrent(
    @field:Element(name = "link")
    var link: String = "",

    @field:Element(name = "contentLength")
    var contentLength: Long = 0,

    @field:Element(name = "pubDate")
    var pubDate: String = ""
)

@Root(name = "enclosure", strict = false)
data class Enclosure(
    @field:Attribute(name = "type", required = false)
    var type: String? = null,

    @field:Attribute(name = "length", required = false)
    var length: Long? = null,

    @field:Attribute(name = "url", required = false)
    var url: String? = null
)

interface MikanApiService {
    @GET("Home/BangumiCoverFlowByDayOfWeek")
    suspend fun getDataBySeason(@Query("year") year: Int, @Query("seasonStr") season: String): ResponseBody

    @Streaming
    @GET
    suspend fun getImage(@Url url: String): ResponseBody

    @GET("Home/Bangumi/{id}")
    suspend fun getDetailPage(@Path("id") mikanId: Int): ResponseBody

    @GET("RSS/Bangumi")
    suspend fun getTorrentLinks(@Query("bangumiId") mikanId: Int): RssFeed
}