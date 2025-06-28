package com.daonvshu.shared.database

import com.daonvshu.shared.database.schema.MikanDataRecordService
import com.daonvshu.shared.database.schema.MikanTorrentLinkCacheService
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object Databases {

    private const val databaseLocalPath = ".data/pokkit.db"

    lateinit var db: Database

    lateinit var mikanDataRecordService: MikanDataRecordService
    lateinit var mikanTorrentLinkCacheService: MikanTorrentLinkCacheService

    fun init() {
        db = Database.connect(
            url = "jdbc:sqlite:$databaseLocalPath",
            driver = "org.sqlite.JDBC",
        )

        MigrationRunner.runAllMigrations(db)

        mikanDataRecordService = MikanDataRecordService()
        mikanTorrentLinkCacheService = MikanTorrentLinkCacheService()
    }
}

fun <T> dbQuery(block: Transaction.() -> T): T {
    return transaction(Databases.db) {
        block()
    }
}