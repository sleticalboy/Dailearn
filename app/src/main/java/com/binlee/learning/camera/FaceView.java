/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.binlee.learning.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public final class FaceView extends View {

  private static final String TAG = "FaceView";
  // The value for android.hardware.Camera.setDisplayOrientation.
  private int mDisplayOrientation;
  // The orientation compensation for the face indicator to make it look
  // correctly in all device orientations. Ex: if the value is 90, the
  // indicator should be rotated 90 degrees counter-clockwise.
  private int mOrientation;
  private boolean mMirror;
  private Face[] mFaces;

  private final Matrix mMatrix = new Matrix();
  private final RectF mRect = new RectF();
  private final Paint mPaint = new Paint();
  private final Point mSpec = new Point();

  public FaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setBackgroundColor(Color.TRANSPARENT);
    mPaint.setColor(Color.WHITE);
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeWidth(2.5f);
  }

  public void setFaces(Face[] faces, int displayOrientation, boolean mirror) {
    mFaces = faces;
    mDisplayOrientation = displayOrientation;
    mMirror = mirror;
    invalidate();
  }

  public void setOrientation(int orientation) {
    mOrientation = orientation;
    invalidate();
  }

  public void clearFaces() {
    mFaces = null;
    invalidate();
  }

  @Override public boolean dispatchTouchEvent(MotionEvent event) {
    return false;
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    Log.d(TAG, "onTouchEvent() called with: event = [" + event + "]");
    return performClick();
  }

  @Override public boolean performClick() {
    if (Boolean.TRUE) return false;
    return super.performClick();
  }

  @Override protected void onDraw(Canvas canvas) {
    if (mFaces == null || mFaces.length == 0) return;

    // Prepare the matrix.
    mSpec.set(getWidth(), getHeight());
    CameraX.prepareMatrix(mMatrix, mMirror, mDisplayOrientation, mSpec);

    canvas.save();
    // Focus indicator is directional. Rotate the matrix and the canvas
    // so it looks correctly in all orientations.
    // postRotate is clockwise
    mMatrix.postRotate(mOrientation);
    // rotate is counter-clockwise (for canvas)
    canvas.rotate(-mOrientation);
    for (Face face : mFaces) {
      mRect.set(face.rect);
      mMatrix.mapRect(mRect);

      // 画脸矩形框
      canvas.drawRect(mRect, mPaint);
    }
    canvas.restore();
  }
}
