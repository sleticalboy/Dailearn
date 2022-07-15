//
// Created by binlee on 2022/7/15.
//

#include <string>

#ifndef DAILY_WORK_JVMTI_UTIL_H
#define DAILY_WORK_JVMTI_UTIL_H

namespace jvmti {

  // 线程信息：名字、组、id
  std::string getThreadInfo(jvmtiEnv *jvmti, jthread thread);

  std::string getMethodInfo(jvmtiEnv *jvmti, jmethodID method);

  std::string getClassInfo(jvmtiEnv *jvmti, jclass klass, jlong size = 0);
}

#endif //DAILY_WORK_JVMTI_UTIL_H
