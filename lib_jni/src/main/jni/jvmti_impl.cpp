//
// Created by binlee on 2022/7/11.
//

#include <cstring>
#include "jvmti_impl.h"
#include "jvmti_util.h"
#include "jni_logger.h"
#include <sys/mman.h>
#include <sys/fcntl.h>
#include <unistd.h>
#include <map>
#include <cerrno>

#define LOG_TAG "JVMTI_IMPL"

int *mem_buf = nullptr;
// buffer 偏移
int buf_offset = 0;
// 内存映射大小指定为页大小的整数倍, 首次指定为一个页大小
int buf_size = 0;
int fd = -1;

std::string root_dir;

bool ensure_open() {
  if (fd > 0) return true;

  if (root_dir.empty()) root_dir = std::string("/data/data/com.sleticalboy.learning/files");
  ALOGD("%s root dir: %s", __func__, root_dir.c_str())

  std::string file_path = root_dir + "/ttt.txt";
  FILE *fp = fopen(file_path.c_str(), "w+");
  if (fp == nullptr) {
    ALOGE("%s create file error: %p", __func__, fp)
    return false;
  }
  fclose(fp);
  // 找个时机把文件句柄关闭了，否则会内存泄露
  fd = open(file_path.c_str(), O_RDWR);
  if (errno || fd == -1) {
    ALOGE("%s failed as errno: %d, fd: %d", __func__, errno, fd)
    if (fd != -1) close(fd);
    fd = -1;
    return false;
  }
  if (buf_size == 0) {
    buf_size = getpagesize();
    // 必须先 ftruncate 再 mmap，否则映射出来的内存地址不能 write
    int res = ftruncate(fd, buf_size);
    if (res != 0) {
      ALOGE("%s ftruncate res: %d", __func__, res)
      return false;
    }
  }

  // 如果文件已有内容，要覆盖还是要追加？
  // 如果追加的话，如何追加？
  // 1、将原文件内容全部读取出来，上传到服务器/加载到内存中；
  // 2、将原内容使用内存数据进行覆盖；
  mem_buf = (int *) mmap(nullptr, buf_size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
  if (mem_buf == MAP_FAILED) {
    close(fd);
    ALOGE("%s mmap failed: %d", __func__, errno)
    return false;
  }
  ALOGD("%s mmap mem_buf: %p, fd: %d", __func__, mem_buf, fd)
  return true;
}

// 扩展文件大小到指定尺寸，最好是 page size 整数倍
bool resize_mmap(int resize) {
  int old_size = buf_size;
  do {
    buf_size *= 2;
  } while (buf_size < resize);
  // 文件扩容
  int res = ftruncate(fd, buf_size);
  if (res != 0) {
    ALOGE("%s ftruncate res: %d", __func__, res)
    return false;
  }
  // 解除原有映射并重新映射
  munmap(mem_buf, old_size);
  mem_buf = (int *) mmap(nullptr, buf_size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
  if (mem_buf == MAP_FAILED) {
    ALOGE("%s mmap failed: %d", __func__, errno)
    return false;
  }
  ALOGD("%s buf_size: %d", __func__, buf_size)
  return true;
}

void dispatchDataPersist(const char *tag, const char *data) {
  if (!ensure_open()) {
    ALOGE("%s abort fd: %d", __func__, fd)
    return;
  }
  ALOGD("%s page size: %d, buffer size is %d", __func__, getpagesize(), buf_size)

  // 写入数据的长度
  int write_size = strlen(data);
  ALOGD("%s write data size: %d", __func__, write_size)

  if (buf_offset + write_size >= buf_size) {
    // 文件过小要扩容
    resize_mmap(buf_offset + write_size);
  }

  // res = close(fd);
  // if (res != 0) {
  //   ALOGE("%s close res: %d", __func__, res)
  // }

  ALOGD("%s start write to mem_buf, offset: %d", __func__, buf_offset)
  // 从文件尾部开始追加
  memcpy(mem_buf + buf_offset / 4, data, write_size);
  buf_offset += write_size;
  // 内存对齐
  if (write_size % 4 != 0) {
    buf_offset += (4 - write_size % 4);
  }
  ALOGD("%s write to mem_buf by memcpy done, offset: %d", __func__, buf_offset)

  // res = munmap(buffer, buffer_size);
  // if (res != 0) {
  //   ALOGE("%s munmap res: %d", __func__, res)
  // }
}

void callbackVMInit(jvmtiEnv *jvmti, JNIEnv *env, jthread thread) {
  ALOGE("%s", __func__)
}

void callbackVMDeath(jvmtiEnv *jvmti, JNIEnv *env) {
  ALOGE("%s", __func__)
}

// 将数据持久化
void persistJson(std::map<std::string, std::string> *json, const char *tag) {
  std::map<std::string, std::string>::iterator it;
  std::string buffer("{");
  for (it = json->begin(); it != json->end(); it++) {
    buffer.append("\"").append(it->first).append("\": \"").append(it->second).append("\",");
  }
  buffer.erase(buffer.length() - 1).append("}");
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
}

void callbackVMObjectAlloc(jvmtiEnv *jvmti, JNIEnv *env, jthread thread, jobject object,
                           jclass klass, jlong size) {
  // 哪个线程创建了哪个类的实例对象、分配了多少空间、
  if (size < 1024) return;

  // 声明 map 存储信息
  std::map<std::string, std::string> json = {};

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
  std::map<std::string, std::string> json = {};
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
  std::map<std::string, std::string> json = {};
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