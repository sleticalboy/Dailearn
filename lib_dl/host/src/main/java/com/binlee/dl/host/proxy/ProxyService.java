package com.binlee.dl.host.proxy;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.binlee.dl.host.IMaster;
import com.binlee.dl.puppet.IPuppetService;

/**
 * Created on 2022-08-28.
 *
 * @author binlee
 */
public final class ProxyService extends Service implements IMaster {

  private boolean mCreated = false;
  private IPuppetService mPuppet;

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    final String target = intent.getStringExtra(TARGET_COMPONENT);
    mPuppet = PuppetFactory.create(target);
    if (mPuppet != null) {
      if (!mCreated) {
        mPuppet.onCreate(/*Context*/this);
        mCreated = true;
      }
      return mPuppet.onStartCommand(intent, flags, startId);
    }
    return super.onStartCommand(intent, flags, startId);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return mPuppet != null ? mPuppet.onBind(intent) : null;
  }

  @Override public boolean onUnbind(Intent intent) {
    return mPuppet != null ? mPuppet.onUnbind(intent) : super.onUnbind(intent);
  }

  @Override public void onRebind(Intent intent) {
    if (mPuppet != null) mPuppet.onRebind(intent);
  }

  @Override public void onDestroy() {
    if (mPuppet != null) mPuppet.onDestroy();
  }
}
