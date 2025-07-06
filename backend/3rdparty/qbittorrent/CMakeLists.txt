cmake_minimum_required (VERSION 3.16)

list(APPEND CMAKE_MODULE_PATH cmake/Modules)

# use CONFIG mode first in find_package
set(CMAKE_FIND_PACKAGE_PREFER_CONFIG ON)
# version requirements - older versions may work, but you are on your own
set(minBoostVersion 1.76)
set(minQt6Version 6.9.1)
set(minOpenSSLVersion 3.0.2)
set(minLibtorrent1Version 1.2.19)
set(minLibtorrentVersion 2.0.10)
set(minZlibVersion 1.2.11)

# features (some are platform-specific)
list(APPEND CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR}/cmake/Modules)
include(FeatureSummary)
include(FeatureOptionsSetup)

configure_file("base/version.h.in" "${CMAKE_CURRENT_SOURCE_DIR}/base/version.h" @ONLY)

macro(find_libtorrent version)
    if (UNIX AND (NOT APPLE) AND (NOT CYGWIN))
        find_package(LibtorrentRasterbar QUIET ${version} COMPONENTS torrent-rasterbar)
        if (NOT LibtorrentRasterbar_FOUND)
            include(FindPkgConfig)
            pkg_check_modules(LibtorrentRasterbar IMPORTED_TARGET GLOBAL "libtorrent-rasterbar>=${version}")
            if (NOT LibtorrentRasterbar_FOUND)
                message(
                    FATAL_ERROR
                    "Package LibtorrentRasterbar >= ${version} not found"
                    " with CMake or pkg-config.\n- Set LibtorrentRasterbar_DIR to a directory containing"
                    " a LibtorrentRasterbarConfig.cmake file or add the installation prefix of LibtorrentRasterbar"
                    " to CMAKE_PREFIX_PATH.\n- Alternatively, make sure there is a valid libtorrent-rasterbar.pc"
                    " file in your system's pkg-config search paths (use the system environment variable PKG_CONFIG_PATH"
                    " to specify additional search paths if needed)."
                )
            endif()
            add_library(LibtorrentRasterbar::torrent-rasterbar ALIAS PkgConfig::LibtorrentRasterbar)
            # force a fake package to show up in the feature summary
            set_property(GLOBAL APPEND PROPERTY
                PACKAGES_FOUND
                "LibtorrentRasterbar via pkg-config (version >= ${version})"
            )
            set_package_properties("LibtorrentRasterbar via pkg-config (version >= ${version})"
                PROPERTIES
                TYPE REQUIRED
            )
        else()
            set_package_properties(LibtorrentRasterbar PROPERTIES TYPE REQUIRED)
        endif()
    else()
        cmake_policy(SET CMP0167 OLD)
        find_package(LibtorrentRasterbar ${version} REQUIRED COMPONENTS torrent-rasterbar)
    endif()
endmacro()

find_libtorrent(${minLibtorrent1Version})
if (LibtorrentRasterbar_FOUND AND (LibtorrentRasterbar_VERSION VERSION_GREATER_EQUAL 2.0))
    find_libtorrent(${minLibtorrentVersion})
endif()

# force variable type so that it always shows up in ccmake/cmake-gui frontends
set_property(CACHE LibtorrentRasterbar_DIR PROPERTY TYPE PATH)
cmake_policy(SET CMP0167 OLD)
find_package(Boost ${minBoostVersion} REQUIRED)
find_package(OpenSSL ${minOpenSSLVersion} REQUIRED)
find_package(ZLIB ${minZlibVersion} REQUIRED)
find_package(Qt6 ${minQt6Version} REQUIRED COMPONENTS Core Network Sql Xml LinguistTools)
if (DBUS)
    find_package(Qt6 ${minQt6Version} REQUIRED COMPONENTS DBus)
    set_package_properties(Qt6DBus PROPERTIES
        DESCRIPTION "Qt6 module for inter-process communication over the D-Bus protocol"
        PURPOSE "Required by the DBUS feature"
    )
endif()

# automatically call Qt moc, rcc and uic as needed for all targets by default
set(CMAKE_AUTORCC_OPTIONS --compress 9 --threshold 5)
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTORCC ON)
set(CMAKE_AUTOUIC ON)

include_directories(.)

# create interface-only target libraries with common compile options/definitions to link to
include(CommonConfig)

feature_option(MSVC_RUNTIME_DYNAMIC "Use MSVC dynamic runtime library (-MD) instead of static (-MT)" ON)

add_subdirectory(base)

target_include_directories(qbt_base PUBLIC .)

feature_option(VERBOSE_CONFIGURE "Show information about PACKAGES_FOUND and PACKAGES_NOT_FOUND in the configure output (only useful for debugging the CMake build scripts)" OFF)

if (VERBOSE_CONFIGURE)
    feature_summary(WHAT ALL)
else()
    feature_summary(WHAT ENABLED_FEATURES DISABLED_FEATURES)
endif()
