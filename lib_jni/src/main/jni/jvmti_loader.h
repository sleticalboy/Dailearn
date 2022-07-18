//
// Created by binlee on 2022/7/11.
//

#include <jni.h>
#include "jvmti_config.h"

#ifndef DAILEARN_JVMTI_LOADER_H
#define DAILEARN_JVMTI_LOADER_H

namespace jvmti {
  void attachAgent(JNIEnv *env, const char *library);
}

#endif //DAILEARN_JVMTI_LOADER_H
