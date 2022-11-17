package com.example.fileserver.core;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.fileserver.FileServer;
import com.yanzhenjie.andserver.annotation.Interceptor;
import com.yanzhenjie.andserver.framework.HandlerInterceptor;
import com.yanzhenjie.andserver.framework.handler.RequestHandler;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;

/**
 * Created on 2022/11/17
 *
 * @author binlee
 */
@Interceptor
public class RequestRecorder implements HandlerInterceptor {

  private static final String TAG = "RequestRecorder";

  @Override public boolean onIntercept(@NonNull HttpRequest request, @NonNull HttpResponse response,
    @NonNull RequestHandler handler) throws Exception {
    final String record = "remote: "
      + request.getRemoteHost()
      + ":"
      + request.getRemotePort()
      + " "
      + request.getMethod()
      + " "
      + request.getPath()
      + " with: "
      + request.getParameter();
    Log.d(TAG, "onIntercept() " + record);
    FileServer.getRequest().postValue(record);
    return false;
  }
}
