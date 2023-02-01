package com.binlee.luban;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created on 2023/2/1
 *
 * @author binlee
 */
public final class Luban {

  private static final String TAG = "Luban";

  static {
    System.loadLibrary("luban");
    Log.d(TAG, "load lib luban");
  }

  private Luban() {
    //no instance
  }

  public static boolean compressImage(@NonNull String src, int quality, String outPath) {
    return compressImage(new File(src), quality, outPath);
  }

  public static boolean compressImage(@NonNull File src, int quality, String outPath) {
    try {
      return compressImage(new FileInputStream(src), quality, outPath);
    } catch (FileNotFoundException e) {
      Log.e(TAG, "compressImage() error " + e.getMessage(), e);
      return false;
    }
  }

  public static boolean compressImage(@NonNull InputStream src, int quality, String outPath) {
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = 2;
    final Bitmap bitmap = BitmapFactory.decodeStream(src, null, options);
    return compressImage(bitmap, quality, outPath);
  }

  public static boolean compressImage(Bitmap bitmap, int quality, String outPath) {
    // quality = 60 就可以了
    final boolean success = nativeCompress(bitmap, quality, outPath);
    Log.d(TAG, "compressImage() " + (success ? "success" : "failed"));
    return success;
  }

  private native static boolean nativeCompress(Bitmap bitmap, int quality, String outPath);
}
