package com.binlee.learning.http.builder;

/**
 * Created on 18-9-3.
 *
 * @author leebin
 */
public final class PutBuilder extends RequestBuilder {

  @Override
  protected void realMethod() {
    mRequestBuilder.method("PUT", mBodyBuilder.build());
  }
}
