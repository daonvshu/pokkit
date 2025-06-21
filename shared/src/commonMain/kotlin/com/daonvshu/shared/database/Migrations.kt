package com.daonvshu.shared.database

import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

interface Migration {
    val version: Int
    val description: String
    fun apply(transaction: JdbcTransaction)
}

object MigrationV1: Migration {
    override val version: Int = 1

    override val description: String = "Add information columns for MikanDataRecord"

    override fun apply(transaction: JdbcTransaction) {
        transaction.exec("ALTER TABLE MikanDataRecord ADD COLUMN \"summary\" TEXT DEFAULT '';")
        transaction.exec("ALTER TABLE MikanDataRecord ADD COLUMN \"officialSite\" TEXT DEFAULT '';")
        transaction.exec("ALTER TABLE MikanDataRecord ADD COLUMN \"eps\" INTEGER DEFAULT 0;")
        transaction.exec("ALTER TABLE MikanDataRecord ADD COLUMN \"sites\" TEXT DEFAULT '';")
    }
}