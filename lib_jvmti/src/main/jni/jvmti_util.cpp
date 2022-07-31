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
  char *class_name = nullptr;
  jvmtiError error = jvmti->GetClassSignature(klass, &class_name, nullptr);
  if (error == JVMTI_ERROR_NONE) {
    data_.append("class: ").append(class_name).append(", ");
  }
  data_.append("size: ").append(to_string(size));
}

/// class info

ClassInfo::ClassInfo(jvmtiEnv *jvmti, jclass clazz) {
  char *class_name = nullptr;
  char *generic = nullptr;
  jvmtiError error = jvmti->GetClassSignature(clazz, &class_name, &generic);
  if (error == JVMTI_ERROR_NONE) {
    data_.append("class: ").append(class_name);
    if (generic != nullptr) data_.append(", generic: ").append(generic);
    int status;
    error = jvmti->GetClassStatus(clazz, &status);
    if (error == JVMTI_ERROR_NONE) {
      data_.append(", status: ").append(to_string(status));
    }
  }
}

/// thread info

ThreadInfo::ThreadInfo(jvmtiEnv *jvmti, jthread thread) {
  // 线程信息
  jvmtiThreadInfo thread_info = {};
  jvmtiError error = jvmti->GetThreadInfo(thread, &thread_info);
  // ALOGW("ThreadInfo#%s GetThreadInfo %s", __func__, getErrorName(jvmti, error))
  if (error == JVMTI_ERROR_NONE) {
    if (thread_info.is_daemon) data_.append("daemon ");
    data_.append("thread(");
    if (thread_info.name != nullptr) data_.append(thread_info.name);
    data_.append(") priority ").append(to_string(thread_info.priority));
    // 线程状态
    jint state = 0;
    error = jvmti->GetThreadState(thread, &state);
    // ALOGW("ThreadInfo#%s GetThreadState %s", __func__, getErrorName(jvmti, error))
    if (error == JVMTI_ERROR_NONE) {
      data_.append(" state ").append(to_string(state)).append(" ");
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
      if (group_info.is_daemon) data_.append(", daemon ");
      data_.append("group(");
      if (group_info.name != nullptr) data_.append(group_info.name);
      data_.append(") priority ").append(to_string(group_info.max_priority));
    }
  }
}

/// method info

MethodInfo::MethodInfo(jvmtiEnv *jvmti, jmethodID method) {
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
        data_.append(owner_class).append("#");
        string s = string(owner_class);
        // printable_ = s.find("Ljava") == string::npos && s.find("Landroid") == string::npos
        //   && s.find("Lkotlin") == string::npos && s.find("Lsun/") == string::npos
        //   && s.find("Lleakcanary") == string::npos && s.find("android/internal") == string::npos
        //   && s.find("Ldalvik") == string::npos && s.find("Llibcore") == string::npos
        //   && s.find("Lokio") == string::npos && s.find("Lcom/airbnb") == string::npos
        //   ;
        printable_ = s.find("com/binlee") != string::npos || s.find("java/lang/Thread") != string::npos;
        // ALOGW("%s %s is printable: %d", __func__, owner_class, printable_)
      }
    }
    data_.append(method_name).append(method_signature);
    if (method_generic != nullptr) data_.append(method_generic);
  }
}

const char *getErrorName(jvmtiEnv *jvmti, jvmtiError &error) {
  char *error_name = nullptr;
  jvmtiError err = jvmti->GetErrorName(error, &error_name);
  if (err != JVMTI_ERROR_NONE) {
    ALOGE("%s GetErrorName error: %d", __func__, err)
  }
  return error_name;
}

void fromJavaConfig(JNIEnv *env, jobject jConfig, Config *config) {
  jclass cls_config = env->FindClass("com/binlee/apm/jvmti/JvmtiConfig");

  jfieldID field = env->GetFieldID(cls_config, "rootDir", "Ljava/lang/String;");
  auto root_dir = (jstring) env->GetObjectField(jConfig, field);
  config->root_dir = env->GetStringUTFChars(root_dir, JNI_FALSE);

  field = env->GetFieldID(cls_config, "objectAlloc", "Z");
  config->object_alloc = env->GetBooleanField(jConfig, field);
  field = env->GetFieldID(cls_config, "objectFree", "Z");
  config->object_free = env->GetBooleanField(jConfig, field);

  field = env->GetFieldID(cls_config, "exceptionCreate", "Z");
  config->exception_create = env->GetBooleanField(jConfig, field);
  field = env->GetFieldID(cls_config, "exceptionCatch", "Z");
  config->exception_catch = env->GetBooleanField(jConfig, field);

  field = env->GetFieldID(cls_config, "methodEnter", "Z");
  config->method_enter = env->GetBooleanField(jConfig, field);
  field = env->GetFieldID(cls_config, "methodExit", "Z");
  config->method_exit = env->GetBooleanField(jConfig, field);
}
} // namespace util
} // namespace jvmti
