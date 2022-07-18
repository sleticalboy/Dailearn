//
// Created by binlee on 2022/7/11.
//

#include <cstring>
#include <map>
#include "jvmti_impl.h"
#include "jvmti_util.h"
#include "jni_logger.h"
#include "mem_file.h"

#define LOG_TAG "JVMTI_IMPL"

// string/map
using namespace std;

jvmti::MemFile *memFile = nullptr;

void callbackVMInit(jvmtiEnv *jvmti, JNIEnv *env, jthread thread) {
  ALOGE("%s", __func__)
}

void callbackVMDeath(jvmtiEnv *jvmti, JNIEnv *env) {
  ALOGE("%s", __func__)
}

// 将数据持久化
void persistJson(map<string, string> *json, const char *tag) {
  map<string, string>::iterator it;
  string buffer("{");
  for (it = json->begin(); it != json->end(); it++) {
    buffer.append("\"").append(it->first).append("\": \"").append(it->second).append("\",");
  }
  buffer.erase(buffer.length() - 1).append("}");
  // 数据持久化
  try {
    if (memFile == nullptr) {
      memFile = new jvmti::MemFile("/data/data/com.sleticalboy.learning/files/ttt.txt");
      ALOGD("%s create MemFile from %s", __func__, tag)
    }
    memFile->Append(buffer.c_str(), buffer.length());
  } catch (const exception &e) {
    ALOGE("%s error: %s", __func__, e.what())
  }
  // 这里打印日志到控制台
  ALOGD("%s %s", tag, buffer.c_str())
  // 释放内存
  json->clear();
}

void callbackVMObjectAlloc(jvmtiEnv *jvmti, JNIEnv *env, jthread thread, jobject object,
                           jclass klass, jlong size) {
  // 哪个线程创建了哪个类的实例对象、分配了多少空间、
  if (size < 1024) return;

  // 声明 map 存储信息
  map<string, string> json = {};

  // 填充类信息
  json["class_info"] = jvmti::getClassInfo(jvmti, klass, size);
  // 填充线程信息
  json["thread"] = jvmti::getThreadInfo(jvmti, thread);

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
  map<string, string> json = {};
  // 填充方法信息
  json["method"] = jvmti::getMethodInfo(jvmti, method);
  // 填充异常信息
  jclass klazz = env->GetObjectClass(exception);
  json["exception"] = jvmti::getClassInfo(jvmti, klazz);
  env->DeleteLocalRef(klazz);
  // 在哪个方法中被 catch 的
  json["catch_method"] = jvmti::getMethodInfo(jvmti, catch_method);
  // 填充线程信息
  json["thread"] = jvmti::getThreadInfo(jvmti, thread);

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
  map<string, string> json = {};
  // 填充方法信息
  json["method"] = jvmti::getMethodInfo(jvmti, method);
  // 填充异常信息
  jclass klazz = env->GetObjectClass(exception);
  json["exception"] = jvmti::getClassInfo(jvmti, klazz);
  env->DeleteLocalRef(klazz);
  // 填充线程信息
  json["thread"] = jvmti::getThreadInfo(jvmti, thread);

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
  error = jvmti->CreateRawMonitor("agent data", &data.lock);
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

jint Agent_OnAttach(JavaVM *vm, char *options, void *reserved) {
  ALOGD("%s options: %s", __func__, options)
  return Agent_Init(vm);
}

void Agent_OnUnload(JavaVM *vm) {
  ALOGD("%s", __func__)
}