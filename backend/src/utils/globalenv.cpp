#include "globalenv.h"

#include <qnetworkproxy.h>

void GlobalEnv::setProxy(bool enabled, const QString &address, int port) {
    instance().enabled = enabled;
    instance().proxyAddress = address;
    instance().proxyPort = port;
}

GlobalEnv::GlobalEnv() {
}

GlobalEnv &GlobalEnv::instance() {
    static GlobalEnv env;
    return env;
}

QNetworkAccessManager *GlobalEnv::getNetworkAccessManager() {
    auto manager = new QNetworkAccessManager;
    if (instance().enabled && !instance().proxyAddress.isEmpty()) {
        QNetworkProxy proxy;
        proxy.setType(QNetworkProxy::HttpProxy);
        proxy.setHostName(instance().proxyAddress);
        proxy.setPort(instance().proxyPort);
        manager->setProxy(proxy);
    }
    return manager;
}
