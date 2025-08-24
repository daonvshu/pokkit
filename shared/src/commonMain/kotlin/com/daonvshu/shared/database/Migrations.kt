package com.daonvshu.shared.database

import com.daonvshu.shared.database.schema.DownloadRecordService
import com.daonvshu.shared.database.schema.MikanDataRecordService
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

interface Migration {
    val version: Int
    val description: String
    val targetTb: Table
    fun apply(transaction: JdbcTransaction)
}

object MigrationV1: Migration {
    override val version: Int = 1
    override val description: String = "Add information columns for MikanDataRecord"
    override val targetTb: Table = MikanDataRecordService.MikanDataRecords

    override fun apply(transaction: JdbcTransaction) {
        transaction.exec("ALTER TABLE MikanDataRecord ADD COLUMN \"summary\" TEXT DEFAULT '';")
        transaction.exec("ALTER TABLE MikanDataRecord ADD COLUMN \"officialSite\" TEXT DEFAULT '';")
        transaction.exec("ALTER TABLE MikanDataRecord ADD COLUMN \"eps\" INTEGER DEFAULT 0;")
        transaction.exec("ALTER TABLE MikanDataRecord ADD COLUMN \"sites\" TEXT DEFAULT '';")
    }
}

object MigrationV2: Migration {
    override val version: Int = 2
    override val description: String = "Add column 'fansub' for download record"
    override val targetTb: Table = DownloadRecordService.DownloadRecords

    override fun apply(transaction: JdbcTransaction) {
        transaction.exec("ALTER TABLE downloadrecord ADD COLUMN \"fansub\" TEXT DEFAULT '';")
    }
}

object MigrationV3: Migration {
    override val version: Int = 3
    override val description: String = "Add column 'saveDir' and 'autoCreateDir' for download record"
    override val targetTb: Table = DownloadRecordService.DownloadRecords

    override fun apply(transaction: JdbcTransaction) {
        transaction.exec("ALTER TABLE downloadrecord ADD COLUMN \"save_dir\" TEXT DEFAULT '';")
        transaction.exec("ALTER TABLE downloadrecord ADD COLUMN \"auto_create_dir\" BOOLEAN DEFAULT TRUE;")
    }
}