#pragma once

#include <qobject.h>
#include <protocolcodecengine.h>

#include "services/downloadservice.h"

#include "datapublish.h"

class DownloadServiceProvider;
class IdentifyAuthConfirmedCallback {
public:
    virtual void onReadChannelReady() = 0;
};

class CommandDataHandler : public QObject, public DataPublishInterface {
    Q_OBJECT

public:
    explicit CommandDataHandler(IdentifyAuthConfirmedCallback* callback, QObject *parent = nullptr);

    void addBuffer(const QByteArray& data);

signals:
    void dataFeedback(const QByteArray& data);

private:
    IdentifyAuthConfirmedCallback* callback;
    protocol_codec::ProtocolCodecEngine codecEngine;

    DownloadServiceProvider* downloadServiceProvider;

private:
    void onIdentifyAuthRequest(const struct IdentifyAuthRequest& request);
    void onProxyInfoSync(const struct ProxyInfoSync& request);
    void onRequestOpenDir(const struct RequestOpenDir& request);

    void publish(const std::function<QByteArray (protocol_codec::ProtocolCodecEngine &)> &) override;

    void sendBufferTest(const TorrentContentFetchRequest &request);
};
