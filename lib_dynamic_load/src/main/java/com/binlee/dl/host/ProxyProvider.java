package com.binlee.dl.host;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.binlee.dl.puppet.IContentProvider;

/**
 * Created on 2022-08-28.
 *
 * @author binlee
 */
public class ProxyProvider extends ContentProvider {

  private IContentProvider mProvider;

  @Override public void attachInfo(Context context, ProviderInfo info) {
    super.attachInfo(context, info);
    // 从 info 中拿到类名，实例化并启动
    if (info.name.startsWith(".")) {
      mProvider = ComponentInitializer.initialize(context.getClassLoader(), info.packageName + info.name);
    } else {
      mProvider = ComponentInitializer.initialize(context.getClassLoader(), info.name);
    }
    if (mProvider != null) mProvider.attachInfo(context, info);
  }

  @Override public boolean onCreate() {
    return mProvider != null && mProvider.onCreate();
  }

  @Nullable @Override
  public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs,
    @Nullable String sortOrder) {
    return mProvider != null ? mProvider.query(uri, projection, selection, selectionArgs, sortOrder) : null;
  }

  @Nullable @Override public String getType(@NonNull Uri uri) {
    return mProvider != null ? mProvider.getType(uri) : null;
  }

  @Nullable @Override public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
    return mProvider != null ? mProvider.insert(uri, values) : null;
  }

  @Override public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
    return mProvider != null ? mProvider.delete(uri, selection, selectionArgs) : 0;
  }

  @Override
  public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
    return mProvider != null ? mProvider.update(uri, values, selection, selectionArgs) : 0;
  }
}
