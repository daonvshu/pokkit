package com.daonvshu.shared.database.schema

import com.daonvshu.shared.database.Databases
import com.daonvshu.shared.database.dbQuery
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.inList
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

data class DownloadRecord(
    val id: Int = -1,
    val linkedMikanId: Int,
    val title: String,
    val thumbnail: String,
    val torrentInfoHash: String,
    val torrentData: String, //内容base64
    val torrentSrcName: String, //原链接名
    val torrentName: String, //解析链接名
    val finished: Boolean = false,
) {
    companion object {
        fun empty() = DownloadRecord(
            id = -1,
            linkedMikanId = -1,
            title = "",
            thumbnail = "",
            torrentInfoHash = "",
            torrentData = "",
            torrentSrcName = "",
            torrentName = "",
            finished = false
        )
    }
}

class DownloadRecordService {
    object DownloadRecords : IntIdTable("downloadrecord") {
        val linkedMikanId = integer("linked_mikan_id")
        val title = text("title")
        val thumbnail = text("thumbnail")
        val torrentInfoHash = text("torrent_info_hash")
        val torrentData = text("torrent_data")
        val torrentSrcName = text("torrent_src_name")
        val torrentName = text("torrent_name")
        val finished = bool("finished").default(false)
    }

    init {
        transaction(Databases.db) {
            SchemaUtils.create(DownloadRecords)
        }
    }

    fun createRecord(record: DownloadRecord): Int {
        return dbQuery {
            DownloadRecords.insert {
                it[linkedMikanId] = record.linkedMikanId
                it[title] = record.title
                it[thumbnail] = record.thumbnail
                it[torrentInfoHash] = record.torrentInfoHash
                it[torrentData] = record.torrentData
                it[torrentSrcName] = record.torrentSrcName
                it[torrentName] = record.torrentName
                it[finished] = record.finished
            } [DownloadRecords.id].value
        }
    }

    fun getAllRecords(): List<DownloadRecord> {
        return dbQuery {
            DownloadRecords.selectAll().map {
                DownloadRecord(
                    id = it[DownloadRecords.id].value,
                    linkedMikanId = it[DownloadRecords.linkedMikanId],
                    title = it[DownloadRecords.title],
                    thumbnail = it[DownloadRecords.thumbnail],
                    torrentInfoHash = it[DownloadRecords.torrentInfoHash],
                    torrentData = it[DownloadRecords.torrentData],
                    torrentSrcName = it[DownloadRecords.torrentSrcName],
                    torrentName = it[DownloadRecords.torrentName],
                    finished = it[DownloadRecords.finished]
                )
            }
        }
    }

    fun makeFinished(recordId: Int) = dbQuery {
        DownloadRecords.update({ DownloadRecords.id.eq(recordId) }) {
            it[finished] = true
        }
    }

    fun removeRecord(recordId: Int) = dbQuery {
        DownloadRecords.deleteWhere { DownloadRecords.id.eq(recordId) }
    }

    fun removeRecords(recordIds: List<Int>) = dbQuery {
        DownloadRecords.deleteWhere { DownloadRecords.id.inList(recordIds) }
    }
}