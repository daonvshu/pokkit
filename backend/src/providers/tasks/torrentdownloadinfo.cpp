#include "torrentdownloadinfo.h"
#include "base/utils/misc.h"
#include "base/types.h"

QPair<TorrentStateType, QString> TorrentDisplayInfo::translateState(const BitTorrent::TorrentState &state) {
    static QHash<BitTorrent::TorrentState, QString> statusStringMap = {
            {BitTorrent::TorrentState::Downloading, QStringLiteral("下载中")},
            {BitTorrent::TorrentState::StalledDownloading, QStringLiteral("等待")},
            {BitTorrent::TorrentState::DownloadingMetadata, QStringLiteral("下载元数据")},
            {BitTorrent::TorrentState::ForcedDownloadingMetadata, QStringLiteral("[F] 下载元数据")},
            {BitTorrent::TorrentState::ForcedDownloading, QStringLiteral("[F] 下载元数据")},
            {BitTorrent::TorrentState::Uploading, QStringLiteral("做种")},
            {BitTorrent::TorrentState::StalledUploading, QStringLiteral("做种")},
            {BitTorrent::TorrentState::ForcedUploading, QStringLiteral("[F] 做种")},
            {BitTorrent::TorrentState::QueuedDownloading, QStringLiteral("排队")},
            {BitTorrent::TorrentState::QueuedUploading, QStringLiteral("排队")},
            {BitTorrent::TorrentState::CheckingDownloading, QStringLiteral("校验")},
            {BitTorrent::TorrentState::CheckingUploading, QStringLiteral("校验")},
            {BitTorrent::TorrentState::CheckingResumeData, QStringLiteral("校验恢复数据")},
            {BitTorrent::TorrentState::StoppedDownloading, QStringLiteral("暂停")},
            {BitTorrent::TorrentState::StoppedUploading, QStringLiteral("完成")},
            {BitTorrent::TorrentState::Moving, QStringLiteral("移动中")},
            {BitTorrent::TorrentState::MissingFiles, QStringLiteral("丢失文件")},
            {BitTorrent::TorrentState::Error, QStringLiteral("错误")}
    };

    TorrentStateType stateType;
    switch (state) {
        case BitTorrent::TorrentState::Downloading:
        case BitTorrent::TorrentState::ForcedDownloading:
        case BitTorrent::TorrentState::DownloadingMetadata:
        case BitTorrent::TorrentState::ForcedDownloadingMetadata:
            stateType = TorrentStateType::Downloading;
            break;
        case BitTorrent::TorrentState::StalledDownloading:
            stateType = TorrentStateType::StalledDownloading;
            break;
        case BitTorrent::TorrentState::StalledUploading:
            stateType = TorrentStateType::StalledUploading;
            break;
        case BitTorrent::TorrentState::Uploading:
        case BitTorrent::TorrentState::ForcedUploading:
            stateType = TorrentStateType::Uploading;
            break;
        case BitTorrent::TorrentState::StoppedDownloading:
            stateType = TorrentStateType::Paused;
            break;
        case BitTorrent::TorrentState::StoppedUploading:
            stateType = TorrentStateType::Completed;
            break;
        case BitTorrent::TorrentState::QueuedDownloading:
        case BitTorrent::TorrentState::QueuedUploading:
            stateType = TorrentStateType::Queued;
            break;
        case BitTorrent::TorrentState::CheckingDownloading:
        case BitTorrent::TorrentState::CheckingUploading:
        case BitTorrent::TorrentState::CheckingResumeData:
        case BitTorrent::TorrentState::Moving:
            stateType = TorrentStateType::Checking;
            break;
        case BitTorrent::TorrentState::Unknown:
        case BitTorrent::TorrentState::MissingFiles:
        case BitTorrent::TorrentState::Error:
            stateType = TorrentStateType::Error;
            break;
    }
    return qMakePair(stateType, statusStringMap[state]);
}

QString TorrentDisplayInfo::formatSpeed(int speed) {
    return Utils::Misc::friendlyUnit(speed, true);
}

QString TorrentDisplayInfo::formatEta(qlonglong eta) {
    return Utils::Misc::userFriendlyDuration(eta, MAX_ETA);
}

QString TorrentDisplayInfo::formatSeeds(int seeds, int totalSeeds) {
    return QString("%1/%2").arg(seeds).arg(totalSeeds);
}

QString TorrentDisplayInfo::formatSize(qlonglong size) {
    return Utils::Misc::friendlyUnit(size, false);
}
