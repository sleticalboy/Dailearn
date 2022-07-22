package com.sleticalboy.learning.bt

import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.text.Html
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.sleticalboy.learning.R
import com.sleticalboy.learning.base.BaseListFragment
import com.sleticalboy.learning.base.BaseRVAdapter
import com.sleticalboy.learning.base.BaseRVHolder
import com.sleticalboy.learning.bt.ble.BleException
import com.sleticalboy.learning.bt.ble.BleScanner
import com.sleticalboy.learning.bt.ble.BleService
import com.sleticalboy.learning.bt.ble.Connection
import com.sleticalboy.learning.bt.ble.IConnectCallback
import com.sleticalboy.learning.databinding.BtCommonHeaderBinding

/**
 * Created on 20-8-18.
 *
 * @author Ben binli@grandstream.cn
 */
class BleFragment : BaseListFragment<BleScanner.Result>() {

  private var mService: BleService.LeBinder? = null
  private val mConn: ServiceConnection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName, service: IBinder) {
      Log.d(logTag(), "onServiceConnected() name: $name, service: $service")
      mService = service as BleService.LeBinder
    }

    override fun onServiceDisconnected(name: ComponentName) {
      Log.d(logTag(), "onServiceDisconnected() name:$name")
      mService?.release()
      mService = null
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    val intent = Intent(context, BleService::class.java)
    context.bindService(intent, mConn, Context.BIND_AUTO_CREATE)
  }

  override fun createAdapter(): BaseRVAdapter<BleScanner.Result> = DevicesAdapter()

  override fun initHeader(headerContainer: FrameLayout) {
    val bind = BtCommonHeaderBinding.inflate(layoutInflater, headerContainer, true)
    bind.startScan.setOnClickListener { startBtScan() }
    bind.stopScan.setOnClickListener { stopBtScan() }
  }

  private fun startBtScan() {
    if (mService == null) return
    val request = BleScanner.Request()
    request.mCallback = object : BleScanner.Callback() {
      override fun onScanResult(result: BleScanner.Result) {
        val position = getAdapter().addData(result)
        Log.d(logTag(), "onDeviceScanned() device: $result, index: $position")
      }

      override fun onScanFailed(errorCode: Int) {
        Log.d(logTag(), "onScanFailed() errorCode = $errorCode")
      }
    }
    request.mDuration = 10000L
    mService!!.startScan(request)
  }

  private fun stopBtScan() {
    if (mService != null) {
      mService!!.stopScan()
    }
  }

  private fun doConnect(device: BluetoothDevice) {
    Log.d(logTag(), "connect to $device")
    if (mService != null) {
      mService!!.connect(device, object : IConnectCallback {
        override fun onFailure(connection: Connection, e: BleException) {
          Log.d(logTag(), "onFailure() connection = $connection error: $e")
        }

        override fun onSuccess(connection: Connection) {
          Log.d(logTag(), "onSuccess() connection = $connection")
        }
      })
    }
  }

  private fun doCancel(device: BluetoothDevice?) {
    mService?.cancel(device)
  }

  override fun onDestroy() {
    super.onDestroy()
    stopBtScan()
    doCancel(null)
    context?.unbindService(mConn)
  }

  override fun logTag(): String = "BleFragment"

  private inner class DevicesAdapter : BaseRVAdapter<BleScanner.Result>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
      return DeviceHolder(parent, R.layout.item_ble_recycler)
    }
  }

  private inner class DeviceHolder(parent: ViewGroup, layout: Int) :
    BaseRVHolder<BleScanner.Result>(parent, layout) {

    val tvName: TextView = itemView.findViewById(R.id.tv_ble_name)
    val btnConnect: TextView = itemView.findViewById(R.id.btn_connect)

    override fun bindData(result: BleScanner.Result, position: Int) {
      tvName.text = Html.fromHtml(
        "<font color='red'>" + result.mDevice?.name
            + "</font>  <font color='blue'>" + result.mDevice?.address
      )
      btnConnect.isEnabled = result.mConnectable
      btnConnect.setOnClickListener {
        result.mDevice?.let { it1 -> doConnect(it1) }
      }
    }
  }
}