package com.binlee.learning.camera;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import androidx.annotation.NonNull;
import com.binlee.learning.R;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author binlee sleticalboy@gmail.com
 * created by IDEA on 3/25/23
 */
public class CameraUtil {

  private static final String TAG = "CameraUtil";
  private static final int NOT_FOUND = -1;
  public static final String KEY_PICTURE_SIZE = "camera_sp_key_picture_size_"/*cameraId*/;

  public static void prepareMatrix(Matrix matrix, boolean mirror, int displayOrientation,
                                   int viewWidth, int viewHeight) {
    // Need mirror for front camera.
    matrix.setScale(mirror ? -1 : 1, 1);
    // This is the value for android.hardware.Camera.setDisplayOrientation.
    matrix.postRotate(displayOrientation);
    // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
    // UI coordinates range from (0, 0) to (width, height).
    matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
    matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);
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
    return optimalSize;
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
    final Display display = activity.getWindowManager().getDefaultDisplay();
    Log.d(TAG, "setupPictureSize() display w: " + display.getWidth() + ", h: " + display.getHeight());
    List<Camera.Size> supported = params.getSupportedPictureSizes();
    if (supported == null) return;
    final SharedPreferences sp = activity.getSharedPreferences("camera_settings", Context.MODE_PRIVATE);
    final String value = sp.getString(KEY_PICTURE_SIZE + cameraId, "");
    if (setCameraPictureSize(value, supported, params)) {
      Log.e(TAG, "Use last picture size settings: " + value);
      return;
    }
    int width = display.getHeight();
    int height = display.getWidth();
    for (Camera.Size size : supported) {
      if (size.height == height && size.width < width) {
        if (Math.round(size.width * 1f / width) == 1f) {
          params.setPictureSize(size.width, size.height);
          sp.edit().putString(KEY_PICTURE_SIZE + cameraId, size.width + "x" + size.height).apply();
          return;
        }
      }
    }
    Log.e(TAG, "No supported picture size found");
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

}
