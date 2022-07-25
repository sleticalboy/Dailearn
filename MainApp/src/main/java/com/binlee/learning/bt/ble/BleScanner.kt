package com.binlee.learning.bt.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi

class BleScanner(context: Context?, private val mHandler: Handler) {

  private val mAdapter: BluetoothAdapter

  @Volatile
  private var mStarted = false
  private var mRawCallback: Any? = null
  private var mRequest: Request? = null

  fun startScan(request: Request?) {
    requireNotNull(request) { "request is null." }
    if (mStarted || !mAdapter.isEnabled) {
      if (!mStarted) {
        Log.d(TAG, "Bluetooth is disabled, enable it.")
        mAdapter.enable()
        mHandler.postDelayed({ startScan(request) }, 500L)
      }
      return
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      startAfterLL(request)
    } else {
      startBeforeLL(request)
    }
    mRequest = request
    mStarted = true
    if (request.mDuration > 0) {
      mHandler.postDelayed({ stopScan() }, request.mDuration)
    }
  }

  private fun startBeforeLL(request: Request?) {
    val rawCallback =
      LeScanCallback { device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray? ->
        if (request?.mCallback == null) {
          return@LeScanCallback
        }
        if (request.mCallback!!.filter(scanRecord)) {
          val rst = Result.obtain()
          rst.mDevice = device
          rst.mRssi = rssi
          rst.mConnectable = true
          request.mCallback!!.onScanResult(rst)
          rst.recycle()
        }
      }
    mAdapter.startLeScan(rawCallback)
    mRawCallback = rawCallback
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private fun startAfterLL(request: Request?) {
    val rawCallback: ScanCallback = object : ScanCallback() {
      override fun onScanResult(callbackType: Int, result: ScanResult) {
        // Log.d(TAG, "onScanResult() callbackType: " + callbackType + ", result: " + result);
        if (request?.mCallback == null || result.scanRecord == null) {
          return
        }
        val record = result.scanRecord
        if (request.mCallback!!.filter(record?.bytes)) {
          var connectable = true
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            connectable = result.isConnectable
          }
          val rst = Result.obtain()
          rst.mDevice = result.device
          rst.mRssi = result.rssi
          rst.mConnectable = connectable
          request.mCallback!!.onScanResult(rst)
          rst.recycle()
        }
      }

      override fun onScanFailed(errorCode: Int) {
        if (request?.mCallback != null) {
          request.mCallback!!.onScanFailed(errorCode)
        }
      }
    }
    // final ScanFilter filter = new ScanFilter.Builder().build();
    // final ScanSettings settings = new ScanSettings.Builder()
    //         .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
    //         .build();
    mAdapter.bluetoothLeScanner.startScan(rawCallback)
    mRawCallback = rawCallback
  }

  fun stopScan() {
    if (!mStarted) {
      return
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      mAdapter.bluetoothLeScanner.stopScan(mRawCallback as ScanCallback?)
    } else {
      mAdapter.stopLeScan(mRawCallback as LeScanCallback?)
    }
    mRequest!!.mCallback = null
    mRequest = null
    mRawCallback = null
    mStarted = false
  }

  abstract class Callback {

    abstract fun onScanResult(result: Result)

    open fun onScanFailed(errorCode: Int) {}

    fun filter(scanRecord: ByteArray?): Boolean {
      return true
    }
  }

  class Request {
    var mCallback: Callback? = null
    var mDuration: Long = 0
  }

  class Result private constructor() {

    var mDevice: BluetoothDevice? = null
    var mRssi = 0
    var mConnectable = true
    private var mNext: Result? = null
    private var mInUse = 0

    override fun equals(o: Any?): Boolean {
      if (this === o) {
        return true
      }
      return if (o == null || javaClass != o.javaClass) {
        false
      } else mDevice == (o as Result).mDevice
    }

    override fun hashCode(): Int {
      return mDevice.hashCode()
    }

    override fun toString(): String {
      return "{device=$mDevice, rssi=$mRssi, connectable=$mConnectable}"
    }

    fun recycle() {
      if (mInUse == 0) {
        return
      }
      mDevice = null
      mRssi = 0
      mConnectable = true
      mInUse = 0
      synchronized(POOL_LOCK) {
        if (sPoolSize < 15) {
          mNext = sPool
          sPool = this
          sPoolSize++
        }
      }
    }

    companion object {
      private var sPool: Result? = null
      private var sPoolSize = 0
      private val POOL_LOCK = Any()
      fun obtain(): Result {
        synchronized(POOL_LOCK) {
          if (sPool != null) {
            val r = sPool
            sPool = r!!.mNext
            r.mNext = null
            r.mInUse = 0
            sPoolSize--
            return r
          }
        }
        return Result()
      }
    }
  }

  companion object {
    private const val TAG = "BleScanner"
  }

  init {
    mAdapter = BluetoothAdapter.getDefaultAdapter()
  }
}