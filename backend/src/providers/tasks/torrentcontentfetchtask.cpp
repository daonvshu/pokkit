#include "torrentcontentfetchtask.h"

#include "utils/globalenv.h"

#include <qeventloop.h>
#include <qdir.h>

CancelableTorrentDownloadTask::CancelableTorrentDownloadTask(const QString &url, QObject *parent)
    : QObject(parent)
    , url(url)
{
    setAutoDelete(false);
    holdData.linkData().invalid = true;
    holdData.linkData().invalidType = 0;
}

void CancelableTorrentDownloadTask::run() {
    QNetworkRequest request(url);
    {
        QMutexLocker locker(&mutex);
        if (cancelled) {
            emit finished(url, false);
            return;
        }
    }
    auto manager = GlobalEnv::getNetworkAccessManager();
    reply = manager->get(request);
    connect(this, &CancelableTorrentDownloadTask::cancelRequested, this, [this] {
        QMutexLocker locker(&mutex);
        cancelled = true;
        if (reply && reply->isRunning()) {
            reply->abort();
        }
    });

    QEventLoop loop;
    connect(reply, &QNetworkReply::finished, &loop, &QEventLoop::quit);
    loop.exec();

    bool success = false;
    QString errorString;

    {
        QMutexLocker locker(&mutex);
        if (cancelled) {
            emit finished(url, false);
            if (reply) reply->deleteLater();
            reply = nullptr;
            manager->deleteLater();
            return;
        }
    }

    auto& linkData = holdData.linkData();
    if (reply->error() == QNetworkReply::NoError) {
        auto sourceData = reply->readAll();
        holdData.torrentContent = QString::fromLatin1(sourceData.toBase64());

        if (!sourceData.isEmpty()) {
            auto content = BitTorrent::TorrentDescriptor::load(sourceData);
            if (content) {
                success = true;
                torrentInfo = content.value();
                solveTrackers();
            } else {
                linkData.invalidType = 1;
                errorString = content.error();
            }
        }
    } else {
        errorString = reply->errorString();
    }
    linkData.invalid = !success;
    linkData.errorString = errorString;
    reply->deleteLater();
    manager->deleteLater();

    emit finished(url, success);
}

void CancelableTorrentDownloadTask::solveTrackers() {
    bool isExist = false;
    auto id = BitTorrent::TorrentID::fromInfoHash(torrentInfo.infoHash());
    if (BitTorrent::Session::instance()->isKnownTorrent(torrentInfo.infoHash())) {
        auto torrent = BitTorrent::Session::instance()->findTorrent(torrentInfo.infoHash());
        if (torrent) {
            if (!torrent->isPrivate()) {
                torrent->addTrackers(torrentInfo.trackers());
                torrent->addUrlSeeds(torrentInfo.urlSeeds());
            } {
                //update trackers here!
            }
            isExist = true;
        }
    }

    holdData.linkUrl = url;
    holdData.linkName = torrentInfo.name();
    for (int i = 0; i < torrentInfo.info()->filesCount(); i++) {
        TorrentInfoPathData pathData;
        pathData.path = torrentInfo.info()->filePath(i).toString();
        pathData.size = torrentInfo.info()->fileSize(i);
        holdData.linkData().filePaths() << pathData;
    }
    if (isExist) {
        holdData.linkData().invalidType = 2;
    }
}

TorrentContentFetchTask::TorrentContentFetchTask(QObject *parent)
    : QObject(parent)
{
    threadPool.setMaxThreadCount(5);
}

void TorrentContentFetchTask::start(const QStringList &urls) {
    totalSize = urls.size();
    for (const auto &url : urls) {
        submit(url);
    }
}

void TorrentContentFetchTask::cancelAll() {
    // 取消队列中的任务
    while (!taskQueue.isEmpty()) {
        auto task = taskQueue.dequeue();
        task->deleteLater();
    }
    taskQueue.clear();

    // 取消正在运行的任务
    for (auto task : runningTasks) {
        task->cancelRequested();
    }
    runningTasks.clear();
}

void TorrentContentFetchTask::submit(const QString &torrentUrl) {
    auto task = new CancelableTorrentDownloadTask(torrentUrl);
    taskQueue.enqueue(task);
    connect(task, &CancelableTorrentDownloadTask::finished, this, &TorrentContentFetchTask::taskFinished);

    tryStartNext();
}

void TorrentContentFetchTask::taskFinished(const QUrl &url, bool success) {
    auto senderTask = qobject_cast<CancelableTorrentDownloadTask *>(sender());
    if (senderTask) {
        cacheData << senderTask->holdData;
        runningTasks.remove(senderTask);
        senderTask->deleteLater();
    }
    finishedCount++;
    emit progress(finishedCount, totalSize);
    tryStartNext();
}

void TorrentContentFetchTask::tryStartNext() {
    while (runningTasks.size() < threadPool.maxThreadCount() && !taskQueue.isEmpty()) {
        auto task = taskQueue.dequeue();
        runningTasks.insert(task);
        threadPool.start(task);
    }
}

QList<TorrentHoldData> TorrentContentFetchTask::getData() const {
    return cacheData;
}



