package com.daonvshu.shared.database.schema

import com.daonvshu.shared.database.Databases
import com.daonvshu.shared.database.MigrationRunner.SchemaMigrations.index
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.addLogger
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert

@Serializable
data class MikanDataRecord(
    val mikanId: Int = -1,
    val bindBangumiId: Int = -1,
    val link: String = "",
    val seasonTime: Long,
    val dayOfWeek: Int,
    val title: String,
    val thumbnail: String,
    val favorite: Boolean = false,
)

class MikanDataRecordService {
    object MikanDataRecords : Table("MikanDataRecord") {
        val mikanId = integer("mikanId")
        val bindBangumiId = integer("bindBangumiId")
        val link = text("link")
        val seasonTime = long("seasonTime")
        val dayOfWeek = integer("dayOfWeek")
        val title = text("title")
        val thumbnail = text("thumbnail")
        val favorite = bool("favorite").default(false)
    }

    init {
        index("unique_id_mikan", isUnique = true,
            MikanDataRecords.mikanId, MikanDataRecords.seasonTime, MikanDataRecords.dayOfWeek)
        transaction(Databases.db) {
            SchemaUtils.create(MikanDataRecords)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) {
            //addLogger(StdOutSqlLogger)
            block()
        }

    suspend fun insertData(record: MikanDataRecord) = dbQuery {
        MikanDataRecords.upsert {
            it[mikanId] = record.mikanId
            it[bindBangumiId] = record.bindBangumiId
            it[link] = record.link
            it[seasonTime] = record.seasonTime
            it[dayOfWeek] = record.dayOfWeek
            it[title] = record.title
            it[thumbnail] = record.thumbnail
            it[favorite] = record.favorite
        }
    }

    suspend fun insertData(mikanData: List<MikanDataRecord>) {
        for (data in mikanData) {
            insertData(data)
        }
    }

    suspend fun isEmptyRecord(seasonTime: Long): Boolean = dbQuery {
        val count = transaction(Databases.db) {
            addLogger(StdOutSqlLogger)
            MikanDataRecords
                .select(MikanDataRecords.mikanId)
                .where { MikanDataRecords.seasonTime eq seasonTime }
                .count()
        }

        return@dbQuery count == 0L
    }

    suspend fun getAllData(seasonTime: Long): List<MikanDataRecord> = dbQuery {
        MikanDataRecords
            .selectAll()
            .where { MikanDataRecords.seasonTime eq seasonTime }
            .map {
                MikanDataRecord(
                    mikanId = it[MikanDataRecords.mikanId],
                    bindBangumiId = it[MikanDataRecords.bindBangumiId],
                    link = it[MikanDataRecords.link],
                    seasonTime = it[MikanDataRecords.seasonTime],
                    dayOfWeek = it[MikanDataRecords.dayOfWeek],
                    title = it[MikanDataRecords.title],
                    thumbnail = it[MikanDataRecords.thumbnail],
                    favorite = it[MikanDataRecords.favorite]
                )
            }
    }
}