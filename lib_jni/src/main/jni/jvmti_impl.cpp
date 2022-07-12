//
// Created by binlee on 2022/7/11.
//

#include <cstring>
#include "jvmti_impl.h"
#include "jni_logger.h"
#include <sys/mman.h>
#include <sys/fcntl.h>
#include <unistd.h>

#define LOG_TAG "JVMTI_IMPL"

void callbackVMInit(jvmtiEnv *jvmti, JNIEnv *env, jthread thread) {
  ALOGE("%s", __func__)
  int fd = open("", O_RDWR | O_CREAT);
  int page_size = getpagesize();
  ftruncate(fd, page_size);
  void *buffer = mmap(nullptr, 1024 * 1024 * 4, PROT_WRITE, MAP_SHARED, fd, 0);
  close(fd);
  lseek(fd, 4, SEEK_END);
  memcpy(buffer, "", 0);
  munmap(buffer, 1024 * 1024 * 4);
}

void callbackVMDeath(jvmtiEnv *jvmti, JNIEnv *env) {
  ALOGE("%s", __func__)
}

void callbackVMObjectAlloc(jvmtiEnv *jvmti, JNIEnv *env, jthread thread, jobject object,
                           jclass klass, jlong size) {
  if (size < 150) return;
  char *className = {};
  jvmtiError error = jvmti->GetClassSignature(klass, &className, nullptr);
  if (error == JVMTI_ERROR_NONE && className != nullptr) {
    ALOGD("%s %s object is allocated with size %ld", __func__, className, size)
  }
  delete className;
}

void callbackObjectFree(jvmtiEnv *jvmti, jlong tag) {
  ALOGD("%s tag: %ld", __func__, tag)
}

void callbackException(jvmtiEnv *jvmti, JNIEnv *env, jthread thread, jmethodID method,
                       jlocation location, jobject exception, jmethodID catch_method,
                       jlocation catch_location) {
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
  ALOGE("%s in method: %s%s generic signature: %s", __func__, name, sig, gsig)
}

void callbackExceptionCatch(jvmtiEnv *jvmti, JNIEnv *env, jthread thread, jmethodID method,
                            jlocation location, jobject exception) {
  char *name = {};
  char *sig = {};
  char *gsig = {};
  jvmtiError error = jvmti->GetMethodName(method, &name, &sig, &gsig);
  if (error != JVMTI_ERROR_NONE) {
    ALOGE("%s GetMethodName error: %d", __func__, error)
    return;
  }
  ALOGE("%s in method: %s%s generic signature: %s", __func__, name, sig, gsig)
}

int Agent_Init(JavaVM *vm) {
  ALOGD("%s start GetJvmtiEnv", __func__)
  jvmtiEnv *jvmti = nullptr;
  // 支持的 jvmti 版本：JVMTI_VERSION_1_0、JVMTI_VERSION_1_1、JVMTI_VERSION_1_2
  if (vm->GetEnv((void **) &jvmti, JVMTI_VERSION_1_2) != JNI_OK || jvmti == nullptr) {
    ALOGE("%s GetJvmtiEnv error", __func__)
    return JNI_FALSE;
  }
  ALOGD("%s jvmti env: %p", __func__, jvmti)

  ALOGD("%s start GetJNIEnv", __func__)
  JNIEnv *env = nullptr;
  if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK || env == nullptr) {
    ALOGE("%s GetJNIEnv error", __func__)
    return JNI_FALSE;
  }
  ALOGD("%s JNI env: %p", __func__, env)

  jvmti::AgentData data;
  memset(&data, 0, sizeof(jvmti::AgentData));
  jvmti::gdata = &data;
  jvmti::gdata->isAgentInit = true;
  jvmti::gdata->jvmti = jvmti;

  jvmtiError error = JVMTI_ERROR_NONE;

  ALOGD("%s start CreateRawMonitor", __func__)
  error= jvmti->CreateRawMonitor("agent data", &data.lock);
  if (error != JVMTI_ERROR_NONE) {
    ALOGE("%s CreateRawMonitor error: %d", __func__, error)
    return JNI_FALSE;
  }
  ALOGD("%s raw monitor: %p", __func__, jvmti::gdata->lock)

  jvmtiCapabilities capa;
  memset(&capa, 0, sizeof(jvmtiCapabilities));
  error = jvmti->GetCapabilities(&capa);
  if (error != JVMTI_ERROR_NONE) {
    ALOGE("%s GetCapabilities error: %d", __func__, error)
  }
  capa.can_signal_thread = 1;
  capa.can_get_owned_monitor_info = 1;
  capa.can_generate_method_entry_events = 1;
  capa.can_generate_exception_events = 1;
  capa.can_generate_vm_object_alloc_events = 1;
  capa.can_tag_objects = 1;
  error = jvmti->AddCapabilities(&capa);
  // check_jvmti_error(jvmti, error, "Unable to get necessary JVMTI capabilities");
  if (error != JVMTI_ERROR_NONE) {
    ALOGE("%s AddCapabilities error: %d", __func__, error)
  }

  error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_INIT, nullptr);
  error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_DEATH, nullptr);

  error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_OBJECT_ALLOC, nullptr);
  error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_OBJECT_FREE, nullptr);

  error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_EXCEPTION, nullptr);
  error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_EXCEPTION_CATCH, nullptr);
  // check_jvmti_error(jvmti, error, "Can not set event notification");
  if (error != JVMTI_ERROR_NONE) {
    ALOGE("%s SetEventNotificationMode error: %d", __func__, error)
  }

  jvmtiEventCallbacks callbacks = {};
  memset(&callbacks, 0, sizeof(jvmtiEventCallbacks));
  callbacks.VMInit = &callbackVMInit;
  callbacks.VMDeath = &callbackVMDeath;
  callbacks.VMObjectAlloc = &callbackVMObjectAlloc;
  callbacks.ObjectFree = &callbackObjectFree;
  callbacks.Exception = &callbackException;
  callbacks.ExceptionCatch = &callbackExceptionCatch;
  error = jvmti->SetEventCallbacks(&callbacks, sizeof(callbacks));
  // check_jvmti_error(jvmti, error, "Can not set JVMTI callbacks");
  if (error != JVMTI_ERROR_NONE) {
    ALOGE("%s SetEventCallbacks error: %d", __func__, error)
  }
  return JNI_OK;
}

jint Agent_OnLoad(JavaVM *vm, char *options, void *reserved) {
  ALOGD("%s options: %s", __func__, options)
  return JNI_TRUE;
}

jint Agent_OnAttach(JavaVM* vm, char* options, void* reserved) {
  ALOGD("%s options: %s", __func__, options)
  return Agent_Init(vm);
}

void Agent_OnUnload(JavaVM *vm) {
  ALOGD("%s", __func__)
}