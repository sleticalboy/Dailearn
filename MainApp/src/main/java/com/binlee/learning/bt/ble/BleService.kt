package com.binlee.learning.bt.ble

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothProfile.ServiceListener
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import android.util.Log

class BleService : Service(), Handler.Callback {

  private var mBinder: LeBinder? = null
  private var handler: Handler? = null
  private var mScanner: BleScanner? = null
  private var mDispatcher: Dispatcher? = null

  private val mBtReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      // all logic is handled in mCoreHandler thread
      val action = intent.action
      val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
      if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
        if (intent.getIntExtra(
            BluetoothDevice.EXTRA_BOND_STATE,
            BluetoothDevice.BOND_NONE
          ) == BluetoothDevice.BOND_BONDED
        ) {
          Log.e(TAG, "receive action: $action start notify connection state.")
          mDispatcher!!.notifyConnectionState(device)
        }
      } else if (ACTION_HID_CONNECTION_STATE_CHANGED == action) {
        Log.e(TAG, "receive action: $action will notify connection state.")
        // mDispatcher.notifyConnectionState(device);
      }
    }
  }

  override fun onCreate() {
    val thread = HandlerThread("BleCoreThread")
    thread.start()
    handler = Handler(thread.looper, this)
    mScanner = BleScanner(this, handler!!)
    mDispatcher = Dispatcher(this)
    bindHidProxy()
    registerBtReceiver()
  }

  override fun onBind(intent: Intent): IBinder {
    if (mBinder == null) mBinder = LeBinder(this)
    return mBinder!!
  }

  override fun onDestroy() {
    super.onDestroy()
    unbindHidProxy()
    unregisterBtReceiver()
    mBinder = null
  }

  private fun bindHidProxy() {
    val adapter = BluetoothAdapter.getDefaultAdapter()
    adapter.getProfileProxy(this, object : ServiceListener {
      override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
        mDispatcher?.setHidHost(proxy)
      }

      override fun onServiceDisconnected(profile: Int) {
        mDispatcher?.setHidHost(null)
      }
    }, 4 /*BluetoothProfile.HID_HOST*/)
  }

  private fun unbindHidProxy() {
    val adapter = BluetoothAdapter.getDefaultAdapter()
    adapter.closeProfileProxy(4 /*BluetoothProfile.HID_HOST*/, mDispatcher?.getHidHost())
  }

  private fun registerBtReceiver() {
    val filter = IntentFilter()
    filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
    filter.addAction(ACTION_HID_CONNECTION_STATE_CHANGED)
    registerReceiver(mBtReceiver, filter, null, handler)
  }

  private fun unregisterBtReceiver() {
    unregisterReceiver(mBtReceiver)
  }

  override fun handleMessage(msg: Message): Boolean {
    if (msg.what == MSG_START_SCAN) {
      mScanner!!.startScan(msg.obj as BleScanner.Request)
    } else if (msg.what == MSG_STOP_SCAN) {
      mScanner!!.stopScan()
    } else if (msg.what == MSG_START_CONNECT) {
      mDispatcher!!.enqueue(msg.obj as Connection)
    } else if (msg.what == MSG_CANCEL_CONNECT) {
      if (msg.obj is BluetoothDevice) {
        mDispatcher!!.cancel(msg.obj as BluetoothDevice)
      } else {
        mDispatcher!!.cancelAll()
      }
    }
    return true
  }

  class LeBinder(private var mService: BleService?) : Binder() {

    fun startScan(request: BleScanner.Request?) {
      val msg = Message.obtain()
      msg.obj = request
      msg.what = MSG_START_SCAN
      mService?.handler?.sendMessage(msg)
    }

    fun stopScan() {
      mService?.handler?.sendEmptyMessage(MSG_STOP_SCAN)
    }

    fun connect(device: BluetoothDevice, callback: IConnectCallback) {
      val msg = Message.obtain()
      msg.obj = Connection(device, callback)
      msg.what = MSG_START_CONNECT
      mService?.handler?.sendMessage(msg)
    }

    fun cancel(device: BluetoothDevice?) {
      val msg = Message.obtain()
      msg.what = MSG_CANCEL_CONNECT
      msg.obj = device
      mService?.handler?.sendMessage(msg)
    }

    fun release() {
      mService = null
    }
  }

  companion object {
    private const val TAG = "BleService"
    const val ACTION_HID_CONNECTION_STATE_CHANGED =
      "android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED"
    private const val MSG_START_SCAN = 0x20
    private const val MSG_STOP_SCAN = 0x22
    private const val MSG_START_CONNECT = 0x23
    private const val MSG_CANCEL_CONNECT = 0x24
  }
}