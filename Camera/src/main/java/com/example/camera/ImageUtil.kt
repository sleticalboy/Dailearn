package com.example.camera

import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by renlei
 * DATE: 15-11-5
 * Time: 下午7:21
 * Email: lei.ren@renren-inc.com
 */
object ImageUtil {

    private const val TAG = "ImageUtil"

    @Throws(IOException::class)
    fun saveImage(data: ByteArray?, path: String) {
        // save
        val file = File(path)
        if (file.exists()) {
            file.delete()
        } else {
            file.createNewFile()
        }
        val fos = FileOutputStream(file, true)
        fos.write(data)
        fos.close()

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(data, 0, data!!.size, options)
        Log.d(TAG, "saveImage() width: ${options.outWidth}, height: ${options.outHeight}")
        // val degrees = rotateDegree(path)
    }

    private fun rotateDegree(path: String?): Int {
        try {
            val exifInterface = ExifInterface(path!!)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )
            val degrees = rotateDegrees(orientation)
            Log.d(TAG, degrees.toString() + "")
            return degrees
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return 0
    }

    private fun rotateDegrees(exifOrientation: Int): Int {
        var degrees = 0
        when (exifOrientation) {
            ExifInterface.ORIENTATION_NORMAL -> degrees = 0
            ExifInterface.ORIENTATION_ROTATE_90 -> degrees = 90
            ExifInterface.ORIENTATION_ROTATE_180 -> degrees = 180
            ExifInterface.ORIENTATION_ROTATE_270 -> degrees = 270
        }
        return degrees
    }
}