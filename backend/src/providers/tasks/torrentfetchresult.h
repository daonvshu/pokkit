#pragma once

#include "utils/datakey.h"

using namespace QDataUtil;

struct TorrentInfoPathData : public DataDumpInterface {
    DATA_KEY(QString, path);
    DATA_KEY(qint64, size);

    QList<DataReadInterface *> prop() override {
        return {&path, &size};
    }

    TorrentInfoPathData& operator=(const TorrentInfoPathData& other) {
        path = other.path();
        size = other.size();
        return *this;
    }
};

struct TorrentInfoData : public DataDumpInterface {
    DATA_KEY(QString, srcName);
    DATA_KEY(QString, linkUrl);
    DATA_KEY(QString, linkName);
    DATA_KEY(QString, torrentContent);
    DATA_KEY(bool, invalid);
    DATA_KEY(int, invalidType); //0: download fail, 1: invalid torrent, 2: already in download list
    DATA_KEY(QString, errorString);
    DATA_KEY(QList<TorrentInfoPathData>, filePaths);

    QList<DataReadInterface *> prop() override {
        return { &srcName, &linkUrl, &linkName, &torrentContent, &invalid, &invalidType, &errorString, &filePaths };
    }

    TorrentInfoData& operator=(const TorrentInfoData& other) {
        srcName = other.srcName();
        linkUrl = other.linkUrl();
        linkName = other.linkName();
        torrentContent = other.torrentContent();
        invalid = other.invalid();
        invalidType = other.invalidType();
        errorString = other.errorString();
        filePaths = other.filePaths();
        return *this;
    }
};