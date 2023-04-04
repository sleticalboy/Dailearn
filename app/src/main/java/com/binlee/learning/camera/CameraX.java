package com.binlee.learning.camera;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import androidx.annotation.NonNull;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * @author binlee sleticalboy@gmail.com
 * created by IDEA on 3/25/23
 */
public class CameraX {

  private static final String TAG = "CameraX";
  private static final int NOT_FOUND = -1;
  private static final String KEY_PICTURE_SIZE = "camera_sp_key_picture_size_"/*cameraId*/;
  private static final String KEY_LAST_THUMBNAIL = "camera_sp_key_last_thumbnail";

  private CameraX() {
    //no instance
  }

  public static void prepareMatrix(Matrix matrix, boolean mirror, int displayOrientation, Point spec) {
    // Need mirror for front camera.
    matrix.setScale(mirror ? -1 : 1, 1);
    // This is the value for android.hardware.Camera.setDisplayOrientation.
    matrix.postRotate(displayOrientation);
    // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
    // UI coordinates range from (0, 0) to (width, height).
    matrix.postScale(spec.x / 2000f, spec.y / 2000f);
    matrix.postTranslate(spec.x / 2f, spec.y / 2f);
  }

  @NonNull public static Face[] convertFaces(Camera.Face[] faces) {
    final Face[] dest = new Face[faces == null ? 0 : faces.length];
    for (int i = 0; i < dest.length; i++) {
      dest[i] = new Face();
      dest[i].id = faces[i].id;
      dest[i].score = faces[i].score;
      dest[i].rect = faces[i].rect;
      dest[i].leftEye = faces[i].leftEye;
      dest[i].rightEye = faces[i].rightEye;
      dest[i].mouth = faces[i].mouth;
    }
    return dest;
  }

  public static void setupPictureSize(Activity activity, Camera.Parameters params, int cameraId) {
    // When launching the camera app first time, we will set the picture
    // size to the first one in the list defined in "arrays.xml" and is also
    // supported by the driver.
    final List<Camera.Size> supported = params.getSupportedPictureSizes();
    if (supported == null) return;

    final SharedPreferences sp = activity.getSharedPreferences("camera_settings", Context.MODE_PRIVATE);
    final String value = sp.getString(KEY_PICTURE_SIZE + cameraId, "");
    if (setCameraPictureSize(value, supported, params)) {
      Log.e(TAG, "setupPictureSize() use last picture size settings: " + value);
      return;
    }

    final Display display = activity.getWindowManager().getDefaultDisplay();
    int width = display.getHeight();
    int height = display.getWidth();
    // 屏幕宽高比例 & 所支持的高和屏幕高的最小差值
    final float rawRatio = width * 1f / height;
    float diff = 1000f;
    Log.w(TAG, "setupPictureSize() display w: " + width + ", h: " + height + ", ratio: " + rawRatio);

    Camera.Size target = null;
    for (Camera.Size size : supported) {
      final float ratio = size.width * 1f / size.height;
      Log.w(TAG, "setupPictureSize() size w: " + size.width + ", h: " + size.height + ", ratio: "
          + ratio + " <-> " + Math.abs(ratio - rawRatio) + ", diff: " + Math.abs(height - size.height));
      if (Math.abs(ratio - rawRatio) > 0.125) continue;

      if (Math.abs(height - size.height) < diff) {
        diff = Math.abs(height - size.height);
        target = size;
      }
    }
    if (target != null) {
      params.setPictureSize(target.width, target.height);
      sp.edit().putString(KEY_PICTURE_SIZE + cameraId, target.width + "x" + target.height).apply();
    } else {
      final float ratio = 2688 / 1512f;
      Log.e(TAG, "setupPictureSize() not found, use (2688, 1512), ratio: " + ratio + " <-> "
          + Math.abs(ratio - rawRatio) + ", diff: " + Math.abs(1512 - height));
      params.setPictureSize(2688, 1512);
    }
  }

  public static Camera.Size optimizePreviewSize(Activity activity, List<Camera.Size> sizes, float targetRatio) {
    if (sizes == null) return null;

    // Use a very small tolerance because we want an exact match.
    final float ASPECT_TOLERANCE = 0.001f;
    float minDiff = 1000f;

    // Because of bugs of overlay and layout, we sometimes will try to
    // layout the viewfinder in the portrait orientation and thus get the
    // wrong size of mSurfaceView. When we change the preview size, the
    // new overlay will be created before the old one closed, which causes
    // an exception. For now, just get the screen size

    Display display = activity.getWindowManager().getDefaultDisplay();
    int targetHeight = Math.min(display.getHeight(), display.getWidth());

    if (targetHeight <= 0) {
      // We don't know the size of SurfaceView, use screen height
      targetHeight = display.getHeight();
    }

    Log.d(TAG, "optimizePreviewSize() target r: " + targetRatio + ", h: " + targetHeight);

    // Try to find an size match aspect ratio and size
    Camera.Size optimalSize = null;
    for (Camera.Size size : sizes) {
      float ratio = size.width * 1f / size.height;
      // Log.d(TAG, "optimizePreviewSize() size(" + size.width + ", " + size.height + "), ratio: " + ratio
      //   + " <-> " + Math.abs(ratio - targetRatio));
      if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;

      if (Math.abs(size.height - targetHeight) < minDiff) {
        optimalSize = size;
        minDiff = Math.abs(size.height - targetHeight);
      }
    }

    // Cannot find the one match the aspect ratio. This should not happen.
    // Ignore the requirement.
    if (optimalSize == null) {
      Log.w(TAG, "optimizePreviewSize() not found");
      for (Camera.Size size : sizes) {
        if (Math.abs(size.height - targetHeight) < minDiff) {
          optimalSize = size;
          minDiff = Math.abs(size.height - targetHeight);
        }
      }
    }
    if (optimalSize != null) {
      Log.w(TAG, "optimizePreviewSize() (" + optimalSize.width + ", " + optimalSize.height + "), ratio: "
          + (optimalSize.width * 1f / optimalSize.height));
    }
    return optimalSize;
  }

  public static boolean setCameraPictureSize(String candidate, List<Camera.Size> supported, Camera.Parameters params) {
    int index = candidate.indexOf('x');
    if (index == NOT_FOUND) return false;
    int width = Integer.parseInt(candidate.substring(0, index));
    int height = Integer.parseInt(candidate.substring(index + 1));
    for (Camera.Size size : supported) {
      if (size.width == width && size.height == height) {
        params.setPictureSize(width, height);
        return true;
      }
    }
    return false;
  }

  public static Rect getTapArea(Point surfaceSpec, Point focusSpec, float scale, PointF point, Matrix matrix) {
    float areaWidth = focusSpec.x * scale;
    float areaHeight = focusSpec.y * scale;

    float left = clamp(point.x - areaWidth / 2f, 0, surfaceSpec.x - areaWidth);
    float top = clamp(point.y - areaHeight / 2f, 0, surfaceSpec.y - areaHeight);
    RectF rectF = new RectF(left, top, left + areaWidth, top + areaHeight);
    matrix.mapRect(rectF);

    return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
  }

  public static float clamp(float x, float min, float max) {
    return x > max ? max : Math.max(x, min);
  }

  private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);

  public static String generateFilepath(String suffix) {
    return generateFilepath(null, suffix);
  }

  public static String generateFilepath(String prefix, String suffix) {
    if (!TextUtils.isEmpty(prefix)) {
      return String.format("%s_%s_%s", prefix, FORMAT.format(System.currentTimeMillis()), suffix);
    } else {
      return String.format("%s_%s", FORMAT.format(System.currentTimeMillis()), suffix);
    }
  }

  public static Uri addImage(ContentResolver resolver, String path, long date,
    Location location, int orientation, byte[] jpeg, int width, int height) {
    // Save the image.
    try (FileOutputStream out = new FileOutputStream(path)) {
      out.write(jpeg);
    } catch (Exception e) {
      Log.e(TAG, "Failed to write image", e);
      return null;
    }

    // 取文件名
    String title = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));
    final int index = title.indexOf('_');
    final int lastIndex = title.lastIndexOf('_');
    if (index != lastIndex) {
      title = title.substring(0, lastIndex);
    }

    // Insert into MediaStore.
    ContentValues values = new ContentValues(9);
    values.put(MediaStore.Images.ImageColumns.TITLE, title);
    values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, title + ".jpg");
    values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, date);
    values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg");
    values.put(MediaStore.Images.ImageColumns.ORIENTATION, orientation);
    values.put(MediaStore.Images.ImageColumns.DATA, path);
    values.put(MediaStore.Images.ImageColumns.SIZE, jpeg.length);
    values.put(MediaStore.Images.ImageColumns.WIDTH, width);
    values.put(MediaStore.Images.ImageColumns.HEIGHT, height);

    if (location != null) {
      values.put(MediaStore.Images.ImageColumns.LATITUDE, location.getLatitude());
      values.put(MediaStore.Images.ImageColumns.LONGITUDE, location.getLongitude());
    }

    Uri uri = null;
    try {
      uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    } catch (Throwable tr)  {
      // This can happen when the external volume is already mounted, but
      // MediaScanner has not notify MediaProvider to add that volume.
      // The picture is still safe and MediaScanner will find it and
      // insert it into MediaProvider. The only problem is that the user
      // cannot click the thumbnail to review the picture.
      Log.e(TAG, "Failed to write MediaStore" + tr);
    }
    return uri;
  }

  public static void setThumbnail(Context context, String path) {
    final SharedPreferences sp = context.getSharedPreferences("camera_settings", Context.MODE_PRIVATE);
    sp.edit().putString(KEY_LAST_THUMBNAIL, path).apply();
  }

  public static String getLastThumbnail(Context context) {
    final SharedPreferences sp = context.getSharedPreferences("camera_settings", Context.MODE_PRIVATE);
    return sp.getString(KEY_LAST_THUMBNAIL, null);
  }
}
