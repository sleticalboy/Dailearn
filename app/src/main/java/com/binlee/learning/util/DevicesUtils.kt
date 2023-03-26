package com.binlee.learning.util

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.annotation.RequiresPermission
import com.binlee.learning.util.ShellUtils.CommandResult
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.text.DecimalFormat
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Created on 18-5-14.
 *
 * @author leebin
 * @description
 */
object DevicesUtils {

  private const val DEFAULT_MAC_ADDRESS = "02:00:00:00:00:00"

  private fun getConnectedWifiInfo(context: Context?): WifiInfo? {
    if (context == null) {
      return null
    }
    val mWifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE)
        as WifiManager
    return if (mWifi.isWifiEnabled) {
      mWifi.connectionInfo
      //WifiInfo wifiInfo = mWifi.getConnectionInfo();
      ////获取被连接网络的名称
      //String netName = wifiInfo.getSSID();
      //Log.d("AttendUtils", "netName = " + netName);
      ////获取被连接网络的mac地址
      //String netMac = wifiInfo.getBSSID();
      //Log.d("AttendUtils", "netMac = " + netMac);
      ////final String macAddress = wifiInfo.getMacAddress(); // 02:00:00:00:00:00
      ////Log.d("AttendUtils", macAddress);
      //return netMac;
    } else null
  }

  fun getConnectedWifiMacAddress(context: Context?): String? {
    val wifiInfo = getConnectedWifiInfo(context)
    return wifiInfo?.bssid
  }

  fun getConnectedWifiName(context: Context?): String? {
    val wifiInfo = getConnectedWifiInfo(context)
    return wifiInfo?.ssid
  }

  /**
   * Return the MAC address.
   * from [github DeviceUtils.java](https://github.com/Blankj/AndroidUtilCode/blob/master/utilcode/src/main/java/com/blankj/utilcode/util/DeviceUtils.java)
   *
   * Must hold
   * `<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />`,
   * `<uses-permission android:name="android.permission.INTERNET" />`
   *
   * @return the MAC address
   */
  @RequiresPermission(allOf = [permission.ACCESS_WIFI_STATE, permission.INTERNET])
  fun getMacAddress(context: Context): String {
    var macAddress = getMacAddressByWifiInfo(context)
    if (DEFAULT_MAC_ADDRESS != macAddress) {
      return macAddress
    }
    macAddress = macAddressByNetworkInterface
    if (DEFAULT_MAC_ADDRESS != macAddress) {
      return macAddress
    }
    macAddress = macAddressByInetAddress
    if (DEFAULT_MAC_ADDRESS != macAddress) {
      return macAddress
    }
    macAddress = macAddressByFile
    return if (DEFAULT_MAC_ADDRESS != macAddress) {
      macAddress
    } else "please open wifi"
  }

  @SuppressLint("HardwareIds", "MissingPermission")
  private fun getMacAddressByWifiInfo(context: Context): String {
    try {
      val wifi = context.applicationContext
        .getSystemService(Context.WIFI_SERVICE) as WifiManager
      val info = wifi.connectionInfo
      if (info != null) {
        return info.macAddress
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return DEFAULT_MAC_ADDRESS
  }

  private val macAddressByNetworkInterface: String
    get() {
      try {
        val nis = NetworkInterface.getNetworkInterfaces()
        while (nis.hasMoreElements()) {
          val ni = nis.nextElement()
          if (ni == null || !"wlan0".equals(ni.name, ignoreCase = true)) {
            continue
          }
          val macBytes = ni.hardwareAddress
          if (macBytes != null && macBytes.isNotEmpty()) {
            val sb = StringBuilder()
            for (b in macBytes) {
              sb.append(String.format("%02x:", b))
            }
            return sb.substring(0, sb.length - 1)
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
      return DEFAULT_MAC_ADDRESS
    }
  private val macAddressByInetAddress: String
    get() {
      try {
        val inetAddress = inetAddress
        if (inetAddress != null) {
          val ni = NetworkInterface.getByInetAddress(inetAddress)
          if (ni != null) {
            val macBytes = ni.hardwareAddress
            if (macBytes != null && macBytes.size > 0) {
              val sb = StringBuilder()
              for (b in macBytes) {
                sb.append(String.format("%02x:", b))
              }
              return sb.substring(0, sb.length - 1)
            }
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
      return DEFAULT_MAC_ADDRESS
    }

  // To prevent phone of xiaomi return "10.0.2.15"
  private val inetAddress: InetAddress?
    get() {
      try {
        val nis = NetworkInterface.getNetworkInterfaces()
        while (nis.hasMoreElements()) {
          val ni = nis.nextElement()
          // To prevent phone of xiaomi return "10.0.2.15"
          if (!ni.isUp) {
            continue
          }
          val addresses = ni.inetAddresses
          while (addresses.hasMoreElements()) {
            val inetAddress = addresses.nextElement()
            if (!inetAddress.isLoopbackAddress) {
              val hostAddress = inetAddress.hostAddress
              if (hostAddress.indexOf(':') < 0) {
                return inetAddress
              }
            }
          }
        }
      } catch (e: SocketException) {
        e.printStackTrace()
      }
      return null
    }
  private val macAddressByFile: String
    get() {
      var result: CommandResult = ShellUtils.execCmd("getprop wifi.interface", false)
      if (result.result == 0) {
        val name = result.successMsg
        if (name != null) {
          result = ShellUtils.execCmd("cat /sys/class/net/$name/address", false)
          if (result.result == 0) {
            val address = result.successMsg
            if (address != null && address.isNotEmpty()) {
              return address
            }
          }
        }
      }
      return DEFAULT_MAC_ADDRESS
    }

  /**
   * 根据用户指定的两个经纬度坐标点，计算这两个点间的直线距离，单位为米.<br></br>
   * from 高德地图 api: [
     * AMapUtils.calculateLineDistance(latLng1,latLng2)](http://lbs.amap.com/api/android-sdk/guide
     * /computing-equipment/calcute-distance-tool)
   */
  fun getDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    var var4 = lng1
    var var6 = lat1
    var var8 = lng2
    var var10 = lat2
    var4 *= 0.01745329251994329
    var6 *= 0.01745329251994329
    var8 *= 0.01745329251994329
    var10 *= 0.01745329251994329
    val var12 = sin(var4)
    val var14 = sin(var6)
    val var16 = cos(var4)
    val var18 = cos(var6)
    val var20 = sin(var8)
    val var22 = sin(var10)
    val var24 = cos(var8)
    val var26 = cos(var10)
    val var28 = DoubleArray(3)
    val var29 = DoubleArray(3)
    var28[0] = var18 * var16
    var28[1] = var18 * var12
    var28[2] = var14
    var29[0] = var26 * var24
    var29[1] = var26 * var20
    var29[2] = var22
    val var30 = sqrt(
      (var28[0] - var29[0]) * (var28[0] - var29[0])
          + (var28[1] - var29[1]) * (var28[1] - var29[1])
          + (var28[2] - var29[2]) * (var28[2] - var29[2])
    )
    val var31 = asin(var30 / 2.0) * 1.27420015798544E7
    return DecimalFormat("##.###").format(var31).toDouble()
  }
}