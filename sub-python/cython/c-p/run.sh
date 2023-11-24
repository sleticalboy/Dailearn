#!/bin/bash

set -e

if [[ -f ./sample ]]; then
  rm -rf ./sample
fi

if [[ ! -d src/cc ]]; then
  mkdir -p src/cc
fi

protoc -I ./protos --cpp_out=src/cc --python_out=scripts --pyi_out=scripts ./protos/*.proto

CFLAGS='-I./src/gencc -I/usr/include/python3.10 -I/usr/include/google/protobuf'
LDFLAGS='-L/usr/lib/x86_64-linux-gnu/ -lpython3.10 -L/usr/lib -lprotobuf'

g++ ./src/cc/py_algo.pb.cc \
  src/cc/audio_whisper.pb.cc \
  src/main.cc \
  $CFLAGS $LDFLAGS -o sample

# 运行程序
./sample
