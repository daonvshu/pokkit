#pragma once

#include <qobject.h>
#include <qpointer.h>
#include <qlocalserver.h>
#include <qlocalsocket.h>

#include "qtsinglecoreapplication.h"

#include "commanddatahandler.h"

class PokkitBackendService : public QtSingleCoreApplication, public IdentifyAuthConfirmedCallback {
public:
    PokkitBackendService(int argc, char **argv);

    void onReadChannelReady() override;

    void start();

private:
    QLocalServer server;
    QPointer<QLocalSocket> writeChannelSocket;
    CommandDataHandler* commandDataHandler;

private:
    void newConnection();
};
