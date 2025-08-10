package com.daonvshu.bangumi.repository

import com.daonvshu.shared.database.Databases
import com.daonvshu.shared.database.schema.DownloadRecord

interface DownloadDataRepositoryInterface {
    /**
     * 获取番剧记录列表，linkId -> List<Data>
     */
    suspend fun getBangumiDownloadRecordList(): Map<Int, List<DownloadRecord>>
}

class DebugDownloadDataRepository : DownloadDataRepositoryImpl() {

}

open class DownloadDataRepositoryImpl : DownloadDataRepositoryInterface {
    override suspend fun getBangumiDownloadRecordList(): Map<Int, List<DownloadRecord>> {
        return Databases.downloadRecordService.getAllRecords().groupBy {
            it.linkedMikanId
        }
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