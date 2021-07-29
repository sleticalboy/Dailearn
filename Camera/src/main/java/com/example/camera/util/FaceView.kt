package com.example.camera.util

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import com.example.camera.R
import kotlin.math.roundToInt

/**
 * Created by renlei
 * DATE: 15-11-11
 * Time: 上午11:34
 * Email: renlei0109@yeah.net
 */
class FaceView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {

    private var mFaces: Array<Camera.Face>? = null
    private val mMatrix = Matrix()
    private var mirror = false
    private lateinit var mLinePaint: Paint
    private val rectF = RectF()
    private var mFaceIndicator: Drawable? = null

    fun setFaces(faces: Array<Camera.Face>?) {
        mFaces = faces
        Log.d(TAG, "invalidate")
        // ((View)getParent()).invalidate();
        invalidate()
        /*postInvalidate();
        invalidate();
        forceLayout();
        requestLayout();*/
    }

    fun clearFaces() {
        mFaces = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
//        Log.d("Faceview", "onDraw");
        if (mFaces == null || mFaces!!.isEmpty()) {
            return
        }
        if (mFaces != null) {
            Log.d(TAG, "onDraw" + mFaces!!.size)
            val id: Int = CameraInstance.get().cameraId
            mirror = id == Camera.CameraInfo.CAMERA_FACING_FRONT
            canvas.save()
            prepareMatrix()
            mMatrix.postRotate(0f) //Matrix.postRotate默认是顺时针
            canvas.rotate(-0f) //Canvas.rotate()默认是逆时针
            for (i in mFaces!!.indices) {
                rectF.set(mFaces!![i].rect)
                mMatrix.mapRect(rectF)
                mFaceIndicator!!.setBounds(
                    rectF.left.roundToInt(), rectF.top.roundToInt(),
                    rectF.right.roundToInt(), rectF.bottom.roundToInt()
                )
                mFaceIndicator!!.draw(canvas)
            }
            canvas.restore()
        }
        super.onDraw(canvas)
    }

    /**
     *
     * Here is the matrix to convert driver coordinates to View coordinates
     * in pixels.
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
    </pre> *
     */
    private fun prepareMatrix() {
        mMatrix.setScale((if (mirror) -1 else 1).toFloat(), 1F)
        mMatrix.postRotate(9F)
        mMatrix.postScale(width / 2000F, height / 2000F)
        mMatrix.postTranslate(width / 2F, height / 2F)
    }

    private fun initPaint() {
        mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        // int color = Color.rgb(0, 150, 255);
        val color = Color.rgb(98, 212, 68)
        // mLinePaint.setColor(Color.RED);
        mLinePaint.color = color
        mLinePaint.style = Paint.Style.STROKE
        mLinePaint.strokeWidth = 5f
        mLinePaint.alpha = 180
    }

    init {
        initPaint()
        mFaceIndicator = context.resources.getDrawable(R.drawable.ic_face_find_2)
    }

    companion object {
        private const val TAG = "FaceView"
    }
}