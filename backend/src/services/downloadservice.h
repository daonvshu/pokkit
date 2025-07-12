#pragma once

#include "dumputil.h"

struct TorrentContentFetchRequest: DataDumpProtocol<TorrentContentFetchRequest> {
    enum {
        Type = 1
    };

    DATA_KEY(QString, proxyAddress);
    DATA_KEY(int, proxyPort);
    DATA_KEY(QStringList, torrentUrls);

    QList<DataReadInterface *> prop() override {
        return {
            &proxyAddress,
            &proxyPort,
            &torrentUrls,
        };
    }
};