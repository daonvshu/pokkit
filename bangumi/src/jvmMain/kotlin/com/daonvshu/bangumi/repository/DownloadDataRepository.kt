package com.daonvshu.bangumi.repository

import com.daonvshu.shared.database.Databases
import com.daonvshu.shared.database.schema.DownloadRecord

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