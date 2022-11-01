package com.example.dyvd.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.dyvd.VideoItem;
import com.example.dyvd.VideoParser;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
public class FakeVideoDb implements Closeable {

  // key: shareUrl, value: json
  private SharedPreferences mSp;

  private Map<String, VideoItem> mCache;

  public FakeVideoDb(Context context) {
    mSp = context.getSharedPreferences("all_dy_videos", Context.MODE_PRIVATE);
    mCache = new LinkedHashMap<>();
  }

  @Override public void close() throws IOException {
    mSp = null;
    mCache.clear();
    mCache = null;
  }

  public List<VideoItem> getAllVideos() {
    final List<VideoItem> items = new ArrayList<>(mSp.getAll().size());
    VideoItem item;
    for (String key : mSp.getAll().keySet()) {
      final String text = mSp.getString(key, null);
      Log.d("FakeVideoDb", "getAllVideos() " + text);
      if (text != null && (item = VideoParser.fromJson(key, text)) != null) {
        items.add(item);
        mCache.put(item.shareUrl, item);
      }
    }
    return items;
  }

  public boolean hasVideo(String key) {
    return mCache.containsKey(key);
  }

  public void insert(VideoItem item) {
    mSp.edit().putString(item.shareUrl, item.toString()).apply();
    mCache.put(item.shareUrl, item);
  }
}
