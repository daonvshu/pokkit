#pragma once

#include "dumputil.h"

#include "providers/tasks/torrentfetchresult.h"

struct TorrentContentFetchRequest: DataDumpProtocol<TorrentContentFetchRequest> {
    enum {
        Type = 100
    };

    DATA_KEY(qint64, requestId);
    DATA_KEY(QStringList, torrentUrls);

    QList<DataReadInterface *> prop() override {
        return {
            &requestId,
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
    DATA_KEY(QList<TorrentHoldData>, data);

    QList<DataReadInterface *> prop() override {
        return {
            &requestId, &data,
        };
    }
};