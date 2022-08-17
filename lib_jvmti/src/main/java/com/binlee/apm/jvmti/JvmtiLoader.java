package com.binlee.apm.jvmti;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.text.TextUtils;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created on 2022-07-09.
 *
 * @author binlee
 */
public final class JvmtiLoader {

  private static final String TAG = "JvmtiLoader";
  private static boolean sAttached = false;

  static {
    System.loadLibrary("jvmti");
  }

  private JvmtiLoader() {
    //no instance
  }

  public static void attachAgent(Context context) {
    // 上传上次记录的文件
    // uploadLastFile(context);

    final String nativeLibraryDir = context.getApplicationInfo().nativeLibraryDir;
    final String[] libs = new File(nativeLibraryDir).list();
    Log.d(TAG, "attachAgent() native libraries: " + Arrays.toString(libs) + ", in " + nativeLibraryDir);
    // 这个路径中有 '='，系统会检测 lib 路径中是否包含 '='，如果包含会直接报错，因此要拷贝到私有目录中
    final File dest = new File(context.getFilesDir(), "jvmti-agent.so");
    try {
      copyFile(new File(nativeLibraryDir, "libjvmti.so"), dest);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    Log.d(TAG, "attachAgent() jvmti agent: " + dest);
    final JvmtiConfig config = new JvmtiConfig(context.getFilesDir().getAbsolutePath());
    config.agentLib = dest.getAbsolutePath();
    attachInternal(config);
  }

  private static void uploadLastFile(Context context) {
    try {
      final File file = new File(context.getFilesDir(), "ttt.txt");
      if (file.exists()) {
        ByteArrayOutputStream memory = null;
        final FileInputStream stream = new FileInputStream(file);
        byte b;
        while ((b = (byte) stream.read()) != -1) {
          // 重置 buffer
          if (b == '{') memory = new ByteArrayOutputStream();
          // 读数据的时候，跳过 '\0'
          if (memory != null && b != '\0') memory.write(b);
          // 一个 buffer 结束
          if (memory != null && b == '}') {
            parseJson(memory.toString().trim());
            memory = null;
          }
        }
      }
    } catch (IOException e) {
      Log.w(TAG, "uploadLastFile() failed", e);
    }
  }

  private static void parseJson(String jsonString) {
    Log.d(TAG, "parseJson() " + jsonString);
    try {
      final JSONObject json = new JSONObject(jsonString);
      String value = json.optString("alloc_info");
      if (!TextUtils.isEmpty(value)) Log.d(TAG, "alloc_info -> " + value);
      value = json.optString("thread");
      if (!TextUtils.isEmpty(value)) Log.d(TAG, "thread -> " + value);
      value = json.optString("exception");
      if (!TextUtils.isEmpty(value)) Log.d(TAG, "exception -> " + value);
      value = json.optString("method");
      if (!TextUtils.isEmpty(value)) Log.d(TAG, "method -> " + value);
      value = json.optString("catch_method");
      if (!TextUtils.isEmpty(value)) Log.d(TAG, "catch_method -> " + value);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private static void attachInternal(JvmtiConfig config) {
    if (sAttached) return;
    final String options = config.toOptions();
    nativeAttachAgent(config.agentLib, options);
    // javaAttachAgent(config.agentLib, options);
    sAttached = true;
  }

  @SuppressLint("SoonBlockedPrivateApi")
  private static void javaAttachAgent(String library, String options) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      try {
        Debug.attachJvmtiAgent(library, options, null);
      } catch (IOException e) {
        e.printStackTrace();
      }
      sAttached = true;
      return;
    }
    try {
      final Class<?> clazz = Class.forName("dalvik.system.VMDebug");
      final Method method = clazz.getDeclaredMethod("attachAgent", String.class);
      method.setAccessible(true);
      method.invoke(null, library + "=" + options);
    } catch (Throwable e) {
      e.printStackTrace();
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

  private static native void nativeAttachAgent(String library, String options);
}
