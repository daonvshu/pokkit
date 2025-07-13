#include "downloadserviceprovider.h"

#include <qdebug.h>

DownloadServiceProvider::DownloadServiceProvider(DataPublishInterface* publishInterface, QObject *parent)
    : QObject(parent)
    , publishInterface(publishInterface)
{
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

    task->start(request.torrentUrls());
}

void DownloadServiceProvider::getTorrentContentCancel() {
    if (task) {
        task->cancelAll();
        task->deleteLater();
    }
}
