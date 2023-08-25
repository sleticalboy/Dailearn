#include <python3.10/Python.h>
#include <dirent.h>
#include "gencc/algo_args.pb.h"

void debug_py_obj(PyObject *obj, const char *who) {
  auto what_type = [&](PyTypeObject *typ, PyTypeObject *exp, const char *desc) {
    if (typ == exp) {
      printf("%s => PyObject is '%s'\n", who, desc);
    }
  };
  what_type(Py_TYPE(obj), &PyLong_Type, "py long");
  what_type(Py_TYPE(obj), &PyFloat_Type, "py float");
  what_type(Py_TYPE(obj), &PyBytes_Type, "py bytes");
  what_type(Py_TYPE(obj), &PyList_Type, "py list");
  what_type(Py_TYPE(obj), &PySet_Type, "py set");
  what_type(Py_TYPE(obj), &PyDict_Type, "py dict");
  what_type(Py_TYPE(obj), &PyTuple_Type, "py tuple");
  what_type(Py_TYPE(obj), &PyFunction_Type, "py function");
  what_type(Py_TYPE(obj), &PyModule_Type, "py module");
  what_type(Py_TYPE(obj), &PyMethod_Type, "py method");
}

void call_hello() {
  // 调用简单语句
  PyRun_SimpleString("print('hello via c call')");
}

void call_no_arg_func(PyObject *pm) {
  // 调用无参函数
  PyObject *func = PyObject_GetAttrString(pm, "hello");
  printf("======> %s() pm: %p, func: %p\n", __func__, pm, func);
  if (PyCallable_Check(func)) {
    PyObject_CallObject(func, nullptr);
  }
}

void call_arg_func(PyObject *pm) {
  // 调用有参函数
  PyObject *func = PyObject_GetAttrString(pm, "add");
  printf("======> %s() pm: %p, func: %p\n", __func__, pm, func);
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
  printf("======> %s() pm: %p, func: %p\n", __func__, pm, func);
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
  printf("======> %s() pm: %p, func: %p\n", __func__, pm, func);
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
  printf("======> %s() pm: %p, func: %p\n", __func__, pm, func);
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
    debug_py_obj(ret, __func__);
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
  printf("======> %s() pm: %p, func: %p\n", __func__, pm, func);
  if (PyCallable_Check(func)) {
    PyObject *list = PyObject_CallObject(func, nullptr);
    Py_ssize_t size = PyList_Size(list);
    for (int i = 0; i < size; i++) {
      PyObject *it = PyList_GetItem(list, i);
      char *s;
      PyArg_Parse(it, "s", &s);
      printf("list[%d]: %s, ", i, s);
    }
    printf("\n");
  }
}

void call_get_dict(PyObject *pm) {
  // 调用无参函数
  PyObject *func = PyObject_GetAttrString(pm, "get_dict");
  printf("======> %s() pm: %p, func: %p\n", __func__, pm, func);
  if (PyCallable_Check(func)) {
    PyObject *dict = PyObject_CallObject(func, nullptr);
    char *name;
    int age;
    PyArg_Parse(PyDict_GetItemString(dict, "name"), "s", &name);
    PyArg_Parse(PyDict_GetItemString(dict, "age"), "i", &age);
    printf("dict: name: %s, age: %d\n", name, age);
  }
}

PyObject *wrapped_square(PyObject*, PyObject *args) {
  // 这个 args 是 python 传过来的参数，类型是 tuple
  debug_py_obj(args, __func__);
  int num;
  // static char *kwlist[] = {"num", nullptr};
  // if (!PyArg_ParseTupleAndKeywords(args, kws, "i", kwlist, &num)) {
  if (!PyArg_ParseTuple(args, "i", &num)) {
    PyErr_SetString(PyExc_RuntimeError, "Failed to parse python args");
    return nullptr;
  }
  return Py_BuildValue("i", num * num);
}

PyObject *wrapped_post_value(PyObject*, PyObject *args) {
  // 这个 args 是 python 传过来的参数，类型是 tuple
  debug_py_obj(args, __func__);
  int num;
  char *msg;
  // 这三种方式均可以解析 tuple
  // PyArg_Parse(PyTuple_GetItem(args, 0), "i", &num);
  // PyArg_Parse(PyTuple_GetItem(args, 1), "s", &msg);
  // if (!PyArg_Parse(args, "(is)", &num, &msg)) {
  if (!PyArg_ParseTuple(args, "is", &num, &msg)) {
    PyErr_SetString(PyExc_RuntimeError, "Failed to parse python args");
    Py_RETURN_NONE;
  }
  printf("num: %d, msg: %s\n", num, msg);
  // return Py_BuildValue("i", 0);
  Py_RETURN_NONE;
}

PyObject *wrapped_sum_int(PyObject*, PyObject *args) {
  // 这个 args 是 python 传过来的参数，类型是 tuple
  debug_py_obj(args, __func__);
  int a, b;
  if (!PyArg_ParseTuple(args, "ii", &a, &b)) {
    PyErr_SetString(PyExc_RuntimeError, "Failed to parse python args");
    return nullptr;
  }
  printf("a: %d, b: %d\n", a, b);
  return Py_BuildValue("i", a + b);
}

static PyMethodDef module_methods[] = {
    {"square", wrapped_square, METH_VARARGS, "A c++ square function."},
    {"post_value", wrapped_post_value, METH_VARARGS, "A c++ post_value function."},
    {"sum_int", wrapped_sum_int, METH_VARARGS, "A c++ sum_int function."},
    {nullptr}
};

static struct PyModuleDef M_native_functions = {
    PyModuleDef_HEAD_INIT,
    "native_functions",
    "c++ 中定义的函数",
    -1,
    module_methods,
};

PyMODINIT_FUNC PyInit_nativeMethods(void) {
  PyObject *m = PyModule_Create(&M_native_functions);
  if (m == nullptr) {
    PyErr_Print();
    return nullptr;
  }
  return m;
}

void call_fptr(PyObject *pm) {
  // 向 python 传递 c 函数指针
  PyObject *func = PyObject_GetAttrString(pm, "call_c_fptr");
  printf("======> %s() pm: %p, func: %p\n", __func__, pm, func);
  if (PyCallable_Check(func)) {
    PyObject *tuple = PyTuple_New(1);
    PyTuple_SetItem(tuple, 0, PyInit_nativeMethods());
    PyObject_CallObject(func, tuple);
    PyErr_Print();
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
    call_fptr(pm);
  } else {
    PyErr_Print();
  }
  // 释放资源
  Py_Finalize();
  return 0;
}
