package com.binlee.learning.camera;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import androidx.annotation.NonNull;
import java.util.List;

/**
 * @author binlee sleticalboy@gmail.com
 * created by IDEA on 3/25/23
 */
public class CameraX {

  private static final String TAG = "CameraUtil";
  private static final int NOT_FOUND = -1;
  public static final String KEY_PICTURE_SIZE = "camera_sp_key_picture_size_"/*cameraId*/;

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
    float diff = 0f;
    Log.w(TAG, "setupPictureSize() display w: " + width + ", h: " + height + ", ratio: " + rawRatio);

    Camera.Size target = null;
    for (Camera.Size size : supported) {
      final float ratio = size.width * 1f / size.height;
      Log.w(TAG, "setupPictureSize() size w: " + size.width + ", h: " + size.height + ", ratio: "
          + ratio + " <-> " + Math.abs(ratio - rawRatio) + ", diff: " + Math.abs(height - size.height));
      if (Math.abs(ratio - rawRatio) <= 0.12) {
        if (Math.abs(height - size.height) < diff) {
          diff = Math.abs(height - size.height);
          target = size;
        }
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

  public static Camera.Size optimizePreviewSize(Activity activity, List<Camera.Size> sizes, double targetRatio) {
    // Use a very small tolerance because we want an exact match.
    final double ASPECT_TOLERANCE = 0.001;
    if (sizes == null) return null;

    Camera.Size optimalSize = null;
    double minDiff = Double.MAX_VALUE;

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

    // Try to find an size match aspect ratio and size
    for (Camera.Size size : sizes) {
      double ratio = (double) size.width / size.height;
      if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
      if (Math.abs(size.height - targetHeight) < minDiff) {
        optimalSize = size;
        minDiff = Math.abs(size.height - targetHeight);
      }
    }

    // Cannot find the one match the aspect ratio. This should not happen.
    // Ignore the requirement.
    if (optimalSize == null) {
      Log.w(TAG, "No preview size match the aspect ratio");
      minDiff = Double.MAX_VALUE;
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

    float left = clamp(point.x - areaWidth / 2f, 0, surfaceSpec.x - focusSpec.x);
    float top = clamp(point.y - areaHeight / 2f, 0, surfaceSpec.y - focusSpec.y);
    RectF rectF = new RectF(left, top, left + areaWidth, top + areaHeight);
    matrix.mapRect(rectF);

    return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
  }

  public static float clamp(float x, float min, float max) {
    return x > max ? max : Math.max(x, min);
  }
}
