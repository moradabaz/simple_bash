cmake_minimum_required(VERSION 3.7)
project(simplesh)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_FLAGS)
set(SOURCE_FILES
        libreadline.supp
        Makefile
        simplesh.c
        valgrind.sh)

add_executable(simplesh ${SOURCE_FILES})