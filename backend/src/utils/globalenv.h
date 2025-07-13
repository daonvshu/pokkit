#pragma once

#include <qobject.h>
#include <qnetworkaccessmanager.h>

class GlobalEnv {
public:
    static void setProxy(const QString& address, int port);

    static QNetworkAccessManager* getNetworkAccessManager();

private:
    GlobalEnv();

    static GlobalEnv& instance();

private:
    QString proxyAddress;
    int proxyPort = 0;
};
