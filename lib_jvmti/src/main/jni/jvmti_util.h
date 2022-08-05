//
// Created by binlee on 2022/7/15.
//

#include <string>
#include <jni.h>
#include "jvmti_config.h"

#ifndef DAILY_WORK_JVMTI_UTIL_H
#define DAILY_WORK_JVMTI_UTIL_H

namespace jvmti {
namespace util {

class AllocInfo {
private:
  char *data_ = nullptr;
public:
  AllocInfo(jvmtiEnv *jvmti, jclass klass, jlong size);

  const char *ToString() {
    return data_;
  }

  ~AllocInfo() {
    delete[] data_;
  }
};

/**
 * 获取线程信息：名字、组、id
 */
class ThreadInfo {
private:
  char *data_;
public:
  ThreadInfo(jvmtiEnv *jvmti, jthread thread);

  const char *ToString() {
    return data_;
  }

  ~ThreadInfo() {
    delete[] data_;
  }
};

/**
 * 获取类信息
 */
class ClassInfo {
private:
  char *data_;
public:
  ClassInfo(jvmtiEnv *jvmti, jclass clazz);

  const char *ToString() {
    return data_;
  }

  ~ClassInfo() {
    delete[] data_;
  }
};

/**
 * 获取方法信息
 */
class MethodInfo {
private:
  char *data_;
  bool printable_ = false;
public:
  MethodInfo(jvmtiEnv *jvmti, jmethodID method);

  bool Printable() {
    return printable_;
  }

  const char *ToString() {
    return data_;
  }

  ~MethodInfo() {
    delete[] data_;
  }
};

// 获取错误名
const char *GetErrStr(jvmtiEnv *jvmti, jvmtiError &error);

// java options 转 cpp 结构体
JvmtiOptions *ParseOptions(const char *options);

} // namespace util
} // namespace jvmti

#endif //DAILY_WORK_JVMTI_UTIL_H
