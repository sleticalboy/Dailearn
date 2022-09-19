package com.binlee.dl;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on 19-4-5.
 *
 * @author leebin
 */
public final class FileUtils {

  private static final String TAG = "FileUtils";

  public static boolean copy(File src, File dest) {
    Log.d(TAG, "copy() called with: source = [" + src + "], target = [" + dest + "]");
    if (!src.exists()) {
      Log.e(TAG, "src: " + src + " not exists");
      return false;
    }
    if (dest.exists()) {
      if (dest.delete()) {
        Log.d(TAG, "delete old fix file");
      }
    }
    try {
      return copy(new FileInputStream(src), new FileOutputStream(dest));
    } catch (IOException e) {
      Log.e(TAG, "copy file from " + src + " to " + dest + " failed " + e.getMessage(), e);
      return false;
    }
  }

  public static boolean copy(InputStream src, OutputStream dest) throws IOException {
    final BufferedInputStream input = new BufferedInputStream(src);
    final BufferedOutputStream out = new BufferedOutputStream(dest);
    byte[] buf = new byte[1 << 13];
    int len;
    while ((len = input.read(buf)) != -1) {
      out.write(buf, 0, len);
    }
    out.flush();
    input.close();
    out.close();
    return true;
  }

  public static boolean validateFile(String filepath) {
    if (filepath == null || filepath.trim().length() == 0) {
      Log.e(TAG, "validateFile() failed: " + filepath);
      return false;
    }
    final File file = new File(filepath.trim());
    if (!file.exists()) {
      Log.e(TAG, "validateFile() failed: " + filepath + " not existed");
      return false;
    }
    if (!file.canRead() && !file.setReadable(true)) {
      Log.e(TAG, "validateFile() failed: " + filepath + " not readable");
      return false;
    }
    return true;
  }
}
