#include "downloadserviceprovider.h"

#include <qdebug.h>

DownloadServiceProvider::DownloadServiceProvider(DataPublishInterface* publishInterface, QObject *parent)
    : QObject(parent)
    , publishInterface(publishInterface)
{
}

void DownloadServiceProvider::publishTorrentStatus() {
    connect(BitTorrent::Session::instance(), &BitTorrent::Session::torrentsUpdated, this, &DownloadServiceProvider::onTorrentUpdated);
}

void DownloadServiceProvider::getTorrentContent(const TorrentContentFetchRequest &request) {
    qInfo() << "torrent content fetch request urls:" << request.torrentUrls();

    auto requestId = request.requestId();
    if (task) {
        task->cancelAll();
        task->deleteLater();
    }
    task = new TorrentContentFetchTask(this);
    connect(task, &TorrentContentFetchTask::progress, this, [this, requestId] (int finishedCount, int totalCount) {
        TorrentContentFetchProgressUpdate progressData;
        progressData.requestId = requestId;
        progressData.finishedCount = finishedCount;
        progressData.totalCount = totalCount;
        publishInterface->publish([&] (ProtocolCodecEngine& codec) {
            return codec.encode(progressData);
        });
        qInfo() << QString("post progress: %1/%2").arg(finishedCount).arg(totalCount);

        if (finishedCount == totalCount) {
            TorrentContentFetchResult fetchResult;
            fetchResult.requestId = requestId;
            fetchResult.data = task->getData();
            publishInterface->publish([&] (ProtocolCodecEngine& codec) {
                return codec.encode(fetchResult);
            });
            qInfo() << "fetch finished, post result...";
        }
    });

    task->start(request.torrentSrcNames(), request.torrentUrls());
}

void DownloadServiceProvider::getTorrentContentCancel() {
    if (task) {
        task->cancelAll();
        task->deleteLater();
    }
}

void DownloadServiceProvider::beginDownload(const TorrentDownloadRequest& request) {
    for (const auto& data : request.data()) {
        auto params = BitTorrent::AddTorrentParams();

        QVector<BitTorrent::DownloadPriority> filePriorities;
        for (const auto& path : data.paths()) {
            params.filePaths.append(Path(path.path()));
            filePriorities.append(path.ignored() ? BitTorrent::DownloadPriority::Ignored : BitTorrent::DownloadPriority::Normal);
        }
        params.filePriorities = filePriorities;

        params.contentLayout = BitTorrent::TorrentContentLayout::Original;
        params.useAutoTMM = false;
        params.savePath = Path(request.savePath());
        params.useDownloadPath = false;
        auto sourceData = QByteArray::fromBase64(data.content().toLatin1());
        auto torrentInfo = BitTorrent::TorrentDescriptor::load(sourceData);
        Q_ASSERT(torrentInfo);
        BitTorrent::Session::instance()->addTorrent(torrentInfo.value(), params);
    }
}

void DownloadServiceProvider::onTorrentUpdated(const QVector<BitTorrent::Torrent *> &torrents) {
    TorrentStatusList statusList;

    for (const auto& torrent : torrents) {
        TorrentDisplayInfo displayInfo;

        auto state = TorrentDisplayInfo::translateState(torrent->state());
        displayInfo.torrentHash = torrent->infoHash().v1().toString();

        //if (!cachedDownloadInfo.contains(displayInfo.torrentHash())) {
        //    cachedDownloadInfo[displayInfo.torrentHash()] = DownloadingTorrentModel::readByHash(displayInfo.torrentHash());
        //}
        //const auto& downloadingInfo = cachedDownloadInfo[displayInfo.torrentHash()];
        //if (downloadingInfo.getBangumiName().isEmpty()) {
        //    displayInfo.group = QStringLiteral("[未分组]");
        //    displayInfo.timePoint = 0;
        //} else {
        //    displayInfo.group = downloadingInfo.getBangumiName();
        //    displayInfo.timePoint = downloadingInfo.getTimePoint();
        //}

        displayInfo.state = (int)state.first;

        static QList<TorrentStateType> downloadingStates = {
                TorrentStateType::Downloading,
                TorrentStateType::StalledDownloading,
                TorrentStateType::Queued,
                TorrentStateType::Checking,
                TorrentStateType::Paused,
        };
        static QList<TorrentStateType> uploadingStates = {
                TorrentStateType::Uploading,
                TorrentStateType::StalledUploading,
                TorrentStateType::Completed,
        };
        if (downloadingStates.contains(state.first)) {
            displayInfo.downloadState = TorrentDownloadStateType::Downloading;
        } else if (uploadingStates.contains(state.first)) {
            displayInfo.downloadState = TorrentDownloadStateType::Uploading;
        } else {
            displayInfo.downloadState = TorrentDownloadStateType::Error;
        }

        displayInfo.stateString = state.second + (state.first == TorrentStateType::Error ? (torrent->error().isEmpty() ? QString() : (": " + torrent->error())) : QString());
        displayInfo.name = torrent->name();
        displayInfo.torrentName = ""; //downloadingInfo.getTorrentSrcName();
        displayInfo.speed = TorrentDisplayInfo::formatSpeed(state.first == TorrentStateType::Uploading ||
                                                            state.first == TorrentStateType::StalledUploading ? torrent->uploadPayloadRate() :
                                                            torrent->downloadPayloadRate());
        displayInfo.eta = TorrentDisplayInfo::formatEta(torrent->eta());
        displayInfo.seeds = TorrentDisplayInfo::formatSeeds(torrent->seedsCount(), torrent->totalSeedsCount());
        displayInfo.downloadedSize = TorrentDisplayInfo::formatSize(torrent->totalDownload());
        displayInfo.totalSize = TorrentDisplayInfo::formatSize(torrent->wantedSize());
        displayInfo.progress = torrent->totalDownload() * 1.0 / torrent->wantedSize();
        if (qIsNaN(displayInfo.progress())) {
            displayInfo.progress = 0;
        }
        displayInfo.filePath = torrent->savePath().toString();
        displayInfo.createTime = torrent->addedTime().toMSecsSinceEpoch();
        displayInfo.torrentLinkUrl = ""; //downloadingInfo.getExternal() == 0 ? downloadingInfo.getTorrentLink() : QString();

        statusList.status().append(displayInfo);
    }

    qDebug() << "torrent download status updated:" << statusList.dumpToJson();
}
