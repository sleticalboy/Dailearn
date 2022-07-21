#include <jni.h>
#include <string>
#include "jni_logger.h"
#include "jvmti_loader.h"
#include "jvmti_util.h"

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

void LibJni_nativeLoadJvmti(JNIEnv *env, jclass clazz, jobject jConfig) {
  static jvmti::Config config;
  memset(&config, 0, sizeof(jvmti::Config));
  jvmti::util::fromJavaConfig(env, jConfig, &config);
  jvmti::g_config = &config;
  ALOGI("%s, jvmti::g_config: %p, config: %p", __func__, jvmti::g_config, &config)

  jclass cls_config = env->GetObjectClass(jConfig);
  jfieldID field_agent_file = env->GetFieldID(cls_config, "agentFile", "Ljava/lang/String;");
  auto j_str = (jstring) env->GetObjectField(jConfig, field_agent_file);
  const char *library = env->GetStringUTFChars(j_str, JNI_FALSE);
  jvmti::loader::attachAgent(env, library);
  env->ReleaseStringUTFChars(j_str, library);
}

JNINativeMethod methods[] = {
  // com.binlee.sample.jni.LibJni.nativeGetString
  {"nativeGetString", "()Ljava/lang/String;",                   (void *) LibJni_nativeGetString},
  {"nativeCallJava",  "(Landroid/content/Context;)V",           (void *) LibJni_nativeCallJava},
  {"nativeLoadJvmti", "(Lcom/binlee/sample/jni/JvmtiConfig;)V", (void *) LibJni_nativeLoadJvmti}
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