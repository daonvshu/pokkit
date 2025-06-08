package com.daonvshu.mikan.repository

import com.daonvshu.shared.database.AppDatabase
import com.daonvshu.shared.database.DriverFactory
import com.daonvshu.shared.database.MikanDataRecord

object MikanDbRepository {

    private val dbQuery by lazy {
        val database = AppDatabase(DriverFactory.createDriver())
        database.mikanDataRecordQueries
    }

    fun insertData(mikanData: MikanDataRecord) {
        dbQuery.insertData(
            mikanId = mikanData.mikanId,
            bindBangumiId = mikanData.bindBangumiId,
            link = mikanData.link,
            seasonTime = mikanData.seasonTime,
            dayOfWeek = mikanData.dayOfWeek,
            title = mikanData.title,
            thumbnail = mikanData.thumbnail,
            favorite = mikanData.favorite,
        )
    }

    fun insertData(mikanData: List<MikanDataRecord>) {
        dbQuery.transaction {
            for (data in mikanData) {
                insertData(data)
            }
        }
    }

    fun isEmptyRecord(seasonTime: Long): Boolean {
        return dbQuery.countOfSeasonTime(seasonTime).executeAsOne() == 0L
    }

    fun getAllData(seasonTime: Long): List<MikanDataRecord> {
        return dbQuery.selectAll(seasonTime).executeAsList()
    }
}