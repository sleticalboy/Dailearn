#!/bin/bash

set -e

if [[ -f ./main ]]; then
  rm -rv ./main
fi

if [[ -f algo_args.pb.h ]]; then
  rm -rv algo_args.pb.*
fi

if [[ -f algo_args_pb2.py ]]; then
  rm -rv algo_args_pb*
fi

protoc --cpp_out=. --python_out=. --pyi_out=. algo_args.proto

CFLAGS='-I. -I/usr/include/python3.10 -I/usr/include/google/protobuf'
LDFLAGS='-L/usr/lib/x86_64-linux-gnu/ -lpython3.10 -L/usr/lib -lprotobuf'

g++ algo_args.pb.cc main.cc $CFLAGS $LDFLAGS -o sample

# 运行程序
./sample
