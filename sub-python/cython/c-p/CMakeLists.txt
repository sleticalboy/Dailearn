cmake_minimum_required(VERSION 3.5)
project(c-p)

# 指定 C 编译器
#set(CMAKE_C_COMPILER clang)
# 指定 C++ 编译器
#set(CMAKE_CXX_COMPILER clang++)

include_directories(
        ${CMAKE_CURRENT_SOURCE_DIR}/src/gencc
        /usr/include/python3.10
        /usr/include/google/protobuf
)

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR})
add_executable(sample
        ${CMAKE_CURRENT_SOURCE_DIR}/src/gencc/algo_args.pb.cc
        src/main.cc
        )
# 链接动态库
target_link_libraries(sample
        /usr/lib/x86_64-linux-gnu/libprotobuf.so
        /usr/lib/x86_64-linux-gnu/libpython3.10.so
        )