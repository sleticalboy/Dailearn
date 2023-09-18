cmake_minimum_required(VERSION 3.5)
project(c-p)

# 指定 C 编译器
#set(CMAKE_C_COMPILER clang)
# 指定 C++ 编译器
#set(CMAKE_CXX_COMPILER clang++)

include_directories(
        /usr/include/python3.10
        /usr/include/google/protobuf
        /src/gencc
)

link_directories(
        /usr/lib/x86_64-linux-gnu/
        /usr/lib/
)

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR})
add_executable(sample
        src/main.cc
        src/gencc/py_algo_spec.pb.cc
        src/gencc/audio_whisper.pb.cc
        )
# 链接动态库
target_link_libraries(sample python3.10 protobuf)