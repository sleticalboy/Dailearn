#include <python3.10/Python.h>
#include <dirent.h>
#include "gencc/algo_args.pb.h"

void call_hello() {
  // 调用简单语句
  PyRun_SimpleString("print('hello via c call')");
}

void call_no_arg_func(PyObject *pm) {
  // 调用无参函数
  PyObject *func = PyObject_GetAttrString(pm, "hello");
  if (PyCallable_Check(func)) {
    PyObject_CallObject(func, nullptr);
  }
}

void call_arg_func(PyObject *pm) {
  // 调用有参函数
  PyObject *func = PyObject_GetAttrString(pm, "add");
  if (PyCallable_Check(func)) {
    // 以下两种方式均可构建一个 python tuple 对象
    // PyObject *tuple = PyTuple_New(2);
    // PyTuple_SetItem(tuple, 0, Py_BuildValue("i", 2));
    // PyTuple_SetItem(tuple, 1, Py_BuildValue("i", 3));
    PyObject *tuple = Py_BuildValue("(i, i)", 2, 3);
    PyObject *ret = PyObject_CallObject(func, tuple);
    int r;
    PyArg_Parse(ret, "i", &r);
    printf("2 + 3 = %d\n", r);
  }
}

void call_print_list(PyObject *pm) {
  PyObject *func = PyObject_GetAttrString(pm, "print_list");
  if (PyCallable_Check(func)) {
    PyObject *list = PyList_New(0);
    PyList_Append(list, Py_BuildValue("i", 30));
    PyList_Append(list, Py_BuildValue("f", 10.8));
    PyList_Append(list, Py_BuildValue("d", 0.98));
    PyList_Append(list, Py_BuildValue("s", "c string"));
    PyList_Append(list, Py_BuildValue("(s, s, i)", "1 str", "2 str", 30));
    PyObject *tuple = PyTuple_New(1);
    PyTuple_SetItem(tuple, 0, list);
    PyObject_CallObject(func, tuple);
  }
}

void call_obj_func(PyObject *pm) {
  // 获取 User 类
  PyObject *clazz = PyObject_GetAttrString(pm, "User");
  // 创建 User 类实例 tom
  PyObject *tom = PyObject_CallObject(clazz, Py_BuildValue("(s)", "Tom"));
  // 调用 User 实例方法 say_hello
  PyObject *func = PyObject_GetAttrString(tom, "say_hello");
  if (PyCallable_Check(func)) {
    PyObject_CallObject(func, nullptr);
  }
}

void call_algo_obj(PyObject *pm) {
  // 获取 AlgoProc 类
  PyObject *clazz = PyObject_GetAttrString(pm, "AlgoProc");
  // 创建 AlgoProc 类实例 tom
  PyObject *algo = PyObject_CallObject(clazz, Py_BuildValue("(s)", "prj-parse"));
  // 调用 AlgoProc 实例方法 process
  PyObject *func = PyObject_GetAttrString(algo, "process");
  if (PyCallable_Check(func)) {
    AlgoRequest request{};
    request.set_algo_name("prj-parse");
    auto inputs = request.mutable_inputs();
    inputs->operator[]("a") = "a";
    inputs->operator[]("b") = "b";
    inputs->operator[]("c") = "c";
    printf("c algo request is: %s\n", request.ShortDebugString().c_str());
    PyObject *tuple = PyTuple_New(1);
    PyTuple_SetItem(tuple, 0, PyBytes_FromString(request.SerializeAsString().c_str()));
    PyObject *ret = PyObject_CallObject(func, tuple);
    if (ret == nullptr) {
      printf("PyObject_CallObject() failed!\n");
      PyErr_Print();
      return;
    }
    auto what_type = [](PyTypeObject *typ, PyTypeObject *exp, const char *desc) {
      if (typ == exp) {
        printf("PyObject is '%s'\n", desc);
      }
    };
    what_type(Py_TYPE(ret), &PyLong_Type, "py long");
    what_type(Py_TYPE(ret), &PyFloat_Type, "py float");
    what_type(Py_TYPE(ret), &PyBytes_Type, "py bytes");
    what_type(Py_TYPE(ret), &PyList_Type, "py list");
    what_type(Py_TYPE(ret), &PySet_Type, "py set");
    what_type(Py_TYPE(ret), &PyDict_Type, "py dict");
    AlgoResponse response{};
    response.ParseFromString(PyBytes_AsString(ret));
    printf("c algo response is: %s\n", response.ShortDebugString().c_str());
  }
  // 调用 AlgoProc 实例方法 release
  func = PyObject_GetAttrString(algo, "release");
  if (PyCallable_Check(func)) {
    PyObject_CallObject(func, nullptr);
  }
}

void call_get_list(PyObject *pm) {
  // 调用无参函数
  PyObject *func = PyObject_GetAttrString(pm, "get_list");
  if (PyCallable_Check(func)) {
    PyObject *list = PyObject_CallObject(func, nullptr);
    Py_ssize_t size = PyList_Size(list);
    for (size_t i = 0; i < size; i++) {
      PyObject *it = PyList_GetItem(list, i);
      char *s;
      PyArg_Parse(it, "s", &s);
      printf("list[%ld]: %s, ", i, s);
    }
    printf("\n");
  }
}

void call_get_dict(PyObject *pm) {
  // 调用无参函数
  PyObject *func = PyObject_GetAttrString(pm, "get_dict");
  if (PyCallable_Check(func)) {
    PyObject *dict = PyObject_CallObject(func, nullptr);
    char *name;
    int age;
    PyArg_Parse(PyDict_GetItemString(dict, "name"), "s", &name);
    PyArg_Parse(PyDict_GetItemString(dict, "age"), "i", &age);
    printf("dict: name: %s, age: %d\n", name, age);
  }
}

std:: string find_algo_impl_module(const std::string &who) {
  std::string algo_impl;
  char *cwd = new char[64];
  getcwd(cwd, 64);
  std::string root = std::string(cwd) + "/" + who;
  delete[] cwd;
  printf("%s() starting find algo module in: %s\n", __func__, root.c_str());
  auto dir = opendir(root.c_str());
  struct dirent *f;
  while ((f = readdir(dir)) != nullptr) {
    // 过滤当前目录以及父目录，否则会导致死循环
    if (strcmp(f->d_name, ".") == 0 || strcmp(f->d_name, "..") == 0) continue;

    // printf("%s() -> %s\n", __func__, f->d_name);
    auto s = std::string(f->d_name);
    auto pos = s.rfind('.');
    if (pos == std::string::npos) continue;
    auto name = s.substr(0, pos);
    // printf("%s() pure name is: %s\n", __func__, name.c_str());
    pos = name.rfind("_impl");
    if (pos != std::string::npos && name.substr(pos) == "_impl" && name.find("algo_") == 0) {
      return name;
    }
  }
  return "";
}

int main() {
  // 初始化
  Py_Initialize();
  call_hello();

  // 导入当前目录
  PyRun_SimpleString("import sys");
  PyRun_SimpleString("sys.path.append('src')");
  // 找到并导入当前算法脚本文件，命名规则要符合 algo_xxx_impl.py
  auto algo_module = find_algo_impl_module("src");
  if (algo_module.empty()) {
    perror("cannot find an algo impl module.\n");
    return 0;
  }
  printf("%s() find algo impl module: %s\n", __func__, algo_module.c_str());
  PyObject *pm = PyImport_ImportModule(algo_module.c_str());
  printf("%s() algo impl pm: %p\n", __func__, pm);
  if (pm != nullptr) {
    call_algo_obj(pm);
  } else {
    PyErr_Print();
  }

  pm = PyImport_ImportModule("samples");
  printf("%s() samples pm: %p\n", __func__, pm);
  if (pm != nullptr) {
    call_no_arg_func(pm);
    call_arg_func(pm);
    call_print_list(pm);
    call_obj_func(pm);
    call_get_list(pm);
    call_get_dict(pm);
  } else {
    PyErr_Print();
  }
  // 释放资源
  Py_Finalize();
  return 0;
}
