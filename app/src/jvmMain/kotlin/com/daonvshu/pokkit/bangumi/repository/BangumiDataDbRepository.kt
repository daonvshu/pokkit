package com.daonvshu.pokkit.bangumi.repository

import com.daonvshu.pokkit.settings.AppSettings
import com.daonvshu.shared.utils.LogCollector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.max
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

data class BangumiDetail(
    val officialSite: String?,
    val sites: String?
)

data class BangumiSiteData(
    val itemId: Int,
    val siteTitle: String,
    val siteType: String,
    val urlResolved: String?,
    val url: String?,
)

data class BangumiDbInfo(
    val dbVersion: String,
    val generatedAt: String,
)

object BangumiDataDbRepository {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val lock = ReentrantReadWriteLock()
    private val downloadMutex = Mutex()

    private val dbFile = File(".data/bangumi-data.db")
    private const val REMOTE_URL = "https://daonvshu.github.io/bangumi-data-db/bangumi-latest.db"

    private var database: Database? = null

    // 获取数据入口
    suspend fun getData(bangumiId: Int): BangumiDetail {
        ensureDbExists() // 确保有数据库文件
        checkForAsyncUpdate() // 如果过期则触发后台更新（异步）

        // 读数据库时加读锁
        return lock.read {
            readDetail(bangumiId)
        }
    }

    fun getDbInfo(): BangumiDbInfo {
        return lock.read {
            if (database == null) {
                connectDatabase()
            }
            transaction(database) {
                val metaAll = MetaTable.select(
                    MetaTable.key, MetaTable.value
                ).map {
                    it[MetaTable.key] to it[MetaTable.value]
                }

                val version = metaAll.firstOrNull { it.first == "version" }?.second
                val generatedAt = metaAll.firstOrNull { it.first == "generated_at" }?.second
                val ts = if (generatedAt != null) {
                    val instance = Instant.ofEpochMilli(generatedAt.toLong())
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        .withZone(ZoneId.systemDefault()) // 使用系统时区
                    "[${formatter.format(instance)}]"
                } else {
                    ""
                }
                BangumiDbInfo(
                    dbVersion = version ?: "（下载失败）",
                    generatedAt = ts
                )
            }
        }
    }

    fun updateDb() {
        val tmpFile = File(dbFile.parent, dbFile.name + ".tmp")
        downloadDbFile(tmpFile)

        // 替换时加写锁，等所有读操作结束
        lock.write {
            LogCollector.addLog("replace database ...")
            database?.let {
                TransactionManager.closeAndUnregister(it)
            }
            tmpFile.copyTo(dbFile, overwrite = true)
            tmpFile.delete()
            connectDatabase()
            LogCollector.addLog("database updated")
        }
    }

    // 确保数据库存在
    private suspend fun ensureDbExists() {
        if (!dbFile.exists()) {
            downloadMutex.withLock {
                if (!dbFile.exists()) {
                    LogCollector.addLog("downloading database from $REMOTE_URL ...")
                    downloadDbFile(dbFile)
                    connectDatabase()
                }
            }
        } else if (database == null) {
            connectDatabase()
        }
    }

    // 异步更新检查
    private fun checkForAsyncUpdate() {
        if (dbFile.exists() && isExpired()) {
            scope.launch {
                downloadMutex.withLock {
                    if (isExpired()) {
                        LogCollector.addLog("database is expired, downloading ...")
                        updateDb()
                    }
                }
            }
        }
    }

    // 判断是否过期（3 天）
    private fun isExpired(): Boolean {
        val db = database ?: return true
        val generatedAt = transaction(db) {
            MetaTable.select(
                MetaTable.value
            ).where {
                MetaTable.key.eq("generated_at")
            }.map {
                it[MetaTable.value]
            }.singleOrNull()
        }

        if (generatedAt == null) {
            return true
        }

        return try {
            val tsLong = generatedAt.toLong()
            val ts = Instant.ofEpochMilli(tsLong)
            ts.plusSeconds(3 * 24 * 3600).isBefore(Instant.now())
        } catch (e: Exception) {
            true
        }
    }

    private fun connectDatabase() {
        database = Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}",
            driver = "org.sqlite.JDBC"
        )
    }

    private fun downloadDbFile(target: File) {
        val request = Request.Builder()
            .url(REMOTE_URL)
            .build()

        val httpClient = OkHttpClient.Builder()
            .proxy(AppSettings.getProxy())
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                LogCollector.addLog("download database fail: ${response.code}")
                return
            }

            target.parentFile?.mkdirs()
            response.body?.byteStream().use { input ->
                FileOutputStream(target).use { output ->
                    input?.copyTo(output)
                }
            }
        }
    }

    private fun readDetail(bangumiId: Int): BangumiDetail {
        return transaction(database) {

            val lastItemId = SitesTable.select(
                SitesTable.itemId.max()
            ).where {
                SitesTable.siteName.eq("bangumi") and SitesTable.siteId.eq(bangumiId.toString())
            }.map {
                it[SitesTable.itemId.max()] as? Int
            }.singleOrNull()

            if (lastItemId == null) {
                return@transaction BangumiDetail(null, null)
            }

            val siteData = SitesTable.select(
                SitesTable.itemId,
                SitesTable.siteTitle,
                SitesTable.siteType,
                SitesTable.urlResolved,
                SitesTable.url,
            ).where {
                SitesTable.itemId.eq(lastItemId)
            }.map {
                BangumiSiteData(
                    itemId = it[SitesTable.itemId],
                    siteTitle = it[SitesTable.siteTitle],
                    siteType = it[SitesTable.siteType],
                    urlResolved = it[SitesTable.urlResolved],
                    url = it[SitesTable.url],
                )
            }

            val siteList = siteData.map {
                val title = it.siteTitle.ifEmpty { " " }
                val url = when {
                    it.url != null && it.url.isNotEmpty() -> it.url
                    it.urlResolved != null && it.urlResolved.isNotEmpty() -> it.urlResolved
                    else -> ""
                }
                val type = it.siteType.ifEmpty { " " }
                "$title,$url,$type"
            }

            val officialSite = ItemsTable.select(
                ItemsTable.officialSite
            ).where {
                ItemsTable.id.eq(lastItemId)
            }.map {
                it[ItemsTable.officialSite]
            }.singleOrNull()
            BangumiDetail(officialSite, siteList.joinToString(","))
        }
    }
}