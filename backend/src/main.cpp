#include <qlogcollector.h>
#include <qdebug.h>

#include "pokkitbackendservice.h"

int main(int argc, char* argv[]) {
    PokkitBackendService service(argc, argv);

    logcollector::styleConfig
            .windowApp()
            .ide_clion(false)
            .wordWrap(120)
            .projectSourceCodeRootPath(QString::fromLatin1(ROOT_PROJECT_PATH))
            ;
    logcollector::QLogCollector::instance().registerLog();

    if (service.isRunning()) {
        qCritical() << "Service already running.";
        return 0;
    }

    return service.exec();
}