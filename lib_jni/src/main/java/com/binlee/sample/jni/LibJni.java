package com.binlee.sample.jni;

import android.content.Context;

/**
 * Created on 2022-07-09.
 *
 * @author binlee
 */
public final class LibJni {

  static {
    System.loadLibrary("jni");
  }

  public static native String nativeGetString();

  public static native void nativeCallJava(Context context);
}
