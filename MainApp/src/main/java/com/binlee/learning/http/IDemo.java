package com.binlee.learning.http;

import com.binlee.learning.http.bean.Apis;
import io.reactivex.Observable;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;

/**
 * Created on 21-3-18.
 *
 * @author binlee sleticalboy@gmail.com
 */
public interface IDemo {

  @Headers({ Constants.REWRITE_HOST + ": " + Constants.HOST_BAIDU })
  @GET("/index.html")
  Call<String> visit(@Header(value = "Content-Type") String header);

  @Headers({ Constants.REWRITE_HOST + ": " + Constants.HOST_BAIDU })
  @GET("/")
  Observable<String> visit();

  @Headers({ Constants.REWRITE_HOST + ": " + Constants.HOST_BAIDU })
  @GET("/")
  List<String> list();

  @Headers({ Constants.REWRITE_HOST + ": " + Constants.HOST_BAIDU })
  @GET("/")
  byte[] byteArray();

  @Headers({ Constants.REWRITE_HOST + ": " + Constants.HOST_BAIDU })
  @GET("/")
  WebPage webPage();

  /**
   * 获取所有的 API
   */
  @Headers({ Constants.REWRITE_HOST + ": " + Constants.HOST_GITHUB })
  @GET("/")
  Call<Apis> listApis();

  /**
   * 获取 user 的全部仓库
   *
   * @param user 用户名
   * @return 该用户所有的仓库
   */
  @Headers({ Constants.REWRITE_HOST + ": " + Constants.HOST_GITHUB })
  @GET("/users/{user}/repos")
  Call<List<?>> listRepos(@Path("user") String user);
}
