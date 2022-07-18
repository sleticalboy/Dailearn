//
// Created by binlee on 2022/7/11.
//

#include "jvmti.h"

#ifndef DAILEARN_JVMTI_IMPL_H
#define DAILEARN_JVMTI_IMPL_H

namespace jvmti {

  typedef struct AgentData {
    jvmtiEnv *jvmti;
    jboolean isAgentInit;
    jrawMonitorID lock;
  } AgentData;

  static AgentData *gData;

  typedef struct Config {

    const char *root_dir;

    bool object_alloc;
    bool object_free;

    bool exception_create;
    bool exception_catch;

    bool method_enter;
    bool method_exit;
  } Config;

  static Config *gConfig;
}

#endif //DAILEARN_JVMTI_IMPL_H
