package com.binlee.http;

import com.google.gson.annotations.SerializedName;

/**
 * Created on 21-3-19.
 *
 * @author bnli@grandstream.cn
 */
public final class WebPage {

  @SerializedName(value = "content")
  private final String mContent;

  public WebPage() {
    this(null);
  }

  public WebPage(String content) {
    mContent = content;
  }

  public String getContent() {
    return mContent;
  }

  @Override
  public String toString() {
    return getContent();
  }
}
