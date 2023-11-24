#include <python3.10/Python.h>
#include <dirent.h>
#include <string>
#include <map>
#include <vector>
#include <sstream>

#include "cc/algo.pb.h"
#include "cc/py_algo.pb.h"
#include "cc/audio_whisper.pb.h"

#include "lib_main.h"

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
    PyList_SetItem(pl, i, PyBytes_FromString(known_keys[i].data()));
  }
  return pl;
}

void test_algo_obj(PyObject *pm) {
  // 获取 AlgoProc 类
  PyObject *clazz = PyObject_GetAttrString(pm, "AlgoProcImpl");
  // 创建 AlgoProc 类实例
  PyObject *algo = PyObject_CallObject(clazz, nullptr);
  // 调用 AlgoProc 实例方法 init
  PyObject *func = PyObject_GetAttrString(algo, "init");
  printf("======> %s() pm: %p, init func: %p\n", __func__, pm, func);
  if (PyCallable_Check(func)) {
    auto req = py_algopb::PyAlgoRequest();
    req.set_algo_name("prj-parse");
    auto whisper = audio_whisperpb::AudioWhisperRequest();
    whisper.set_language("en");
    whisper.set_op_type(audio_whisperpb::Whisper);
    whisper.set_audio_url("https://www.example.com/demo.wav");
    whisper.set_model_size("medium");
    req.set_request_buf(whisper.SerializeAsString());

    auto urls = algopb::AlgoDownloadUrlMap();
    urls.mutable_kvs()->operator[]("audio_url").set_url("https://www.example.com/a.index");
    urls.mutable_kvs()->operator[]("audio_url").set_is_unzip(true);
    urls.mutable_kvs()->operator[]("audio_url").set_is_cache(true);
    req.set_download_urls_buf(urls.SerializeAsString());

    PyObject_CallObject(func, Py_BuildValue("(O)", PyBytes_FromString(req.SerializeAsString().c_str())));
    if (!PyErr_CheckSignals()) {
      PyErr_Print();
    }
  }

  // 调用 AlgoProc 实例方法 process
  func = PyObject_GetAttrString(algo, "process");
  printf("======> %s() pm: %p, process func: %p\n", __func__, pm, func);
  if (PyCallable_Check(func)) {
    PyObject *ret = PyObject_CallObject(func, nullptr);
    if (ret == nullptr) {
      printf("PyObject_CallObject('process') failed!\n");
      PyErr_Print();
      return;
    }
    debug_py_obj(ret, __func__);
    auto resp = py_algopb::PyAlgoResponse();
    resp.ParseFromString(PyBytes_AsString(ret));
    printf("%s() response: %s\n", __func__, resp.SerializeAsString().c_str());
  }
  // 调用 AlgoProc 实例方法 release
  func = PyObject_GetAttrString(algo, "release");
  printf("======> %s() pm: %p, release func: %p\n", __func__, pm, func);
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
    static char *kwlist[] = {(char *) "suffix", (char *) "additional", nullptr};
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

PyObject *wrapped_on_progress(PyObject *, PyObject *args) {
  // debug_py_obj(args, __func__);
  float progress;
  if (!PyArg_ParseTuple(args, "f", &progress)) {
    PyErr_SetString(PyExc_RuntimeError, "Failed to parse python args");
    return nullptr;
  }
  if ((int) progress % 10 == 0) {
    printf("%s() progress: %.2f%%\n", __func__, progress);
  }
  Py_RETURN_NONE;
}

static PyMethodDef module_methods[] = {
    {"square",      wrapped_square,                 METH_VARARGS, "A c++ square function."},
    {"post_value",  wrapped_post_value,             METH_VARARGS, "A c++ post_value function."},
    {"sum_int",     wrapped_sum_int,                METH_VARARGS, "A c++ sum_int function."},
    {"gen_path",    (PyCFunction) wrapped_gen_path, METH_VARARGS | METH_KEYWORDS, "A c++ sum_int function."},
    {"on_progress", wrapped_on_progress,            METH_VARARGS, "A c++ on_progress function."},
    {nullptr,       nullptr, 0,                                   nullptr}
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

void test_protobuf(PyObject *pm) {
  // 调用无参函数
  PyObject *func = PyObject_GetAttrString(pm, "parse_protobuf");
  printf("======> %s() pm: %p, func: %p, proto version: %d\n", __func__, pm, func, GOOGLE_PROTOBUF_VERSION);
  if (PyCallable_Check(func)) {
    auto url = algopb::AlgoDownloadUrl();
    url.set_url("https://example.com/index.html");
    url.set_is_local_file(false);
    url.set_is_cache(true);
    PyObject_CallObject(func, Py_BuildValue("(S)", PyBytes_FromString(url.SerializeAsString().c_str())));
    PyErr_Print();
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

int run_main_test() {
  // 初始化
  Py_Initialize();
  test_hello_world();

  // 导入当前目录
  PyRun_SimpleString("import sys\nsys.path.append('./src')");
  PyObject *pm = nullptr;

  // 找到并导入当前算法脚本文件，命名规则要符合 algo_xxx_impl.py
  auto algo_module = find_algo_impl_module("src");
  if (algo_module.empty()) {
    perror("cannot find an algo impl module.\n");
    return -1;
  }
  printf("%s() find algo impl module: %s\n", __func__, algo_module.c_str());
  pm = PyImport_ImportModule(algo_module.c_str());
  std::cout << "run_main_test() algo impl pm: " << pm << std::endl;
  if (pm != nullptr) {
    test_algo_obj(pm);
  } else {
    PyErr_Print();
  }

  return 0;

  printf("\n%s() start test samples modules...\n", __func__);

  try {
    pm = PyImport_ImportModule("samples");
    PyErr_Print();
  } catch (std::exception &e) {
    printf("PyImport_ImportModule() failed: %s\n", e.what());
  }
  printf("%s() samples pm: %p\n", __func__, pm);
  if (pm != nullptr) {
    // test_no_arg_func(pm);
    // test_arg_func(pm);
    // test_print_list(pm);
    // test_print_dict(pm);
    // test_obj_func(pm);
    // test_get_list(pm);
    // test_get_dict(pm);
    // test_get_user(pm);
    //
    // // 测试给 python 设置 cpp 回调
    test_set_cpp_callback(pm);
    test_do_hard_work(pm);

    // 测试 proto 数据传递
    // test_protobuf(pm);
  } else {
    PyErr_Print();
  }
  // 释放资源
  Py_Finalize();
  return 0;
}
