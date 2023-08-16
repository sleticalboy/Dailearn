#include <python3.10/Python.h>
#include "algo_args.pb.h"

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
    // PyObject *dict = PyDict_New();
    // PyDict_SetItemString(dict, "1", Py_BuildValue("s", "hello"));
    AlgoInput input{};
    auto args = input.mutable_iargs();
    args->operator[]("a") = "a";
    args->operator[]("b") = "b";
    // auto size = (int) input.ByteSizeLong();
    // auto *ibuf = new unsigned char [size];
    // if (!input.SerializeToArray(ibuf, size)) {
    //   printf("SerializeToArray() failed!\n");
    //   return;
    // }
    // printf("c algo ibuf is: %s\n", ibuf);
    auto ibuf = input.SerializeAsString();
    printf("c algo ibuf is: %s\n", ibuf.c_str());
    PyObject *tuple = PyTuple_New(1);
    PyTuple_SetItem(tuple, 0, Py_BuildValue("s", ibuf.c_str(), ibuf.size()));
    PyObject *ret = PyObject_CallObject(func, tuple);
    if (ret == nullptr) {
      printf("PyObject_CallObject() failed!\n");
      PyErr_Print();
      return;
    }
    char *obuf;
    if (PyArg_Parse(ret, "s", &obuf) != 0) {
      printf("PyArg_Parse() failed!\n");
      PyErr_Print();
      return;
    }
    // printf("c algo buf is: %s\n", pbuf);
    AlgoOutput output{};
    output.ParseFromString(std::string(obuf));
    printf("c algo output is: %s\n", output.ShortDebugString().c_str());
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

int main() {
  // 初始化
  Py_Initialize();
  call_hello();

  // 导入当前目录
  PyRun_SimpleString("import sys");
  PyRun_SimpleString("sys.path.append('./')");
  // 导入文件
  PyObject *pm = PyImport_ImportModule("samples");
  printf("%s() pm: %p\n", __func__, pm);
  if (pm != nullptr) {
    call_no_arg_func(pm);
    call_arg_func(pm);
    call_print_list(pm);
    call_obj_func(pm);
    call_algo_obj(pm);
    call_get_list(pm);
    call_get_dict(pm);
  } else {
    PyErr_Print();
  }
  // 释放资源
  Py_Finalize();
  return 0;
}
