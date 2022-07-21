//
// Created by binlee on 2022/7/11.
//

#include "jvmti.h"

#ifndef DAILEARN_JVMTI_IMPL_H
#define DAILEARN_JVMTI_IMPL_H

namespace jvmti {

typedef struct AgentData {
  jvmtiEnv *jvmti;
  jboolean is_agent_init;
  jrawMonitorID lock;
} AgentData;

static AgentData *g_data;

typedef struct Config {

  const char *root_dir;

  bool object_alloc;
  bool object_free;

  bool exception_create;
  bool exception_catch;

  bool method_enter;
  bool method_exit;
} Config;

static Config *g_config;

class ThreadInfo {
private:
  jvmtiEnv *jvmti_;
  jthread thread_;
  jint state_;
  char *content_;
  jvmtiThreadInfo thread_info_;
  jvmtiThreadGroupInfo group_info_;
public:

  ThreadInfo(jvmtiEnv *jvmti, jthread thread);

  const char *ToString() {
    if (content_ == nullptr) {
      content_ = new char[1024];
      strcpy(content_, "thread:");
      strcpy(content_, thread_info_.name);
      strcpy(content_, ", thread group:");
      strcpy(content_, group_info_.name);
    }
    return content_;
  }

  ~ThreadInfo() {
    free(content_);
    content_ = nullptr;
  }
};
} // namespace jvmti

#endif //DAILEARN_JVMTI_IMPL_H
