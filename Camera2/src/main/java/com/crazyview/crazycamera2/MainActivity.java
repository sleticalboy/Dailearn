package com.crazyview.crazycamera2;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements Camera2Helper.AfterDoListener {

  private Camera2Helper camera2Helper;
  private File file;
  private ImageView imageView;
  private ProgressBar progressBar;
  public static final String PHOTO_PATH = Environment.getExternalStorageDirectory().getPath();
  public static final String PHOTO_NAME = "camera2";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    init();
  }

  @Override
  protected void onResume() {
    super.onResume();
    camera2Helper.startCameraPreView();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    camera2Helper.onDestroyHelper();
  }

  private void init() {
    AutoFitTextureView textureView = (AutoFitTextureView) findViewById(R.id.texture);
    imageView = (ImageView) findViewById(R.id.imv_photo);
    Button button = (Button) findViewById(R.id.btn_take_photo);
    progressBar = (ProgressBar) findViewById(R.id.progressbar_loading);
    file = new File(PHOTO_PATH, PHOTO_NAME + ".jpg");
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        camera2Helper.takePicture();
      }
    });
    camera2Helper = Camera2Helper.getInstance(textureView, file);
    camera2Helper.setAfterDoListener(this);
  }

  @Override
  public void onAfterPreviewBack() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        progressBar.setVisibility(View.GONE);
      }
    });
  }

  @Override
  public void onAfterTakePicture() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        InputStream input = null;
        try {
          input = new FileInputStream(file);
          byte[] byt = new byte[input.available()];
          input.read(byt);
          imageView.setImageBitmap(BitmapUtil.bytes2Bitmap(byt));
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }
}
