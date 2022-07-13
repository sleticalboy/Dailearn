//
// Created by binlee on 2022/7/11.
//

#include <cstring>
#include "jvmti_impl.h"
#include "jni_logger.h"
#include <sys/mman.h>
#include <sys/fcntl.h>
#include <unistd.h>
#include <string>
#include <map>
#include <cerrno>

#define LOG_TAG "JVMTI_IMPL"

void dispatchDataPersist(const char *tag, const char *data) {
  int fd = open("/sdcard/ttt.txt", O_RDWR);
  ALOGD("%s open fd: %d, err: %d", __func__, fd, errno)
  if (errno) {
    ALOGE("%s abort as errno: %d", __func__, errno)
    return;
  }
  int page_size = getpagesize();
  ALOGD("%s page size: %d", __func__, page_size)
  int res = ftruncate(fd, page_size);
  ALOGD("%s ftruncate res: %d", __func__, res)
  void *buffer = mmap(nullptr, 1024 * 1024 * 4, PROT_WRITE, MAP_SHARED, fd, 0);
  ALOGD("%s mmap buffer: %p", __func__, buffer)
  res = lseek(fd, 4, SEEK_END);
  ALOGD("%s lseek res: %d", __func__, res)
  res = close(fd);
  ALOGD("%s close res: %d", __func__, res)
  memcpy(buffer, data, strlen(data));
  res = munmap(buffer, 1024 * 1024 * 4);
  ALOGD("%s munmap res: %d", __func__, res)
}

void callbackVMInit(jvmtiEnv *jvmti, JNIEnv *env, jthread thread) {
  ALOGE("%s", __func__)
}

void callbackVMDeath(jvmtiEnv *jvmti, JNIEnv *env) {
  ALOGE("%s", __func__)
}

// 线程信息：名字、组、id
std::string fillThreadInfo(jvmtiEnv *jvmti, jthread thread) {
  std::string buffer;
  jvmtiThreadInfo info = {};
  jvmtiError error = jvmti->GetThreadInfo(thread, &info);
  if (error == JVMTI_ERROR_NONE) {
    buffer.append("thread(").append(info.name) += "), priority: "
      + std::to_string(info.priority) + ", daemon: " + (info.is_daemon ? "true" : "false");
    jvmtiThreadGroupInfo gInfo = {};
    error = jvmti->GetThreadGroupInfo(info.thread_group, &gInfo);
    if (error == JVMTI_ERROR_NONE) {
      buffer.append(", group(").append(gInfo.name) += ") priority: "
        + std::string(gInfo.name) + ", daemon: " + (gInfo.is_daemon ? "true" : "false");
    } else {
      ALOGE("%s GetThreadGroupInfo error: %d", __func__, error)
    }
  } else {
    ALOGE("%s GetThreadInfo error: %d", __func__, error)
  }
  return buffer;
}

std::string fillMethodInfo(jvmtiEnv *jvmti, jmethodID method) {
  std::string buffer;
  char *name = {};
  char *sig = {};
  char *gsig = {};
  jvmtiError error = jvmti->GetMethodName(method, &name, &sig, &gsig);
  if (error == JVMTI_ERROR_NONE) {
    buffer.append(name).append(sig).append(gsig == nullptr ? "" : gsig);
  } else {
    ALOGE("%s GetMethodName error: %d", __func__, error)
  }
  return buffer;
}

std::string fillClassInfo(jvmtiEnv *jvmti, jclass klass, jlong size = 0) {
  std::string buffer;

  char *className = {};
  jvmtiError error = jvmti->GetClassSignature(klass, &className, nullptr);
  if (error == JVMTI_ERROR_NONE) {
    buffer.append("class: ").append(className);
  }
  if (size > 0) {
    buffer.append(", size: ") += std::to_string(size);
  }
  delete className;
  return buffer;
}

// 将数据持久化
void persistJson(std::map<std::string, std::string> *json, const char *tag) {
  std::string buffer;
  std::map<std::string, std::string>::iterator it;
  for (it = json->begin(); it != json->end(); it++) {
    buffer.append(it->first).append(": ").append(it->second).append("\n");
  }
  // 数据持久化
  try {
    dispatchDataPersist(tag, buffer.c_str());
  } catch (const std::exception &e) {
    ALOGE("%s error: %s", __func__, e.what())
  }
  // 这里打印日志到控制台
  ALOGD("%s %s", tag, buffer.c_str())
  // 释放内存
  json->clear();
  free(json);
}

void callbackVMObjectAlloc(jvmtiEnv *jvmti, JNIEnv *env, jthread thread, jobject object,
                           jclass klass, jlong size) {
  // 哪个线程创建了哪个类的实例对象、分配了多少空间、
  if (size < 1024) return;

  // 声明 map 存储信息
  std::map<std::string, std::string> json = {};

  // 填充类信息
  json["class_info"] = fillClassInfo(jvmti, klass, size);
  // 填充线程信息
  json["thread"] = fillThreadInfo(jvmti, thread);
  
  // 给 object 打标记
  jint hc;
  jvmti->GetObjectHashCode(object, &hc);
  jvmti->SetTag(object, hc);

  persistJson(&json, __func__);
}

void callbackObjectFree(jvmtiEnv *jvmti, jlong tag) {
  jint count;
  jobject *objs = {};
  jlong *tags = {};
  jvmtiError error = jvmti->GetObjectsWithTags(1, &tag, &count, &objs, &tags);
  if (error != JVMTI_ERROR_NONE) {
    if (count > 0) {
      // 遍历所有获取的对象
      for (int i = 0; i < count; ++i) {
        // 输出被释放的对象信息
      }
    } else {
      ALOGE("%s GetObjectsWithTags count %d", __func__, count)
    }
  } else {
    ALOGE("%s GetObjectsWithTags error %d", __func__, error)
  }
  ALOGD("%s tag: %ld", __func__, tag)
}

void callbackException(jvmtiEnv *jvmti, JNIEnv *env, jthread thread, jmethodID method,
                       jlocation location, jobject exception, jmethodID catch_method,
                       jlocation catch_location) {
  // 声明 map 存储信息
  std::map<std::string, std::string> json = {};
  // 填充方法信息
  json["method"] = fillMethodInfo(jvmti, method);
  // 填充异常信息
  jclass klazz = env->GetObjectClass(exception);
  json["exception"] = fillClassInfo(jvmti, klazz);
  env->DeleteLocalRef(klazz);
  // 在哪个方法中被 catch 的
  json["catch_method"] = fillMethodInfo(jvmti, catch_method);
  // 填充线程信息
  json["thread"] = fillThreadInfo(jvmti, thread);

  // jobject *monitors = {};
  // error = jvmti->GetOwnedMonitorInfo(thread, 0, &monitors);
  // if (error == JVMTI_ERROR_NONE) {
  //   ALOGE("%s ", __func__)
  // }
  persistJson(&json, __func__);
}

void callbackExceptionCatch(jvmtiEnv *jvmti, JNIEnv *env, jthread thread, jmethodID method,
                            jlocation location, jobject exception) {
  // 声明 map 存储信息
  std::map<std::string, std::string> json = {};
  // 填充方法信息
  json["method"] = fillMethodInfo(jvmti, method);
  // 填充异常信息
  jclass klazz = env->GetObjectClass(exception);
  json["exception"] = fillClassInfo(jvmti, klazz);
  env->DeleteLocalRef(klazz);
  // 填充线程信息
  json["thread"] = fillThreadInfo(jvmti, thread);

  persistJson(&json, __func__);
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

  static jvmti::AgentData data;
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