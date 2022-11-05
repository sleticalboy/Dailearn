package com.example.dyvd.engine;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.dyvd.VideoItem;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.json.JSONException;

/**
 * Created on 2022/11/3
 *
 * @author binlee
 */
public abstract class Engine {

  protected final String TAG = getClass().getSimpleName();

  public static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 12; M2012K11AG Build/SKQ1.211006.001; wv)"
    + " AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/106.0.5249.126 Mobile Safari/537.36";

  public String shortUrl;

  public Engine(String text) {
    this.shortUrl = parseShareUrl(text);
  }

  protected abstract String parseShareUrl(String text);

  public final Result parseItem() {
    Log.d(TAG, "parseItem() start parse: " + shortUrl);
    try {
      return Result.of(fromJson(shortUrl, getVideoInfo()));
    } catch (IOException | JSONException e) {
      return Result.error(e);
    }
  }

  protected abstract VideoItem fromJson(String shareUrl, String text);

  protected abstract String getVideoInfo() throws IOException, JSONException;

  protected void dumpHeaders(HttpURLConnection conn, String step) {
    if (conn == null) return;
    Log.d(TAG, "dumpHeaders() " + step + " >>>>>>>>>>>>>>>>>>>>>>>>>>");
    for (String key : conn.getHeaderFields().keySet()) {
      Log.d(TAG, "dumpHeaders() " + key + ": " + conn.getHeaderField(key));
    }
  }

  public static final class Result {

    public final VideoItem result;
    public final Throwable error;

    public Result(VideoItem result, Throwable error) {
      this.result = result;
      this.error = error;
    }

    public static Result of(VideoItem item) {
      return new Result(item, null);
    }

    public static Result error(Throwable error) {
      return new Result(null, error);
    }

    public boolean success() {
      return result != null && error == null;
    }

    @NonNull
    @Override public String toString() {
      return "Result{" +
        "result=" + result +
        ", error=" + error +
        '}';
    }
  }
}
