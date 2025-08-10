package com.daonvshu.shared.backendservice.bean

data class TorrentDownloadPath(
    val path: String,
    val ignored: Boolean
)

data class TorrentDownloadInfo(
    val content: String,
    val paths: List<TorrentDownloadPath>,
)

enum class TorrentStateType {
    Downloading,
    StalledDownloading,
    StalledUploading,
    Uploading,
    Paused,
    Completed,
    Queued,
    Checking,
    Error,

    ;

    companion object {
        fun of(index: Int): TorrentStateType {
            require(index >= 0 && index < entries.size)
            return entries[index]
        }
    }
}

enum class TorrentDownloadStateType {
    Downloading,
    Uploading,
    Error,

    ;

    companion object {
        fun of(index: Int): TorrentDownloadStateType {
            require(index >= 0 && index < entries.size)
            return entries[index]
        }
    }
}

data class TorrentDisplayInfo(
    val torrentHash: String,
    val state: Int, //TorrentStateType
    val downloadState: Int, //TorrentDownloadStateType
    val stateString: String,
    val speed: String,
    val eta: String,
    val seeds: String,
    val downloadedSize: String,
    val totalSize: String,
    val progress: Double,
    val filePath: String,
    val createTime: Long,
)