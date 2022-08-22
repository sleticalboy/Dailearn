package com.binlee.learning.http.cookie;

import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import java.util.Collections;
import java.util.List;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Created on 18-9-11.
 *
 * @author leebin
 */
public final class CookieJarImpl implements CookieJar {

  public static final CookieJar ALL_COOKIE = new CookieJarImpl();
  // 让 WebView 利用 okhttp 的缓存功能
  private final CookieManager mCookieManager;

  public CookieJarImpl() {
    mCookieManager = CookieManager.getInstance();
  }

  @Override
  public void saveFromResponse(final HttpUrl url, final List<Cookie> cookies) {
    for (final Cookie cookie : cookies) {
      if (cookie != null) {
        mCookieManager.setCookie(url.url().toString(), cookies.get(0).toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          mCookieManager.flush();
        } else {
          // 需要先创建再使用
          CookieSyncManager.createInstance(null).sync();
        }
      }
    }
  }

  @Override
  public List<Cookie> loadForRequest(final HttpUrl url) {
    return Collections.emptyList();
  }
}
