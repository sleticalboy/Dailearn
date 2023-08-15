from distutils.core import setup, Extension

from Cython.Build import cythonize

ext = Extension(
    name='hello',
    sources=['hello.pyx', 'hello-clib.c'],
    language='c',
)

setup(ext_modules=cythonize(ext))
