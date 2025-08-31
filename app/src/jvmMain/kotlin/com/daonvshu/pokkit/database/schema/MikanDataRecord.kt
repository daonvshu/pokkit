@file:Suppress("PROVIDED_RUNTIME_TOO_LOW", "INLINE_CLASSES_NOT_SUPPORTED")
package com.daonvshu.pokkit.database.schema

import com.daonvshu.pokkit.database.Databases
import com.daonvshu.pokkit.database.MigrationRunner.SchemaMigrations.uniqueIndex
import com.daonvshu.pokkit.database.dbQuery
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.addLogger
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert

@Serializable
data class MikanDataRecord(
    val mikanId: Int = -1,
    val bindBangumiId: Int = -1,
    val link: String = "",
    val seasonTime: Long = -1,
    val dayOfWeek: Int = -1,
    val title: String = "",
    val thumbnail: String = "",
    val favorite: Boolean = false,
    val summary: String = "",
    val officialSite: String = "",
    val eps: Int = 0,
    val sites: String = "",
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
        val summary = text("summary").default("")
        val officialSite = text("officialSite").default("")
        val eps = integer("eps").default(0)
        val sites = text("sites").default("")

        init {
            uniqueIndex("unique_id_mikan", mikanId, seasonTime, dayOfWeek)
        }
    }

    init {
        transaction(Databases.db) {
            SchemaUtils.create(MikanDataRecords)
        }
    }

    fun insertData(record: MikanDataRecord) = dbQuery {
        MikanDataRecords.upsert(
            onUpdateExclude = arrayListOf(
                MikanDataRecords.bindBangumiId,
                MikanDataRecords.favorite,
                MikanDataRecords.summary,
                MikanDataRecords.officialSite,
                MikanDataRecords.eps,
                MikanDataRecords.sites
            )
        ) {
            it[MikanDataRecords.bindBangumiId] = record.bindBangumiId
            it[MikanDataRecords.mikanId] = record.mikanId
            it[MikanDataRecords.link] = record.link
            it[MikanDataRecords.seasonTime] = record.seasonTime
            it[MikanDataRecords.dayOfWeek] = record.dayOfWeek
            it[MikanDataRecords.title] = record.title
            it[MikanDataRecords.thumbnail] = record.thumbnail
        }
    }

    fun insertData(mikanData: List<MikanDataRecord>) {
        for (data in mikanData) {
            insertData(data)
        }
    }

    fun isEmptyRecord(seasonTime: Long): Boolean = dbQuery {
        val count = transaction(Databases.db) {
            addLogger(StdOutSqlLogger)
            MikanDataRecords
                .select(MikanDataRecords.mikanId)
                .where { MikanDataRecords.seasonTime eq seasonTime }
                .count()
        }

        return@dbQuery count == 0L
    }

    fun getAllData(seasonTime: Long): List<MikanDataRecord> = dbQuery {
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
                    favorite = it[MikanDataRecords.favorite],
                    summary = it[MikanDataRecords.summary],
                    officialSite = it[MikanDataRecords.officialSite],
                    eps = it[MikanDataRecords.eps],
                    sites = it[MikanDataRecords.sites]
                )
            }
    }

    fun getByMikanId(mikanId: Int): MikanDataRecord? = dbQuery {
        MikanDataRecords
            .selectAll()
            .where { MikanDataRecords.mikanId eq mikanId }
            .map {
                MikanDataRecord(
                    mikanId = it[MikanDataRecords.mikanId],
                    bindBangumiId = it[MikanDataRecords.bindBangumiId],
                    link = it[MikanDataRecords.link],
                    seasonTime = it[MikanDataRecords.seasonTime],
                    dayOfWeek = it[MikanDataRecords.dayOfWeek],
                    title = it[MikanDataRecords.title],
                    thumbnail = it[MikanDataRecords.thumbnail],
                    favorite = it[MikanDataRecords.favorite],
                    summary = it[MikanDataRecords.summary],
                    officialSite = it[MikanDataRecords.officialSite],
                    eps = it[MikanDataRecords.eps],
                    sites = it[MikanDataRecords.sites]
                )
            }
            .singleOrNull()
    }

    fun updateFavorite(record: MikanDataRecord) = dbQuery {
        MikanDataRecords.update({ (MikanDataRecords.mikanId eq record.mikanId) and
                    (MikanDataRecords.seasonTime eq record.seasonTime) and
                    (MikanDataRecords.dayOfWeek eq record.dayOfWeek)
        }) {
            it[MikanDataRecords.favorite] = record.favorite
        }
    }

    fun updateDetailInfo(
        mikanId: Int,
        bindBangumiId: Int,
        summary: String,
        officialSite: String,
        eps: Int,
        sites: String
    ) = dbQuery {
        MikanDataRecords.update({
            MikanDataRecords.mikanId eq mikanId
        }) {
            it[MikanDataRecords.bindBangumiId] = bindBangumiId
            it[MikanDataRecords.summary] = summary
            it[MikanDataRecords.officialSite] = officialSite
            it[MikanDataRecords.eps] = eps
            it[MikanDataRecords.sites] = sites
        }
    }
}