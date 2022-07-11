package com.binlee.sample.event;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import com.binlee.sample.core.IWhat;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created on 21-2-7.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class ConnectEvent extends BluetoothGattCallback implements AsyncEvent {

  private final BluetoothDevice mTarget;
  @Type
  private final int mType;
  private boolean mFinished;
  private Context mContext;
  private Handler mHandler;
  private BluetoothGatt mGatt;
  private int mStatus;

  public ConnectEvent(BluetoothDevice target, @Type int type) {
    mTarget = target;
    mType = type;
  }

  @Override
  public void setContext(Context context) {
    mContext = context;
  }

  @Override
  public void setHandler(Handler handler) {
    mHandler = handler;
  }

  @Override
  public int type() {
    return mType;
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
    releaseGatt();
    mFinished = true;
    mStatus = reason == REASON_CONNECT_DONE ? STATUS_CONNECTED : STATUS_NOT_CONNECTED;
    reportConnectStatus(mStatus);
  }

  @Override
  public void run() {
    mFinished = false;
    int state = target().getBondState();
    if (state == BluetoothDevice.BOND_BONDED) {
      // notify GmdManager to care of this
      mHandler.obtainMessage(IWhat.BONDED_CHANGED, state, 0, target()).sendToTarget();
    } else if (state == BluetoothDevice.BOND_NONE) {
      // create bond
      mHandler.obtainMessage(IWhat.GATT_CREATE_BOND, target()).sendToTarget();
    }
  }

  @Override
  public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
    if (!target().equals(gatt.getDevice())) return;
    if (status != BluetoothGatt.GATT_SUCCESS) {
      // status 8/19/22/133
      return;
    }
    if (newState != BluetoothProfile.STATE_CONNECTED) return;
    reportGattStatus(status);
    reportConnectStatus(STATUS_CONNECTING);
  }

  @Override
  public void onServicesDiscovered(BluetoothGatt gatt, int status) {
    if (!target().equals(gatt.getDevice())) return;
    if (status != BluetoothGatt.GATT_SUCCESS) return;
  }

  @Override
  public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic bgc,
    int status) {
    if (!target().equals(gatt.getDevice())) return;
    if (status != BluetoothGatt.GATT_SUCCESS) return;
    // 当所有信息读取回来之后，再开始给对端写配置信息
    mHandler.obtainMessage(IWhat.GATT_START_CONFIG, target()).sendToTarget();
  }

  @Override
  public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic bgc,
    int status) {
    if (!target().equals(gatt.getDevice())) return;
    if (status != BluetoothGatt.GATT_SUCCESS) return;
    // 一个属性写成功后再写下一个，知道全部写完
    reportConnectStatus(STATUS_CONFIG_START);
    reportConnectStatus(STATUS_CONFIG_SECOND);
    reportConnectStatus(STATUS_CONFIG_OVER);
  }

  public void connectGatt() {
    mGatt = mTarget.connectGatt(mContext, false, this, BluetoothDevice.DEVICE_TYPE_LE);
  }

  public boolean startConfig(int pipe, String channels) {
    if (mGatt == null) return false;
    BluetoothGattService service = mGatt.getService(UUID.randomUUID());
    if (service == null) return false;
    BluetoothGattCharacteristic bgc = service.getCharacteristic(UUID.randomUUID());
    if (bgc == null) return false;
    bgc.setValue((pipe + channels).getBytes(Charset.defaultCharset()));
    return mGatt.writeCharacteristic(bgc);
  }

  private void reportConnectStatus(int status) {
    // if (mStatus == status) return;
    mStatus = status;
    mHandler.obtainMessage(IWhat.CONNECT_STATUS_CHANGE, status, 0, this).sendToTarget();
  }

  private void reportGattStatus(int status) {
    mHandler.obtainMessage(IWhat.GATT_STATUS_REPORTED, status, 0, this).sendToTarget();
  }

  private void releaseGatt() {
    if (mGatt == null) return;
    mGatt.disconnect();
    mGatt.close();
    mGatt = null;
  }
}
