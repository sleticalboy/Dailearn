package com.binlee.learning.http.callback;

import androidx.annotation.UiThread;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

/**
 * Created on 18-9-3.
 * <p>
 * OkHttp 网络回调
 *
 * @author leebin
 */
public abstract class HttpCallback<T> implements Serializable {

  private static final long serialVersionUID = 4260682290842271483L;

  @SuppressWarnings("unchecked")
  public final Class<T> getType() {
    return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
  }

  @UiThread
  public abstract void onSuccess(T response);

  @UiThread
  public abstract void onFailure(Throwable e);

  public final boolean isStringType() {
    return getType() == String.class;
  }
}
