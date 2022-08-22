package com.binlee.learning.http.interceptor;

import androidx.annotation.NonNull;
import com.binlee.learning.http.OkUtils;
import java.io.IOException;
import java.util.HashMap;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created on 18-9-10.
 * <p>
 * 用于 header 注入
 *
 * @author leebin
 */
public final class InjectHeaderInterceptor implements Interceptor {

  // 对外暴露一个接口：
  // 1,需要拦截注入的 url 的规则: boolean matched(String url);
  // 2,注入 header 的 map: Map<String, String> headers();

  public static InjectHeaderInterceptor newInstance() {
    return new InjectHeaderInterceptor();
  }

  private InjectHeaderInterceptor() {
  }

  @Override
  public Response intercept(final Chain chain) throws IOException {
    final Request oldRequest = chain.request();
    final String url = oldRequest.url().toString();
    // 非敏行 api 或者 ping 时 不需要 header 注入
    if (!url.startsWith("baseUrl")/* || url.contains("/api/v1/ping?a")*/) {
      return chain.proceed(oldRequest);
    }
    final Headers.Builder builder = oldRequest.headers().newBuilder();
    injectHeaders(builder, url);
    final Request newRequest = oldRequest.newBuilder().headers(builder.build()).build();
    return chain.proceed(newRequest);
  }

  /**
   * header 注入
   */
  private void injectHeaders(Headers.Builder builder, @NonNull final String url) {
    // 敏行 api 发请求的时候需要带着
    builder.set("NETWORK-ID", "network_id");
    // domain-id 和 bearer token 在加载图片的时候需要注入[普通的敏行 api 请求貌似也是需要注入的]
    builder.set("DOMAIN-ID", "1");
    builder.set("Authorization", "Bearer " + "access_token");
    // User-Agent 貌似是使用 Http(s)UrlConnection 的时候才需要注入 UA
    builder.set("User-Agent", "user_agent");
    // 自定义 header
    final HashMap<String, String> customHeaders = null;
    if (!OkUtils.empty(customHeaders)) {
      for (final String name : customHeaders.keySet()) {
        builder.set(name, customHeaders.get(name));
      }
    }
    // 启用隧道的时候需要注入 tunnel-use
    builder.set("Tunnel-Use", "login_name");
  }
}
