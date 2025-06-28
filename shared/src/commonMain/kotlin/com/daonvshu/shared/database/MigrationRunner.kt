package com.daonvshu.shared.database

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object MigrationRunner {
    private val migrations = listOf<Migration>(
        MigrationV1,
    )

    fun runAllMigrations(database: Database) {
        transaction(database) {
            try {
                SchemaUtils.create(SchemaMigrations)

                val appliedVersions = SchemaMigrations.selectAll()
                    .orderBy(SchemaMigrations.version to SortOrder.DESC)
                    .map { it[SchemaMigrations.version] }

                if (appliedVersions.isEmpty()) {
                    println("No need run migrations. Database is empty.")
                    SchemaMigrations.insert {
                        it[version] = migrations.last().version
                        it[description] = "init database version."
                    }
                    return@transaction
                }

                val pendingMigrations = migrations.filter { it.version > appliedVersions.first() }

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