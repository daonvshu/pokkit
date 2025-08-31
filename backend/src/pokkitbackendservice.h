#pragma once

#include <qobject.h>
#include <qpointer.h>
#include <qlocalserver.h>
#include <qlocalsocket.h>

#include "commanddatahandler.h"

class PokkitBackendService : public QObject, public IdentifyAuthConfirmedCallback {
public:
    PokkitBackendService();

    void onReadChannelReady() override;

    void start();

private:
    QLocalServer server;
    QPointer<QLocalSocket> writeChannelSocket;
    CommandDataHandler* commandDataHandler;

private:
    void newConnection();
};
