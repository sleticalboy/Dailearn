package com.binlee.sample.event;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import com.binlee.sample.core.IWhat;
import com.binlee.sample.util.NrfHelper;

/**
 * Created on 21-2-7.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class DisconnectEvent implements AsyncEvent {

  private final BluetoothDevice mTarget;
  @Type
  private final int mType;
  private boolean mFinished;
  private Handler mHandler;
  private int mPipe;
  private int mStatus;

  public DisconnectEvent(BluetoothDevice target, @Type int type) {
    mTarget = target;
    mType = type;
  }

  @Override
  public void setHandler(Handler handler) {
    mHandler = handler;
  }

  @Override
  public BluetoothDevice target() {
    return mTarget;
  }

  @Override
  public boolean isFinished() {
    return mFinished;
  }

  @Override
  public void onFinish(int reason) {
    if (isFinished()) return;
    mFinished = true;
    mStatus = reason == REASON_DISCONNECT_DONE ? STATUS_NOT_CONNECTED : mStatus;
  }

  @Override
  public int type() {
    return mType;
  }

  @Override
  public void run() {
    mFinished = true;
    if (mStatus != STATUS_NOT_CONNECTED) {
      disconnectNrf();
      mStatus = STATUS_DISCONNECTING;
    }
    reportConnectStatus(mStatus);
  }

  public void setStatus(int status) {
    mStatus = status;
  }

  public void setPipe(int pipe) {
    mPipe = pipe;
  }

  private void reportConnectStatus(int status) {
    mStatus = status;
    mHandler.obtainMessage(IWhat.CONNECT_STATUS_CHANGE, status, 0, this).sendToTarget();
  }

  private void disconnectNrf() {
    if (mPipe < 0) return;
    NrfHelper.disconnect(mPipe);
  }
}
