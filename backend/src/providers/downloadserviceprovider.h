#pragma once

#include <qobject.h>
#include <qpointer.h>

#include "services/downloadservice.h"

#include "tasks/torrentcontentfetchtask.h"

#include "datapublish.h"

class DownloadServiceProvider : public QObject {
public:
    explicit DownloadServiceProvider(DataPublishInterface* publishInterface, QObject *parent = nullptr);

    void publishTorrentStatus();

    void getTorrentContent(const TorrentContentFetchRequest& request);

    void getTorrentContentCancel();

    void beginDownload(const TorrentDownloadRequest& request);

    void refreshAllTorrentsStatus();

    void onTorrentPauseOrResumeRequest(const TorrentPauseOrResumeRequest& request);

    void onTorrentRemoveRequest(const TorrentRemoveRequest& request);

    void onTorrentContentFetch2Request(const TorrentContentFetch2Request& request);

    void getGlobalSpeed();

    void updateGlobalSpeed(const GlobalSpeedLimitUpdateRequest& request);

    void getTrackersRequest();

    void updateTrackersRequest(const TrackerListUpdateRequest& request);

private:
    DataPublishInterface* publishInterface;
    QPointer<TorrentContentFetchTask> task;
    BitTorrent::TorrentDescriptor lastDescriptor;
    qint64 lastDescriptorRequestId = -1;

private slots:
    void onTorrentUpdated(const QVector<BitTorrent::Torrent *> &torrents);
    void onTorrentStatusUpdated();
    void onMetadataDownloaded(const BitTorrent::TorrentInfo &metadata);

private:
    void sendTorrentResult(qint64 requestId, const QList<TorrentInfoData>& data);
    bool isTorrentExist(const BitTorrent::TorrentDescriptor& descriptor);
};
