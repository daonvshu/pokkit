#include "pokkitbackendservice.h"

#include "base/bittorrent/session.h"
#include "base/exceptions.h"
#include "base/logger.h"
#include "base/profile.h"
#include "base/preferences.h"
#include "base/net/downloadmanager.h"
#include "base/net/proxyconfigurationmanager.h"
#include "base/bittorrent/infohash.h"

#include <qdebug.h>

PokkitBackendService::PokkitBackendService(int argc, char **argv)
    : QtService<QtSingleCoreApplication>(argc, argv, "Pokkit Backend Service")
{
    setServiceDescription("Pokkit app backend download service.");
    setServiceFlags(QtServiceBase::CanBeSuspended);

    connect(&server, &QLocalServer::newConnection, this, &PokkitBackendService::newConnection);

    commandDataHandler = new CommandDataHandler(this);
    connect(commandDataHandler, &CommandDataHandler::dataFeedback, this, [&] (const QByteArray& data) {
        if (writeChannelSocket && writeChannelSocket->isOpen()) {
            writeChannelSocket->write(data);
        }
    });
}

bool PokkitBackendService::isRunning() {
    return application()->isRunning();
}

void PokkitBackendService::start() {
    if (server.isListening()) {
        return;
    }
    QString pipeName("pokkit_backend_pipe");
    QLocalServer::removeServer(pipeName);
    if (!server.listen(pipeName)) {
        qCritical() << "Failed to start server:" << server.errorString();
        return;
    }
    qInfo() << "Server started, listen client connect!";

    Profile::initInstance(Path(), QString(), false);

    try {
        Logger::initInstance();
        SettingsStorage::initInstance();
        Preferences::initInstance();
        Net::ProxyConfigurationManager::initInstance();
        BitTorrent::Session::initInstance();
    } catch (const RuntimeError& err) {
        qWarning() << "BitTorrent initialize failed! error:" << err.message();
    }
}

void PokkitBackendService::pause() {
    BitTorrent::Session::instance()->pause();
}

void PokkitBackendService::resume() {
    BitTorrent::Session::instance()->resume();
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
