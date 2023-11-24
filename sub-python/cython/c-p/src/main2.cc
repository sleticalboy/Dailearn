#include "dlfcn.h"
#include "iostream"

#include <python3.10/Python.h>

int main() {
  std::cout << "python version: " << Py_GetVersion() << std::endl;
  void *handle = dlopen("./libsample-so.so", RTLD_LAZY);
  std::cout << "handle: " << handle << ", err: " << errno << std::endl;
  if (handle == nullptr) {
    return 1;
  }
  typedef int (*run_fn)();
  auto run = (run_fn) dlsym(handle, "run_main_test");
  std::cout << "run fn: " << run << std::endl;
  if (run == nullptr) {
    return 1;
  }
  int ret;
  Py_BEGIN_ALLOW_THREADS
  ret = run();
  Py_END_ALLOW_THREADS
  std::cout << "ret: " << ret << std::endl;
  dlclose(handle);
  return 0;
}
