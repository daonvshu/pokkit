package com.daonvshu.mikan.repository

import com.daonvshu.mikan.network.MikanApi
import com.daonvshu.mikan.utils.MikanDataParseUtil
import com.daonvshu.shared.database.MikanDataRecord
import com.daonvshu.shared.settings.AppSettings
import java.io.File
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
}

class DebugMikanDataRepository : MikanDataRepositoryInterface {
    override suspend fun getDataBySeason(year: Int, seasonIndex: Int): List<MikanDataRecord> {
        val data = File(".test/mikan_season_data.html").readText()
        return MikanDataParseUtil.parseData(MikanDataParseUtil.getSeasonTimePointByIndex(year, seasonIndex), data)
    }
}

class MikanDataRepositoryImpl : MikanDataRepositoryInterface {
    override suspend fun getDataBySeason(year: Int, seasonIndex: Int): List<MikanDataRecord> {
        val seasonTime = MikanDataParseUtil.getSeasonTimePointByIndex(year, seasonIndex)
        if (!isDataReloadRequired(seasonTime)) {
            println("reload mikan season data from cache...")
            return MikanDbRepository.getAllData(seasonTime)
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
        val records = MikanDataParseUtil.parseData(seasonTime, data)
        MikanDbRepository.insertData(records)
        AppSettings.settings.general.mikan.lastUpdateTime = System.currentTimeMillis()
        AppSettings.save()
        return records
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
        if (MikanDbRepository.isEmptyRecord(seasonTime)) {
            return true
        }
        return false
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

