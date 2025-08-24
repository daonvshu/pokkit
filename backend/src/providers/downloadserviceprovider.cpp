#include "downloadserviceprovider.h"

#include "base/bittorrent/sessionstatus.h"

#include <qfile.h>
#include <qdebug.h>

DownloadServiceProvider::DownloadServiceProvider(DataPublishInterface* publishInterface, QObject *parent)
    : QObject(parent)
    , publishInterface(publishInterface)
{
}

void DownloadServiceProvider::publishTorrentStatus() {
    connect(BitTorrent::Session::instance(), &BitTorrent::Session::torrentsUpdated, this, &DownloadServiceProvider::onTorrentUpdated);
    connect(BitTorrent::Session::instance(), &BitTorrent::Session::statsUpdated, this, &DownloadServiceProvider::onTorrentStatusUpdated);
    connect(BitTorrent::Session::instance(), &BitTorrent::Session::metadataDownloaded, this, &DownloadServiceProvider::onMetadataDownloaded);
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
            sendTorrentResult(requestId, task->getData());
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

void DownloadServiceProvider::onTorrentUpdated(const QVector<BitTorrent::Torrent *>&) {
    TorrentStatusList statusList;

    QVector<BitTorrent::Torrent *> torrentsStopUploading;
    auto torrentAll = BitTorrent::Session::instance()->torrents();
    for (const auto& torrent : torrentAll) {
        TorrentDisplayInfo displayInfo;

        auto state = TorrentDisplayInfo::translateState(torrent->state());
        displayInfo.torrentHash = torrent->infoHash().v1().toString();
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

        statusList.status().append(displayInfo);
    }

    //qDebug() << "torrent download status updated:" << statusList.dumpToJson();
    publishInterface->publish([&] (ProtocolCodecEngine& codec) {
        return codec.encode(statusList);
    });
}

void DownloadServiceProvider::refreshAllTorrentsStatus() {
    onTorrentUpdated({});
}

void DownloadServiceProvider::onTorrentPauseOrResumeRequest(const TorrentPauseOrResumeRequest &request) {
    auto session = BitTorrent::Session::instance();
    for (const auto& torrent : session->torrents()) {
        auto torrentHash = torrent->infoHash().v1().toString();
        if (request.isAll() || request.torrentHash().contains(torrentHash)) {
            if (request.isPause()) {
                torrent->stop();
            } else {
                torrent->start();
            }
        }
    }
}

void DownloadServiceProvider::onTorrentStatusUpdated() {
    const auto& sessionStatus = BitTorrent::Session::instance()->status();
    TorrentSpeedUpdated speedUpdated;
    speedUpdated.downloadSpeed = TorrentDisplayInfo::formatSpeed(sessionStatus.payloadDownloadRate);
    speedUpdated.uploadSpeed = TorrentDisplayInfo::formatSpeed(sessionStatus.payloadUploadRate);
    publishInterface->publish([&] (ProtocolCodecEngine& codec) {
        return codec.encode(speedUpdated);
    });
}

void DownloadServiceProvider::onMetadataDownloaded(const BitTorrent::TorrentInfo &metadata) {
    TorrentInfoData data;
    if (!metadata.isValid()) {
        data.invalid = true;
        data.invalidType = 1;
    } else {
        lastDescriptor.setTorrentInfo(metadata);
        data.torrentInfoHash = lastDescriptor.infoHash().v1().toString();
        data.linkName = lastDescriptor.name();
        for (int i = 0; i < metadata.filesCount(); i++) {
            TorrentInfoPathData pathData;
            pathData.path = metadata.filePath(i).toString();
            pathData.size = metadata.fileSize(i);
            data.filePaths() << pathData;
        }
        auto result = lastDescriptor.saveToBuffer();
        if (!result) {
            data.invalidType = 2;
        } else {
            data.torrentContent = result.value().toBase64();
            data.invalid = false;
        }
    }
    sendTorrentResult(lastDescriptorRequestId, { data });
}

void DownloadServiceProvider::onTorrentRemoveRequest(const TorrentRemoveRequest &request) {
    auto session = BitTorrent::Session::instance();
    for (const auto& torrent : session->torrents()) {
        auto torrentHash = torrent->infoHash().v1().toString();
        if (request.torrentHash().contains(torrentHash)) {
            session->removeTorrent(torrent->id(), request.removeSrcFile() ?
                BitTorrent::TorrentRemoveOption::RemoveContent : BitTorrent::TorrentRemoveOption::KeepContent);
        }
    }
}

void DownloadServiceProvider::onTorrentContentFetch2Request(const TorrentContentFetch2Request &request) {

    TorrentInfoData data;
    data.invalid = true;
    data.invalidType = 1;

    if (request.type() == 0) {
        //load from file
        do {
            QFile file(request.target());
            data.srcName = file.fileName();
            if (!file.open(QIODevice::ReadOnly)) {
                break;
            }
            auto fileContent = file.readAll();
            data.torrentContent = fileContent.toBase64();

            auto content = BitTorrent::TorrentDescriptor::load(fileContent);
            if (!content) {
                break;
            }
            auto torrentInfo = content.value();
            if (isTorrentExist(torrentInfo)) {
                data.invalidType = 2;
                break;
            }

            data.torrentInfoHash = torrentInfo.infoHash().v1().toString();
            data.linkName = torrentInfo.name();
            for (int i = 0; i < torrentInfo.info()->filesCount(); i++) {
                TorrentInfoPathData pathData;
                pathData.path = torrentInfo.info()->filePath(i).toString();
                pathData.size = torrentInfo.info()->fileSize(i);
                data.filePaths() << pathData;
            }
            data.invalid = false;
        } while (false);
        sendTorrentResult(request.requestId(), { data });
    } else {
        auto descr = BitTorrent::TorrentDescriptor::parse(request.target());
        if (!descr) {
            data.invalidType = 0;
            sendTorrentResult(request.requestId(), { data });
            return;
        }

        auto torrentInfo = descr.value();
        if (isTorrentExist(torrentInfo)) {
            data.invalidType = 2;
            sendTorrentResult(request.requestId(), { data });
            return;
        }
        lastDescriptor = torrentInfo;
        lastDescriptorRequestId = request.requestId();
        BitTorrent::Session::instance()->downloadMetadata(torrentInfo);
    }
}

void DownloadServiceProvider::sendTorrentResult(qint64 requestId, const QList<TorrentInfoData> &data) {
    TorrentContentFetchResult fetchResult;
    fetchResult.requestId = requestId;
    fetchResult.data = data;
    publishInterface->publish([&] (ProtocolCodecEngine& codec) {
        return codec.encode(fetchResult);
    });
}

bool DownloadServiceProvider::isTorrentExist(const BitTorrent::TorrentDescriptor &descriptor) {
    bool isExist = false;
    auto id = BitTorrent::TorrentID::fromInfoHash(descriptor.infoHash());
    if (BitTorrent::Session::instance()->isKnownTorrent(descriptor.infoHash())) {
        auto torrent = BitTorrent::Session::instance()->findTorrent(descriptor.infoHash());
        if (torrent) {
            if (!torrent->isPrivate()) {
                torrent->addTrackers(descriptor.trackers());
                torrent->addUrlSeeds(descriptor.urlSeeds());
            } {
                //update trackers here!
            }
            isExist = true;
        }
    }
    return isExist;
}

void DownloadServiceProvider::getGlobalSpeed() {
    GlobalSpeedLimitFeedback data;
    data.download = BitTorrent::Session::instance()->globalDownloadSpeedLimit();
    data.upload = BitTorrent::Session::instance()->globalUploadSpeedLimit();
    publishInterface->publish([&] (ProtocolCodecEngine& codec) {
        return codec.encode(data);
    });
}

void DownloadServiceProvider::updateGlobalSpeed(const GlobalSpeedLimitUpdateRequest &request) {
    BitTorrent::Session::instance()->setGlobalDownloadSpeedLimit(request.download());
    BitTorrent::Session::instance()->setGlobalUploadSpeedLimit(request.upload());
}

void DownloadServiceProvider::getTrackersRequest() {
    TrackerListFeedback data;
    data.enabled = BitTorrent::Session::instance()->isTrackerEnabled();
    data.trackers = BitTorrent::Session::instance()->additionalTrackers();
    publishInterface->publish([&] (ProtocolCodecEngine& codec) {
        return codec.encode(data);
    });
}

void DownloadServiceProvider::updateTrackersRequest(const TrackerListUpdateRequest &request) {
    BitTorrent::Session::instance()->setTrackerEnabled(request.enable());
    BitTorrent::Session::instance()->setAdditionalTrackers(request.trackers());
}

