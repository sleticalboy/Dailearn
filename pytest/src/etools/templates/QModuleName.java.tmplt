package com.quvideo.mobile.component.{pkg-name};

import com.quvideo.mobile.component.common.AIFrameInfo;
import com.quvideo.mobile.component.common.AIInitResult;
import com.quvideo.mobile.component.common.QAIBase;

/**
 * Created on 2022/10/28
 *
 * @author binlee
 */
public final class Q{module-name} extends QAIBase {

  // 和引擎对接的版本号，会影响到引擎对接的结构体时，一定一定一定要改版本号。
  private static final int SDK_VERSION = 1;

  private Q{module-name}() {
    super(QE{module-name}Client.getAiType());
  }

  @Override protected AIInitResult create(InitArgs args) {
    return nativeInit(args.modelPath);
  }

  public static long init() {
    return new Q{module-name}().initHandle();
  }

  public static int getVersion() {
    return SDK_VERSION;
  }

  private static native AIInitResult nativeInit(String modelPath);

  public static native int nativeForwardProcess(long handle);

  public static native int nativeSetProp(long handle, int jKey, long jValue);

  public static native int nativeGetProp(long handle, int jKey, long jValue);

  static native String nativeGetVersion();

  public static native void nativeRelease(long handle);

  public static native int nativeForward4J(long handle, AIFrameInfo input, int width, int height, AIFrameInfo output);
}
