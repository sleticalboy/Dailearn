package com.example.dyvd;

import android.database.Cursor;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
public final class VideoParser {

  private static final String TAG = "VideoParser";

  private VideoParser() {
    //no instance
  }

  public static VideoItem fromCursor(Cursor cursor) {
    return null;
  }

  public static VideoItem fromJson(String shareUrl, String text) {
    try {
      final JSONObject json = new JSONObject(text);
      // desc: 不怕反派坏，就怕反派帅#萌宠 #赵文卓 #画画 #变装
      // title: 不怕反派坏，就怕反派帅
      // tags: #萌宠 #赵文卓 #画画 #变装
      final VideoItem item = new VideoItem();
      item.shareUrl = shareUrl;
      final String desc = json.getString("desc");
      int index;
      if ((index = desc.indexOf('#')) >= 0) {
        item.title = desc.substring(0, index);
        item.tags = desc.substring(index);
      }
      item.coverUrl = json.getString("imgUrl");
      item.url = json.getString("videoUrl");
      item.bgmUrl = json.getString("musicUrl");
      return item;
    } catch (JSONException e) {
      Log.w(TAG, "fromJson() invalid json: " + text);
      return null;
    }
  }
}
