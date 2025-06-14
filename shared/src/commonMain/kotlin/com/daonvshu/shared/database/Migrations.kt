package com.daonvshu.shared.database

import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

interface Migration {
    val version: Int
    val description: String
    fun apply(transaction: JdbcTransaction)
}