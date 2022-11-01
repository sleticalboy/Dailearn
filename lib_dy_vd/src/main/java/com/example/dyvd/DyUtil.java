package com.example.dyvd;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.UiThread;
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

  private static final Handler sMain = new Handler(Looper.getMainLooper());
  private static final Handler sWorker = createWorker();

  private DyUtil() {
    //no instance
  }

  private static Handler createWorker() {
    HandlerThread worker = new HandlerThread("DyWorker");
    worker.start();
    return new Handler(worker.getLooper());
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

  public interface ParseCallback {
    @UiThread void onResult(VideoItem item);

    @UiThread default void onError(Throwable thr) {
    }
  }

  public static void parseItem(String shareUrl, ParseCallback callback) {
    sWorker.post(() -> {
      try {
        final VideoItem videoItem = VideoParser.fromJson(shareUrl, getVideoJson(shareUrl));
        sMain.post(() -> callback.onResult(videoItem));
      } catch (IOException | JSONException e) {
        e.printStackTrace();
        sMain.post(() -> callback.onError(e));
      }
    });
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

  public interface DownloadCallback {

    void onComplete(VideoItem item);

    default void onError(VideoItem item) {}
  }

  public static void download(Context context, final VideoItem item, DownloadCallback callback) {
    // 先查询，如果不存在，则下载
    Log.d(TAG, "download() " + item.title);
    sWorker.post(() -> {
      if (isDownloaded(context, item)) {
        item.state = DyState.DOWNLOADED;
        sMain.post(() -> callback.onComplete(item));
        return;
      }

      try {
        final HttpURLConnection connection = (HttpURLConnection) new URL(item.url).openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("User-Agent", USER_AGENT);
        final int code = connection.getResponseCode();
        Log.d(TAG, "download() http code: " + code
                + ", msg: [" + connection.getResponseMessage() + "]"
                + ", mime: " + connection.getContentType()
                + ", length: " + connection.getContentLength()
        );

        if (code >= 400) {
          item.state = DyState.BROKEN;
          item.reason = "http code: " + code + ", msg: " + connection.getResponseMessage();
          sMain.post(() -> callback.onError(item));
          return;
        }

        final String name = item.title + ".mp4";

        // 使用系统下载库？
        final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(item.url))
                .setTitle(context.getString(R.string.app_name))
                .setDescription(item.title)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name)
                .setMimeType("video/mp4")
                .addRequestHeader("User-Agent", USER_AGENT);
        final DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final long id = mgr.enqueue(request);
        Log.d(TAG, "download() id: " + id);
        // 把 id 更新给 item
        item.id = id;
        item.state = DyState.DOWNLOADING;
        DownloadObserver.get().addCallback(item, callback);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    DownloadObserver.get().start(context);
  }

  // _id: 45
  // mediaprovider_uri: content://media/external/video/media/70
  // description: 东土大唐来的喵星人
  // uri: https://v5-g.douyinvod.com/...
  // hint: file:///storage/emulated/0/Download/东土大唐来的喵星人.mp4
  // media_type: video/mp4
  // local_uri: file:///storage/emulated/0/Download/%E4%B8%9C%E5%9C%9F%E5%A4%A7%E5%94%90%E6%9D%A5%E7%9A%84%E5%96%B5%E6%98%9F%E4%BA%BA.mp4

  private static final Uri DOWNLOAD_URI = Uri.parse("content://downloads/my_downloads");
  private static final Uri ALL_DOWNLOAD_URI = Uri.parse("content://downloads/all_downloads");
  private static final String[] COLUMNS = {
    DownloadManager.COLUMN_ID,
    DownloadManager.COLUMN_MEDIAPROVIDER_URI,
    DownloadManager.COLUMN_DESCRIPTION,
    DownloadManager.COLUMN_URI,
    /*DownloadManager.COLUMN_FILE_NAME_HINT*/"hint",
    DownloadManager.COLUMN_MEDIA_TYPE,
    DownloadManager.COLUMN_LOCAL_URI,
  };
  private static final String SELECTION = DownloadManager.COLUMN_DESCRIPTION + " = ? OR "
    + DownloadManager.COLUMN_URI + " = ?";

  private static boolean isDownloaded(Context context, VideoItem item) {
    final ContentResolver resolver = context.getContentResolver();
    final String[] args = { "" + item.url, "" + item.title };
    try (Cursor cursor = resolver.query(DOWNLOAD_URI, COLUMNS, SELECTION, args, null)) {
      final int cursorCount = cursor.getCount();
      dumpCursor(cursor);
      return cursorCount > 0;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  private static void dumpCursor(Cursor cursor) {
    Log.d(TAG, "dumpCursor() >>>>>>>>> " + cursor);
    int raw = 0;
    while (cursor.moveToNext()) {
      raw++;
      for (String column : COLUMNS) {
        String value;
        try {
          value = cursor.getString(cursor.getColumnIndexOrThrow(column));
        } catch (Throwable tr) {
          value = tr.getMessage();
        }
        Log.d(TAG, "dumpCursor() row: " + raw + " -> " + column + ": " + value);
      }
    }
    Log.d(TAG, "dumpCursor() <<<<<<<<< " + cursor);
  }
}
