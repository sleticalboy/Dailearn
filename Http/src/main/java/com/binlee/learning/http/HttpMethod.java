package com.binlee.learning.http;

import androidx.annotation.StringDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class HttpMethod {

  public static final String GET = "GET";
  public static final String POST = "POST";
  public static final String PUT = "PUT";
  public static final String DELETE = "DELETE";
  public static final String PATCH = "PATCH";
  public static final String HEAD = "HEAD";

  public HttpMethod() {
  }

  @StringDef({ GET, POST, PUT, DELETE, PATCH, HEAD })
  @Retention(RetentionPolicy.SOURCE)
  public @interface Method {
  }
}
