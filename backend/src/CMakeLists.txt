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

set(INSTALL_DIR ${CMAKE_SOURCE_DIR}/../build/main-release/app/pokkit)
set(OUTPUT_FILE_PATH ${INSTALL_DIR}/${PROJECT_NAME})

if (${CMAKE_BUILD_TYPE} STREQUAL "Release")
    get_target_property(QT_QMAKE_EXECUTABLE Qt${QT_VERSION_MAJOR}::qmake IMPORTED_LOCATION)
    get_filename_component(QT_WINDEPLOYQT_EXECUTABLE ${QT_QMAKE_EXECUTABLE} PATH)
    set(QT_WINDEPLOYQT_EXECUTABLE "${QT_WINDEPLOYQT_EXECUTABLE}/windeployqt.exe")
    message("windeployqt executable path: ${QT_WINDEPLOYQT_EXECUTABLE}")

    add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E make_directory ${INSTALL_DIR}
        COMMAND ${CMAKE_COMMAND} -E copy_if_different $<TARGET_FILE:${PROJECT_NAME}> ${INSTALL_DIR}
        COMMAND ${CMAKE_COMMAND} -E copy_if_different ${LibtorrentRasterbar_DIR}/../../../bin/torrent-rasterbar.dll ${INSTALL_DIR}
        COMMAND ${QT_WINDEPLOYQT_EXECUTABLE} --no-translations ${INSTALL_DIR}/$<TARGET_FILE_NAME:${PROJECT_NAME}>
    )
endif()