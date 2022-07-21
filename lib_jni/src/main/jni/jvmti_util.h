//
// Created by binlee on 2022/7/15.
//

#include <string>
#include <jni.h>
#include "jvmti_config.h"

#ifndef DAILY_WORK_JVMTI_UTIL_H
#define DAILY_WORK_JVMTI_UTIL_H

namespace jvmti {

// 获取线程信息：名字、组、id
std::string getThreadInfo(jvmtiEnv *jvmti, jthread thread);

// 获取方法信息
std::string getMethodInfo(jvmtiEnv *jvmti, jmethodID method);

// 获取类信息
std::string getClassInfo(jvmtiEnv *jvmti, jclass klass, jlong size = 0);

// 获取错误名
const char *getErrorName(jvmtiEnv *jvmti, jvmtiError &error);

// java 结构体转 cpp 结构体
void fromJavaConfig(JNIEnv *env, jobject jConfig, Config *config);
} // namespace jvmti

#endif //DAILY_WORK_JVMTI_UTIL_H
