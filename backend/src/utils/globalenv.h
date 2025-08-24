#pragma once

#include <qobject.h>
#include <qnetworkaccessmanager.h>

class GlobalEnv {
public:
    static void setProxy(bool enabled, const QString& address, int port);

    static QNetworkAccessManager* getNetworkAccessManager();

private:
    GlobalEnv();

    static GlobalEnv& instance();

private:
    bool enabled = false;
    QString proxyAddress;
    int proxyPort = 0;
};
