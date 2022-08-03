package com.binlee.learning.http.builder;

/**
 * Created on 18-9-3.
 *
 * @author leebin
 */
public final class DeleteBuilder extends RequestBuilder {

  @Override
  protected void realMethod() {
    mRequestBuilder.method("DELETE", mIsEmptyRequestBody ? EMPTY_BODY : mBodyBuilder.build());
  }
}
