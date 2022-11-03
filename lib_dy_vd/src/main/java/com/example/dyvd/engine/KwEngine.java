package com.example.dyvd.engine;

import android.net.Uri;
import android.util.Log;
import com.example.dyvd.DyUtil;
import com.example.dyvd.VideoItem;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created on 2022/11/3
 *
 * @author binlee
 */
public class KwEngine extends Engine {

  public static final String DOMAIN = "v.kuaishou.com";
  public static final String TOOL_URL = "https://3g.gljlw.com/diy/ttxs_ks_2021.php";

  // 去水印原理：
  // https://www.likecs.com/show-204863386.html
  public KwEngine(String shareUrl) {
    super(shareUrl);
  }

  // https://v.kuaishou.com/BgUhrh 今日播下善良的种子，明日必能开出幸运之花！ "天生我有戏 "剧星计划 "快手弹幕 该作品在快手被播放过223.9万次，点击链接，打开【快手】直接观看！
  // https://v.kuaishou.com/AWFZfn "健身增肌减脂塑型 "健身健美 "健身 爱生活 爱健美 打开头像看现场精彩直播 该作品在快手被播放过41.8万次，点击链接，打开【快手】直接观看！
  // https://v.kuaishou.com/wQG7ly 小李这一手把大哥差点气哭 该作品在快手被播放过504.7万次，点击链接，打开【快手】直接观看！
  // https://v.kuaishou.com/AkiUFc 如果你有特异功能，你会怎么帮他们"随机舞蹈 "快手舞蹈  该作品在快手被播放过180.4万次，点击链接，打开【快手】直接观看！
  @Override protected String parseShareUrl(String text) {
    if (text == null || !text.contains(DOMAIN)) return null;

    // 解析 url
    int start = text.indexOf("http");
    if (start < 0) return null;
    final int end = text.indexOf(' ');
    return end < 0 ? text.substring(start) : text.substring(start, end);
  }

  @Override protected VideoItem fromJson(String shareUrl, String text) {
    return null;
  }

  @Override protected String getVideoInfo() throws IOException, JSONException {
    HttpURLConnection conn = (HttpURLConnection) new URL(shareUrl).openConnection();
    conn.setRequestMethod("GET");
    // conn.addRequestProperty("Accept", "*/*");
    conn.addRequestProperty("Host", "v.kuaishou.com");
    // conn.addRequestProperty("Origin", "https://www.ilovetools.cn");
    // conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
    conn.addRequestProperty("User-Agent", USER_AGENT);
    // conn.setDoOutput(true);
    // conn.getOutputStream().write(("shareUrl=" + shareUrl).getBytes());
    final int code = conn.getResponseCode();
    Log.d(TAG, "getVideoInfo() http code: " + code + ", msg: [" + conn.getResponseMessage()
      + "], mime: " + conn.getContentType());

    final String location = conn.getHeaderField("Location");
    Log.d(TAG, "getVideoInfo() location: " + location);

    int start = location.indexOf("userId=") + 7;
    int end = location.indexOf('&', start);
    final String userId = location.substring(start, end);
    Log.d(TAG, "getVideoInfo() userId: " + userId);

    start = location.indexOf("redirectPath=") + 13;
    end = location.indexOf('&', start);
    final String temp = location.substring(start, end);
    Log.d(TAG, "getVideoInfo() redirectPath: " + temp);
    final String segment = temp.substring(temp.lastIndexOf("%2F") + 3);
    Log.d(TAG, "getVideoInfo() last segment: " + segment);

    tryNext("https://live.kuaishou.com/u/" + userId + "/" + segment);
    return null;
  }

  private void tryNext(String url) throws IOException {

    Log.d(TAG, "tryNext() called with: url = [" + url + "]");

    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    conn.setRequestMethod("GET");
    // conn.addRequestProperty("Accept", "*/*");
    conn.addRequestProperty("Host", "v.kuaishou.com");
    // conn.addRequestProperty("Origin", "https://www.ilovetools.cn");
    // conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
    conn.addRequestProperty("User-Agent", USER_AGENT);
    // conn.setDoOutput(true);
    // conn.getOutputStream().write(("shareUrl=" + shareUrl).getBytes());
    final int code = conn.getResponseCode();
    Log.d(TAG, "tryNext() http code: " + code + ", msg: [" + conn.getResponseMessage()
            + "], mime: " + conn.getContentType());

    // stream to string
    String result;
    if (code >= 200 && code < 400) {
      result = DyUtil.streamAsString(conn.getInputStream());
    } else {
      result = DyUtil.streamAsString(conn.getErrorStream());
    }
    Log.d(TAG, "tryNext() " + result);
    // Split by line, then ensure each line can fit into Log's maximum length.
    for (int i = 0, length = result.length(); i < length; i++) {
      int newline = result.indexOf('\n', i);
      newline = newline != -1 ? newline : length;
      do {
        int limit = Math.min(newline, i + MAX_LOG_LENGTH);
        Log.d(TAG, "tryNext() " + result.substring(i, limit));
        i = limit;
      } while (i < newline);
    }
  }

  private static final int MAX_LOG_LENGTH = 4000;
}
