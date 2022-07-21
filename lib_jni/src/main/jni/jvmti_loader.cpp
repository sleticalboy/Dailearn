//
// Created by binlee on 2022/7/11.
//

#include "jvmti_loader.h"
#include "jni_logger.h"
#include <android/api-level.h>

#define LOG_TAG "JVMTI_LOADER"

namespace jvmti {
namespace loader {

void attachAgent(JNIEnv *env, const char *library) {
  jstring _library = env->NewStringUTF(library);
  ALOGD("%s start attaching jvmti agent via reflect: %s \nenv: %p, lib: %p", __func__, library, env, _library)
  if (android_get_device_api_level() >= __ANDROID_API_P__) {
    jclass cls_debug = env->FindClass("android/os/Debug");
    jmethodID method = env->GetStaticMethodID(cls_debug, "attachJvmtiAgent",
                                              "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V");
    ALOGD("%s start call Debug#attachJvmtiAgent()", __func__)
    env->CallStaticVoidMethod(cls_debug, method,
                              _library/*library*/,
                              (jstring) nullptr /*options*/,
                              (jobject) nullptr /*classloader*/);
  } else {
    jclass cls_vm_debug = env->FindClass("dalvik/system/VMDebug");
    jmethodID method = env->GetStaticMethodID(cls_vm_debug, "attachAgent", "(Ljava/lang/String;)V");
    ALOGD("%s start call VMDebug#attachAgent()", __func__)
    env->CallStaticVoidMethod(cls_vm_debug, method,
                              _library/*library*/);
  }
  if (env->ExceptionCheck()) {
    ALOGE("%s exception checked:", __func__)
    env->ExceptionDescribe();
    env->ExceptionClear();
  }
  env->ReleaseStringUTFChars(_library, library);
  ALOGD("%s attach jvmti agent finished", __func__)
}

} // namespace loader
} // namespace jvmti
