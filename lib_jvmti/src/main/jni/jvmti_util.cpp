//
// Created by binlee on 2022/7/15.
//

#include "jvmti_util.h"
#include "jni_logger.h"

#define LOG_TAG "JVMTI_UTIL"

namespace jvmti {
namespace util {

// 线程信息：名字、组、id
string getThreadInfo(jvmtiEnv *jvmti, jthread thread) {
  std::string buffer;
  jvmtiError error;
  // 先判断一下线程状态再获取信息，可以规避一些错误
  // unsigned int state;
  // error = jvmti->GetThreadState(thread, (jint *) &state);
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
  jvmtiThreadInfo info = {};
  error = jvmti->GetThreadInfo(thread, &info);
  if (error == JVMTI_ERROR_NONE) {
    buffer.append("thread(").append(info.name) += "), priority: "
        + std::to_string(info.priority) + ", daemon: " + (info.is_daemon ? "true" : "false");
    jvmtiThreadGroupInfo gInfo = {};
    error = jvmti->GetThreadGroupInfo(info.thread_group, &gInfo);
    if (error == JVMTI_ERROR_NONE) {
      buffer.append(", group(").append(gInfo.name) += ") priority: "
          + std::string(gInfo.name) + ", daemon: " + (gInfo.is_daemon ? "true" : "false");
    } else {
      ALOGE("%s GetThreadGroupInfo error: %s", __func__, getErrorName(jvmti, error))
    }
  } else {
    ALOGE("%s GetThreadInfo error: %s", __func__, getErrorName(jvmti, error))
  }
  return buffer;
}

string getMethodInfo(jvmtiEnv *jvmti, jmethodID method) {
  string buffer;
  char *name = {};
  char *sig = {};
  char *gsig = {};
  jvmtiError error = jvmti->GetMethodName(method, &name, &sig, &gsig);
  if (error == JVMTI_ERROR_NONE) {
    buffer.append(name).append(sig).append(gsig == nullptr ? "" : gsig);
  } else {
    ALOGE("%s GetMethodName error: %s", __func__, getErrorName(jvmti, error))
  }
  return buffer;
}

string getClassInfo(jvmtiEnv *jvmti, jclass klass, jlong size) {
  string buffer;

  char *className = {};
  jvmtiError error = jvmti->GetClassSignature(klass, &className, nullptr);
  if (error == JVMTI_ERROR_NONE) {
    buffer.append("class: ").append(className);
  } else {
    ALOGE("%s GetClassSignature error: %s", __func__, getErrorName(jvmti, error))
  }
  if (size > 0) {
    buffer.append(", size: ") += std::to_string(size);
  }
  delete className;
  return buffer;
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
