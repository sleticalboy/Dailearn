package com.binlee.learning.luban;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.binlee.learning.base.BaseActivity;
import com.binlee.learning.databinding.ActivityLubanBinding;
import com.binlee.luban.Luban;
import com.bumptech.glide.Glide;
import java.io.File;

/**
 * Created on 2023/2/1
 *
 * @author binlee
 */
public class LubanActivity extends BaseActivity {

  private static final String TAG = "LubanActivity";

  private ActivityLubanBinding mBinding;
  private int mCompressRate = 50;

  private Handler mWorker;

  @Override protected void initData() {
    final HandlerThread thread = new HandlerThread("LubanCompressor");
    thread.start();
    mWorker = new Handler(thread.getLooper());
  }

  @NonNull @Override protected View layout() {
    mBinding = ActivityLubanBinding.inflate(getLayoutInflater(), null, false);
    return mBinding.getRoot();
  }

  @Override protected void initView() {
    mBinding.btnSingleImage.setOnClickListener(view -> choosePicture(false));
    mBinding.btnMultiImage.setOnClickListener(view -> choosePicture(true));
    mBinding.rgCompressRate.setOnCheckedChangeListener((group, checkedId) -> {
      final RadioButton rb = group.findViewById(checkedId);
      Log.d(TAG, "current compress rate: " + rb.getText());
      mCompressRate = Integer.parseInt(rb.getText().subSequence(0, rb.length() - 1).toString());
    });
  }

  private void choosePicture(boolean multi) {
    Intent intent = new Intent();
    intent.setAction(Intent.ACTION_PICK);
    intent.setType("image/*");
    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multi);
    startActivityForResult(intent, 0x10);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 0x10 && resultCode == RESULT_OK && data != null) {
      try {
        // 获取系统返回的照片的 Uri
        Uri selectedImage = data.getData();
        if (selectedImage != null) {
          // 单张图片
          handleImage(selectedImage, 0, 1);
        } else {
          // 多张图片
          final ClipData clipData = data.getClipData();
          if (clipData != null) {
            for (int i = 0, size = clipData.getItemCount(); i < size; i++) {
              handleImage(clipData.getItemAt(i).getUri(), i, size);
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void handleImage(Uri uri, int index, int total) {
    mWorker.post(() -> compressImage(uri, index, total));
  }

  private void compressImage(Uri uri, int index, int total) {
    final String[] projection = { MediaStore.Images.Media.DATA,
      MediaStore.Images.Media.SIZE,
      MediaStore.Images.Media.WIDTH,
      MediaStore.Images.Media.HEIGHT,
    };
    // 从系统表中查询指定 Uri 对应的照片
    final Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
    cursor.moveToFirst();
    // 获取照片路径
    final String input = cursor.getString(cursor.getColumnIndexOrThrow(projection[0]));
    final long size = cursor.getLong(cursor.getColumnIndexOrThrow(projection[1]));
    final int width = cursor.getInt(cursor.getColumnIndexOrThrow(projection[2]));
    final int height = cursor.getInt(cursor.getColumnIndexOrThrow(projection[3]));
    cursor.close();

    Log.d(TAG, "compressImage() input: " + input + ", index: " + index);

    runOnUiThread(() -> {
      mBinding.tvRawInfo.setText("原始图片信息：\n宽高：" + width + "x" + height + "\n大小：" + sizeFormat(size, 0));
      Glide.with(this).load(input).into(mBinding.ivRaw);
    });
    final File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "luban");
    if (!dir.exists()) dir.mkdirs();
    final File output = new File(dir, System.currentTimeMillis() + ".jpg");
    if (Luban.compressImage(input, mCompressRate, output.getAbsolutePath())) {
      runOnUiThread(() -> {
        mBinding.tvLubanInfo.setText("压缩后图片信息：\n宽高：" + width + "x" + height + "\n大小：" + sizeFormat(output.length(), 0));
        Glide.with(this).load(output).into(mBinding.ivLuban);

        // 进度
        if (index + 1 < total) {
          mBinding.tvProgress.setText("进度：" + (index + 1) + "/" + total);
        } else {
          mBinding.tvProgress.setText("进度：" + total + " 个图片压缩完成");
        }
      });
      // 插入系统相册
      if (mBinding.cbInsertGallery.isChecked()) {
        updateGallery(getApplicationContext(), output, width, height);
      }
    }
  }

  public static void updateGallery(Context context, File jpeg, int width, int height) {
    final ContentResolver resolver = context.getContentResolver();
    final ContentValues values = new ContentValues(8);
    values.put(MediaStore.Images.Media.TITLE, jpeg.getName());
    values.put(MediaStore.Images.Media.DISPLAY_NAME, jpeg.getName());
    values.put(MediaStore.Images.Media.DATA, jpeg.getAbsolutePath());
    values.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000);
    values.put(MediaStore.Images.Media.SIZE, jpeg.length());
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
    values.put(MediaStore.Images.Media.WIDTH, width);
    values.put(MediaStore.Images.Media.HEIGHT, height);
    final Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    Log.d(TAG, "insertGallery() " + jpeg + ", result: " + uri);

    final Intent updater = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    updater.setData(Uri.fromFile(jpeg));
    context.sendBroadcast(updater);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mBinding = null;
    mWorker.removeCallbacksAndMessages(null);
  }

  private static final String[] UNITS = {"B", "K", "M", "G"};

  private static String sizeFormat(float size, int level) {
    if (size < 1024f) return size + UNITS[level];
    return sizeFormat(size / 1024f, level + 1);
  }
}
