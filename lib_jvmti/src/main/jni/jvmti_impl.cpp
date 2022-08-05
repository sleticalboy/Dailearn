//
// Created by binlee on 2022/7/11.
//

#include <cstring>
#include <map>
#include <memory>
#include "jvmti_config.h"
#include "jvmti_util.h"
#include "jni_logger.h"
#include "mem_file.h"

#define LOG_TAG "JVMTI_IMPL"

// string/map
using namespace std;

jvmti::JvmtiAgent *jvmti_agent = nullptr;
jvmti::JvmtiOptions *jvmti_options = nullptr;
jvmti::MemFile *memFile = nullptr;

// 将数据持久化
void persistJson(map<string, const char*> *json, const char *tag) {
  if (jvmti_options == nullptr) {
    ALOGE("%s abort from %s because options.root_dir is null", __func__, tag)
    return;
  }

  map<string, const char*>::iterator it;
  string buffer("{");
  for (it = json->begin(); it != json->end(); it++) {
    buffer.append("\"").append(it->first).append("\": \"").append(it->second).append("\",");
  }
  buffer.erase(buffer.length() - 1).append("}");
  // 数据持久化
  try {
    if (memFile == nullptr) {
      string path = string(jvmti_options->root_dir).append("/ttt.txt");
      ALOGD("%s create MemFile from %s(), path: %s", __func__, tag, path.c_str())
      memFile = new jvmti::MemFile(path.c_str());
    }
    memFile->Append(buffer.c_str(), buffer.length());
  } catch (const exception &e) {
    ALOGE("%s error: %s", __func__, e.what())
  }
  // 这里打印日志到控制台
  // ALOGD("%s#%s %s", __func__, tag, buffer.c_str())
  // 释放内存
  json->clear();
}

void callbackVMObjectAlloc(jvmtiEnv *jvmti, JNIEnv *env, jthread thread, jobject object,
                           jclass klass, jlong size) {
  // 哪个线程创建了哪个类的实例对象、分配了多少空间
  if (size < 1024) return;

  // 声明 map 存储信息
  map<string, const char*> json = {};
  // 填充类信息
  auto alloc_info = make_unique<jvmti::util::AllocInfo>(jvmti, klass, size);
  json["alloc_info"] = alloc_info->ToString();
  // 填充线程信息
  auto thread_info = make_unique<jvmti::util::ThreadInfo>(jvmti, thread);
  json["thread"] = thread_info->ToString();

  // 给 object 打标记
  // jint hash;
  // jvmtiError error = jvmti->GetObjectHashCode(object, &hash);
  // if (error == JVMTI_ERROR_NONE) {
  //   ALOGI("%s GetObjectHashCode hash: %d", __func__, hash)
  //   error = jvmti->SetTag(object, hash);
  //   if (error == JVMTI_ERROR_NONE) {
  //     //
  //   } else {
  //     ALOGE("%s SetTag error: %d", __func__, error)
  //   }
  // } else {
  //   ALOGE("%s GetObjectHashCode error: %d", __func__, error)
  // }
  persistJson(&json, __func__);
}

void callbackObjectFree(jvmtiEnv *jvmti, jlong tag) {
  ALOGD("%s tag: %ld", __func__, tag)
  jint count = 1;
  jobject *objs = nullptr;
  jlong *tags = nullptr;
  jvmtiError error = jvmti->GetObjectsWithTags(count, &tag, &count, &objs, &tags);
  if (error == JVMTI_ERROR_NONE) {
    ALOGI("%s GetObjectsWithTags count %d", __func__, count)
    if (count > 0) {
      // 遍历所有获取的对象
      for (int i = 0; i < count; ++i) {
        // 输出被释放的对象信息
      }
    }
  } else {
    ALOGE("%s GetObjectsWithTags error %d", __func__, error)
  }
}

void callbackException(jvmtiEnv *jvmti, JNIEnv *env, jthread thread, jmethodID method,
                       jlocation location, jobject exception, jmethodID catch_method,
                       jlocation catch_location) {
  // 声明 map 存储信息
  map<string, const char*> json = {};

  // 填充方法信息
  auto method_info = make_unique<jvmti::util::MethodInfo>(jvmti, method);
  json["method"] = method_info->ToString();

  // 填充异常信息
  jclass klazz = env->GetObjectClass(exception);
  auto exception_info = make_unique<jvmti::util::ClassInfo>(jvmti, klazz);
  json["exception"] = exception_info->ToString();
  env->DeleteLocalRef(klazz);

  // 在哪个方法中被 catch 的
  auto catch_method_info = make_unique<jvmti::util::MethodInfo>(jvmti, catch_method);
  json["catch_method"] = catch_method_info->ToString();

  // 填充线程信息
  auto thread_info = make_unique<jvmti::util::ThreadInfo>(jvmti, thread);
  json["thread"] = thread_info->ToString();

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
  map<string, const char*> json = {};

  // 填充方法信息
  auto method_info = make_unique<jvmti::util::MethodInfo>(jvmti, method);
  json["method"] = method_info->ToString();

  // 填充异常信息
  jclass klazz = env->GetObjectClass(exception);
  auto exception_info = make_unique<jvmti::util::ClassInfo>(jvmti, klazz);
  json["exception"] = exception_info->ToString();
  env->DeleteLocalRef(klazz);

  // 填充线程信息
  auto thread_info = make_unique<jvmti::util::ThreadInfo>(jvmti, thread);
  json["thread"] = thread_info->ToString();

  persistJson(&json, __func__);
}

void callbackMethodEntry(jvmtiEnv *jvmti, JNIEnv* env, jthread thread, jmethodID method) {
  auto method_info = make_unique<jvmti::util::MethodInfo>(jvmti, method);
  // 日志打印量有点恐怖，先简单过滤一下
  if (!method_info->Printable()) return;
  ALOGI("%s %s", __func__, method_info->ToString())

  // 一个莫名奇妙的 crash
  // 2022-07-19 22:18:58.544 A: decStrong() called on 0x7ffa5c000 too many times
  // --------- beginning of crash
  // 2022-07-19 22:18:58.545 A: Fatal signal 6 (SIGABRT), code -6 (SI_TKILL) in tid 5764 (RenderThread), pid 5733 (calboy.learning)
  // 2022-07-19 22:28:24.566 A: Fatal signal 11 (SIGSEGV), code 1 (SEGV_MAPERR), fault addr 0x200000000 in tid 6879 (RenderThread), pid 6848 (calboy.learning)
  // 2022-07-19 22:28:24.571 E: getThreadInfo GetThreadGroupInfo error: JVMTI_ERROR_INVALID_THREAD_GROUP
}

void callbackMethodExit(jvmtiEnv *jvmti, JNIEnv* env, jthread thread, jmethodID method,
     jboolean was_popped_by_exception, jvalue return_value) {
  auto method_info = make_unique<jvmti::util::MethodInfo>(jvmti, method);
  // 日志打印量有点恐怖，先简单过滤一下
  if (!method_info->Printable()) return;
  ALOGI("%s %s", __func__, method_info->ToString())
}

int Agent_Init(JavaVM *vm, const char *options) {
  ALOGD("%s start GetJvmtiEnv", __func__)
  jvmtiEnv *jvmti = nullptr;
  // 支持的 jvmti 版本：JVMTI_VERSION_1_0、JVMTI_VERSION_1_1、JVMTI_VERSION_1_2
  if (vm->GetEnv((void **) &jvmti, JVMTI_VERSION_1_2) != JNI_OK || jvmti == nullptr) {
    ALOGE("%s GetJvmtiEnv error", __func__)
    return JNI_ERR;
  }
  ALOGD("%s jvmti env: %p", __func__, jvmti)

  ALOGD("%s start GetJNIEnv", __func__)
  JNIEnv *env = nullptr;
  if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK || env == nullptr) {
    ALOGE("%s GetJNIEnv error", __func__)
    return JNI_ERR;
  }
  ALOGD("%s JNI env: %p", __func__, env)

  jvmti_agent = new jvmti::JvmtiAgent();
  jvmti_agent->is_agent_attached = true;
  jvmti_agent->jvmti = jvmti;

  ALOGI("%s, jvmti_agent: %p", __func__, jvmti_agent)

  jvmtiError error = JVMTI_ERROR_NONE;

  ALOGD("%s start CreateRawMonitor", __func__)
  error = jvmti->CreateRawMonitor("agent data", &jvmti_agent->lock);
  if (error != JVMTI_ERROR_NONE) {
    ALOGE("%s CreateRawMonitor error: %s", __func__, jvmti::util::GetErrStr(jvmti, error))
    return JNI_ERR;
  }
  ALOGD("%s raw monitor: %p", __func__, jvmti_agent->lock)

  jvmtiCapabilities capa;
  memset(&capa, 0, sizeof(jvmtiCapabilities));
  error = jvmti->GetCapabilities(&capa);
  if (error != JVMTI_ERROR_NONE) {
    ALOGE("%s GetCapabilities error: %s", __func__, jvmti::util::GetErrStr(jvmti, error))
  }

  jvmti_options = jvmti::util::ParseOptions(options);
  ALOGI("%s, jvmti_options: %p", __func__, jvmti_options)

  // if (jvmti::gConfig != nullptr) {
  //   if (jvmti::gConfig->object_alloc) {
  //     //
  //   }
  //   if (jvmti::gConfig->object_free) {
  //     //
  //   }
  //
  //   if (jvmti::gConfig->exception_create) {
  //     //
  //   }
  //   if (jvmti::gConfig->exception_catch) {
  //     //
  //   }
  //
  //   if (jvmti::gConfig->method_enter) {
  //     //
  //   }
  //   if (jvmti::gConfig->method_exit) {
  //     //
  //   }
  // }
  
  capa.can_signal_thread = 1;
  capa.can_get_owned_monitor_info = 1;
  // 方法进入和退出
  capa.can_generate_method_entry_events = 1;
  capa.can_generate_method_exit_events = 1;
  // 异常
  capa.can_generate_exception_events = 1;
  // 内存分配和释放
  capa.can_generate_vm_object_alloc_events = 1;
  capa.can_generate_object_free_events = 1;
  // 标记对象和从对象获取标记
  capa.can_tag_objects = 1;
  // 线程开始和结束
  error = jvmti->AddCapabilities(&capa);
  // check_jvmti_error(jvmti, error, "Unable to get necessary JVMTI capabilities");
  if (error != JVMTI_ERROR_NONE) {
    ALOGE("%s AddCapabilities error: %s", __func__, jvmti::util::GetErrStr(jvmti, error))
  }

  // 对象分配与释放
  error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_OBJECT_ALLOC, nullptr);
  error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_OBJECT_FREE, nullptr);
  // 方法进入与退出
  error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_METHOD_ENTRY, nullptr);
  error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_METHOD_EXIT, nullptr);
  // 异常
  error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_EXCEPTION, nullptr);
  error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_EXCEPTION_CATCH, nullptr);
  // check_jvmti_error(jvmti, error, "Can not set event notification");
  if (error != JVMTI_ERROR_NONE) {
    ALOGE("%s SetEventNotificationMode error: %s", __func__, jvmti::util::GetErrStr(jvmti, error))
  }

  jvmtiEventCallbacks callbacks = {};
  memset(&callbacks, 0, sizeof(jvmtiEventCallbacks));
  // 对象分配和释放事件
  callbacks.VMObjectAlloc = &callbackVMObjectAlloc;
  callbacks.ObjectFree = &callbackObjectFree;
  // 方法进入和退出事件
  callbacks.MethodEntry = &callbackMethodEntry;
  callbacks.MethodExit = &callbackMethodExit;
  // 异常产生与抓取
  callbacks.Exception = &callbackException;
  callbacks.ExceptionCatch = &callbackExceptionCatch;
  error = jvmti->SetEventCallbacks(&callbacks, sizeof(callbacks));
  // check_jvmti_error(jvmti, error, "Can not set JVMTI callbacks");
  if (error != JVMTI_ERROR_NONE) {
    ALOGE("%s SetEventCallbacks error: %s", __func__, jvmti::util::GetErrStr(jvmti, error))
  }
  return JNI_OK;
}

// 在 JVM 启动时就指定 agent 路径，会回调此方法
jint Agent_OnLoad(JavaVM *vm, char *options, void *reserved) {
  ALOGD("%s options: %s", __func__, options)
  return JNI_TRUE;
}

// 在 JVM 启动后动态绑定 agent ，会回调此方法
jint Agent_OnAttach(JavaVM *vm, char *options, void *reserved) {
  ALOGD("%s options: %s", __func__, options)
  return Agent_Init(vm, options);
}

void Agent_OnUnload(JavaVM *vm) {
  ALOGD("%s", __func__)
}