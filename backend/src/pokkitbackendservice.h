#pragma once

#include <qobject.h>

#include "qtservice.h"
#include "qtsinglecoreapplication.h"

class PokkitBackendService : public QtService<QtSingleCoreApplication> {
public:
    PokkitBackendService(int argc, char **argv);

    bool isRunning();

    void start() override;

    void pause() override;

    void resume() override;
};
