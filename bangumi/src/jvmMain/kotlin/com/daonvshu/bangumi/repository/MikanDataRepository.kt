package com.daonvshu.bangumi.repository

import com.daonvshu.bangumi.network.BgmListApi
import com.daonvshu.bangumi.network.BgmTvApi
import com.daonvshu.bangumi.network.MikanApi
import com.daonvshu.bangumi.utils.MikanDataParseUtil
import com.daonvshu.shared.database.Databases
import com.daonvshu.shared.database.schema.MikanDataRecord
import com.daonvshu.shared.database.schema.MikanTorrentLinkCache
import com.daonvshu.shared.settings.AppSettings
import java.io.File
import java.net.URLEncoder
import java.time.LocalDate
import java.time.ZoneId

interface MikanDataRepositoryInterface {
    /**
     * 获取指定年份和季度的番剧数据
     * @param year 年份
     * @param seasonIndex 季度索引
     * @return 番剧数据
     * @throws okio.IOException 网络请求错误
     */
    suspend fun getDataBySeason(year: Int, seasonIndex: Int): List<MikanDataRecord>

    /**
     * 获取指定mikanId的番剧数据
     * @param mikanId mikanId
     * @return 番剧数据
     * @throws okio.IOException 网络请求错误
     */
    suspend fun getDataById(mikanId: Int): MikanDataRecord?

    /**
     * 修改收藏状态
     * @param item 番剧数据
     */
    suspend fun changeFavorite(item: MikanDataRecord)

    /**
     * 更新番剧详情
     * @param item 番剧数据
     */
    suspend fun updateDetail(item: MikanDataRecord): Boolean

    /**
     * 获取番剧种子链接
     * @param item 番剧数据
     * @param reload 是否强制重新获取种子链接
     */
    suspend fun getTorrentLinks(item: MikanDataRecord, reload: Boolean): List<MikanTorrentLinkCache>
}

class DebugMikanDataRepository : MikanDataRepositoryImpl() {
    override suspend fun getDataBySeason(year: Int, seasonIndex: Int): List<MikanDataRecord> {
        return super.getDataBySeason(year, seasonIndex)
        //val data = File(".test/mikan_season_data.html").readText()
        //return MikanDataParseUtil.parseData(MikanDataParseUtil.getSeasonTimePointByIndex(year, seasonIndex), data)
    }

    override suspend fun changeFavorite(item: MikanDataRecord) {
        super.changeFavorite(item)
        //println("change favorite: ${item.title}")
    }

    override suspend fun updateDetail(item: MikanDataRecord): Boolean {
        val data = File(".test/mikan_detail_page.html").readText()
        val bangumiId = MikanDataParseUtil.getBangumiIdFromData(data)
        println("get bangumi id: $bangumiId")
        return true
    }
}

data class TorrentSimpleInfo(
    val title: String,
    val description: String,
    val downloadUrl: String,
)

open class MikanDataRepositoryImpl : MikanDataRepositoryInterface {
    override suspend fun getDataBySeason(year: Int, seasonIndex: Int): List<MikanDataRecord> {
        val seasonTime = MikanDataParseUtil.getSeasonTimePointByIndex(year, seasonIndex)
        if (!isDataReloadRequired(seasonTime)) {
            println("reload mikan season data from cache...")
            return Databases.mikanDataRecordService.getAllData(seasonTime)
        }

        val seasonStrings = arrayOf("冬", "春", "夏", "秋")
        println("request season data: year: $year, season: ${seasonStrings[seasonIndex]}")
        val response = MikanApi.apiService.getDataBySeason(year, seasonStrings[seasonIndex])
        val data = response.string()
        /*
        File(".test/mikan_season_data.html")
            .apply {
                parentFile.mkdirs()
            }
            .writeText(data)
        println("write test data finish...")
         */
        println("parse data...")
        var records = MikanDataParseUtil.parseData(seasonTime, data)
        Databases.mikanDataRecordService.insertData(records)
        AppSettings.settings.general.mikan.lastUpdateTime = System.currentTimeMillis()
        AppSettings.save()

        records = Databases.mikanDataRecordService.getAllData(seasonTime)
        return records
    }

    override suspend fun getDataById(mikanId: Int): MikanDataRecord? {
        return Databases.mikanDataRecordService.getByMikanId(mikanId)
    }

    override suspend fun changeFavorite(item: MikanDataRecord) {
        Databases.mikanDataRecordService.updateFavorite(item)
    }

    override suspend fun updateDetail(item: MikanDataRecord): Boolean {
        var bangumiId = item.bindBangumiId
        if (bangumiId == -1) {
            println("fetch bangumi id...")
            val mikanDetailPage = MikanApi.apiService.getDetailPage(item.mikanId)
            bangumiId = MikanDataParseUtil.getBangumiIdFromData(mikanDetailPage.string())
            //File(".test/mikan_detail_page.html").writeText(mikanDetailPage.string())
        }
        if (bangumiId == -1) {
            println("get bangumi id fail!")
            return false
        }
        println("fetch bangumi detail...")
        val detailData = BgmTvApi.apiService.getBangumiDetail(bangumiId)

        val seasonTime = item.seasonTime
        val date = java.time.Instant.ofEpochMilli(seasonTime)
            .atZone(ZoneId.systemDefault())

        val year = date.year // 获取年份
        val season = when(date.monthValue) { // 月份（1-12）
            1,2,3 -> 1
            4,5,6 -> 2
            7,8,9 -> 3
            10,11,12 -> 4
            else -> 1
        }
        println("fetch bangumi list info...")
        val bgmItems = BgmListApi.apiService.getBangumiList(year, season)
        val target = bgmItems.items.firstOrNull { item ->
            item.sites.any {
                it.id == bangumiId.toString()
            }
        }

        val sites = if (target != null) {
            println("fetch site meta data...")
            val siteMeta = BgmListApi.apiService.getSiteMeta()
            val siteList = target.sites.map { site ->
                val meta = siteMeta[site.site]
                val url = meta?.urlTemplate?.replace("{{id}}", URLEncoder.encode(site.id, "UTF-8"))
                "${meta?.title ?: " "},$url,${meta?.type ?: " "}"
            }.filter {
                it.isNotEmpty()
            }
            siteList.joinToString(",")
        } else {
            println("get site meta fail!")
            ""
        }

        val summary = detailData.summary
        val officialSite = target?.officialSite ?: ""
        val eps = detailData.eps
        Databases.mikanDataRecordService.updateDetailInfo(item.mikanId, bangumiId, summary, officialSite, eps, sites)

        return true
    }

    override suspend fun getTorrentLinks(item: MikanDataRecord, reload: Boolean): List<MikanTorrentLinkCache> {
        if (!reload && !isTorrentLinkReloadRequired(item.mikanId)) {
            return Databases.mikanTorrentLinkCacheService.getCaches(item.mikanId)
        }

        val data = MikanApi.apiService.getTorrentLinks(item.mikanId)
        val links = mutableListOf<TorrentSimpleInfo>()
        data.channel?.items?.forEach { item ->
            val title = item.title
            val description = item.description
            val downloadUrl = item.enclosure?.url ?: ""
            if (title.isNotEmpty() && description.isNotEmpty() && downloadUrl.isNotEmpty()) {
                links.add(TorrentSimpleInfo(title, description, downloadUrl))
            }
        }
        val updateTime = System.currentTimeMillis()
        val caches = links.map { link ->
            var title = link.title.trim()
            if (title.startsWith("\u8203")) {
                title = title.substring(1)
            }
            val groupInfo = extractBracketContent(title)
            title = groupInfo.second

            MikanTorrentLinkCache(
                bindMikanId = item.mikanId,
                fansub = groupInfo.first,
                description = link.description,
                eps = getEps(title),
                gb = title.contains(Regex("""简[体繁日中]?|中日|[Cc][Hh][Ss]|\[GB]|GB_|GB&| GB |【GB】""")),
                downloadUrl = link.downloadUrl,
                updateTime = updateTime
            )
        }
        Databases.mikanTorrentLinkCacheService.clearCache(item.mikanId)
        Databases.mikanTorrentLinkCacheService.insertCaches(caches)
        println("cache fetch size: ${caches.size}")
        return caches
    }

    private fun isDataReloadRequired(seasonTime: Long): Boolean {
        val mikanGeneral = AppSettings.settings.general.mikan
        val todayStart = LocalDate.now()
            .atStartOfDay()  // 转成当天00:00:00的LocalDateTime
            .atZone(ZoneId.systemDefault()) // 转成带时区的时间
            .toInstant()
            .toEpochMilli()
        if (mikanGeneral.lastUpdateTime < todayStart) {
            return true
        }
        if (Databases.mikanDataRecordService.isEmptyRecord(seasonTime)) {
            return true
        }
        return false
    }

    private fun isTorrentLinkReloadRequired(mikanId: Int): Boolean {
        val lastUpdate = Databases.mikanTorrentLinkCacheService.getLastUpdateTime(mikanId)
        return lastUpdate + 6 * 60 * 60 * 1000 < System.currentTimeMillis() // 6小时
    }

    private fun extractBracketContent(input: String): Pair<String, String> {
        // 正则，匹配开头的【内容】或者[内容]
        val pattern = Regex("""^(?:【([^】]+)】|\[([^]]+)])""")
        val matchResult = pattern.find(input)

        return if (matchResult != null) {
            // group 1或group 2有值即为提取内容
            val content = matchResult.groups[1]?.value ?: matchResult.groups[2]?.value
            // 剩余字符串从匹配结束位置开始截取
            val remaining = input.substring(matchResult.range.last + 1)
            Pair(content ?: "其他", remaining)
        } else {
            // 不匹配返回null和原字符串
            Pair("其他", input)
        }
    }

    private fun getEps(input: String): Int {
        val pattern = Regex("""第(\d{1,3})[话集]|-\s*(\d{1,3})[vV]?\d?|\[(\d{1,3})[vV]?\d?]|【(\d{1,3})[vV]?\d?】""")
        val cleaned = input.replace(Regex("-\\s*\\d+[bB][iI][tT]"), "")
        val matchResult = pattern.find(cleaned)
        return matchResult?.groups?.drop(1)?.firstOrNull() { it != null }?.value?.toIntOrNull() ?: -1
    }
}

object MikanDataRepository {
    private val instance by lazy {
        MikanDataRepositoryImpl()
        //DebugMikanDataRepository()
    }

    fun get(): MikanDataRepositoryInterface {
        return instance
    }
}

