package com.binlee.sample.event;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.binlee.sample.AsyncCall;
import com.binlee.sample.IMessages;

import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created on 21-2-7.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class ConnectEvent extends BluetoothGattCallback implements IEvent, AsyncCall {

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
    public void onFinish() {
        mFinished = true;
        release();
    }

    @Override
    public boolean isFinished() {
        return mFinished;
    }

    @Override
    public void run() {
        mFinished = false;
        int state = target().getBondState();
        if (state == BluetoothDevice.BOND_BONDED) {
            // notify GmdManager to care of this
            mHandler.obtainMessage(IMessages.BONDED_CHANGED, state, 0, target()).sendToTarget();
        } else if (state == BluetoothDevice.BOND_NONE) {
            // create bond
            mHandler.obtainMessage(IMessages.GATT_CREATE_BOND, target()).sendToTarget();
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (!target().equals(gatt.getDevice())) {
            return;
        }
        if (status != BluetoothGatt.GATT_SUCCESS) {
            // status 8/19/22/133
            return;
        }
        if (newState != BluetoothProfile.STATE_CONNECTED) {
            return;
        }
        reportGattStatus(status);
        updateConnectStatus(STATUS_CONNECTING);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (!target().equals(gatt.getDevice())) {
            return;
        }
        if (status != BluetoothGatt.GATT_SUCCESS) {
            return;
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic bgc,
                                     int status) {
        if (!target().equals(gatt.getDevice())) {
            return;
        }
        if (status != BluetoothGatt.GATT_SUCCESS) {
            return;
        }
        // 当所有信息读取回来之后，再开始给对端写配置信息
        mHandler.obtainMessage(IMessages.GATT_START_CONFIG, target()).sendToTarget();
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic bgc,
                                      int status) {
        if (!target().equals(gatt.getDevice())) {
            return;
        }
        if (status != BluetoothGatt.GATT_SUCCESS) {
            return;
        }
        // 一个属性写成功后再写下一个，知道全部写完
        updateConnectStatus(STATUS_CONFIG_START);
        updateConnectStatus(STATUS_CONFIG_SECOND);
        updateConnectStatus(STATUS_CONFIG_OVER);
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

    private void updateConnectStatus(int status) {
        mStatus = status;
        mHandler.obtainMessage(IMessages.CONNECT_STATUS_CHANGE, status, 0, this).sendToTarget();
    }

    private void reportGattStatus(int status) {
        mHandler.obtainMessage(IMessages.GATT_STATUS_REPORTED, status, 0, this).sendToTarget();
    }

    private void release() {
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
            mGatt = null;
        }
    }
}