package com.binlee.http;

import android.os.Looper;
import androidx.annotation.NonNull;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ThreadFactory;
import okhttp3.Headers;
import okhttp3.Response;
import okio.Buffer;

public final class OkUtils {

  public static final String INVALID_HOST = "Invalid URL host";
  private static final Comparator<String> FIELD_NAME_COMPARATOR = (a, b) -> {
    if (Objects.equals(a, b)) {
      return 0;
    } else if (a == null) {
      return -1;
    } else if (b == null) {
      return 1;
    } else {
      return String.CASE_INSENSITIVE_ORDER.compare(a, b);
    }
  };

  private OkUtils() {
    throw new RuntimeException();
  }

  public static String streamToString(InputStream in) {
    ByteArrayOutputStream baos = null;
    try {
      baos = new ByteArrayOutputStream();
      final byte[] buffer = new byte[8192];
      int len;
      while ((len = in.read(buffer)) != -1) {
        baos.write(buffer, 0, len);
      }
      return baos.toString();
    } catch (IOException e) {
      return "";
    } finally {
      closeSilently(in);
      closeSilently(baos);
    }
  }

  public static void closeSilently(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException ignored) {
      }
    }
  }

  @NonNull
  public static URL parse(String urlString) {
    try {
      return new URL(urlString);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e); // Unexpected!
    }
  }

  /**
   * Returns an immutable map containing each field to its list of values.
   *
   * @param valueForNullKey the request line for requests, or the status line for responses. If
   * non-null, this value is mapped to the null key.
   */
  public static Map<String, List<String>> toMultimap(Headers headers, String valueForNullKey) {
    Map<String, List<String>> result = new TreeMap<>(FIELD_NAME_COMPARATOR);
    for (int i = 0, size = headers.size(); i < size; i++) {
      String fieldName = headers.name(i);
      String value = headers.value(i);

      List<String> allValues = new ArrayList<>();
      List<String> otherValues = result.get(fieldName);
      if (otherValues != null) {
        allValues.addAll(otherValues);
      }
      allValues.add(value);
      result.put(fieldName, Collections.unmodifiableList(allValues));
    }
    if (valueForNullKey != null) {
      result.put(null, Collections.unmodifiableList(Collections.singletonList(valueForNullKey)));
    }
    return Collections.unmodifiableMap(result);
  }

  public static String responseSourceHeader(Response response) {
    if (response.networkResponse() == null) {
      if (response.cacheResponse() == null) {
        return "NONE";
      }
      return "CACHE " + response.code();
    }
    if (response.cacheResponse() == null) {
      return "NETWORK " + response.code();
    }
    return "CONDITIONAL_CACHE " + response.networkResponse().code();
  }

  /**
   * Throws {@code throwable} as either an IOException, RuntimeException, or Error.
   */
  public static IOException propagate(Throwable throwable) throws IOException {
    if (throwable instanceof IOException) throw (IOException) throwable;
    if (throwable instanceof Error) throw (Error) throwable;
    if (throwable instanceof RuntimeException) throw (RuntimeException) throwable;
    throw new AssertionError();
  }

  /**
   * Returns {@code s} with control characters and non-ASCII characters replaced with '?'.
   */
  public static String toHumanReadableAscii(String s) {
    for (int i = 0, length = s.length(), c; i < length; i += Character.charCount(c)) {
      c = s.codePointAt(i);
      if (c > '\u001f' && c < '\u007f') continue;

      Buffer buffer = new Buffer();
      buffer.writeUtf8(s, 0, i);
      buffer.writeUtf8CodePoint('?');
      for (int j = i + Character.charCount(c); j < length; j += Character.charCount(c)) {
        c = s.codePointAt(j);
        buffer.writeUtf8CodePoint(c > '\u001f' && c < '\u007f' ? c : '?');
      }
      return buffer.readUtf8();
    }
    return s;
  }

  public static <R> void requiresNoNull(R ref) {
    requiresNoNull(ref, "ref == null");
  }

  public static <R> void requiresNoNull(R ref, String msg) {
    if (ref == null) {
      throw new NullPointerException(msg);
    }
  }

  public static <R> R checkNotNull(final R ref) {
    return checkNotNull(ref, "ref == null");
  }

  public static <R> R checkNotNull(final R ref, String msg) {
    if (ref == null) {
      throw new NullPointerException(msg);
    }
    return ref;
  }

  public static ThreadFactory threadFactory(final String name, final boolean daemon) {
    return runnable -> {
      Thread result = new Thread(runnable, name);
      result.setDaemon(daemon);
      return result;
    };
  }

  /**
   * 转换成进度
   *
   * @param read 已下载
   * @param total 总长度
   * @return 进度
   */
  public static int toProgress(long read, long total) {
    return (int) (100F * read / total);
  }

  public static void releaseCall(retrofit2.Call call) {
    if (call != null && !call.isCanceled()) {
      call.cancel();
    }
  }

  public static void releaseCall(final okhttp3.Call call) {
    if (call != null && !call.isCanceled()) {
      call.cancel();
    }
  }

  public static Proxy createProxy(final int port) {
    return createProxy(null, port);
  }

  public static Proxy createProxy(String hostname, final int port) {
    if (port < 0) return null;
    if (hostname == null) {
      hostname = "127.0.0.1";
    }
    return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, port));
  }

  public static <E> boolean empty(Collection<E> collection) {
    return collection == null || collection.size() == 0;
  }

  public static <K, V> boolean empty(Map<K, V> map) {
    return map == null || map.size() == 0;
  }

  @NonNull
  public static <E> String listToString(@NonNull List<E> list, String startSplit, String split) {
    return arrayToString(list.toArray(new Object[0]), startSplit, split);
  }

  /**
   * from {@link Arrays#toString(Object[])}
   *
   * @param a the array whose string representation to return
   * @return a string representation of <tt>a</tt>
   */
  @NonNull
  public static String arrayToString(@NonNull Object[] a, String startSplit, String split) {
    int iMax = a.length - 1;
    if (iMax == -1) return "";
    final StringBuilder b = new StringBuilder(startSplit);
    for (int i = 0; ; i++) {
      b.append(a[i]);
      if (i == iMax) return b.toString();
      b.append(split);
    }
  }

  /**
   * Returns {@code true} if called on a background thread, {@code false} otherwise.
   */
  public static boolean isOnWorkerThread() {
    return !isOnMainThread();
  }

  /**
   * Returns {@code true} if called on the main thread, {@code false} otherwise.
   */
  public static boolean isOnMainThread() {
    return Looper.myLooper() == Looper.getMainLooper();
  }

  public static boolean empty(final String target) {
    return target == null || target.trim().length() == 0;
  }
}
