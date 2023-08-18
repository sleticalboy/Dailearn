all: sample

CC	= $(CROSS)clang
CXX	= $(CROSS)clang++
LD	= $(CROSS)clang++

CFLAGS = -I/usr/include/python3.10 -I/usr/include/google/protobuf -I./src/gencc
LDFLAGS = -L/usr/lib/x86_64-linux-gnu/ -lpython3.10 -L/usr/lib/ -lprotobuf

prepare:
	mkdir -p src/gencc && mkdir -p src/genpy && \
	protoc --cpp_out=src/gencc --python_out=src/genpy algo_args.proto

gen-pyi: # 生成 python 类存根，方便开发时使用 ide 调用
	echo "not supported now!" && \
	/home/binlee/code/open-source/my-protobuf/bin/protoc --pyi_out=src/genpy algo_args.proto

sample: prepare gen-pyi
	rm -rv src/genpy/*.pyi && \
	g++ src/main.cc src/gencc/algo_args.pb.cc ${CFLAGS} ${LDFLAGS} -o $@

libalgo-agent-py.so: prepare
	rm -rv src/genpy/*.pyi && \
	g++ -shared -fPIC src/main.cc src/gencc/algo_args.pb.cc ${CFLAGS} ${LDFLAGS} -o $@

run: prepare sample
	./sample

clean:
	rm -rv sample; rm -rv src/gen*; rm -rv src/__init__.py