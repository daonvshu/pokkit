package com.daonvshu.pokkit.database

import com.daonvshu.pokkit.database.schema.DownloadRecordService
import com.daonvshu.pokkit.database.schema.MikanDataRecordService
import com.daonvshu.pokkit.database.schema.MikanTorrentLinkCacheService
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object Databases {

    private const val databaseLocalPath = ".data/pokkit.db"

    lateinit var db: Database

    lateinit var mikanDataRecordService: MikanDataRecordService
    lateinit var mikanTorrentLinkCacheService: MikanTorrentLinkCacheService
    lateinit var downloadRecordService: DownloadRecordService

    fun init() {
        db = Database.connect(
            url = "jdbc:sqlite:$databaseLocalPath",
            driver = "org.sqlite.JDBC",
        )

        MigrationRunner.runAllMigrations(db)

        mikanDataRecordService = MikanDataRecordService()
        mikanTorrentLinkCacheService = MikanTorrentLinkCacheService()
        downloadRecordService = DownloadRecordService()
    }
}

fun <T> dbQuery(block: Transaction.() -> T): T {
    return transaction(Databases.db) {
        block()
    }
}