#pragma once

#include <qobject.h>
#include <qthreadpool.h>
#include <qrunnable.h>
#include <qnetworkreply.h>
#include <qmutex.h>
#include <qurl.h>
#include <qqueue.h>

#include "base/bittorrent/torrentdescriptor.h"
#include "base/bittorrent/infohash.h"
#include "base/bittorrent/session.h"

#include "torrentfetchresult.h"

class CancelableTorrentDownloadTask : public QObject, public QRunnable {
    Q_OBJECT

public:
    explicit CancelableTorrentDownloadTask(const QString& srcName, const QString& url, QObject *parent = nullptr);

    void run() override;

signals:
    void cancelRequested();
    void finished(const QUrl& url, bool success);

private:
    QString url;
    bool cancelled = false;
    QNetworkReply* reply = nullptr;
    QMutex mutex;

    TorrentInfoData holdData;
    BitTorrent::TorrentDescriptor torrentInfo;

    friend class TorrentContentFetchTask;

private:
    void solveTrackers();
};

class TorrentContentFetchTask : public QObject {
    Q_OBJECT

public:
    explicit TorrentContentFetchTask(QObject *parent = nullptr);

    void start(const QStringList& srcNames, const QStringList& urls);

    void cancelAll();

    QList<TorrentInfoData> getData() const;

signals:
    void progress(int finishedCount, int totalCount);

private:
    QThreadPool threadPool;
    QQueue<CancelableTorrentDownloadTask*> taskQueue;
    QSet<CancelableTorrentDownloadTask*> runningTasks;
    int totalSize = 0;
    int finishedCount = 0;

    QList<TorrentInfoData> cacheData;

private:
    void submit(const QString& srcName, const QString& torrentUrl);
    void taskFinished(const QUrl& url, bool success);
    void tryStartNext();
};
