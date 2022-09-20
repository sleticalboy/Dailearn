package com.binlee.dl.host.proxy;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.binlee.dl.puppet.IPuppetProvider;

/**
 * Created on 2022-08-28.
 *
 * @author binlee
 */
public final class ProxyProvider extends ContentProvider {

  private IPuppetProvider mPuppet;

  @Override public void attachInfo(Context context, ProviderInfo info) {
    super.attachInfo(context, info);
    if (ProxyProvider.class.getName().equals(info.name)) {
      return;
    }
    // 从 info 中拿到类名，实例化并启动
    if (info.name.startsWith(".")) {
      mPuppet = PuppetFactory.load(context.getClassLoader(), info.packageName + info.name);
    } else {
      mPuppet = PuppetFactory.load(context.getClassLoader(), info.name);
    }
    if (mPuppet != null) mPuppet.attachInfo(context, info);
  }

  @Override public boolean onCreate() {
    return mPuppet != null && mPuppet.onCreate();
  }

  @Nullable @Override
  public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs,
    @Nullable String sortOrder) {
    return mPuppet != null ? mPuppet.query(uri, projection, selection, selectionArgs, sortOrder) : null;
  }

  @Nullable @Override public String getType(@NonNull Uri uri) {
    return mPuppet != null ? mPuppet.getType(uri) : null;
  }

  @Nullable @Override public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
    return mPuppet != null ? mPuppet.insert(uri, values) : null;
  }

  @Override public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
    return mPuppet != null ? mPuppet.delete(uri, selection, selectionArgs) : 0;
  }

  @Override
  public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
    return mPuppet != null ? mPuppet.update(uri, values, selection, selectionArgs) : 0;
  }
}
