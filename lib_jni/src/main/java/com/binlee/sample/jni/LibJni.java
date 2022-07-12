package com.binlee.sample.jni;

import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    // javaLoadJvmti(context, dest.getAbsolutePath());
  }

  private static void javaLoadJvmti(Context context, String library) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      try {
        Debug.attachJvmtiAgent(library, null, context.getClassLoader());
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      try {
        final Class<?> clazz = Class.forName("dalvik.system.VMDebug");
        final Method method = clazz.getDeclaredMethod("attachAgent", String.class);
        method.setAccessible(true);
        method.invoke(null, library);
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
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
