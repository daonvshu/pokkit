package com.daonvshu.pokkit.bangumi.repository

import com.daonvshu.pokkit.bangumi.network.BgmTvApi
import com.daonvshu.pokkit.bangumi.network.MikanApi
import com.daonvshu.pokkit.bangumi.utils.MikanDataParseUtil
import com.daonvshu.pokkit.database.Databases
import com.daonvshu.pokkit.database.schema.MikanDataRecord
import com.daonvshu.pokkit.database.schema.MikanTorrentLinkCache
import com.daonvshu.pokkit.settings.AppSettings
import com.daonvshu.shared.utils.LogCollector
import java.io.File
import java.net.URLEncoder
import java.time.Instant
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
        LogCollector.addLog("get bangumi id: $bangumiId")
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
            LogCollector.addLog("reload mikan season data from cache...")
            return Databases.mikanDataRecordService.getAllData(seasonTime)
        }

        val seasonStrings = arrayOf("冬", "春", "夏", "秋")
        LogCollector.addLog("request season data: year: $year, season: ${seasonStrings[seasonIndex]}")
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
            LogCollector.addLog("fetch bangumi id...")
            val mikanDetailPage = MikanApi.apiService.getDetailPage(item.mikanId)
            bangumiId = MikanDataParseUtil.getBangumiIdFromData(mikanDetailPage.string())
            //File(".test/mikan_detail_page.html").writeText(mikanDetailPage.string())
        }
        if (bangumiId == -1) {
            LogCollector.addLog("get bangumi id fail!")
            return false
        }
        LogCollector.addLog("fetch bangumi detail...")
        val detailData = BgmTvApi.apiService.getBangumiDetail(bangumiId)

        LogCollector.addLog("fetch bangumi detail from db...")
        val detailData2 = BangumiDataDbRepository.getData(bangumiId)

        val summary = detailData.summary
        val officialSite = detailData2.officialSite ?: ""
        val eps = detailData.eps
        Databases.mikanDataRecordService.updateDetailInfo(item.mikanId, bangumiId, summary, officialSite, eps, detailData2.sites ?: "")

        return true
    }

    override suspend fun getTorrentLinks(item: MikanDataRecord, reload: Boolean): List<MikanTorrentLinkCache> {
        if (!reload && !isTorrentLinkReloadRequired(item.mikanId)) {
            LogCollector.addLog("reload torrent link from cache...")
            return Databases.mikanTorrentLinkCacheService.getCaches(item.mikanId)
        }

        LogCollector.addLog("request torrent link...")
        val data = MikanApi.apiService.getTorrentLinks(item.mikanId)
        LogCollector.addLog("parse torrent link base info...")
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
        LogCollector.addLog("parse torrent link to cache...")
        val caches = links.map { link ->
            var title = link.title.trim()
            if (title.startsWith("\u8203")) {
                title = title.substring(1)
            }
            val groupInfo = extractBracketContent(title)
            title = groupInfo.second

            val eps = getEps(title)
            if (eps == -1) {
                LogCollector.addLog("get eps fail! title: $title")
            }
            MikanTorrentLinkCache(
                bindMikanId = item.mikanId,
                fansub = groupInfo.first,
                description = link.description,
                eps = eps,
                gb = title.contains(Regex("""简[体繁日中]?|中日|[Cc][Hh][Ss]|\[GB]|GB_|GB&| GB |【GB】""")),
                downloadUrl = link.downloadUrl,
                updateTime = updateTime
            )
        }
        Databases.mikanTorrentLinkCacheService.clearCache(item.mikanId)
        Databases.mikanTorrentLinkCacheService.insertCaches(caches)
        LogCollector.addLog("cache fetch size: ${caches.size}")
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

