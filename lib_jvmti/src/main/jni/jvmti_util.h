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
  std::string data_;
public:
  AllocInfo(jvmtiEnv *jvmti, jclass klass, jlong size);

  std::string ToString() {
    return data_;
  }

  ~AllocInfo() {
    data_.clear();
  }
};

/**
 * 获取线程信息：名字、组、id
 */
class ThreadInfo {
private:
  std::string data_;
public:
  ThreadInfo(jvmtiEnv *jvmti, jthread thread);

  std::string ToString() {
    return data_;
  }

  ~ThreadInfo() {
    data_.clear();
  }
};

/**
 * 获取类信息
 */
class ClassInfo {
private:
  std::string data_;
public:
  ClassInfo(jvmtiEnv *jvmti, jclass clazz);

  std::string ToString() {
    return data_;
  }

  ~ClassInfo() {
    data_.clear();
  }
};

/**
 * 获取方法信息
 */
class MethodInfo {
private:
  std::string data_;
  bool printable_ = false;
public:
  MethodInfo(jvmtiEnv *jvmti, jmethodID method);

  bool Printable() {
    return printable_;
  }

  std::string ToString() {
    return data_;
  }

  ~MethodInfo() {
    data_.clear();
  }
};

// 获取错误名
const char *getErrorName(jvmtiEnv *jvmti, jvmtiError &error);

// java 结构体转 cpp 结构体
void fromJavaConfig(JNIEnv *env, jobject jConfig, Config *config);
} // namespace util
} // namespace jvmti

#endif //DAILY_WORK_JVMTI_UTIL_H
