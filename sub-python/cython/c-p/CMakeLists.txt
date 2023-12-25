cmake_minimum_required(VERSION 3.5)
project(c-p)

# 指定 C 编译器
#set(CMAKE_C_COMPILER clang)
# 指定 C++ 编译器
#set(CMAKE_CXX_COMPILER clang++)

include_directories(
        /usr/include/python3.10
        /usr/include/google/protobuf
        /src/cc
)

link_directories(
        /usr/lib/x86_64-linux-gnu/
        /usr/lib/
)

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR})
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR})

# 编译二进制程序
add_executable(sample
        src/main.cc
        src/lib_main.cc
        src/cc/algo.pb.cc
        src/cc/algox.pb.cc
        src/cc/py_algo.pb.cc
        src/cc/audio_whisper.pb.cc
        )
target_link_libraries(sample python3.10 protobuf)

# 编译共享库
add_library(sample-so SHARED
        src/main.cc
        src/lib_main.cc
        src/cc/py_algo.pb.cc
        src/cc/audio_whisper.pb.cc
        )
target_link_libraries(sample-so python3.10 protobuf)

# 编译二进制程序
add_executable(sample2 src/main2.cc)
target_link_libraries(sample2 python3.10 protobuf)
#target_link_libraries(sample2
#        -Wl,--whole-archive
#        python3.10 protobuf
#        -Wl,--whole-archive
#        )
