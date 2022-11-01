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

  private VideosDbHelper mHelper;
  private Map<String, VideoItem> mCache;

  public VideosDb(Context context) {
    mHelper = new VideosDbHelper(context);
    mCache = new HashMap<>();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      context.deleteSharedPreferences("all_dy_videos");
    }
  }

  public void insert(VideoItem item) {
    DbUtil.insertOrReplace(mHelper.getWritableDatabase(), item);
  }

  public List<VideoItem> getAll() {
    return DbUtil.queryAll(mHelper.getReadableDatabase(), VideoItem.class);
  }

  public boolean hasVideo(String key) {
    return mCache.containsKey(key);
  }
}
