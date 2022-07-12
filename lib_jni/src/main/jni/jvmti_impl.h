//
// Created by binlee on 2022/7/11.
//

#include "jvmti.h"

#ifndef DAILEARN_JVMTI_IMPL_H
#define DAILEARN_JVMTI_IMPL_H

namespace jvmti {

  typedef struct GlobalAgentData {
    jvmtiEnv *jvmti;
    jboolean isAgentInit;
    jrawMonitorID lock;
  } AgentData;

  static AgentData *gdata;
}

#endif //DAILEARN_JVMTI_IMPL_H
