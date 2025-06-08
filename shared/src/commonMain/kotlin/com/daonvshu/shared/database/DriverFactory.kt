package com.daonvshu.shared.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import java.util.Properties

object DriverFactory {
    fun createDriver(): SqlDriver {
        val filePath = File(".data/pokkit.db").apply {
            parentFile.let {
                if (!it.exists()) {
                    it.mkdirs()
                }
            }
        }
        return JdbcSqliteDriver("jdbc:sqlite:$filePath", Properties(), AppDatabase.Schema)
    }
}