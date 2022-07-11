package com.binlee.sample.jni;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created on 2022-07-09.
 *
 * @author binlee
 */
public final class LibJni {

  static {
    System.loadLibrary("jni");
  }

  public static void loadJvmti(Context context) {
    final String nativeLibraryDir = context.getApplicationInfo().nativeLibraryDir;
    final String[] libs = new File(nativeLibraryDir).list();
    Log.d("LibJni", "loadJvmti() native libraries: " + Arrays.toString(libs) + ", in " + nativeLibraryDir);
    // 这个路径中有 '='，系统会检测 lib 路径中是否包含 '='，如果包含会直接报错，因此要拷贝到私有目录中
    final File dest = new File(context.getFilesDir(), "jvmti-agent.so");
    try {
      copyFile(new File(nativeLibraryDir, "libjni.so"), dest);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    Log.d("LibJni", "loadJvmti() jvmti agent: " + dest);
    nativeLoadJvmti(dest.getAbsolutePath());
  }

  private static void copyFile(File src, File dest) throws IOException {
    if (dest.exists()) dest.delete();
    dest.createNewFile();
    final FileInputStream input = new FileInputStream(src);
    final FileOutputStream output = new FileOutputStream(dest);
    final byte[] buffer = new byte[8 * 1024];
    int len;
    while ((len = input.read(buffer)) != -1) {
      output.write(buffer, 0, len);
    }
    output.flush();
    input.close();
    output.close();
  }

  public static native String nativeGetString();

  public static native void nativeCallJava(Context context);

  private static native void nativeLoadJvmti(String library);
}
