package com.binlee.learning.bt.ble

/**
 * Created on 20-8-13.
 *
 * @author Ben binli@grandstream.cn
 */
interface IConnectCallback {

  fun onFailure(connection: Connection, e: BleException)
  fun onSuccess(connection: Connection)
}