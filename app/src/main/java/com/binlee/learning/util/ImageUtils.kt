package com.binlee.learning.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Base64
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

/**
 * Created by renlei
 * DATE: 15-11-5
 * Time: 下午7:21
 * Email: lei.ren@renren-inc.com
 */
object ImageUtils {

  private const val TAG = "ImageUtils"

  /**
   * 获取保存文件的路径
   *
   * @return
   */
  val saveImagePath: String
    get() {
      if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        val path: String = Environment.getExternalStorageDirectory().path +
            "/dailywork/" + System.currentTimeMillis() + ".jpg"
        val file = File(path)
        if (!file.parentFile.exists()) {
          file.parentFile.mkdirs()
        }
        return path
      }
      return System.currentTimeMillis().toString() + ".jpg"
    }

  fun saveImage(file: File, data: ByteArray, filePath: String) {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    val tempBitmap = BitmapFactory.decodeFile(filePath, options)
    val degrees = getExifRotateDegree(filePath)
  }

  private fun getExifRotateDegree(path: String): Int {
    try {
      val exifInterface = ExifInterface(path)
      val orientation = exifInterface.getAttributeInt(
        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
      )
      val degrees = getExifRotateDegrees(orientation)
      Log.d(TAG, "degrees = $degrees")
      return degrees
    } catch (e: IOException) {
      e.printStackTrace()
    }
    return 0
  }

  private fun getExifRotateDegrees(exifOrientation: Int): Int {
    var degrees = 0
    when (exifOrientation) {
      ExifInterface.ORIENTATION_NORMAL -> degrees = 0
      ExifInterface.ORIENTATION_ROTATE_90 -> degrees = 90
      ExifInterface.ORIENTATION_ROTATE_180 -> degrees = 180
      ExifInterface.ORIENTATION_ROTATE_270 -> degrees = 270
    }
    return degrees
  }

  fun bitmap2StrByBase64(bit: Bitmap): String {
    val bos = ByteArrayOutputStream()
    bit.compress(Bitmap.CompressFormat.JPEG, 10, bos) //参数100表示不压缩
    val bytes = bos.toByteArray()
    return Base64.encodeToString(bytes, Base64.DEFAULT)
  }

  fun base64ToBitmap(base64Data: String): Bitmap {
    val bytes = Base64.decode(base64Data, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
  }
}
