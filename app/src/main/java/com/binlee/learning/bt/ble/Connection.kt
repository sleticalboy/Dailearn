package com.binlee.learning.bt.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import android.util.Log
import com.binlee.learning.bt.BtUtils
import java.util.Objects
import java.util.concurrent.ExecutorService
import java.util.concurrent.RejectedExecutionException

/**
 * Created on 20-8-13.
 *
 * @author Ben binli@grandstream.cn
 */
class Connection(val device: BluetoothDevice, private var mCallback: IConnectCallback?) :
  BluetoothGattCallback(), Runnable {

  private val mLock = Object()
  private var mCanceled = false
  private var mDispatcher: Dispatcher? = null

  override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
    Log.d(TAG, "onConnectionStateChange() status = [$status], newState = [$newState]")
    if (status == 0 && device == gatt.device) {
      val started = gatt.discoverServices()
      Log.d(TAG, "onConnectionStateChange() -> start search services: $started")
    }
  }

  override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
    Log.d(TAG, "onServicesDiscovered() status = [$status]")
    if (status == 0 && device == gatt.device) {
      Log.d(TAG, "onServicesDiscovered() start resolve all services...")
    }
  }

  override fun onCharacteristicRead(
    gatt: BluetoothGatt,
    bgc: BluetoothGattCharacteristic,
    status: Int
  ) {
  }

  override fun onCharacteristicWrite(
    gatt: BluetoothGatt,
    bgc: BluetoothGattCharacteristic,
    status: Int
  ) {
  }

  override fun onCharacteristicChanged(gatt: BluetoothGatt, bgc: BluetoothGattCharacteristic) {}

  override fun onDescriptorRead(gatt: BluetoothGatt, desc: BluetoothGattDescriptor, status: Int) {}

  override fun onDescriptorWrite(gatt: BluetoothGatt, desc: BluetoothGattDescriptor, status: Int) {}

  override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {}

  override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
    Log.d(TAG, "onReadRemoteRssi() rssi = [$rssi], status = [$status]")
  }

  override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
    Log.d(TAG, "onMtuChanged() mtu = [$mtu], status = [$status]")
  }

  override fun run() {
    val oldName = Thread.currentThread().name
    Thread.currentThread().name = device.address
    try {
      execute()
    } finally {
      Thread.currentThread().name = oldName
    }
  }

  fun cancel() {
    mCallback = null
    mCanceled = true
  }

  override fun equals(o: Any?): Boolean {
    if (this === o) return true
    if (o !is Connection) return false
    return device == o.device
  }

  override fun hashCode(): Int {
    return Objects.hash(device)
  }

  fun setDispatcher(dispatcher: Dispatcher?) {
    mDispatcher = dispatcher
  }

  fun notifyStateChange() {
    synchronized(mLock) {
      mLock.notifyAll()
    }
  }

  fun executeOn(service: ExecutorService) {
    var success = false
    try {
      service.execute(this)
      success = true
      mCallback!!.onSuccess(this)
    } catch (e: RejectedExecutionException) {
      mCallback!!.onFailure(this, BleException("", e))
    } finally {
      if (!success) {
        mDispatcher!!.finish(this)
      }
    }
  }

  private fun execute() {
    // 1、绑定蓝牙
    if (mCanceled) {
      mCallback!!.onFailure(this, BleException("Canceled."))
      return
    }
    if (!BtUtils.createBond(device)) {
      // 发起绑定失败
      mCallback!!.onFailure(this, BleException("Create bond failed."))
      return
    } else {
      try {
        // java.lang.IllegalMonitorStateException: object not locked by thread before wait()
        synchronized(mLock) {
          mLock.wait(2000L)
        }
      } catch (e: InterruptedException) {
        // 绑定超时
        mCallback!!.onFailure(this, BleException("Bond timeout.", e))
        return
      }
    }
    // 2、gatt 操作, 回调方法默认是在主线程执行的，请勿执行耗时操作
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      device.connectGatt(mDispatcher?.getContext(), false, this, BluetoothDevice.TRANSPORT_LE)
    } else {
      device.connectGatt(mDispatcher?.getContext(), false, this)
    }
    // BtUtils.isConnected(device)
  }

  companion object {
    private const val TAG = "Connection"
  }
}