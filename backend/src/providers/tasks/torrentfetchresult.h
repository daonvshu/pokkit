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
    DATA_KEY(QList<TorrentInfoPathData>, filePaths);
    DATA_KEY(QString, name);
    DATA_KEY(bool, invalid);
    DATA_KEY(int, invalidType); //0: download fail, 1: invalid torrent, 2: already in download list
    DATA_KEY(QString, errorString);

    QList<DataReadInterface *> prop() override {
        return {&filePaths, &name, &invalid, &invalidType, &errorString};
    }

    TorrentInfoData& operator=(const TorrentInfoData& other) {
        filePaths = other.filePaths();
        name = other.name();
        invalid = other.invalid();
        invalidType = other.invalidType();
        errorString = other.errorString();
        return *this;
    }
};

struct TorrentHoldData : public DataDumpInterface {
    DATA_KEY(QString, linkUrl);
    DATA_KEY(QString, linkName);
    DATA_KEY(QString, torrentContent);
    DATA_KEY(TorrentInfoData, linkData);

    QList<DataReadInterface *> prop() override {
        return { &linkUrl, &linkName, &torrentContent, &linkData };
    }

    TorrentHoldData& operator=(const TorrentHoldData& other) {
        linkUrl = other.linkUrl();
        linkName = other.linkName();
        torrentContent = other.torrentContent();
        linkData = other.linkData();
        return *this;
    }
};