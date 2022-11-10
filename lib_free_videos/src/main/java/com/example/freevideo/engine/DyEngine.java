package com.example.freevideo.engine;

import android.util.Log;
import com.example.freevideo.DyUtil;
import com.example.freevideo.VideoItem;
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
public final class DyEngine extends Engine {

  public static final String DOMAIN = "v.douyin.com";
  public static final String TOOL_URL = "https://www.ilovetools.cn/douyin/search-video-info";

  // 去水印原理：
  // https://blog.csdn.net/a554829401/article/details/123639230
  // https://zhuanlan.zhihu.com/p/144733172
  // https://blog.csdn.net/qq_35098526/article/details/108142040

  // 1、请求短链，从响应头拿到重定向地址解析出 items_id;
  // 2、访问 https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids={items_id} 获取 json 响应;
  // 3、解析 json 拿到标题、标签、封面、视频地址：
  //  item_list [0]
  //    desc: 标题和标签
  //    video:
  //      play_addr -> url_list[0] 播放地址 playwm -> play
  //      origin_cover -> url_list[0] 原始封面

  public DyEngine(String text) {
    super(text);
  }

  // 1.20 VLj:/ “别让这个城市留下了你的青春，却留不下你”# 离开 # 城市 # 回乡 # 逃离北上广 # 服务员 # 生活 https://v.douyin.com/MxFGWAS/ 复制此链接，打开Dou音搜索，直接观看视频！
  // 2.56 usR:/ 北漂13年，失败告终，带着一身疲惫光荣返乡！踏踏实实做个农民吧！老房改造第一季~# 旧房改造 # 老房改造# 农村生活# 返乡创业青年 https://v.douyin.com/MxFHfSB/ 复制此链接，打开Dou音搜索，直接观看视频！
  // 2.58 lcn:/ 复制打开抖音，看看【返乡军哥的作品】北漂13年，失败告终，带着一身疲惫光荣返乡！踏踏实... https://v.douyin.com/M9VCxkn/
  // title: 北漂13年，失败告终，带着一身疲惫光荣返乡！踏踏实...
  // author: 返乡军哥的作品
  // url: https://v.douyin.com/M9VCxkn/
  @Override protected String parseShareUrl(String text) {
    // 校验域名
    if (text == null || !text.contains(DOMAIN)) return null;

    // 解析 url
    int start = text.indexOf("http");
    if (start < 0) return null;
    final int end = text.lastIndexOf('/');
    return end < 0 ? text.substring(start) : text.substring(start, end + 1);
  }

  protected String getVideoInfo_() throws IOException, JSONException {
    HttpURLConnection conn = (HttpURLConnection) new URL(shortUrl).openConnection();
    conn.setRequestMethod("GET");
    conn.addRequestProperty("user-agent", USER_AGENT);
    int code = conn.getResponseCode();
    Log.d(TAG, "getVideoInfo() http code: " + code + ", msg: [" + conn.getResponseMessage()
            + "], mime: " + conn.getContentType());

    final String temp = conn.toString();
    Log.d(TAG, "getVideoInfo() " + conn);

    dumpHeaders(conn, "get items id");
    final String setCookie = conn.getHeaderField("Set-Cookie");

    int end = temp.indexOf('?');
    if (temp.charAt(end - 1) == '/') end -= 1;
    int start = temp.substring(0, end).lastIndexOf('/');
    final String itemsId = temp.substring(start + 1, end -1);
    Log.d(TAG, "getVideoInfo() itemsId: " + itemsId);

    conn = (HttpURLConnection) new URL("https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids=" + itemsId).openConnection();

    conn.setRequestMethod("GET");
    conn.addRequestProperty("user-agent", USER_AGENT);
    conn.addRequestProperty("set-cookie", setCookie);
    code = conn.getResponseCode();
    Log.d(TAG, "getVideoInfo() http code: " + code + ", msg: [" + conn.getResponseMessage()
            + "], mime: " + conn.getContentType());

    dumpHeaders(conn, "get video detail");

    // stream to string
    String result;
    if (code >= 200 && code < 400) {
      result = DyUtil.streamAsString(conn.getInputStream(), conn.getContentEncoding());
    } else {
      result = DyUtil.streamAsString(conn.getErrorStream(), conn.getContentEncoding());
    }
    Log.d(TAG, "getVideoInfo() result: " + result);
    return null;
  }

  protected String getVideoInfo() throws IOException, JSONException {
    final HttpURLConnection conn = (HttpURLConnection) new URL(TOOL_URL).openConnection();
    conn.setRequestMethod("POST");
    conn.addRequestProperty("Accept", "*/*");
    conn.addRequestProperty("Host", "www.ilovetools.cn");
    conn.addRequestProperty("Origin", "https://www.ilovetools.cn");
    conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
    conn.addRequestProperty("User-Agent", USER_AGENT);
    conn.setDoOutput(true);
    conn.getOutputStream().write(("shareUrl=" + shortUrl).getBytes());
    final int code = conn.getResponseCode();
    Log.d(TAG, "getVideoInfo() http code: " + code + ", msg: [" + conn.getResponseMessage()
      + "], mime: " + conn.getContentType());
    // stream to string
    String result;
    if (code >= 200 && code < 400) {
      result = DyUtil.streamAsString(conn.getInputStream(), conn.getContentEncoding());
    } else {
      result = DyUtil.streamAsString(conn.getErrorStream(), conn.getContentEncoding());
    }
    return new JSONObject(result).getString("data");
  }

  @Override protected VideoItem fromJson(String shareUrl, String text) {
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
      } else {
        item.title = desc;
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
