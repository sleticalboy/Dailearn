#!/bin/bash

set -e

if [[ -f ./sample ]]; then
  rm -rf ./sample
fi

if [[ -d src/gencc || -d src/genpy ]]; then
  rm -rf src/gen*
fi

mkdir -p {src/gencc,src/genpy} && touch src/genpy/__init__.py && touch src/__init__.py
#protoc --cpp_out=src/gencc --python_out=src/genpy *.proto
protoc --cpp_out=src/gencc --python_out=src/genpy --pyi_out=src/genpy *.proto

CFLAGS='-I./src/gencc -I/usr/include/python3.10 -I/usr/include/google/protobuf'
LDFLAGS='-L/usr/lib/x86_64-linux-gnu/ -lpython3.10 -L/usr/lib -lprotobuf'

g++ ./src/gencc/py_algo_spec.pb.cc \
  src/gencc/audio_whisper.pb.cc \
  src/main.cc \
  $CFLAGS $LDFLAGS -o sample

# 运行程序
./sample
