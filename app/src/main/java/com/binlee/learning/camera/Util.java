package com.binlee.learning.camera;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import androidx.annotation.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author binlee sleticalboy@gmail.com
 * created by IDEA on 3/25/23
 */
public class Util {

  private static final String TAG = "CameraUtil";

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

  public static Camera.Size getOptimalPreviewSize(Activity activity, List<Camera.Size> sizes,
                                                  double targetRatio) {
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

  public static void dumpRect(RectF rect, String msg) {
    Log.v(TAG, msg + "=(" + rect.left + "," + rect.top + "," + rect.right + "," + rect.bottom + ")");
  }
}
