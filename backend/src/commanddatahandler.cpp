#include "commanddatahandler.h"

#include "utils/globalenv.h"

#include "providers/downloadserviceprovider.h"

#include <qtimer.h>

using namespace protocol_codec;

struct IdentifyAuthRequest : DataDumpProtocol<IdentifyAuthRequest> {
    enum {
        Type = 0
    };

    DATA_KEY(QString, role);

    QList<DataReadInterface *> prop() override {
        return { &role };
    }
};

struct ProxyInfoSync : DataDumpProtocol<ProxyInfoSync> {
    enum {
        Type = 1
    };

    DATA_KEY(QString, proxyAddress);
    DATA_KEY(int, proxyPort);

    QList<DataReadInterface *> prop() override {
        return { &proxyAddress, &proxyPort };
    }
};

CommandDataHandler::CommandDataHandler(IdentifyAuthConfirmedCallback* callback, QObject *parent)
    : QObject(parent)
    , callback(callback)
{
    codecEngine.frameDeclare("H(FAFE)S2CV(CRC16)E(FE)");
    codecEngine.setVerifyFlags("SC");

    codecEngine.registerType<JsonCodec<IdentifyAuthRequest>>(this, &CommandDataHandler::onIdentifyAuthRequest);
    codecEngine.registerType<JsonCodec<ProxyInfoSync>>(this, &CommandDataHandler::onProxyInfoSync);

    downloadServiceProvider = new DownloadServiceProvider(this, this);
    //request
    codecEngine.registerType<JsonCodec<TorrentContentFetchRequest>>(downloadServiceProvider, &DownloadServiceProvider::getTorrentContent);
    codecEngine.registerType<TorrentContentFetchCancelRequest>(downloadServiceProvider, &DownloadServiceProvider::getTorrentContentCancel);
    //feedback
    codecEngine.registerType<TorrentContentFetchProgressUpdate, JsonCodec>();
    codecEngine.registerType<TorrentContentFetchResult, JsonCodec>();
}

void CommandDataHandler::addBuffer(const QByteArray &data) {
    codecEngine.appendBuffer(data);
}

void CommandDataHandler::onIdentifyAuthRequest(const IdentifyAuthRequest &request) {
    if (request.role() == "Pokkit/ReadChannel") {
        callback->onReadChannelReady();
    }
}

void CommandDataHandler::onProxyInfoSync(const ProxyInfoSync &request) {
    GlobalEnv::setProxy(request.proxyAddress(), request.proxyPort());
}

void CommandDataHandler::publish(const std::function<QByteArray(protocol_codec::ProtocolCodecEngine &)> &getData) {
    emit dataFeedback(getData(codecEngine));
}
