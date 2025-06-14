package com.daonvshu.shared.database

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object MigrationRunner {
    private val migrations = listOf<Migration>(
    )

    fun runAllMigrations(database: Database) {
        transaction(database) {
            try {
                SchemaUtils.create(SchemaMigrations)

                val appliedVersions = SchemaMigrations.selectAll()
                    .map { it[SchemaMigrations.version] }
                    .toSet()

                if (appliedVersions.isEmpty()) {
                    println("No need run migrations. Database is empty.")
                    return@transaction
                }

                val pendingMigrations = migrations.filter { it.version !in appliedVersions }

                if (pendingMigrations.isNotEmpty()) {
                    println("Pending migrations detected: ${pendingMigrations.map { it.version }}")
                } else {
                    println("No pending migrations. Database is up-to-date.")
                    return@transaction
                }

                pendingMigrations.sortedBy { it.version }.forEach { migration ->
                    println("Applying migration ${migration.version}: ${migration.description}")
                    migration.apply(this)
                    SchemaMigrations.insert {
                        it[version] = migration.version
                        it[description] = migration.description
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                rollback()
                throw e
            }
        }
    }

    object SchemaMigrations : Table() {
        val version = integer("version")
        val description = text("description")
        val appliedAt = datetime("applied_at").clientDefault {
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }
}