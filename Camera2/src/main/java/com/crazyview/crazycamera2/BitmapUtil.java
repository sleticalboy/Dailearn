package com.crazyview.crazycamera2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Auther: Crazy.Mo
 * DateTime: 2017/8/25 17:34
 * Summary:
 */
public class BitmapUtil {

  /**
   * 压缩图片
   */
  public static Bitmap compressionBitmap(@NonNull Bitmap bitmap) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
    //判断如果图片大于2M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
    if (baos.toByteArray().length / 1024 > 1024 * 2) {
      LogUtil.showDebugLog("压缩前" + baos.toByteArray().length / 1024);
      //重置baos即清空baos
      baos.reset();
      //这里压缩60%，把压缩后的数据存放到baos中
      bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
    }
    LogUtil.showDebugLog("压缩后" + baos.toByteArray().length / 1024);
    ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
    BitmapFactory.Options newOpts = new BitmapFactory.Options();
    //开始读入图片，此时把options.inJustDecodeBounds 设回true了
    newOpts.inJustDecodeBounds = true;
    BitmapFactory.decodeStream(isBm, null, newOpts);
    newOpts.inJustDecodeBounds = false;
    int w = newOpts.outWidth;
    int h = newOpts.outHeight;
    float hh = 720f;
    float ww = 1280f;
    //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
    int be = 1;//be=1表示不缩放
    if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
      be = (int) (newOpts.outWidth / ww);
    } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
      be = (int) (newOpts.outHeight / hh);
    }
    if (be <= 0) {
      be = 1;
    }
    newOpts.inSampleSize = be;//设置缩放比例
    //降低图片从ARGB888到RGB565
    newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
    //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
    isBm = new ByteArrayInputStream(baos.toByteArray());
    bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
    return bitmap;//压缩好比例大小后再进行质量压缩
  }

  /**
   * byte[]转换成Bitmap
   */
  public static Bitmap bytes2Bitmap(byte[] bytes) {
    if (bytes.length != 0) {
      return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    } else {
      return null;
    }
  }

  /**
   * Bitmap转换成byte[]
   */
  public static byte[] bitmap2Bytes(Bitmap bitmap) {
    ByteArrayOutputStream byteArrOutStream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrOutStream);
    return byteArrOutStream.toByteArray();
  }

  /**
   * 按采样率压缩
   */
  public static Bitmap compressBitmapBySampleSize(@NonNull Bitmap bitmap, int sampleSize) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = sampleSize;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
    return BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.toByteArray().length, options);
  }

  public static Bitmap compressBySampleSize(Bitmap bitmap, int sampleSize) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
    BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length, options);
    options.inSampleSize = sampleSize;
    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length, options);
  }
}
