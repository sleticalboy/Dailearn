package com.binlee.http.builder;

import androidx.annotation.NonNull;
import com.binlee.http.OkUtils;
import java.io.File;
import java.util.List;
import java.util.Map;
import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created on 18-9-3.
 *
 * @author leebin
 */
public abstract class RequestBuilder {

  static final String TYPE_TEXT = "text";
  static final String TYPE_IMAGE = "image";
  static final String TYPE_AUDIO = "audio";
  static final String TYPE_VIDEO = "video";
  static final String TYPE_APPLICATION = "application";

  // url 要有配置文件配置，不应该直接写死
  public static final String BASE_URL = "https://ccb.dehuinet.cn";
  // public static final String BASE_URL = "http://test.dehuinet.com:8030";
  // public static final String BASE_URL = "http://dev4.dehuinet.com:8014";

  static final RequestBody EMPTY_BODY = RequestBody.create(null, new byte[0]);
  final MultipartBody.Builder mBodyBuilder = new MultipartBody.Builder();
  final Request.Builder mRequestBuilder = new Request.Builder();
  private final Headers.Builder mHeaderBuilder = new Headers.Builder();
  boolean mIsEmptyRequestBody = false;
  private long mBreadPoint;
  String mRequestUrl;
  private String mServerHost;
  private boolean isAjax = false;
  private boolean isSilent = false;

  RequestBuilder() {
    mServerHost = BASE_URL;
  }

  /**
   * 本次请求的 url
   *
   * @param url request url
   * @return {@link RequestBuilder}
   */
  public RequestBuilder url(@NonNull String url) {
    inspectUrl(url);
    mRequestBuilder.url(mRequestUrl);
    return this;
  }

  void inspectUrl(@NonNull String url) {
    if (url.startsWith(mServerHost) || (url.startsWith("http") && !url.startsWith(mServerHost))) {
      mRequestUrl = url;
    } else {
      mRequestUrl = mServerHost + url;
    }
  }

  /**
   * 给本次请求打一个 tag, 用于之后可以撤销本次请求
   *
   * @param tag {@link Object}
   * @return {@link RequestBuilder}
   */
  public RequestBuilder tag(Object tag) {
    mRequestBuilder.tag(tag);
    return this;
  }

  /**
   * 添加缓存控制
   *
   * @param cacheControl {@link CacheControl}
   * @return {@link RequestBuilder}
   */
  public RequestBuilder cacheControl(CacheControl cacheControl) {
    mRequestBuilder.cacheControl(cacheControl);
    return this;
  }

  public Request build() {
    mRequestBuilder.headers(mHeaderBuilder.build());
    realMethod();
    return mRequestBuilder.build();
  }

  protected abstract void realMethod();

  /**
   * 移除 header
   *
   * @param name name of the header
   * @return {@link RequestBuilder}
   */
  public RequestBuilder removeHeader(String name) {
    mHeaderBuilder.removeAll(name);
    return this;
  }

  /**
   * 添加 header
   *
   * @param name key
   * @param value value
   * @return {@link RequestBuilder}
   */
  public RequestBuilder header(@NonNull String name, String value) {
    mHeaderBuilder.set(name, value);
    return this;
  }

  /**
   * method
   * 添加 header
   *
   * @param headers request headers
   * @return {@link RequestBuilder}
   */
  public RequestBuilder headers(@NonNull Map<String, String> headers) {
    if (!OkUtils.empty(headers)) {
      for (final String key : headers.keySet()) {
        mHeaderBuilder.set(key, headers.get(key));
      }
    }
    return this;
  }

  /**
   * 添加请求参数
   *
   * @param name params key
   * @param value params value
   * @return {@link RequestBuilder}
   */
  public RequestBuilder param(@NonNull String name, String value) {
    mBodyBuilder.addFormDataPart(name, value);
    return this;
  }

  /**
   * 添加请求参数
   *
   * @param params 参数 map
   * @return {@link RequestBuilder}
   */
  public RequestBuilder params(Map<String, String> params) {
    if (!OkUtils.empty(params)) {
      for (final String key : params.keySet()) {
        mBodyBuilder.addFormDataPart(key, params.get(key));
      }
    }
    return this;
  }

  /**
   * 添加请求参数
   *
   * @param params 参数 map
   * @param files 文件
   * @return {@link RequestBuilder}
   */
  public RequestBuilder params(Map<String, String> params, List<String> files) {
    mIsEmptyRequestBody = OkUtils.empty(params) && OkUtils.empty(files);
    if (!OkUtils.empty(params)) {
      RequestBuilder.this.params(params);
    }
    if (!OkUtils.empty(files)) {
      for (final String path : files) {
        final File file = new File(path);
        if (!file.exists()) {
          continue;
        }
        mBodyBuilder.addFormDataPart("files", file.getName(), RequestBody.create(MediaType.parse("*/*"), file));
      }
    }
    return this;
  }

  /**
   * 用于断点续传和多线程下载
   *
   * @param startPoint 开始位置
   * @param endPoint 结束位置
   * @return {@link RequestBuilder}
   */
  public RequestBuilder breakPoint(Long startPoint, Long endPoint) {
    mBreadPoint = startPoint;
    mHeaderBuilder.add("RANGE", "bytes=" + startPoint + "-" + (endPoint == null ? "" : endPoint));
    return this;
  }

  public RequestBuilder ajax(boolean isAjax) {
    this.isAjax = isAjax;
    return this;
  }

  public boolean isAjax() {
    return isAjax;
  }

  public RequestBuilder silent(boolean isSilent) {
    this.isSilent = isSilent;
    return this;
  }

  public boolean isSilent() {
    return isSilent;
  }

  public long getBreakPoint() {
    return mBreadPoint;
  }
}
