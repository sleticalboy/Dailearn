package com.binlee.sample.model;

import android.bluetooth.BluetoothDevice;

/**
 * Created on 21-2-27.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class ArchDevice {

  private final BluetoothDevice mTarget;
  public String mMac;
  public int content;

  public ArchDevice(BluetoothDevice target) {
    mTarget = target;
  }

  public ArchDevice(String mac) {
    mTarget = null;
    mMac = mac;
  }

  public BluetoothDevice target() {
    return mTarget;
  }

  public String getId() {
    return "0";
  }

  public String getName() {
    return mTarget != null ? mTarget.getName() : mMac;
  }
}
