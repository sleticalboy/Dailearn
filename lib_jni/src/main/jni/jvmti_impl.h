//
// Created by binlee on 2022/7/11.
//

#include "jvmti.h"

#ifndef DAILEARN_JVMTI_IMPL_H
#define DAILEARN_JVMTI_IMPL_H

namespace jvmti {

  typedef struct GlobalAgentData {
    jvmtiEnv *jvmti;
    jboolean vmIsStarted;
    jrawMonitorID lock;
  } AgentData;

  static AgentData *gdata;

  void callbackVMInit(jvmtiEnv *jvmti, JNIEnv *env, jthread thread);

  void callbackVMObjectAlloc(jvmtiEnv *jvmti, JNIEnv *env, jthread thread,
                             jobject object, jclass object_klass, jlong size);

  void callbackVMDeath(jvmtiEnv *jvmti, JNIEnv *env);

  void callbackException(jvmtiEnv *jvmti, JNIEnv *env, jthread thread,
                         jmethodID method, jlocation location, jobject exception,
                         jmethodID catch_method, jlocation catch_location);
}

#endif //DAILEARN_JVMTI_IMPL_H
