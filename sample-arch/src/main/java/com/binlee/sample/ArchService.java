package com.binlee.sample;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import com.binlee.sample.core.ArchManager;
import com.binlee.sample.core.DataSource;
import com.binlee.sample.core.IArchManager;
import com.binlee.sample.util.Glog;
import com.binlee.sample.view.IView;

/**
 * Created on 21-2-4.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class ArchService extends Service {

  private static final String TAG = Glog.wrapTag("Service");

  private final IArchManager mManager;
  private LocalBinder mBinder;
  // IArchManager 是否启动
  private boolean mStarted = false;

  public static void onBootCompleted(Context context) {
    DataSource.get().init(context, hasCache -> {
      Glog.w(TAG, "onBootCompleted() hasCache: " + hasCache);
    });

    final Intent service = new Intent(context, ArchService.class)
      .putExtra("_bootstrap", true);
    context.startService(service);
    Glog.v(TAG, "onBootCompleted() " + context);
  }

  public ArchService() {
    mManager = new ArchManager();
  }

  public static boolean bind(Context context, ServiceConnection conn) {
    final Intent service = new Intent(context, ArchService.class);
    return context.bindService(service, conn, BIND_AUTO_CREATE);
  }

  public static void unbind(Context context, ServiceConnection conn) {
    if (context != null) context.unbindService(conn);
  }

  @Override
  public void onCreate() {
    mManager.onCreate(this);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    startIfNot();
    return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    if (mBinder == null) mBinder = new LocalBinder(mManager);
    Glog.v(TAG, "onBind() " + mBinder);
    startIfNot();
    return mBinder;
  }

  private void startIfNot() {
    Glog.v(TAG, "startIfNot() mStarted: " + mStarted);
    if (mStarted) return;
    DataSource.get().init(this, null);
    mManager.onStart();
    mStarted = true;
  }

  @Override
  public void onDestroy() {
    mManager.onDestroy();
  }

  public static final class LocalBinder extends Binder {

    private final IArchManager mService;

    public LocalBinder(IArchManager service) {
      mService = service;
    }

    public void attachView(IView view, boolean refresh) {
      mService.attachView(view, refresh);
    }

    public void detachView(IView view, boolean abort) {
      mService.detachView(view, abort);
    }
  }
}
