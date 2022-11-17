package com.example.fileserver;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2022/11/2
 *
 * @author binlee
 */
public class FileServer {

  private static final String TAG = "FileServer";

  private Context mContext;

  private Handler mWorker;
  private Server mServer;

  private static MutableLiveData<String> sUrl = new MutableLiveData<>(null);
  private static MutableLiveData<String> sRequest = new MutableLiveData<>(null);

  public static MutableLiveData<String> getUrl() {
    return sUrl;
  }

  public static MutableLiveData<String> getRequest() {
    return sRequest;
  }

  public FileServer() {
  }

  public void start(Context context) {

    mContext = context.getApplicationContext();

    // 启动一个线程
    HandlerThread thread = new HandlerThread(TAG);
    thread.start();
    mWorker = new Handler(thread.getLooper(), null);

    mWorker.post(this::startWebServer);
  }

  private void startWebServer() {
    mServer = AndServer.webServer(mContext)
      .port(8080)
      .timeout(10, TimeUnit.SECONDS)
      .listener(new Server.ServerListener() {
        @Override public void onStarted() {
          final String url = "http://" + FileUtil.lookupIpAddress(mContext) + ":" + mServer.getPort();
          Log.d(TAG, "onStarted() " + url);
          sUrl.postValue(url);
        }

        @Override public void onStopped() {
          Log.d(TAG, "onStopped() called");
          sUrl.postValue(null);
        }

        @Override public void onException(Exception e) {
          Log.d(TAG, "onException() called with: e = [" + e + "]");
          sUrl.postValue(null);
        }
      })
      .build();
    mServer.startup();
  }

  public void stop() {
    mWorker.removeCallbacksAndMessages(null);
    mServer.shutdown();
    mContext = null;
  }

}
