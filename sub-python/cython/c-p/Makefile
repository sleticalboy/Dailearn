all: main

CC	= $(CROSS)clang
CXX	= $(CROSS)clang++
LD	= $(CROSS)clang++

CFLAGS = -I/usr/include/python3.10 -I/usr/include/google/protobuf -I.
LDFLAGS = -L/usr/lib/x86_64-linux-gnu/ -lpython3.10 /usr/lib/x86_64-linux-gnu/libprotobuf.a

prepare:
	protoc --cpp_out=. --python_out=. algo_args.proto

main:
	clang++ main.cc algo_args.pb.cc ${CFLAGS} ${LDFLAGS} -o main

run:
	./main

clean:
	rm -rv main