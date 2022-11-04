package com.example.dyvd;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import com.example.dyvd.engine.Engine;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created on 2022/10/25
 *
 * @author binlee
 */
public final class DyUtil {

  private static final String TAG = "DyUtil";

  private DyUtil() {
    //no instance
  }

  public static String streamAsString(InputStream in, String encoding) throws IOException {
    if (in == null) return "[stream|is|null]";

    if ("gzip".equalsIgnoreCase(encoding)) {
      in = new GZIPInputStream(in);
    }

    final ByteArrayOutputStream baos = new ByteArrayOutputStream(in.available());
    int len;
    final byte[] buffer = new byte[1024 * 8];
    while ((len = in.read(buffer)) != -1) {
      baos.write(buffer, 0, len);
    }
    return baos.toString();
  }

  public interface DownloadCallback {

    default void onStart(VideoItem item) {}

    void onComplete(VideoItem item);

    default void onError(VideoItem item) {}
  }

  public static void download(Context context, final VideoItem item, DownloadCallback callback) {
    // 先查询，如果不存在，则下载
    Log.d(TAG, "download() " + item.title);
    if (isDownloaded(context, item)) {
      item.state = DyState.DOWNLOADED;
      callback.onComplete(item);
      return;
    }

    try {
      final HttpURLConnection conn = (HttpURLConnection) new URL(item.url).openConnection();
      conn.setRequestMethod("GET");
      conn.addRequestProperty("User-Agent", Engine.USER_AGENT);
      final int code = conn.getResponseCode();
      Log.d(TAG, "download() http code: " + code
              + ", msg: [" + conn.getResponseMessage() + "]"
              + ", mime: " + conn.getContentType()
              + ", length: " + conn.getContentLength()
      );

      if (code >= 400) {
        item.state = DyState.BROKEN;
        item.reason = conn.getHeaderField(null);
        callback.onError(item);
        return;
      }

      // 使用系统下载器
      final long id = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE))
        .enqueue(generateRequest(context, item));
      Log.d(TAG, "download() id: " + id);
      // 把 id 更新给 item
      item.id = id;
      item.state = DyState.DOWNLOADING;
      DownloadObserver.get().addCallback(item, callback);
      callback.onStart(item);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static DownloadManager.Request generateRequest(Context context, VideoItem item) {
    return new DownloadManager.Request(Uri.parse(item.url))
      .setTitle(context.getString(R.string.app_name))
      .setDescription(item.title)
      .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
      .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, item.title + ".mp4")
      .setMimeType("video/mp4")
      .addRequestHeader("User-Agent", Engine.USER_AGENT);
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
