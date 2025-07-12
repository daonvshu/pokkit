#include "commanddatahandler.h"

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

CommandDataHandler::CommandDataHandler(IdentifyAuthConfirmedCallback* callback, QObject *parent)
    : QObject(parent)
    , callback(callback)
{
    codecEngine.frameDeclare(QLatin1String("H(FAFE)S2CV(CRC16)E(FE)"));
    codecEngine.setVerifyFlags(QLatin1String("SC"));

    codecEngine.registerType<JsonCodec<IdentifyAuthRequest>>(this, &CommandDataHandler::onIdentifyAuthRequest);
    codecEngine.registerType<JsonCodec<TorrentContentFetchRequest>>(this, &CommandDataHandler::onTorrentContentFetchRequest);
}

void CommandDataHandler::addBuffer(const QByteArray &data) {
    codecEngine.appendBuffer(data);
}

void CommandDataHandler::onTorrentContentFetchRequest(const TorrentContentFetchRequest &request) {
    qDebug() << "torrent content fetch request urls:" << request.torrentUrls();
    QTimer::singleShot(100, this, [this] {
        emit dataFeedback("feedback test!");
    });
}

void CommandDataHandler::onIdentifyAuthRequest(const IdentifyAuthRequest &request) {
    if (request.role() == QLatin1String("Pokkit/ReadChannel")) {
        callback->onReadChannelReady();
    }
}
