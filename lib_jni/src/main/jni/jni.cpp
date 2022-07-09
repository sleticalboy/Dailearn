#include <jni.h>

// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("jni");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("jni")
//      }
//    }

extern "C"
JNIEXPORT jstring
JNICALL
Java_com_binlee_sample_jni_LibJni_nativeGetString(JNIEnv *env, jclass clazz) {
  return nullptr;
}