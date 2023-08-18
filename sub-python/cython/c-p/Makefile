all: sample

CC	= $(CROSS)clang
CXX	= $(CROSS)clang++
LD	= $(CROSS)clang++

CFLAGS = -I/usr/include/python3.10 -I/usr/include/google/protobuf -I.
LDFLAGS = -L/usr/lib/x86_64-linux-gnu/ -lpython3.10 -L/usr/lib/ -lprotobuf

prepare:
	protoc --cpp_out=. --python_out=. algo_args.proto

sample:
	g++ main.cc algo_args.pb.cc ${CFLAGS} ${LDFLAGS} -o sample

run: clean prepare sample
	./sample

clean:
	rm -rv sample; rm -rv algo_args.pb.*; rm -rv algo_args_pb2*