#include <qlogcollector.h>
#include <qtsinglecoreapplication.h>

#include "pokkitbackendservice.h"

int main(int argc, char* argv[]) {
    QtSingleCoreApplication a(argc, argv);
    if (a.isRunning()) {    // 第二个实例直接退出
        return 0;
    }

    logcollector::styleConfig
            .windowApp()
            .ide_clion(false)
            .wordWrap(120)
            .projectSourceCodeRootPath(QString::fromLatin1(ROOT_PROJECT_PATH))
            ;
    logcollector::QLogCollector::instance().registerLog();

    PokkitBackendService service;
    service.start();

    return a.exec();
}