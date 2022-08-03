package com.binlee.learning.http;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created on 18-9-3.
 *
 * @author leebin
 */
public final class FileTypeUtils {

  private static final Map<String, String> sFileTypes = new ArrayMap<>();
  private static final String UNKNOWN_KEY = "000000";
  private static final String UNKNOWN_VALUE = "unknown";

  static {
    // images
    sFileTypes.put("FFD8FF", "jpg");
    sFileTypes.put("89504E47", "png");
    sFileTypes.put("47494638", "gif");
    sFileTypes.put("49492A00", "tif");
    sFileTypes.put("424D", "bmp");
    //other
    sFileTypes.put("41433130", "dwg"); // CAD
    sFileTypes.put("38425053", "psd");
    sFileTypes.put("7B5C727466", "rtf"); // 日记本
    sFileTypes.put("3C3F786D6C", "xml");
    sFileTypes.put("68746D6C3E", "html");
    sFileTypes.put("44656C69766572792D646174653A", "eml"); // 邮件
    sFileTypes.put("D0CF11E0", "doc");
    sFileTypes.put("5374616E64617264204A", "mdb");
    sFileTypes.put("252150532D41646F6265", "ps");
    sFileTypes.put("255044462D312E", "pdf");
    sFileTypes.put("504B0304", "docx");
    sFileTypes.put("52617221", "rar");
    sFileTypes.put("57415645", "wav");
    sFileTypes.put("41564920", "avi");
    sFileTypes.put("2E524D46", "rm");
    sFileTypes.put("000001BA", "mpg");
    sFileTypes.put("000001B3", "mpg");
    sFileTypes.put("6D6F6F76", "mov");
    sFileTypes.put("3026B2758E66CF11", "asf");
    sFileTypes.put("4D546864", "mid");
    sFileTypes.put("1F8B08", "gz");
    sFileTypes.put(UNKNOWN_KEY, UNKNOWN_VALUE);
  }

  public static String getFileType(@NonNull String filePath) {
    return getFileType(new File(filePath));
  }

  public static String getFileType(@NonNull File file) {
    if (!file.exists()) return null;
    try {
      return getType(new FileInputStream(file));
    } catch (FileNotFoundException e) {
      return null;
    }
  }

  private static String getType(@NonNull InputStream is) {
    String header;
    try {
      byte[] b = new byte[4];
      // int read() 从此输入流中读取一个数据字节。
      // int read(byte[] b) 从此输入流中将最多 b.length 个字节的数据读入一个 byte 数组中。
      // int read(byte[] b, int off, int len) 从此输入流中将最多 len 个字节的数据读入一个 byte 数组中。
      is.read(b, 0, b.length);
      header = bytesToHexString(b);
    } catch (Exception ignored) {
      header = UNKNOWN_KEY;
    } finally {
      try {
        is.close();
      } catch (IOException ignored) {
      }
    }
    return sFileTypes.get(header);
  }

  /**
   * 将要读取文件头信息的文件的byte数组转换成string类型表示
   *
   * @param src 要读取文件头信息的文件的byte数组
   * @return 文件头信息
   */
  private static String bytesToHexString(byte[] src) {
    if (src == null || src.length <= 0) return null;
    String hv;
    StringBuilder builder = new StringBuilder();
    for (final byte b : src) {
      // 以十六进制（基数 16）无符号整数形式返回一个整数参数的字符串表示形式，并转换为大写
      hv = Integer.toHexString(b & 0xFF).toUpperCase();
      if (hv.length() < 2) {
        builder.append(0);
      }
      builder.append(hv);
    }
    return builder.toString();
  }

  public static void closeSilently(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException ignored) {
      }
    }
  }

  public static String eraseToken(String url) {
    int index = url.contains("?token=") ? url.indexOf("?token=") : url.indexOf("&token=");
    if (index < 0) return url;
    String token;
    int nextAndIndex = url.indexOf("&", index + 1);
    if (nextAndIndex != -1) {
      token = url.substring(index + 1, nextAndIndex + 1);
    } else {
      token = url.substring(index);
    }
    return url.replace(token, "");
  }

  public static Bitmap circleCrop(Bitmap source, int diameter) {
    final Bitmap result = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);

    int dx = (source.getWidth() - diameter) / 2;
    int dy = (source.getHeight() - diameter) / 2;
    Canvas canvas = new Canvas(result);
    Paint paint = new Paint();
    BitmapShader shader = new BitmapShader(source, Shader.TileMode.CLAMP,
      Shader.TileMode.CLAMP);
    if (dx != 0 || dy != 0) {
      Matrix matrix = new Matrix();
      matrix.setTranslate(-dx, -dy);
      shader.setLocalMatrix(matrix);
    }
    paint.setShader(shader);
    paint.setAntiAlias(true);
    float radius = diameter / 2.0f;
    canvas.drawCircle(radius, radius, radius, paint);
    return result;
  }

  public static Bitmap circle(@NonNull Bitmap toTransform, int diameter) {
    final Bitmap result = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);

    int dx = (toTransform.getWidth() - diameter) / 2;
    int dy = (toTransform.getHeight() - diameter) / 2;
    Canvas canvas = new Canvas(result);
    Paint paint = new Paint();
    BitmapShader shader = new BitmapShader(toTransform, Shader.TileMode.CLAMP,
      Shader.TileMode.CLAMP);
    if (dx != 0 || dy != 0) {
      Matrix matrix = new Matrix();
      matrix.setTranslate(-dx, -dy);
      shader.setLocalMatrix(matrix);
    }
    paint.setShader(shader);
    paint.setAntiAlias(true);
    float radius = diameter / 2.0f;
    canvas.drawCircle(radius, radius, radius, paint);
    return result;
  }


    /*
    class  ProgressSource extends ForwardingSource {

        long mTotalByteRead = 0L;
        int mCurrentProgress;

        ProgressSource(Source delegate) {
            super(delegate);
        }

        @Override
        public long read(@NonNull Buffer sink, long byteCount) throws IOException {
            long bytesRead = super.read(sink, byteCount);
            long fullLength = mBody.contentLength();
            if (bytesRead == -1) {
                mTotalByteRead = fullLength;
            } else {
                mTotalByteRead += bytesRead;
            }
            int progress = (int) (100f * mTotalByteRead / fullLength);
            Log.d(TAG, "download progress is " + progress);
            if (mListener != null && progress != mCurrentProgress) {
                mListener.onProgress(progress);
            }
            if (mListener != null && mTotalByteRead == fullLength) {
                mListener = null;
            }
            mCurrentProgress = progress;
            return bytesRead;
        }
    }
     */
}
