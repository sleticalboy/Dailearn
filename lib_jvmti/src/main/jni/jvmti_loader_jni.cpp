#include <jni.h>
#include <string>
#include <android/api-level.h>
#include "jni_logger.h"
#include "jvmti_util.h"

#define LOG_TAG "JvmtiLoader"

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

void JvmtiLoader_nativeAttachAgent(JNIEnv *env, jclass clazz, jobject jConfig) {
  static jvmti::Config config;
  memset(&config, 0, sizeof(jvmti::Config));
  jvmti::util::fromJavaConfig(env, jConfig, &config);
  jvmti::g_config = &config;
  ALOGI("%s, jvmti::g_config: %p, config: %p", __func__, jvmti::g_config, &config)

  jclass cls_config = env->GetObjectClass(jConfig);
  jfieldID field_agent_file = env->GetFieldID(cls_config, "agentFile", "Ljava/lang/String;");
  auto j_str = (jstring) env->GetObjectField(jConfig, field_agent_file);
  const char *library = env->GetStringUTFChars(j_str, JNI_FALSE);
  attachAgent(env, library);
  env->ReleaseStringUTFChars(j_str, library);
}

JNINativeMethod methods[] = {
  // com.binlee.sample.jni.LibJni.nativeGetString
  {"nativeAttachAgent", "(Lcom/binlee/sample/jni/JvmtiConfig;)V", (void *) JvmtiLoader_nativeAttachAgent}
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