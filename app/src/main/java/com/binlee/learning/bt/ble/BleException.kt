package com.binlee.learning.bt.ble

import android.util.AndroidException

/**
 * Created on 20-8-19.
 *
 * @author Ben binli@grandstream.cn
 */
class BleException : AndroidException {

  constructor(msg: String?) : super(msg)

  constructor(msg: String?, cause: Throwable?) : super(msg, cause)
}