package com.daonvshu.shared.database.schema

import com.daonvshu.shared.database.Databases
import com.daonvshu.shared.database.dbQuery
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

@Serializable
data class MikanTorrentLinkCache(
    val bindMikanId: Int,
    val fansub: String,
    val description: String,
    val eps: Int,
    val gb: Boolean,
    val downloadUrl: String,
    val updateTime: Long,
)

class MikanTorrentLinkCacheService {
    object MikanTorrentLinkCaches : Table() {
        val bindMikanId = integer("bindMikanId")
        val fansub = text("fansub")
        val description = text("description")
        val eps = integer("eps")
        val gb = bool("gb")
        val downloadUrl = text("downloadUrl")
        val updateTime = long("updateTime")
    }

    init {
        transaction(Databases.db) {
            SchemaUtils.create(MikanTorrentLinkCaches)
        }
    }

    fun getLastUpdateTime(mikanId: Int): Long = dbQuery {
        MikanTorrentLinkCaches
            .select(MikanTorrentLinkCaches.updateTime)
            .where { MikanTorrentLinkCaches.bindMikanId.eq(mikanId) }
            .map { it[MikanTorrentLinkCaches.updateTime] }
            .firstOrNull() ?: -1
    }

    fun clearCache(mikanId: Int) = dbQuery {
        MikanTorrentLinkCaches.deleteWhere { MikanTorrentLinkCaches.bindMikanId.eq(mikanId) }
    }

    fun insertCaches(caches: List<MikanTorrentLinkCache>): Unit = dbQuery {
        MikanTorrentLinkCaches.batchInsert(caches) {
            this[MikanTorrentLinkCaches.bindMikanId] = it.bindMikanId
            this[MikanTorrentLinkCaches.fansub] = it.fansub
            this[MikanTorrentLinkCaches.description] = it.description
            this[MikanTorrentLinkCaches.eps] = it.eps
            this[MikanTorrentLinkCaches.gb] = it.gb
            this[MikanTorrentLinkCaches.downloadUrl] = it.downloadUrl
            this[MikanTorrentLinkCaches.updateTime] = it.updateTime
        }
    }

    fun getCaches(mikanId: Int) = dbQuery {
        MikanTorrentLinkCaches
            .selectAll()
            .where { MikanTorrentLinkCaches.bindMikanId.eq(mikanId) }
            .map {
                MikanTorrentLinkCache(
                    bindMikanId = it[MikanTorrentLinkCaches.bindMikanId],
                    fansub = it[MikanTorrentLinkCaches.fansub],
                    description = it[MikanTorrentLinkCaches.description],
                    eps = it[MikanTorrentLinkCaches.eps],
                    gb = it[MikanTorrentLinkCaches.gb],
                    downloadUrl = it[MikanTorrentLinkCaches.downloadUrl],
                    updateTime = it[MikanTorrentLinkCaches.updateTime]
                )
            }
    }
}