#pragma once

#include <qobject.h>
#include <qpointer.h>
#include <qlocalserver.h>
#include <qlocalsocket.h>

#include "qtservice.h"
#include "qtsinglecoreapplication.h"

#include "commanddatahandler.h"

class PokkitBackendService : public QtService<QtSingleCoreApplication>, public QObject, public IdentifyAuthConfirmedCallback {
public:
    PokkitBackendService(int argc, char **argv);

    bool isRunning();

    void start() override;

    void pause() override;

    void resume() override;

    void onReadChannelReady() override;

private:
    QLocalServer server;
    QPointer<QLocalSocket> writeChannelSocket;
    CommandDataHandler* commandDataHandler;

private:
    void newConnection();
};
