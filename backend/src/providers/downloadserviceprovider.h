#pragma once

#include <qobject.h>
#include <qpointer.h>

#include "services/downloadservice.h"

#include "tasks/torrentcontentfetchtask.h"

#include "datapublish.h"

class DownloadServiceProvider : public QObject {
public:
    explicit DownloadServiceProvider(DataPublishInterface* publishInterface, QObject *parent = nullptr);

    void getTorrentContent(const TorrentContentFetchRequest& request);

    void getTorrentContentCancel();

private:
    DataPublishInterface* publishInterface;
    QPointer<TorrentContentFetchTask> task;
};
