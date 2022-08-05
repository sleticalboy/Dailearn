//
// Created by binlee on 2022/7/11.
//

#include "jvmti.h"

#ifndef DAILEARN_JVMTI_IMPL_H
#define DAILEARN_JVMTI_IMPL_H

namespace jvmti {

typedef struct JvmtiAgent {
  jvmtiEnv *jvmti;
  jboolean is_agent_attached;
  jrawMonitorID lock;
} JvmtiAgent;

typedef struct JvmtiOptions {

  char *const root_dir = new char[128];

  bool object_alloc = false;
  bool object_free = false;

  bool exception_create = false;
  bool exception_catch = false;

  bool method_enter = false;
  bool method_exit = false;

  ~JvmtiOptions() {
    delete[] root_dir;
  }
} JvmtiOptions;
} // namespace jvmti

#endif //DAILEARN_JVMTI_IMPL_H
