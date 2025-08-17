#pragma once

#include "dumputil.h"

#include "providers/tasks/torrentfetchresult.h"
#include "providers/tasks/torrentdownloadinfo.h"

struct TorrentContentFetchRequest: DataDumpProtocol<TorrentContentFetchRequest> {
    enum {
        Type = 100
    };

    DATA_KEY(qint64, requestId);
    DATA_KEY(QStringList, torrentSrcNames);
    DATA_KEY(QStringList, torrentUrls);

    QList<DataReadInterface *> prop() override {
        return {
            &requestId,
            &torrentSrcNames,
            &torrentUrls,
        };
    }
};

#define TorrentContentFetchCancelRequest 101

struct TorrentContentFetchProgressUpdate: DataDumpProtocol<TorrentContentFetchProgressUpdate> {
    enum {
        Type = 200
    };

    DATA_KEY(qint64, requestId);
    DATA_KEY(int, finishedCount);
    DATA_KEY(int, totalCount);

    QList<DataReadInterface *> prop() override {
        return {
            &requestId,
            &finishedCount,
            &totalCount,
        };
    }
};

struct TorrentContentFetchResult: DataDumpProtocol<TorrentContentFetchResult> {
    enum {
        Type = 201
    };

    DATA_KEY(qint64, requestId);
    DATA_KEY(QList<TorrentInfoData>, data);

    QList<DataReadInterface *> prop() override {
        return {
            &requestId, &data,
        };
    }
};

struct RequestOpenDir : DataDumpProtocol<RequestOpenDir> {
    enum {
        Type = 202
    };

    DATA_KEY(QStringList, paths);

    QList<DataReadInterface *> prop() override {
        return {
            &paths,
        };
    }
};

struct TorrentDownloadRequest : DataDumpProtocol<TorrentDownloadRequest> {
    enum {
        Type = 203
    };

    DATA_KEY(QString, savePath);
    DATA_KEY(QList<TorrentDownloadData>, data);

    QList<DataReadInterface *> prop() override {
        return {
            &savePath,
            &data,
        };
    }
};

struct TorrentPauseOrResumeRequest : public DataDumpProtocol<TorrentPauseOrResumeRequest> {
    enum {
        Type = 204
    };

    DATA_KEY(bool, isPause);
    DATA_KEY(bool, isAll);
    DATA_KEY(QStringList, torrentHash);

    QList<DataReadInterface *> prop() override {
        return { &isPause, &isAll, &torrentHash };
    }
};

struct TorrentRemoveRequest : public DataDumpProtocol<TorrentRemoveRequest> {
    enum {
        Type = 205
    };

    DATA_KEY(bool, removeSrcFile);
    DATA_KEY(QStringList, torrentHash);

    QList<DataReadInterface *> prop() override {
        return { &removeSrcFile, &torrentHash };
    }
};

#define TorrentStatusRefreshRequest 206

struct TorrentContentFetch2Request : public DataDumpProtocol<TorrentContentFetch2Request> {
    enum {
        Type = 207
    };

    DATA_KEY(qint64, requestId);
    DATA_KEY(int, type); //0: torrent file 1: magnet url
    DATA_KEY(QString, target); //torrent file path or magnet url

    QList<DataReadInterface *> prop() override {
        return { &requestId, &type, &target };
    }
};

struct TorrentStatusList : public DataDumpProtocol<TorrentStatusList> {
    enum {
        Type = 301
    };

    DATA_KEY(QList<TorrentDisplayInfo>, status);

    QList<DataReadInterface *> prop() override {
        return { &status };
    }
};

struct TorrentSpeedUpdated : public DataDumpProtocol<TorrentSpeedUpdated> {
    enum {
        Type = 302
    };

    DATA_KEY(QString, downloadSpeed);
    DATA_KEY(QString, uploadSpeed);

    QList<DataReadInterface *> prop() override {
        return { &downloadSpeed, &uploadSpeed };
    }
};