#include <python3.8/Python.h>
#include <dirent.h>
#include <string>
#include <map>
#include <vector>
#include <sstream>

void debug_py_obj(PyObject *obj, const char *who) {
  if (obj != nullptr) {
    printf("%s => PyObject is '%s'\n", who, obj->ob_type->tp_name);
  }
}

void test_hello_world() {
  // 调用简单语句
  PyRun_SimpleString("print('hello via c call')");
}

void test_no_arg_func(PyObject *pm) {
  // 调用无参函数
  PyObject *func = PyObject_GetAttrString(pm, "hello");
  printf("======> %s() pm: %p, func: %p\n", __func__, pm, func);
  if (PyCallable_Check(func)) {
    PyObject_CallObject(func, nullptr);
  }
}

void test_arg_func(PyObject *pm) {
  // 调用有参函数
  PyObject *func = PyObject_GetAttrString(pm, "add");
  printf("======> %s() pm: %p, func: %p\n", __func__, pm, func);
  if (PyCallable_Check(func)) {
    PyObject *ret = PyObject_CallObject(func, Py_BuildValue("(ii)", 2, 3));
    debug_py_obj(ret, __func__);
    int r;
    PyArg_Parse(ret, "i", &r);
    printf("2 + 3 = %d\n", r);
  }
}

void test_print_list(PyObject *pm) {
  PyObject *func = PyObject_GetAttrString(pm, "print_list");
  printf("======> %s() pm: %p, func: %p\n", __func__, pm, func);
  if (PyCallable_Check(func)) {
    PyObject *list = Py_BuildValue("[ifds(s,s,i)]", 30, 10.8, 0.98, "a C str", "1-str", "2-str", 30);
    PyObject_CallObject(func, Py_BuildValue("(O)", list));
  }
}

void test_print_dict(PyObject *pm) {
  PyObject *func = PyObject_GetAttrString(pm, "print_dict");
  printf("======> %s() pm: %p, func: %p\n", __func__, pm, func);
  if (PyCallable_Check(func)) {
    PyObject *scores = Py_BuildValue("(iii)", 99, 98, 91);
    PyObject *pdict = Py_BuildValue("{s:s,s:i,s:O}", "name", "tom", "age", 25, "scores", scores);
    PyObject_CallObject(func, Py_BuildValue("(O)", pdict));
  }
}

void test_obj_func(PyObject *pm) {
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

PyObject *to_py_dict(std::map<std::string, std::string> &raw_map) {
  PyObject *pd = PyDict_New();
  for (const auto &item: raw_map) {
    PyDict_SetItemString(pd, item.first.data(), PyBytes_FromString(item.second.data()));
  }
  return pd;
}

void to_cpp_map(PyObject *pd, std::map<std::string, std::string> &dst) {
  debug_py_obj(pd, __func__);
  PyObject *keys = PyDict_Keys(pd);
  Py_ssize_t len = keys == nullptr ? 0 : PyDict_Size(pd);
  for (int i = 0; i < len; ++i) {
    PyObject *ks = PyList_GetItem(keys, i);
    const char *_key = PyUnicode_AsUTF8(ks);
    PyObject *vs = PyDict_GetItemString(pd, _key);
    if (vs == nullptr) {
      PyErr_Print();
      PyErr_Clear();
      continue;
    }
    if (Py_TYPE(vs) == &PyBytes_Type) {
      dst.operator[](_key) = PyBytes_AsString(vs);
    } else if (Py_TYPE(vs) == &PyFloat_Type) {
      dst.operator[](_key) = std::to_string(PyFloat_AsDouble(vs));
    } else if (Py_TYPE(vs) == &PyList_Type) {
      dst.operator[](_key) = "[]py list is unsupported now, sorry...";
    } else {
      dst.operator[](_key) = PyUnicode_AsUTF8(vs);
    }
    Py_DECREF(ks);
    Py_DECREF(vs);
  }
}

PyObject *to_py_list(std::vector<std::string> &known_keys) {
  PyObject *pl = PyList_New((Py_ssize_t) known_keys.size());
  for (int i = 0; i < known_keys.size(); ++i) {
    PyList_SetItem(pl, i,  PyBytes_FromString(known_keys[i].data()));
  }
  return pl;
}

void test_algo_obj(PyObject *pm) {
  // 已知参数
  auto input_keys = std::map<std::string, std::string>();
  input_keys.operator[]("algo_name") = "算法名";
  input_keys.operator[]("audio_path") = "音频路径";
  input_keys.operator[]("output_path") = "输出 json 路径";
  input_keys.operator[]("model_size") = "模型大小";
  input_keys.operator[]("op_type") = "当前操作类型";
  input_keys.operator[]("prompt") = "语气提示词";
  input_keys.operator[]("language") = "当前语言";
  auto output_keys = std::map<std::string, std::string>();
  output_keys.operator[]("language") = "检测到的语言";
  output_keys.operator[]("duration") = "音频时长";
  output_keys.operator[]("data") = "文本数据";

  // 获取 AlgoProc 类
  PyObject *clazz = PyObject_GetAttrString(pm, "AlgoProcImpl");
  // 创建 AlgoProc 类实例
  PyObject *algo = PyObject_CallObject(clazz, Py_BuildValue("(sOO)", "prj-parse", to_py_dict(input_keys), to_py_dict(output_keys)));
  // 调用 AlgoProc 实例方法 process
  PyObject *func = PyObject_GetAttrString(algo, "process");
  printf("======> %s() pm: %p, func: %p\n", __func__, pm, func);
  if (PyCallable_Check(func)) {
    auto input_args = std::map<std::string, std::string>();
    input_args.operator[]("algo_name") = "prj-parse";
    input_args.operator[]("audio_path") = "/tmp/audio.wav";
    input_args.operator[]("output_path") = "/tmp/results.json";
    input_args.operator[]("model_size") = "medium";
    PyObject *ret = PyObject_CallObject(func, Py_BuildValue("(O)", to_py_dict(input_args)));
    if (ret == nullptr) {
      printf("PyObject_CallObject() failed!\n");
      PyErr_Print();
      return;
    }
    debug_py_obj(ret, __func__);
    PyObject *results_ = PyTuple_GetItem(ret, 0);
    auto results = std::map<std::string, std::string>();
    to_cpp_map(results_, results);
    for (const auto &item: results) {
      printf("res -> %s: %s\n", item.first.data(), item.second.data());
    }
    if (PyTuple_Size(ret) > 1) {
      PyObject *files_ = PyTuple_GetItem(ret, 1);
      auto files = std::map<std::string, std::string>();
      to_cpp_map(files_, files);
      for (const auto &item: files) {
        printf("files -> %s: %s\n", item.first.data(), item.second.data());
      }
    }
  }
  // 调用 AlgoProc 实例方法 release
  func = PyObject_GetAttrString(algo, "release");
  if (PyCallable_Check(func)) {
    PyObject_CallObject(func, nullptr);
  }
}

void test_get_list(PyObject *pm) {
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

void test_get_dict(PyObject *pm) {
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

void test_get_user(PyObject *pm) {
  // 调用无参函数
  PyObject *func = PyObject_GetAttrString(pm, "get_user");
  printf("======> %s() pm: %p, func: %p\n", __func__, pm, func);
  if (PyCallable_Check(func)) {
    PyObject *user = PyObject_CallObject(func, nullptr);
    debug_py_obj(user, __func__);
    func = PyObject_GetAttrString(user, "say_hello");
    printf("======> %s() pm: %p, func: %p\n", __func__, pm, func);
    if (PyCallable_Check(func)) {
      PyObject_CallObject(func, nullptr);
    }
  }
}

PyObject *wrapped_square(PyObject *, PyObject *args) {
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

PyObject *wrapped_post_value(PyObject *, PyObject *args) {
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

PyObject *wrapped_sum_int(PyObject *, PyObject *args) {
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

PyObject *wrapped_gen_path(PyObject *, PyObject *args, PyObject *kwargs) {
  // args 类型是 tuple, kwargs 类型为 dict
  debug_py_obj(args, "wrapped_gen_path() args");
  debug_py_obj(kwargs, "wrapped_gen_path() kwargs");

  // def generate_path(self, suffix: str = None, additional: str = None) -> str:
  char *suffix = nullptr;
  char *addition = nullptr;
  if (kwargs != nullptr) {
    static char *kwlist[] = { "suffix", "additional", nullptr };
    if (!PyArg_ParseTupleAndKeywords(args, kwargs, "|ss", (char **) kwlist, &suffix, &addition)) {
      PyErr_SetString(PyExc_RuntimeError, "Failed to parse gen_path() kwargs");
      return nullptr;
    }
    printf("kw args suffix: %s, additional: %s\n", suffix, addition);
  }

  std::stringstream path_;
  if (suffix != nullptr && addition != nullptr) {
    // 生成文件名，可以添加附带信息
    path_ << "/algo-server/tmp/xxx-" << addition << "." << suffix;
  } else if (suffix != nullptr) {
    // 生成文件名
    path_ << "/algo-server/tmp/xxx." << suffix;
  } else {
    // 生成目录，内部会先创建该目录
    path_ << "/algo-server/tmp/xxx";
  }
  return Py_BuildValue("s", path_.str().c_str());
}

static PyMethodDef module_methods[] = {
    {"square",     wrapped_square,                 METH_VARARGS, "A c++ square function."},
    {"post_value", wrapped_post_value,             METH_VARARGS, "A c++ post_value function."},
    {"sum_int",    wrapped_sum_int,                METH_VARARGS, "A c++ sum_int function."},
    {"gen_path",   (PyCFunction) wrapped_gen_path, METH_VARARGS | METH_KEYWORDS, "A c++ sum_int function."},
    {nullptr,      nullptr, 0,                                   nullptr}
};

static struct PyModuleDef M_native_functions = {
    PyModuleDef_HEAD_INIT,
    "native_functions",
    "c++ 中定义的函数",
    -1,
    module_methods,
};

PyMODINIT_FUNC PyInit_nativeFunctions(void) {
  PyObject *m = PyModule_Create(&M_native_functions);
  if (m == nullptr) {
    PyErr_Print();
    return nullptr;
  }
  return m;
}

void test_set_cpp_callback(PyObject *pm) {
  // 向 python 传递 c 函数指针
  PyObject *func = PyObject_GetAttrString(pm, "set_cpp_callback");
  printf("======> %s() pm: %p, func: %p\n", __func__, pm, func);
  if (PyCallable_Check(func)) {
    PyObject *r = PyObject_CallObject(func, Py_BuildValue("(O)", PyInit_nativeFunctions()));
    debug_py_obj(r, __func__);
    PyErr_Print();
  }
}

void test_do_hard_work(PyObject *pm) {
  // 调用无参函数
  PyObject *func = PyObject_GetAttrString(pm, "do_hard_work");
  printf("======> %s() pm: %p, func: %p\n", __func__, pm, func);
  if (PyCallable_Check(func)) {
    PyObject_CallObject(func, nullptr);
  }
}

std::string find_algo_impl_module(const std::string &who) {
  std::string algo_impl;
  char *cwd = getcwd(nullptr, 0);
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
  test_hello_world();

  // 导入当前目录
  PyRun_SimpleString("import sys\nsys.path.append('src')");
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
    test_algo_obj(pm);
  } else {
    PyErr_Print();
  }

  printf("\n%s() start test samples modules...\n", __func__);

  try {
    pm = PyImport_ImportModule("samples");
    PyErr_Print();
  } catch (std::exception &e) {
    printf("PyImport_ImportModule() failed: %s\n", e.what());
  }
  printf("%s() samples pm: %p\n", __func__, pm);
  if (pm != nullptr) {
    test_no_arg_func(pm);
    test_arg_func(pm);
    test_print_list(pm);
    test_print_dict(pm);
    test_obj_func(pm);
    test_get_list(pm);
    test_get_dict(pm);
    test_get_user(pm);

    // 测试给 python 设置 cpp 回调
    test_set_cpp_callback(pm);
    test_do_hard_work(pm);
  } else {
    PyErr_Print();
  }
  // 释放资源
  Py_Finalize();
  return 0;
}
