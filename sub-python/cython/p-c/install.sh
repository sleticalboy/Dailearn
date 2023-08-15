#!/bin/bash

rm -rv hello.c hello.cpython* build

python3 setup.py build_ext -i --inplace
