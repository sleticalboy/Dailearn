//
// Created by binlee on 2022/7/15.
//

#include "jvmti_util.h"
#include "jni_logger.h"

#define LOG_TAG "JVMTI_UTIL"

using namespace std;

namespace jvmti {
namespace util {

/// alloc info

AllocInfo::AllocInfo(jvmtiEnv *jvmti, jclass klass, jlong size) {
  data_ = new char[1024];
  int offset = 0;
  char *class_name = nullptr;
  jvmtiError error = jvmti->GetClassSignature(klass, &class_name, nullptr);
  if (error == JVMTI_ERROR_NONE) {
    offset += sprintf(data_ + offset, "class: %s, ", class_name);
  }
  sprintf(data_ + offset, "size: %ld", size);
  delete class_name;
}

/// class info

ClassInfo::ClassInfo(jvmtiEnv *jvmti, jclass clazz) {
  int offset = 0;
  data_ = new char[1024];
  char *class_name = nullptr;
  char *generic = nullptr;
  jvmtiError error = jvmti->GetClassSignature(clazz, &class_name, &generic);
  if (error == JVMTI_ERROR_NONE) {
    offset += sprintf(data_ + offset, "class: %s, generic: %s", class_name, generic);
    int status;
    error = jvmti->GetClassStatus(clazz, &status);
    if (error == JVMTI_ERROR_NONE) {
      offset += sprintf(data_ + offset, ", status: %d", status);
    }
  }
}

/// thread info

ThreadInfo::ThreadInfo(jvmtiEnv *jvmti, jthread thread) {
  int offset = 0;
  data_ = new char[1024];
  // 线程信息
  jvmtiThreadInfo thread_info = {};
  jvmtiError error = jvmti->GetThreadInfo(thread, &thread_info);
  // ALOGW("ThreadInfo#%s GetThreadInfo %s", __func__, getErrorName(jvmti, error))
  if (error == JVMTI_ERROR_NONE) {
    if (thread_info.is_daemon) offset += sprintf(data_ + offset, "daemon ");
    offset += sprintf(data_ + offset, "thread(%s) priority %d", thread_info.name, thread_info.priority);
    // 线程状态
    jint state = 0;
    error = jvmti->GetThreadState(thread, &state);
    // ALOGW("ThreadInfo#%s GetThreadState %s", __func__, getErrorName(jvmti, error))
    if (error == JVMTI_ERROR_NONE) {
      offset += sprintf(data_ + offset, " state %d ", state);
      // 先判断一下线程状态再获取信息，可以规避一些错误
      // if (error == JVMTI_ERROR_NONE) {
      //   // JVMTI_THREAD_STATE_ALIVE = 0x0001,
      //   // JVMTI_THREAD_STATE_TERMINATED = 0x0002,
      //   // JVMTI_THREAD_STATE_RUNNABLE = 0x0004,
      //   // JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER = 0x0400,
      //   // JVMTI_THREAD_STATE_WAITING = 0x0080,
      //   // JVMTI_THREAD_STATE_WAITING_INDEFINITELY = 0x0010,
      //   // JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT = 0x0020,
      //   // JVMTI_THREAD_STATE_SLEEPING = 0x0040,
      //   // JVMTI_THREAD_STATE_IN_OBJECT_WAIT = 0x0100,
      //   // JVMTI_THREAD_STATE_PARKED = 0x0200,
      //   // JVMTI_THREAD_STATE_SUSPENDED = 0x100000,
      //   // JVMTI_THREAD_STATE_INTERRUPTED = 0x200000,
      //   // JVMTI_THREAD_STATE_IN_NATIVE = 0x400000,
      //   if (state & JVMTI_THREAD_STATE_ALIVE) {
      //     ALOGI("%s thread is ALIVE", __func__)
      //   }
      //   if (state & JVMTI_THREAD_STATE_RUNNABLE) {
      //     ALOGI("%s thread is RUNNABLE", __func__)
      //   }
      //   if (state & JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER) {
      //     ALOGI("%s thread is BLOCKED_ON_MONITOR_ENTER", __func__)
      //   }
      //   if (state & JVMTI_THREAD_STATE_WAITING) {
      //     if (state & JVMTI_THREAD_STATE_IN_OBJECT_WAIT) {
      //       if (state & JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT) {
      //         ALOGI("%s thread in Object.wait(long timeout)", __func__)
      //       } else {
      //         ALOGI("%s thread in Object.wait()", __func__)
      //       }
      //     } else if (state & JVMTI_THREAD_STATE_PARKED) {
      //       ALOGI("%s thread in LockSupport.park*()", __func__)
      //     } else if (state & JVMTI_THREAD_STATE_SLEEPING) {
      //       ALOGI("%s thread in Thread.sleep()", __func__)
      //     }
      //   }
      //   if ((state & (JVMTI_THREAD_STATE_ALIVE | JVMTI_THREAD_STATE_TERMINATED)) == 0) {
      //     ALOGI("%s thread is CREATED but not STARTED", __func__)
      //   }
      //   switch (state & JVMTI_JAVA_LANG_THREAD_STATE_MASK) {
      //     case JVMTI_JAVA_LANG_THREAD_STATE_NEW:
      //       buffer.append("NEW ");
      //       break;
      //     case JVMTI_JAVA_LANG_THREAD_STATE_TERMINATED:
      //       buffer.append("TERMINATED ");
      //       break;
      //     case JVMTI_JAVA_LANG_THREAD_STATE_RUNNABLE:
      //       buffer.append("RUNNABLE ");
      //       break;
      //     case JVMTI_JAVA_LANG_THREAD_STATE_BLOCKED:
      //       buffer.append("BLOCKED ");
      //       break;
      //     case JVMTI_JAVA_LANG_THREAD_STATE_WAITING:
      //       buffer.append("WAITING ");
      //       break;
      //     case JVMTI_JAVA_LANG_THREAD_STATE_TIMED_WAITING:
      //       buffer.append("TIMED_WAITING ");
      //       break;
      //     default:
      //       buffer.append("UNKNOWN ");
      //       break;
      //   }
      //   ALOGI("%s GetThreadState %s(%d)", __func__, buffer.c_str(), state)
      // } else {
      //   ALOGE("%s GetThreadState error: %s", __func__, getErrorName(jvmti, error))
      // }
    }
    // 线程组信息
    jvmtiThreadGroupInfo group_info = {};
    error = jvmti->GetThreadGroupInfo(thread_info.thread_group, &group_info);
    // ALOGW("ThreadInfo#%s GetThreadGroupInfo %s", __func__, getErrorName(jvmti, error))
    if (error == JVMTI_ERROR_NONE) {
      if (group_info.is_daemon) offset += sprintf(data_ + offset, ", daemon ");
      sprintf(data_ + offset, "group(%s) priority %d", group_info.name, group_info.max_priority);
    }
  }
}

/// method info

MethodInfo::MethodInfo(jvmtiEnv *jvmti, jmethodID method) {
  int offset = 0;
  data_ = new char[1024];

  char *method_name = nullptr;
  char *method_signature = nullptr;
  char *method_generic = nullptr;
  jvmtiError error = jvmti->GetMethodName(method, &method_name, &method_signature, &method_generic);
  if (error == JVMTI_ERROR_NONE) {
    jclass owned_class = nullptr;
    error = jvmti->GetMethodDeclaringClass(method, &owned_class);
    if (error == JVMTI_ERROR_NONE && owned_class != nullptr) {
      char *owner_class = nullptr;
      error = jvmti->GetClassSignature(owned_class, &owner_class, nullptr);
      if (error == JVMTI_ERROR_NONE) {
        offset += sprintf(data_ + offset, "%s#", owner_class);
        string s = string(owner_class);
        // printable_ = s.find("Ljava") == string::npos && s.find("Landroid") == string::npos
        //   && s.find("Lkotlin") == string::npos && s.find("Lsun/") == string::npos
        //   && s.find("Lleakcanary") == string::npos && s.find("android/internal") == string::npos
        //   && s.find("Ldalvik") == string::npos && s.find("Llibcore") == string::npos
        //   && s.find("Lokio") == string::npos && s.find("Lcom/airbnb") == string::npos
        //   ;
        // printable_ = s.find("com/binlee") != string::npos || s.find("java/lang/Thread") != string::npos;
        // ALOGW("%s %s is printable: %d", __func__, owner_class, printable_)
      }
    }
    offset += sprintf(data_ + offset, "%s%s", method_signature, method_generic);
  }
}

const char *GetErrStr(jvmtiEnv *jvmti, jvmtiError &error) {
  char *error_name = nullptr;
  jvmtiError err = jvmti->GetErrorName(error, &error_name);
  if (err != JVMTI_ERROR_NONE) {
    ALOGE("%s GetErrorName error: %d", __func__, err)
  }
  return error_name;
}

JvmtiOptions *ParseOptions(const char *options) {
  //root_dir:/data/user/0/com.binlee.learning/files;obj_alloc:false;obj_free:false;ex_create:false;ex_catch:false;method_enter:false;method_exit:false

  auto *config = new JvmtiOptions();
  int next = 0;
  string str = string(options);
  for (int i = 0; i < str.size(); ++i) {
    if (str.at(i) == ';') {
      string pair = str.substr(next, i - next/*len*/);
      next = i + 1;

      int index = pair.find_first_of(':');
      string key = pair.substr(0, index);
      string value = pair.substr(index + 1);
      if (key == "root_dir") {
        sprintf(config->root_dir, "%s", value.c_str());
      } else if (key == "obj_alloc") {
        config->object_alloc = value == "true";
      } else if (key == "obj_free") {
        config->object_free = value == "true";
      } else if (key == "ex_create") {
        config->exception_create = value == "true";
      } else if (key == "ex_catch") {
        config->exception_catch = value == "true";
      } else if (key == "method_enter") {
        config->method_enter = value == "true";
      } else if (key == "method_exit") {
        config->method_exit = value == "true";
      }
    }
  }
  return config;
}

} // namespace util
} // namespace jvmti
