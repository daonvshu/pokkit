cmake_minimum_required(VERSION 3.16)

project(pokkit_backend_project)

set(CMAKE_INCLUDE_CURRENT_DIR ON)

add_compile_definitions(ROOT_PROJECT_PATH="${CMAKE_SOURCE_DIR}")

add_subdirectory(3rdparty/qbittorrent)
add_subdirectory(src)
