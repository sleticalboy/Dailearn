package com.binlee.learning.bt

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.os.Build
import java.util.Locale

/**
 * Created on 20-5-4.
 *
 * @author BenLee binli@grandstream.cn
 */
object BtUtils {

  // public static CharSequence errorReason(Context context, String name, int reason) {
  //     int errorMsg;
  //     switch(reason) {
  //         case BluetoothDevice.UNBOND_REASON_AUTH_FAILED:
  //             errorMsg = R.string.bluetooth_pairing_pin_error_message;
  //             break;
  //         case BluetoothDevice.UNBOND_REASON_AUTH_REJECTED:
  //             errorMsg = R.string.bluetooth_pairing_rejected_error_message;
  //             break;
  //         case BluetoothDevice.UNBOND_REASON_REMOTE_DEVICE_DOWN:
  //             errorMsg = R.string.bluetooth_pairing_device_down_error_message;
  //             break;
  //         case BluetoothDevice.UNBOND_REASON_DISCOVERY_IN_PROGRESS:
  //         case BluetoothDevice.UNBOND_REASON_AUTH_TIMEOUT:
  //         case BluetoothDevice.UNBOND_REASON_REPEATED_ATTEMPTS:
  //         case BluetoothDevice.UNBOND_REASON_REMOTE_AUTH_CANCELED:
  //             errorMsg = R.string.bluetooth_pairing_error_message;
  //             break;
  //         case BluetoothDevice.UNBOND_REASON_REMOVED:
  //             return "9 Existing bond was explicitly revoked";
  //         case BluetoothDevice.UNBOND_REASON_AUTH_CANCELED:
  //             return "3 Bonding process was cancelled.";
  //         default:
  //             return "No reason for: " + reason;
  //     }
  //     return reason + " " + context.getResources().getString(errorMsg, name);
  // }
  fun bondStr(state: Int): String {
    return when (state) {
      BluetoothDevice.BOND_NONE -> {
        "NONE(10)"
      }
      BluetoothDevice.BOND_BONDING -> {
        "BONDING(11)"
      }
      BluetoothDevice.BOND_BONDED -> {
        "BONDEd(12)"
      }
      else -> String.format(Locale.US, "Unknown state(%d)", state)
    }
  }

  fun bleTypeToString(type: Int): String {
    return when (type) {
      BluetoothDevice.DEVICE_TYPE_DUAL -> {
        "Dual Mode - BR/EDR/LE(3)"
      }
      BluetoothDevice.DEVICE_TYPE_LE -> {
        "Low Energy - LE-only(2)"
      }
      BluetoothDevice.DEVICE_TYPE_CLASSIC -> {
        "Classic - BR/EDR devices(1)"
      }
      else -> "Unknown(0)"
    }
  }

  fun profileState(state: Int): String {
    return when (state) {
      BluetoothProfile.STATE_DISCONNECTED -> {
        "0 DISCONNECTED"
      }
      BluetoothProfile.STATE_CONNECTING -> {
        "1 CONNECTING"
      }
      BluetoothProfile.STATE_CONNECTED -> {
        "2 CONNECTED"
      }
      BluetoothProfile.STATE_DISCONNECTING -> {
        "3 DISCONNECTING"
      }
      else -> {
        "Unknown state $state"
      }
    }
  }

  fun gattStatusToString(status: Int): String {
    // GATT_ERROR 0x85 133
    // 任何不惧名字的错误都出现这个错误码，出现了就认怂吧，重新连接吧。
    // GATT_CONN_TIMEOUT  0x08 8
    // 连接超时，大多数情况是设备离开可连接范围，然后手机端连接超时断开返回此错误码。
    // GATT_CONN_TERMINATE_PEER_USER 0x13 19 
    // 连接被对端设备终止，直白点就是手机去连接外围设备，外围设备任性不让连接执行了断开。
    // GATT_CONN_TERMINATE_LOCAL_HOST 0x16 22 
    // 连接被本地主机终止，可以解释为手机连接外围设备，但是连接过程中出现一些比如鉴权等问题，无法继续保持连接，主动执行了断开操作。
    // GATT_CONN_FAIL_ESTABLISH 03E 62  连接建立失败。
    return when (status) {
      0 -> "0(Gatt success)"
      1 -> "1(Gatt connect l2c failure: invalid handle)"
      2 -> "2(Gatt read not permit)"
      3 -> "3(Gatt write not permit)"
      4 -> "4(Gatt invalid pdu)"
      5 -> "5(Gatt insuf authentication)"
      6 -> "6(Gatt req not supported)"
      7 -> "7(Gatt invalid offset)"
      8 -> "8(Gatt connect timeout: discover primary service timeout)"
      9 -> "9(Gatt prepare q full)"
      10 -> "10(Gatt not found)"
      11 -> "11(Gatt not long)"
      12 -> "12(Gatt insuf key size)"
      13 -> "13(Gatt invalid attr len)"
      14 -> "14(Gatt err unlikely)"
      15 -> "15(Gatt insuf encryption)"
      16 -> "16(Gatt unsupport grp type)"
      17 -> "17(Gatt insuf resource)"
      19 -> "19(Gatt connect terminate peer user)"
      22 -> "22(Gatt connect terminate local host)"
      34 -> "34(Gatt connect lmp timeout)"
      62 -> "62(Gatt connect fail establish)"
      133 -> "133(Gatt error)"
      256 -> "256(Gatt connect cancel)"
      257 -> "257(Too many open connections)"
      else -> "Unknown status $status"
    }
  }

  fun createBond(bt: BluetoothDevice): Boolean {
    return if (bt.bondState == BluetoothDevice.BOND_NONE) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val ret = invoke(bt, bt.javaClass, "createBond", BluetoothDevice.TRANSPORT_LE)
        ret is Boolean && ret
      } else {
        bt.createBond()
      }
    } else false
  }

  fun removeBond(bt: BluetoothDevice): Boolean {
    val state = bt.bondState
    if (state == BluetoothDevice.BOND_BONDING) {
      invoke(bt, bt.javaClass, "cancelBondProcess")
      return true
    } else if (state == BluetoothDevice.BOND_BONDED) {
      val ret = invoke(bt, bt.javaClass, "removeBond")
      return ret is Boolean && ret
    }
    return false
  }

  fun connectProfile(proxy: BluetoothProfile, device: BluetoothDevice?): Boolean {
    val ret = invoke(proxy, proxy.javaClass, "connect", device!!)
    return ret is Boolean && ret
  }

  fun isConnected(device: BluetoothDevice): Boolean {
    val ret = invoke(device, device.javaClass, "isConnected")
    return ret is Boolean && ret
  }

  private operator fun invoke(obj: Any, clazz: Class<*>, method: String, vararg args: Any): Any? {
    return try {
      val m = if (args.isEmpty()) {
        clazz.getDeclaredMethod(method)
      } else {
        val parameterTypes = arrayOfNulls<Class<*>?>(args.size)
        for (i in args.indices) {
          parameterTypes[i] = getObjClass(args[i])
        }
        clazz.getDeclaredMethod(method, *parameterTypes)
      }
      // 如果参数是 int，则必须是 int.class
      m.isAccessible = true
      m.invoke(obj, *args)
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  private fun getObjClass(obj: Any?): Class<*>? {
    when {
      obj is Class<*> -> {
        return obj
      }
      obj is Boolean -> {
        return Boolean::class.javaPrimitiveType
      }
      obj is Char -> {
        return Char::class.javaPrimitiveType
      }
      obj is Byte -> {
        return Byte::class.javaPrimitiveType
      }
      obj is Short -> {
        return Short::class.javaPrimitiveType
      }
      obj is Int -> {
        return Int::class.javaPrimitiveType
      }
      obj is Long -> {
        return Long::class.javaPrimitiveType
      }
      obj is Float -> {
        return Float::class.javaPrimitiveType
      }
      obj is Double -> {
        return Double::class.javaPrimitiveType
      }
      obj is Void -> {
        return Void.TYPE
      }
      obj != null -> {
        return obj.javaClass
      }
      else -> return null
    }
  }
}