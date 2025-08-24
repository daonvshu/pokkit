#include "pokkitbackendservice.h"

#include "base/bittorrent/session.h"
#include "base/exceptions.h"
#include "base/logger.h"
#include "base/profile.h"
#include "base/preferences.h"
#include "base/net/downloadmanager.h"
#include "base/net/proxyconfigurationmanager.h"
#include "base/net/geoipmanager.h"
#include "base/bittorrent/infohash.h"
#include "base/torrentfileswatcher.h"

#include <qdebug.h>

PokkitBackendService::PokkitBackendService(int argc, char **argv)
    : QtSingleCoreApplication(argc, argv)
{
    connect(&server, &QLocalServer::newConnection, this, &PokkitBackendService::newConnection);

    commandDataHandler = new CommandDataHandler(this);
    connect(commandDataHandler, &CommandDataHandler::dataFeedback, this, [&] (const QByteArray& data) {
        if (writeChannelSocket && writeChannelSocket->isOpen()) {
            writeChannelSocket->write(data);
        }
    });
}

void PokkitBackendService::start() {
    if (server.isListening()) {
        return;
    }
    QString pipeName("pokkit_backend_pipe");
    QLocalServer::removeServer(pipeName);
    server.setSocketOptions(QLocalServer::UserAccessOption);
    if (!server.listen(pipeName)) {
        qCritical() << "Failed to start server:" << server.errorString();
        return;
    }
    qInfo() << "Server started, listen client connect!";

    Profile::initInstance(Path(QCoreApplication::applicationDirPath() + "/.data"), QString(), false);

    try {
        Logger::initInstance();
        SettingsStorage::initInstance();
        Preferences::initInstance();
        Net::ProxyConfigurationManager::initInstance();
        Net::DownloadManager::initInstance();
        Net::GeoIPManager::initInstance();
        TorrentFilesWatcher::initInstance();
        BitTorrent::Session::initInstance();
        commandDataHandler->listenTorrentUpdateStart();
    } catch (const RuntimeError& err) {
        qWarning() << "BitTorrent initialize failed! error:" << err.message();
    }
}

void PokkitBackendService::newConnection() {
    auto socket = server.nextPendingConnection();
    qInfo() << "Client connected!";

    connect(socket, &QLocalSocket::readyRead, this, [this, socket] {
        commandDataHandler->addBuffer(socket->readAll());
    });

    connect(socket, &QLocalSocket::disconnected, socket, [this, socket] {
        if (socket == writeChannelSocket) {
            writeChannelSocket = nullptr;
        }
        socket->deleteLater();
        qInfo() << "Client disconnected!";
    });
}

void PokkitBackendService::onReadChannelReady() {
    writeChannelSocket = qobject_cast<QLocalSocket*>(sender());
    qInfo() << "Identify the client read channel!";
}
