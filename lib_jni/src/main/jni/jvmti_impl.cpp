//
// Created by binlee on 2022/7/11.
//

#include <cstring>
#include "jvmti_impl.h"
#include "jni_logger.h"

#define LOG_TAG "JVMTI_IMPL"

void jvmti::callbackVMInit(jvmtiEnv *jvmti, JNIEnv *env, jthread thread) {
  ALOGE("%s", __func__)
}

void jvmti::callbackVMDeath(jvmtiEnv *jvmti, JNIEnv *env) {
  ALOGE("%s", __func__)
}

void jvmti::callbackVMObjectAlloc(jvmtiEnv *jvmti, JNIEnv *env, jthread thread, jobject object,
                           jclass klass, jlong size) {
  ALOGE("%s", __func__)
  if (size < 50) return;
  char *className = {};
  jvmtiError error = jvmti->GetClassSignature(klass, &className, nullptr);
  if (error == JVMTI_ERROR_NONE && className != nullptr) {
    ALOGE("%s %s object is allocated with size %ld", __func__, className, size)
  }
  delete className;
}

void jvmti::callbackException(jvmtiEnv *jvmti, JNIEnv *env, jthread thread, jmethodID method,
                       jlocation location, jobject exception, jmethodID catch_method,
                       jlocation catch_location) {
  ALOGE("%s", __func__)
  char *name = {};
  char *sig = {};
  char *gsig = {};
  jvmtiError error = jvmti->GetMethodName(method, &name, &sig, &gsig);
  jvmtiThreadInfo info = {};
  error = jvmti->GetThreadInfo(thread, &info);
  if (error == JVMTI_ERROR_NONE) {
    jvmtiThreadGroupInfo grouInfo = {};
    error = jvmti->GetThreadGroupInfo(info.thread_group, &grouInfo);
    if (error == JVMTI_ERROR_NONE) {
      ALOGE("%s Got exception event, thread: %s, group: %s", __func__, info.name, grouInfo.name)
    }
  }
  jobject *monitors = {};
  error = jvmti->GetOwnedMonitorInfo(thread, 0, &monitors);
  if (error == JVMTI_ERROR_NONE) {
    ALOGE("%s ", __func__)
  }
  ALOGE("%s exception in method: %s%s%s", __func__, name, sig, gsig)
}

jint Agent_OnLoad(JavaVM *vm, char *options, void *reserved) {
  ALOGD("%s", __func__)

  static jvmtiEnv *jvmti = nullptr;
  if (vm->GetEnv((void **) &jvmti, JNI_VERSION_1_6) != JNI_OK || jvmti == nullptr) {
    ALOGE("%s GetEnv error", __func__)
    return JNI_FALSE;
  }

  static jvmti::AgentData data;
  memset(&data, 0, sizeof(jvmti::AgentData));
  jvmti::gdata = &data;

  jvmti::gdata->jvmti = jvmti;

  jvmtiError error;
  error = jvmti->CreateRawMonitor("agent data", &jvmti::gdata->lock);
  if (error != JVMTI_ERROR_NONE) {
    ALOGE("%s CreateRawMonitor error: %d", __func__, error)
    return JNI_FALSE;
  }

  static jvmtiCapabilities capa;
  memset(&capa, 0, sizeof(jvmtiCapabilities));
  capa.can_signal_thread = 1;
  capa.can_get_owned_monitor_info = 1;
  capa.can_generate_method_entry_events = 1;
  capa.can_generate_exception_events = 1;
  capa.can_generate_vm_object_alloc_events = 1;
  capa.can_tag_objects = 1;
  jvmti->AddCapabilities(&capa);
  // check_jvmti_error(jvmti, error, "Unable to get necessary JVMTI capabilities");

  jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_INIT, nullptr);
  jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_DEATH, nullptr);
  jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_OBJECT_ALLOC, nullptr);
  // check_jvmti_error(jvmti, error, "Can not set event notification");

  jvmtiEventCallbacks callbacks = {};
  memset(&callbacks, 0, sizeof(jvmtiEventCallbacks));
  callbacks.VMInit = &jvmti::callbackVMInit;
  callbacks.VMDeath = &jvmti::callbackVMDeath;
  callbacks.VMObjectAlloc = &jvmti::callbackVMObjectAlloc;
  callbacks.Exception = &jvmti::callbackException;
  jvmti->SetEventCallbacks(&callbacks, sizeof(callbacks));
  // check_jvmti_error(jvmti, error, "Can not set JVMTI callbacks");
  return JNI_OK;
}

jint Agent_OnAttach(JavaVM* vm, char* options, void* reserved) {
  ALOGD("%s", __func__)
  return JNI_OK;
}

void Agent_OnUnload(JavaVM *vm) {
  ALOGD("%s", __func__)
}