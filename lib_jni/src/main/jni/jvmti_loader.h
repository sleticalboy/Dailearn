//
// Created by binlee on 2022/7/11.
//

#include <jni.h>

#ifndef DAILEARN_JVMTI_LOADER_H
#define DAILEARN_JVMTI_LOADER_H

namespace jvmti {
  void attachAgent(JNIEnv *env, jstring pJstring);
}

#endif //DAILEARN_JVMTI_LOADER_H
