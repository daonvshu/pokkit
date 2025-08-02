project(pokkit_backend VERSION 1.0.0)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(CMAKE_AUTOMOC ON)

find_package(QT NAMES Qt6 Qt5 COMPONENTS Core REQUIRED)
find_package(Qt${QT_VERSION_MAJOR} COMPONENTS Core Network REQUIRED)
find_package(QLogCollectorServer REQUIRED)
find_package(ProtocolCodecEngine REQUIRED)

include(../3rdparty/qtservice/src.cmake)
include(../3rdparty/qtsingleapplication/src.cmake)

include_directories(${CMAKE_CURRENT_SOURCE_DIR})

add_executable(${PROJECT_NAME}
    main.cpp

    pokkitbackendservice.h
    pokkitbackendservice.cpp
    commanddatahandler.h
    commanddatahandler.cpp

    providers/downloadserviceprovider.h
    providers/downloadserviceprovider.cpp

    providers/tasks/torrentcontentfetchtask.h
    providers/tasks/torrentcontentfetchtask.cpp
    providers/tasks/torrentdownloadinfo.h
    providers/tasks/torrentdownloadinfo.cpp

    services/dumputil.h
    services/downloadservice.h

    utils/datakey.h
    utils/globalenv.h
    utils/globalenv.cpp

    ${QtServiceSrc_Win}
    ${QtSingleAppSrc_Win}
)

target_link_libraries(${PROJECT_NAME}
    Qt${QT_VERSION_MAJOR}::Core
    Qt${QT_VERSION_MAJOR}::Network

    qlogcollectorserver
    protocol_codec_engine

    qbt_base
)