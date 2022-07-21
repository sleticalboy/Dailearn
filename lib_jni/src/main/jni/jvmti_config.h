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
} // namespace jvmti

#endif //DAILEARN_JVMTI_IMPL_H
