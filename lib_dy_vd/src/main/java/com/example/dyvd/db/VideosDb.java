package com.example.dyvd.db;

import android.content.Context;
import android.os.Build;
import com.example.dyvd.VideoItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
public final class VideosDb {

  private final VideosDbHelper mHelper;
  private final Map<String, VideoItem> mCache;

  public VideosDb(Context context) {
    mHelper = new VideosDbHelper(context);
    mCache = new HashMap<>();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      context.deleteSharedPreferences("all_dy_videos");
    }
  }

  public void insert(VideoItem item) {
    DbUtil.insertOrReplace(mHelper.getWritableDatabase(), item);
    mCache.put(item.shareUrl, item);
  }

  public void remove(VideoItem item) {
    DbUtil.remove(mHelper.getWritableDatabase(), item);
    mCache.remove(item.shareUrl);
  }

  public List<VideoItem> getAll() {
    mCache.clear();
    final List<VideoItem> items = DbUtil.queryAll(mHelper.getReadableDatabase(), VideoItem.class);
    for (VideoItem item : items) {
      mCache.put(item.shareUrl, item);
    }
    return items;
  }

  public boolean hasVideo(String key) {
    return mCache.containsKey(key);
  }

  public void close() {
    mHelper.close();
  }
}
