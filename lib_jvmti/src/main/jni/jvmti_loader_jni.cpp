#include <jni.h>
#include <string>
#include <android/api-level.h>
#include "jni_logger.h"
#include "jvmti_util.h"

#define LOG_TAG "JvmtiLoader"

void attachAgent(JNIEnv *env, const char *library, const char *options) {
  ALOGD("%s enter env: %p\nlib: %s\noptions: %s", __func__, env, library, options)
  jstring lib = nullptr;
  jstring opt = env->NewStringUTF(options);
  if (android_get_device_api_level() >= __ANDROID_API_P__) {
    jclass cls_debug = env->FindClass("android/os/Debug");
    jmethodID method = env->GetStaticMethodID(cls_debug, "attachJvmtiAgent",
                                              "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V");
    ALOGD("%s start call Debug#attachJvmtiAgent()", __func__)
    lib = env->NewStringUTF(library);
    env->CallStaticVoidMethod(cls_debug, method, lib/*library*/, opt/*options*/, (jobject) nullptr /*classloader*/);
  } else {
    jclass cls_vm_debug = env->FindClass("dalvik/system/VMDebug");
    jmethodID method = env->GetStaticMethodID(cls_vm_debug, "attachAgent", "(Ljava/lang/String;)V");
    char *param = new char[strlen(library) + strlen(options) + 10];
    sprintf(param, "%s=%s", library, options);
    ALOGD("%s start call VMDebug#attachAgent(%s)", __func__, param)
    lib = env->NewStringUTF(param);
    env->CallStaticVoidMethod(cls_vm_debug, method, lib/*library*/);
  }
  if (env->ExceptionCheck()) {
    ALOGE("%s exception checked:", __func__)
    env->ExceptionDescribe();
    env->ExceptionClear();
  }
  env->ReleaseStringUTFChars(lib, library);
  env->ReleaseStringUTFChars(opt, options);
  ALOGD("%s attach jvmti agent finished", __func__)
}

void JvmtiLoader_nativeAttachAgent(JNIEnv *env, jclass clazz, jstring jLibrary, jstring jOptions) {
  const char *library = env->GetStringUTFChars(jLibrary, JNI_FALSE);
  const char *options = env->GetStringUTFChars(jOptions, JNI_FALSE);
  ALOGI("%s, library: %s, options: %s", __func__, library, options)

  attachAgent(env, library, options);

  env->ReleaseStringUTFChars(jLibrary, library);
  env->ReleaseStringUTFChars(jOptions, options);
}

JNINativeMethod methods[] = {
  // com.binlee.sample.jni.LibJni.nativeGetString
  {"nativeAttachAgent", "(Ljava/lang/String;Ljava/lang/String;)V", (void *) JvmtiLoader_nativeAttachAgent}
};

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  JNIEnv *env = nullptr;
  if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK || env == nullptr) {
    return JNI_FALSE;
  }
  ALOGD("%s reserved: %p", __func__, reserved)
  jclass cls_libJni = env->FindClass("com/binlee/apm/jvmti/JvmtiLoader");
  if (env->RegisterNatives(cls_libJni, methods, sizeof(methods) / sizeof(JNINativeMethod)) < 0) {
    ALOGE("%s RegisterNatives error", __func__)
  }
  env->DeleteLocalRef(cls_libJni);
  return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
  JNIEnv *env = nullptr;
  if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK || env == nullptr) {
    return;
  }
  ALOGD("%s reserved: %p", __func__, reserved)
  jclass cls_jniLib = env->FindClass("com/binlee/apm/jvmti/JvmtiLoader");
  env->UnregisterNatives(cls_jniLib);
  env->DeleteLocalRef(cls_jniLib);
  free(env);
}