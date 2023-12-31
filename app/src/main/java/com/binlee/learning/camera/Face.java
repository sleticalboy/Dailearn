package com.binlee.learning.camera;

import android.graphics.Point;
import android.graphics.Rect;

/**
 * @author binlee sleticalboy@gmail.com
 * created by IDEA on 3/25/23
 */
public class Face {

  /**
   * Create an empty face.
   */
  public Face() {
  }

  /**
   * Bounds of the face. (-1000, -1000) represents the top-left of the
   * camera field of view, and (1000, 1000) represents the bottom-right of
   * the field of view. For example, suppose the size of the viewfinder UI
   * is 800x480. The rect passed from the driver is (-1000, -1000, 0, 0).
   * The corresponding viewfinder rect should be (0, 0, 400, 240). It is
   * guaranteed left < right and top < bottom. The coordinates can be
   * smaller than -1000 or bigger than 1000. But at least one vertex will
   * be within (-1000, -1000) and (1000, 1000).
   *
   * <p>The direction is relative to the sensor orientation, that is, what
   * the sensor sees. The direction is not affected by the rotation or
   * mirroring of {@link # setDisplayOrientation(int)}. The face bounding
   * rectangle does not provide any information about face orientation.</p>
   *
   * <p>Here is the matrix to convert driver coordinates to View coordinates
   * in pixels.</p>
   * <pre>
   * Matrix matrix = new Matrix();
   * CameraInfo info = CameraHolder.instance().getCameraInfo()[cameraId];
   * // Need mirror for front camera.
   * boolean mirror = (info.facing == CameraInfo.CAMERA_FACING_FRONT);
   * matrix.setScale(mirror ? -1 : 1, 1);
   * // This is the value for android.hardware.Camera.setDisplayOrientation.
   * matrix.postRotate(displayOrientation);
   * // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
   * // UI coordinates range from (0, 0) to (width, height).
   * matrix.postScale(view.getWidth() / 2000f, view.getHeight() / 2000f);
   * matrix.postTranslate(view.getWidth() / 2f, view.getHeight() / 2f);
   * </pre>
   *
   * @see # startFaceDetection()
   */
  public Rect rect;

  /**
   * <p>The confidence level for the detection of the face. The range is 1 to
   * 100. 100 is the highest confidence.</p>
   *
   * <p>Depending on the device, even very low-confidence faces may be
   * listed, so applications should filter out faces with low confidence,
   * depending on the use case. For a typical point-and-shoot camera
   * application that wishes to display rectangles around detected faces,
   * filtering out faces with confidence less than 50 is recommended.</p>
   *
   * @see # startFaceDetection()
   */
  public int score;

  /**
   * An unique id per face while the face is visible to the tracker. If
   * the face leaves the field-of-view and comes back, it will get a new
   * id. This is an optional field, may not be supported on all devices.
   * If not supported, id will always be set to -1. The optional fields
   * are supported as a set. Either they are all valid, or none of them
   * are.
   */
  public int id = -1;

  /**
   * The coordinates of the center of the left eye. The coordinates are in
   * the same space as the ones for {@link #rect}. This is an optional
   * field, may not be supported on all devices. If not supported, the
   * value will always be set to null. The optional fields are supported
   * as a set. Either they are all valid, or none of them are.
   */
  public Point leftEye = null;

  /**
   * The coordinates of the center of the right eye. The coordinates are
   * in the same space as the ones for {@link #rect}.This is an optional
   * field, may not be supported on all devices. If not supported, the
   * value will always be set to null. The optional fields are supported
   * as a set. Either they are all valid, or none of them are.
   */
  public Point rightEye = null;

  /**
   * The coordinates of the center of the mouth.  The coordinates are in
   * the same space as the ones for {@link #rect}. This is an optional
   * field, may not be supported on all devices. If not supported, the
   * value will always be set to null. The optional fields are supported
   * as a set. Either they are all valid, or none of them are.
   */
  public Point mouth = null;
}
