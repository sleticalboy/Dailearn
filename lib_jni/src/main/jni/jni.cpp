#include <jni.h>
#include <string>
#include <android/log.h>

// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("jni");
//    }

#define LOG_TAG "LibJni"

#ifndef ALOGE
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);
#endif

jstring LibJni_nativeGetString(JNIEnv *env, jclass clazz) {
  std::string string = "this string is from native via jni.";
  return env->NewStringUTF(string.c_str());
}

JNINativeMethod methods[] = {
  // com.binlee.sample.jni.LibJni.nativeGetString
  {
     "nativeGetString", "()Ljava/lang/String;", (void *)LibJni_nativeGetString
  },
};

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  JNIEnv *env = nullptr;
  if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK || env == nullptr) {
    return JNI_FALSE;
  }
  jclass cls_libJni = env->FindClass("com/binlee/sample/jni/LibJni");
  if (env->RegisterNatives(cls_libJni, methods, sizeof(methods) / sizeof(JNINativeMethod)) < 0) {
    ALOGE("%s RegisterNatives error", __func__)
  }
  env->DeleteLocalRef(cls_libJni);
  return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
  JNIEnv *env = nullptr;
  if (vm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK || env == nullptr) {
    return;
  }
  jclass cls_jniLib = env->FindClass("com/binlee/sample/jni/LibJni");
  env->UnregisterNatives(cls_jniLib);
  env->DeleteLocalRef(cls_jniLib);
  free(env);
}