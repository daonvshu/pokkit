package com.daonvshu.shared.database

import com.daonvshu.shared.database.schema.MikanDataRecordService
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object Databases {

    private const val databaseLocalPath = ".data/pokkit.db"

    lateinit var db: Database

    lateinit var mikanDataRecordService: MikanDataRecordService

    fun init() {
        db = Database.connect(
            url = "jdbc:sqlite:$databaseLocalPath",
            driver = "org.sqlite.JDBC",
        )

        MigrationRunner.runAllMigrations(db)

        mikanDataRecordService = MikanDataRecordService()
    }
}

fun <T> dbQuery(block: Transaction.() -> T): T {
    return transaction(Databases.db) {
        block()
    }
}