all: sample

CC	= $(CROSS)clang
CXX	= $(CROSS)clang++
LD	= $(CROSS)clang++

CFLAGS = -I/usr/include/python3.7 -I/usr/include/google/protobuf -I./src/gencc
LDFLAGS = -L/usr/lib/x86_64-linux-gnu/ -lpython3.7 -L/usr/lib/ -lprotobuf

prepare:
	touch src/__init__.py

sample: prepare
	g++ src/main.cc ${CFLAGS} ${LDFLAGS} -o $@

libalgo-agent-py.so: prepare
	g++ -shared -fPIC src/main.cc ${CFLAGS} ${LDFLAGS} -o $@

run: prepare sample
	./sample

clean:
	rm -rv sample; rm -rv src/gen*; rm -rv src/__init__.py