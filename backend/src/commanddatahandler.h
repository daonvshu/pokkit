#pragma once

#include <qobject.h>
#include <protocolcodecengine.h>

#include "services/downloadservice.h"

class IdentifyAuthConfirmedCallback {
public:
    virtual void onReadChannelReady() = 0;
};

class CommandDataHandler : public QObject {
    Q_OBJECT

public:
    explicit CommandDataHandler(IdentifyAuthConfirmedCallback* callback, QObject *parent = nullptr);

    void addBuffer(const QByteArray& data);

signals:
    void dataFeedback(const QByteArray& data);

private:
    IdentifyAuthConfirmedCallback* callback;
    protocol_codec::ProtocolCodecEngine codecEngine;

private:
    void onIdentifyAuthRequest(const struct IdentifyAuthRequest& request);
    void onTorrentContentFetchRequest(const TorrentContentFetchRequest& request);
};
