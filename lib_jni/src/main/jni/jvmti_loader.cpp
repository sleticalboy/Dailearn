//
// Created by binlee on 2022/7/11.
//

#include "jvmti_loader.h"
#include <android/api-level.h>

void jvmti::attachAgent(JNIEnv *env, jstring library) {
  if (android_get_device_api_level() >= __ANDROID_API_P__) {
    jclass cls_debug = env->FindClass("android/os/Debug");
    jmethodID method = env->GetStaticMethodID(cls_debug, "attachJvmtiAgent",
                                              "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V");
    env->CallStaticVoidMethod(cls_debug, method,
                              library/*library*/,
                              (jstring) nullptr /*options*/,
                              (jobject) nullptr /*classloader*/);
  } else {
    jclass cls_vm_debug = env->FindClass("dalvik/system/VMDebug");
    jmethodID method = env->GetStaticMethodID(cls_vm_debug, "attachAent", "(Ljava/lang/String;)V");
    env->CallStaticVoidMethod(cls_vm_debug, method,
                              library/*library*/);
  }
  if (env->ExceptionCheck()) {
    env->ExceptionDescribe();
    env->ExceptionClear();
  }
}
