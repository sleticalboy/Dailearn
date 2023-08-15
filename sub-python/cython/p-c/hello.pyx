from cpython.bytes cimport PyBytes_AsString, PyBytes_FromString, PyBytes_Check, PyBytes_FromObject

cdef extern from 'hello-clib.h':
    void hey();
    void hello(char *s);

def call_hey():
    hey()

def call_hello(s):
    print(f's is: {s}, is str: {PyBytes_Check(s)}')
    # cdef bytes bb = PyBytes_FromObject(s)
    # print('bb is ', bb)
    cdef char *name = PyBytes_AsString(s)
    # print(f's stris is: {name}')
    # cdef bytes b = PyBytes_FromString(name)
    # print(f's is: {b}')
    hello(name)
