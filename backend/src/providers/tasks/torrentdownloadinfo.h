#pragma once

#include "utils/datakey.h"
#include "base/bittorrent/torrent.h"

using namespace QDataUtil;

struct TorrentDownloadPath : public DataDumpInterface {
    DATA_KEY(QString, path);
    DATA_KEY(bool, ignored);

    QList<DataReadInterface *> prop() override {
        return { &path, &ignored };
    }

    TorrentDownloadPath& operator=(const TorrentDownloadPath& other) {
        path = other.path();
        ignored = other.ignored();
        return *this;
    }
};

struct TorrentDownloadData : DataDumpInterface {
    DATA_KEY(QString, content);
    DATA_KEY(QList<TorrentDownloadPath>, paths);

    QList<DataReadInterface *> prop() override {
        return { &content, &paths };
    }

    TorrentDownloadData& operator=(const TorrentDownloadData& other) {
        content = other.content();
        paths = other.paths();
        return *this;
    }
};

enum class TorrentStateType {
    Downloading = 0,
    StalledDownloading,
    StalledUploading,
    Uploading,
    Paused,
    Completed,
    Queued,
    Checking,
    Error,
};

enum TorrentDownloadStateType {
    Downloading = 0,
    Uploading,
    Error,
};

struct TorrentDisplayInfo : DataDumpInterface {
    DATA_KEY(QString, torrentHash);
    DATA_KEY(int, state); //TorrentStateType
    DATA_KEY(int, downloadState); //TorrentDownloadStateType
    DATA_KEY(QString, stateString);
    DATA_KEY(QString, name);
    DATA_KEY(QString, torrentName);
    DATA_KEY(QString, speed);
    DATA_KEY(QString, eta);
    DATA_KEY(QString, seeds);
    DATA_KEY(QString, downloadedSize);
    DATA_KEY(QString, totalSize);
    DATA_KEY(qreal, progress);
    DATA_KEY(QString, filePath);
    DATA_KEY(qint64, createTime);
    DATA_KEY(QString, group);
    DATA_KEY(qint64, timePoint);
    DATA_KEY(QString, torrentLinkUrl);

    QList<DataReadInterface *> prop() override {
        return { &torrentHash, &state, &downloadState, &stateString, &name, &torrentName, &speed, &eta, &seeds, &downloadedSize, &totalSize,
                 &progress, &filePath, &createTime, &group, &timePoint, &torrentLinkUrl };
    }

    TorrentDisplayInfo& operator=(const TorrentDisplayInfo& other) {
        torrentHash = other.torrentHash();
        state = other.state();
        downloadState = other.downloadState();
        stateString = other.stateString();
        name = other.name();
        torrentName = other.torrentName();
        speed = other.speed();
        eta = other.eta();
        seeds = other.seeds();
        downloadedSize = other.downloadedSize();
        totalSize = other.totalSize();
        progress = other.progress();
        filePath = other.filePath();
        createTime = other.createTime();
        group = other.group();
        timePoint = other.timePoint();
        torrentLinkUrl = other.torrentLinkUrl();
        return *this;
    }

    static QPair<TorrentStateType, QString> translateState(const BitTorrent::TorrentState& state);

    static QString formatSpeed(int speed);

    static QString formatEta(qlonglong eta);

    static QString formatSeeds(int seeds, int totalSeeds);

    static QString formatSize(qlonglong size);
};

struct TorrentStatusList : public DataDumpInterface {
    DATA_KEY(QList<TorrentDisplayInfo>, status);

    QList<DataReadInterface *> prop() override {
        return { &status };
    }
};