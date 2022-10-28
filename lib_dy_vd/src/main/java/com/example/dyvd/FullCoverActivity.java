package com.example.dyvd;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.dyvd.databinding.ActivityFullCoverBinding;

/**
 * Created on 2022/10/28
 *
 * @author binlee
 */
public class FullCoverActivity extends AppCompatActivity {

  private static final String KEY_FULL_URL = "key.full.url";

  private ActivityFullCoverBinding mBinding;

  public static void start(Context context, String coverUrl) {
    context.startActivity(new Intent(context, FullCoverActivity.class)
      .putExtra(KEY_FULL_URL, coverUrl)
    );
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mBinding = ActivityFullCoverBinding.inflate(getLayoutInflater());
    setContentView(mBinding.getRoot());

    final String coverUrl = getIntent().getStringExtra(KEY_FULL_URL);
    if (coverUrl == null) {
      finish();
      return;
    }
    Glide.with(this).load(coverUrl).into(mBinding.ivFullCover);
  }
}
