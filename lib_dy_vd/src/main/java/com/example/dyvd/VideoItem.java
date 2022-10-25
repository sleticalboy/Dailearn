package com.example.dyvd;

import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created on 2022/10/25
 *
 * @author binlee
 */
public final class VideoItem {

  /** 标题 */
  public final String title;
  /** 封面图片链接 */
  public final String coverUrl;
  /** 视频链接 */
  public final String url;
  /** 背景音乐链接 */
  public final String bgmUrl;
  private final String textJson;
  /** 分享链接，作为 key */
  private final String shareUrl;
  /** 下载状态状态 */
  public DyState state = DyState.NONE;

  public static VideoItem parse(String shareUrl, String text) {
    try {
      final JSONObject json = new JSONObject(text);
      return new VideoItem(json.getString("desc"),
        json.getString("imgUrl"),
        json.getString("videoUrl"),
        json.getString("musicUrl"),
        text, shareUrl);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }

  private VideoItem(String title, String coverUrl, String url, String bgmUrl, String textJson, String shareUrl) {
    this.title = title;
    this.coverUrl = coverUrl;
    this.url = url;
    this.bgmUrl = bgmUrl;
    this.textJson = textJson;
    this.shareUrl = shareUrl;
  }

  public String getTextJson() {
    return textJson;
  }

  public String getShareUrl() {
    return shareUrl;
  }

  @NonNull @Override public String toString() {
    return "VideoItem{" +
      "title='" + title + '\'' +
      ", url='" + url + '\'' +
      ", shareUrl='" + shareUrl + '\'' +
      ", state='" + state + '\'' +
      '}';
  }
}
