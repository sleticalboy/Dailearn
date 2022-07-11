package com.binlee.sample.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import com.binlee.sample.event.IEvent;
import com.binlee.sample.event.ScanEvent;
import com.binlee.sample.util.Glog;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 21-2-8.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class BleScanner extends ScanCallback implements Handler.Callback {

  private static final String TAG = "BleScanner";

  private final Handler mHandler;
  private final Context mContext;
  private final List<ScanFilter> mFilters = new ArrayList<>();
  private BluetoothLeScanner mScanner;
  private ScanSettings mSettings;
  private ScanEvent mEvent;
  private int mRepetition;

  public BleScanner(Context context, Handler handler) {
    mContext = context;
    mHandler = handler;
    if (handler instanceof InjectableHandler) {
      ((InjectableHandler) handler).injectCallback(this);
    }
  }

  public void start(ScanEvent event) {
    if (event.type() == IEvent.STOP_SCAN) {
      stop();
      return;
    }
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if (adapter == null) {
      Glog.e(TAG, "start() aborted: Bluetooth binder is null.");
      return;
    }

    if (!adapter.isEnabled()) {
      IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
      mContext.registerReceiver(new StateReceiver(event), filter);
      adapter.enable();
      return;
    }
    if (mScanner == null) mScanner = adapter.getBluetoothLeScanner();
    if (mFilters.size() == 0) mFilters.add(new ScanFilter.Builder().build());
    if (mSettings == null) {
      mSettings = new ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build();
    }
    mEvent = event;
    mRepetition++;
    if (event.useUltra()) Glog.d(TAG, "start() send ultra before ble scan");
    if (event.duration() > 0) {
      mHandler.sendEmptyMessageDelayed(IWhat.STOP_SCAN, event.duration());
    }
    mScanner.startScan(mFilters, mSettings, this);
  }

  @Override
  public void onScanResult(int callbackType, ScanResult result) {
    Message msg = mHandler.obtainMessage(IWhat.SCAN_RESULT, result.getDevice());
    msg.arg1 = mEvent.type();
    msg.sendToTarget();
  }

  @Override
  public void onScanFailed(int errorCode) {
    Message msg = mHandler.obtainMessage(IWhat.SCAN_FAILED, mEvent);
    msg.arg1 = errorCode;
    msg.sendToTarget();
  }

  @Override
  public boolean handleMessage(@NonNull Message msg) {
    if (msg.what == IWhat.STOP_SCAN) {
      if (mRepetition < mEvent.repetition()) {
        mHandler.sendEmptyMessageDelayed(IWhat.RESUME_SCAN, mEvent.interval());
      }
      stop();
      return true;
    } else if (msg.what == IWhat.RESUME_SCAN) {
      start(mEvent);
      return true;
    }
    return false;
  }

  private void stop() {
    if (mScanner != null) {
      mScanner.stopScan(this);
    }
  }

  private final class StateReceiver extends BroadcastReceiver {

    private final ScanEvent mEvent;

    private StateReceiver(ScanEvent event) {
      mEvent = event;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
        mContext.unregisterReceiver(this);
        start(mEvent);
      }
    }
  }
}
