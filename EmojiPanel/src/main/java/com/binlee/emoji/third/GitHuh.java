package com.binlee.emoji.third;

import java.util.Map;
import okhttp3.Response;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created on 20-9-21.
 *
 * @author Ben binli@grandstream.cn
 */
public interface GitHuh {

  @POST()
  Response auth(@Path(value = "/auth", encoded = true) String path, @HeaderMap Map<String, String> headers);
}
