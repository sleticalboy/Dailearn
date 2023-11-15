all: sample

CC	= $(CROSS)clang
CXX	= $(CROSS)clang++
LD	= $(CROSS)clang++

CFLAGS = -I/usr/include/python3.8 -I/usr/include/google/protobuf -I./src/cc
LDFLAGS = -L/usr/lib/x86_64-linux-gnu/ -lpython3.8 -L/usr/lib/ -lprotobuf

sample:
	g++ -fPIC src/main.cc \
	src/cc/py_algo.pb.cc \
	src/cc/audio_whisper.pb.cc \
	${CFLAGS} ${LDFLAGS} -o $@

libalgo-agent-py.so:
	g++ -shared -fPIC \
	src/main.cc \
	src/cc/py_algo.pb.cc \
	src/cc/audio_whisper.pb.cc \
	${CFLAGS} ${LDFLAGS} -o $@

run: sample
	./sample

clean:
	rm -rv sample; rm -rv src/gen*; rm -rv src/__init__.py