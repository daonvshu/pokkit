package com.daonvshu.pokkit.bangumi.repository

import com.daonvshu.pokkit.database.Databases
import com.daonvshu.pokkit.database.schema.DownloadRecord
import com.daonvshu.pokkit.settings.AppSettings
import java.io.File

interface DownloadDataRepositoryInterface {
    /**
     * 获取番剧记录列表，linkId -> List<Data>
     */
    suspend fun getBangumiDownloadRecordList(): Map<Int, List<DownloadRecord>>

    /**
     * 获取番剧所有下载记录
     */
    suspend fun getBangumiAllDownloadRecords(): List<DownloadRecord>

    /**
     * 获取番剧记录
     */
    suspend fun getBangumiDownloadRecord(mikanId: Int): List<DownloadRecord>

    /**
     * 获取番剧最后保存目录
     */
    fun getBangumiLastSaveDir(mikanId: Int): Pair<String, Boolean>
}

class DebugDownloadDataRepository : DownloadDataRepositoryImpl() {

}

open class DownloadDataRepositoryImpl : DownloadDataRepositoryInterface {
    override suspend fun getBangumiDownloadRecordList(): Map<Int, List<DownloadRecord>> {
        return getBangumiAllDownloadRecords().groupBy {
            it.linkedMikanId
        }
    }

    override suspend fun getBangumiAllDownloadRecords(): List<DownloadRecord> {
        return Databases.downloadRecordService.getAllRecords()
    }

    override suspend fun getBangumiDownloadRecord(mikanId: Int): List<DownloadRecord> {
        return Databases.downloadRecordService.getRecordsByMikanId(mikanId)
    }

    override fun getBangumiLastSaveDir(mikanId: Int): Pair<String, Boolean> {
        var record = Databases.downloadRecordService.getLastSaveDir(mikanId)
        if (record == null) {
            if (mikanId != -1) {
                record = Databases.downloadRecordService.getLastSaveDir(-1)
                record?.apply {
                    if (autoCreateDir) {
                        saveDir = File(saveDir).parent
                    }
                }
            }
        }
        if (record == null || record.saveDir.isEmpty()) {
            return AppSettings.settings.general.bangumiLastSavePath to AppSettings.settings.general.autoCreateDir
        }
        return record.saveDir to record.autoCreateDir
    }
}

object DownloadDataRepository {
    private val instance by lazy {
        DownloadDataRepositoryImpl()
        //DebugDownloadDataRepository()
    }

    fun get(): DownloadDataRepositoryInterface {
        return instance
    }
}