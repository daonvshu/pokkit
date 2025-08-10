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

struct TorrentStatusList : public DataDumpProtocol<TorrentStatusList> {
    enum {
        Type = 204
    };

    DATA_KEY(QList<TorrentDisplayInfo>, status);

    QList<DataReadInterface *> prop() override {
        return { &status };
    }
};

struct TorrentPauseOrResumeRequest : public DataDumpProtocol<TorrentPauseOrResumeRequest> {
    enum {
        Type = 205
    };

    DATA_KEY(bool, isPause);
    DATA_KEY(bool, isAll);
    DATA_KEY(QStringList, torrentHash);

    QList<DataReadInterface *> prop() override {
        return { &isPause, &isAll, &torrentHash };
    }
};