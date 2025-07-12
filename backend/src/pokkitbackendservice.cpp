#include "pokkitbackendservice.h"

#include <qdebug.h>

PokkitBackendService::PokkitBackendService(int argc, char **argv)
    : QtService<QtSingleCoreApplication>(argc, argv, QLatin1String("Pokkit Backend Service"))
{
    setServiceDescription(QLatin1String("Pokkit app backend download service."));
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
    QLatin1String pipeName("pokkit_backend_pipe");
    QLocalServer::removeServer(pipeName);
    if (!server.listen(pipeName)) {
        qCritical() << "Failed to start server:" << server.errorString();
        return;
    }
    qInfo() << "Server started, listen client connect!";
}

void PokkitBackendService::pause() {
}

void PokkitBackendService::resume() {
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
