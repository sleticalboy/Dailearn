#include <jni.h>
#include <string>
#include "jni_logger.h"
#include "jvmti_loader.h"

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

jstring LibJni_nativeGetString(JNIEnv *env, jclass clazz) {
  std::string string = "this string is from native via jni.";
  return env->NewStringUTF(string.c_str());
}

void LibJni_nativeCallJava(JNIEnv *env, jclass clazz, jobject jContext) {
  jclass cls_toast = env->FindClass("android/widget/Toast");
  jmethodID method = env->GetStaticMethodID(cls_toast, "makeText",
                         "(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;");
  jobject toast = env->CallStaticObjectMethod(cls_toast , method,
                              jContext, env->NewStringUTF("native toast"), 0);
  method = env->GetMethodID(cls_toast, "show", "()V");
  env->CallVoidMethod(toast, method);

  env->DeleteLocalRef(cls_toast);
  env->DeleteLocalRef(toast);
}

void LibJni_nativeLoadJvmti(JNIEnv *env, jclass clazz, jstring library) {
  jvmti::attachAgent(env, env->GetStringUTFChars(library, JNI_FALSE));
}

JNINativeMethod methods[] = {
  // com.binlee.sample.jni.LibJni.nativeGetString
  {"nativeGetString", "()Ljava/lang/String;",         (void *) LibJni_nativeGetString},
  {"nativeCallJava",  "(Landroid/content/Context;)V", (void *) LibJni_nativeCallJava},
  {"nativeLoadJvmti", "(Ljava/lang/String;)V",        (void *) LibJni_nativeLoadJvmti}
};

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  JNIEnv *env = nullptr;
  if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK || env == nullptr) {
    return JNI_FALSE;
  }
  ALOGD("%s reserved: %p", __func__, reserved)
  jclass cls_libJni = env->FindClass("com/binlee/sample/jni/LibJni");
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
  jclass cls_jniLib = env->FindClass("com/binlee/sample/jni/LibJni");
  env->UnregisterNatives(cls_jniLib);
  env->DeleteLocalRef(cls_jniLib);
  free(env);
}