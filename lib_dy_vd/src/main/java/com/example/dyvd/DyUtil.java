package com.example.dyvd;

import android.util.Log;
import androidx.annotation.WorkerThread;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created on 2022/10/25
 *
 * @author binlee
 */
public final class DyUtil {

  private static final String TAG = "DyUtil";

  private static final String DY_DOMAIN = "v.douyin.com";
  private static final String TOOL_URL = "https://www.ilovetools.cn/douyin/search-video-info";
  private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 12; M2012K11AG Build/SKQ1.211006.001; wv)"
    + " AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/106.0.5249.126 Mobile Safari/537.36";

  private DyUtil() {
    //no instance
  }

  // 1.20 VLj:/ “别让这个城市留下了你的青春，却留不下你”# 离开 # 城市 # 回乡 # 逃离北上广 # 服务员 # 生活 https://v.douyin.com/MxFGWAS/ 复制此链接，打开Dou音搜索，直接观看视频！
  // 2.56 usR:/ 北漂13年，失败告终，带着一身疲惫光荣返乡！踏踏实实做个农民吧！老房改造第一季~# 旧房改造 # 老房改造# 农村生活# 返乡创业青年 https://v.douyin.com/MxFHfSB/ 复制此链接，打开Dou音搜索，直接观看视频！
  // 2.58 lcn:/ 复制打开抖音，看看【返乡军哥的作品】北漂13年，失败告终，带着一身疲惫光荣返乡！踏踏实... https://v.douyin.com/M9VCxkn/
  // title: 北漂13年，失败告终，带着一身疲惫光荣返乡！踏踏实...
  // author: 返乡军哥的作品
  // url: https://v.douyin.com/M9VCxkn/
  public static String getOriginalShareUrl(String text) {
    // 是否是抖音分享链接
    if (text == null || !text.contains(DY_DOMAIN)) return null;

    // 解析 url
    int start = text.indexOf("http");
    if (start < 0) return null;
    final int end = text.lastIndexOf('/');
    return end < 0 ? text.substring(start) : text.substring(start, end + 1);
  }

  @WorkerThread
  public static VideoItem parseItem(String shareUrl) throws IOException, JSONException {
    return VideoItem.parse(shareUrl, getVideoJson(shareUrl));
  }

  private static String getVideoJson(String shareText) throws IOException, JSONException {
    final HttpURLConnection connection = (HttpURLConnection) new URL(TOOL_URL).openConnection();
    connection.setRequestMethod("POST");
    connection.addRequestProperty("Accept", "*/*");
    connection.addRequestProperty("Host", "www.ilovetools.cn");
    connection.addRequestProperty("Origin", "https://www.ilovetools.cn");
    connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
    connection.addRequestProperty("User-Agent", USER_AGENT);
    connection.setDoOutput(true);
    connection.getOutputStream().write(("shareUrl=" + shareText).getBytes());
    final int code = connection.getResponseCode();
    Log.d(TAG, "getClearJson() http code: " + code + ", msg: [" + connection.getResponseMessage()
      + "], mime: " + connection.getContentType());
    // stream to string
    String result;
    if (code >= 200 && code < 400) {
      result = streamAsString(connection.getInputStream());
    } else {
      result = streamAsString(connection.getErrorStream());
    }
    return new JSONObject(result).getString("data");
  }

  private static String streamAsString(InputStream in) throws IOException {
    if (in == null) return "[stream|is|null]";

    final ByteArrayOutputStream baos = new ByteArrayOutputStream(in.available());
    int len;
    final byte[] buffer = new byte[1024 * 8];
    while ((len = in.read(buffer)) != -1) {
      baos.write(buffer, 0, len);
    }
    return baos.toString();
  }
}
