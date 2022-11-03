package com.example.dyvd.engine;

import android.util.Log;
import com.example.dyvd.DyUtil;
import com.example.dyvd.VideoItem;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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
    // https://3g.gljlw.com/diy/ttxs_ks_2021.php?url=https://v.kuaishou.com/BgUhrh
    final HttpURLConnection conn = (HttpURLConnection) new URL(TOOL_URL + "?url=" + shareUrl).openConnection();
    conn.setRequestMethod("GET");
    conn.addRequestProperty("Accept", "*/*");
    conn.addRequestProperty("Host", "www.ilovetools.cn");
    conn.addRequestProperty("Origin", "https://www.ilovetools.cn");
    conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
    conn.addRequestProperty("User-Agent", USER_AGENT);
    conn.setDoOutput(true);
    conn.getOutputStream().write(("shareUrl=" + shareUrl).getBytes());
    final int code = conn.getResponseCode();
    Log.d(TAG, "getVideoInfo() http code: " + code + ", msg: [" + conn.getResponseMessage()
      + "], mime: " + conn.getContentType());
    // stream to string
    String result;
    if (code >= 200 && code < 400) {
      result = DyUtil.streamAsString(conn.getInputStream());
    } else {
      result = DyUtil.streamAsString(conn.getErrorStream());
    }
    return new JSONObject(result).getString("data");
  }
}
