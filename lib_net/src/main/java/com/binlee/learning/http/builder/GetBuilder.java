package com.binlee.learning.http.builder;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import com.binlee.learning.http.OkUtils;
import java.util.Map;

/**
 * Created on 18-9-3.
 *
 * @author leebin
 */
public final class GetBuilder extends RequestBuilder {

  private final Map<String, String> map = new ArrayMap<>();

  @Override
  protected void realMethod() {
    mRequestBuilder.url(mRequestUrl + convertParams());
    mRequestBuilder.method("GET", null);
  }

  private String convertParams() {
    final StringBuilder builder = new StringBuilder();
    if (map.size() != 0) {
      builder.append("?");
      int i = 0;
      for (final String key : map.keySet()) {
        builder.append(key).append("=").append(map.get(key));
        if (i == map.size() - 1) {
          return builder.toString();
        }
        builder.append("&");
        i++;
      }
    }
    return builder.toString();
  }

  @Override
  public RequestBuilder url(@NonNull final String url) {
    super.inspectUrl(url);
    return this;
  }

  @Override
  public RequestBuilder param(@NonNull final String name, final String value) {
    map.put(name, value);
    return this;
  }

  @Override
  public RequestBuilder params(final Map<String, String> params) {
    if (!OkUtils.empty(params)) {
      for (final String key : params.keySet()) {
        this.map.put(key, params.get(key));
      }
    }
    return this;
  }
}
