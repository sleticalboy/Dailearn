package com.binlee.learning.ffmpeg;

import android.media.MediaCodec;
import android.util.Log;
import android.util.SparseArray;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created on 2023/3/17
 *
 * @author binlee
 */
public final class AVUtil {

  private static final String TAG = "AVUtil";

  private static final SparseArray<String> BUFFER_FLAGS;

  static {
    BUFFER_FLAGS = getFields(MediaCodec.class, "BUFFER_FLAG_");
  }

  private AVUtil() {
    //no instance
  }

  public static void dumpBufferFlags(int flags) {
    if ((flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0) {
      Log.d(TAG, "dumpBufferFlags() BUFFER_FLAG_KEY_FRAME(1)");
    }
    if ((flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
      Log.d(TAG, "dumpBufferFlags() BUFFER_FLAG_CODEC_CONFIG(2)");
    }
    if ((flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
      Log.d(TAG, "dumpBufferFlags() BUFFER_FLAG_END_OF_STREAM(4)");
    }
    if ((flags & MediaCodec.BUFFER_FLAG_PARTIAL_FRAME) != 0) {
      Log.d(TAG, "dumpBufferFlags() BUFFER_FLAG_PARTIAL_FRAME(8)");
    }
  }

  private static SparseArray<String> getFields(Class<?> clazz, String prefix) {
    final SparseArray<String> map = new SparseArray<>();
    for (Field field : clazz.getDeclaredFields()) {
      if (!Modifier.isStatic(field.getModifiers())) continue;
      final Class<?> type = field.getType();
      if (type == Integer.class || type == int.class) {
        final String name = field.getName();
        if (name.startsWith(prefix)) {
          try {
            final int value = field.getInt(null);
            map.put(value, name + "(" + value + ")");
          } catch (IllegalAccessException ignored) {
          }
        }
      }
    }
    return map;
  }
}
