from cpython.bytes cimport PyBytes_FromStringAndSize
from cpython.bytes cimport PyBytes_AS_STRING

cdef extern from 'hello-clib.c':
    void hey();
    void hello(char *s);

def call_hey():
    hey()

def call_hello(s):
    cdef bytes p_str = PyBytes_FromStringAndSize(s, len(s))
    cdef char *c_str_ptr = <char *>PyBytes_AS_STRING(p_str)
    hello(c_str_ptr)
