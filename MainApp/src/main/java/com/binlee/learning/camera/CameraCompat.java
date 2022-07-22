package com.sleticalboy.learning.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 20-11-13.
 *
 * @author binli
 */
public final class CameraCompat {

  /**
   * The facing of the camera is opposite to that of the screen.
   */
  public static final int CAMERA_FACING_BACK = 0;

  /**
   * The facing of the camera is the same as that of the screen.
   */
  public static final int CAMERA_FACING_FRONT = 1;

  private final Context mContext;

  public CameraCompat(Context context) {
    mContext = context;
  }

  public void openCamera() throws CameraException {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      try {
        openLollipop(mContext);
      } catch (CameraAccessException e) {
        throw new CameraException("open camera error over Lollipop", e);
      }
    } else {
      try {
        openCompat(mContext);
      } catch (IOException e) {
        throw new CameraException("open camera error", e);
      }
    }
  }

  private void openCompat(Context context) throws IOException {
    Camera camera = Camera.open(CAMERA_FACING_BACK);
    camera.setPreviewCallback(new Camera.PreviewCallback() {
      @Override
      public void onPreviewFrame(byte[] data, Camera camera) {
        //
      }
    });
    SurfaceView sv = new SurfaceView(context);
    camera.setPreviewDisplay(sv.getHolder());
    camera.startPreview();
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private void openLollipop(Context context) throws CameraAccessException {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
      != PackageManager.PERMISSION_GRANTED) {
      return;
    }
    CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    Handler handler = new Handler();
    manager.registerAvailabilityCallback(new CameraManager.AvailabilityCallback() {
      @Override
      public void onCameraAvailable(@NonNull String cameraId) {
        //
      }
    }, handler);
    // get available camera id
    String[] list = manager.getCameraIdList();
    String temp = null;
    for (final String id : list) {
      temp = id;
      break;
    }
    final String cameraId = temp;
    // open camera
    manager.openCamera(cameraId, new CameraDevice.StateCallback() {
      @Override
      public void onOpened(@NonNull CameraDevice camera) {
        Size[] temp = null;
        // setup params
        try {
          CameraCharacteristics cc = manager.getCameraCharacteristics(cameraId);
          StreamConfigurationMap map =
            cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
          temp = map.getOutputSizes(ImageFormat.JPEG);
        } catch (CameraAccessException e) {
          e.printStackTrace();
        }
        Size size = temp == null ? new Size(1920, 1080) : temp[0];
        // init image reader
        ImageReader ir =
          ImageReader.newInstance(size.getWidth(), size.getHeight(), ImageFormat.JPEG, 2);
        ir.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
          @Override
          public void onImageAvailable(ImageReader reader) {
            // when image is available, we save it to file
            Image image = reader.acquireNextImage();
            int format = image.getFormat();
            // which format ?
            // ImageFormat.JPEG
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            buffer.rewind();
            byte[] data = new byte[buffer.limit()];
            buffer.get(data);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            // save bitmap to file
            try {
              FileOutputStream fos = new FileOutputStream("file name");
              bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
              fos.flush();
              fos.close();
            } catch (IOException e) {
              e.printStackTrace();
            }

            // ImageFormat.YUV_420_888 -> do stuff
            // ImageFormat.DEPTH16 -> do stuff
            image.close();
          }
        }, handler);
        // create a capture session
        // 1, prepare surfaces
        List<Surface> surfaces = new ArrayList<>();
        SurfaceView sv = new SurfaceView(context);
        // sv.getHolder().addCallback(new SurfaceHolder.Callback() {
        //     @Override
        //     public void surfaceCreated(SurfaceHolder holder) {
        //         surfaces.add(holder.getSurface());
        //     }
        //
        //     @Override
        //     public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //
        //     }
        //
        //     @Override
        //     public void surfaceDestroyed(SurfaceHolder holder) {
        //
        //     }
        // });
        surfaces.add(sv.getHolder().getSurface());
        surfaces.add(ir.getSurface());
        // 2, build capture request
        try {
          CaptureRequest.Builder builder =
            camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
          builder.addTarget(/*holder.getSurface()*/surfaces.get(0));
          CaptureRequest request = builder.build();
          CameraCaptureSession[] sessionHolder = new CameraCaptureSession[1];
          camera.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
              // save this session reference
              sessionHolder[0] = session;
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }
          }, handler);
          sessionHolder[0].setRepeatingRequest(request, new CameraCaptureSession.CaptureCallback() {
          }, handler);
        } catch (CameraAccessException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onDisconnected(@NonNull CameraDevice camera) {

      }

      @Override
      public void onError(@NonNull CameraDevice camera, int error) {

      }
    }, handler);
  }
}
