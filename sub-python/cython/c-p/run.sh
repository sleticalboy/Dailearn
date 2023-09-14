#!/bin/bash

set -e

if [[ -f ./main ]]; then
  rm -rv ./main
fi

if [[ -d src/gencc || -d src/genpy ]]; then
  rm -rv src/gen*
fi

mkdir -p {src/gencc,src/genpy} && touch src/genpy/__init__.py && touch src/__init__.py
protoc --cpp_out=src/gencc --python_out=src/genpy --pyi_out=src/genpy algo_args.proto
protoc --cpp_out=src/gencc --python_out=src/genpy --pyi_out=src/genpy py_algo_spec.proto

CFLAGS='-I./src/gencc -I/usr/include/python3.8 -I/usr/include/google/protobuf'
LDFLAGS='-L/usr/lib/x86_64-linux-gnu/ -lpython3.8 -L/usr/lib -lprotobuf'

#g++ ./src/gencc/algo_args.pb.cc src/main.cc $CFLAGS $LDFLAGS -o sample

# 运行程序
#./sample
