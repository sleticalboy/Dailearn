package com.example.freevideo.engine;

import com.example.freevideo.VideoItem;
import java.io.IOException;
import org.json.JSONException;

/**
 * Created on 2022/11/3
 *
 * @author binlee
 */
public class BiliEngine extends Engine {

  public BiliEngine(String shareUrl) {
    super(shareUrl);
  }

  // 【盘点海克斯科技做饭名场面 直接让老厨师陷入沉思】 https://www.bilibili.com/video/BV1bt4y1L7qa/?share_source=copy_web&vd_source=8c9dd3617eda9d9cb1b067cf2639c1fd
  @Override protected String parseShareUrl(String text) {
    return null;
  }

  @Override protected VideoItem fromJson(String shareUrl, String text) {
    return null;
  }

  @Override protected String getVideoInfo() throws IOException, JSONException {
    return null;
  }
}
