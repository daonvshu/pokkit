#include "pokkitbackendservice.h"

PokkitBackendService::PokkitBackendService(int argc, char **argv)
    : QtService<QtSingleCoreApplication>(argc, argv, QLatin1String("Pokkit Backend Service"))
{
    setServiceDescription(QLatin1String("Pokkit app backend download service."));
    setServiceFlags(QtServiceBase::CanBeSuspended);
}

bool PokkitBackendService::isRunning() {
    return application()->isRunning();
}

void PokkitBackendService::start() {
}

void PokkitBackendService::pause() {
}

void PokkitBackendService::resume() {
}
