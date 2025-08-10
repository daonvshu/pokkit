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

    void onTorrentPauseOrResumeRequest(const TorrentPauseOrResumeRequest& request);

private:
    DataPublishInterface* publishInterface;
    QPointer<TorrentContentFetchTask> task;

private slots:
    void onTorrentUpdated(const QVector<BitTorrent::Torrent *> &torrents);
};
