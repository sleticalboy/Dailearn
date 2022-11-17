package com.example.fileserver;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created on 2022/10/25
 *
 * @author binlee
 */
public final class FileUtil {

  private FileUtil() {
    //no instance
  }

  public static String getWifiLabel(Context context) {
    WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
    if (wifiInfo == null) return "无";
    Log.d("FileUtil", "getWifiLabel() " + wifiInfo.getSSID());
    Log.d("FileUtil", "getWifiLabel() " + wifiInfo);
    String ssid = wifiInfo.getSSID();
    if (ssid.contains("unknown")) {
      ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      final NetworkInfo info = mgr.getActiveNetworkInfo();
      if (info != null && info.isConnected()) {
        ssid = info.getExtraInfo();
      }
    }
    Log.d("FileUtil", "getWifiLabel() " + ssid);
    return ssid == null ? "无" : ssid;
  }


  public static String lookupIpAddress(Context context) {
    ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    final NetworkInfo info = mgr.getActiveNetworkInfo();
    if (info != null && info.isConnected()) {
      if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
        Enumeration<NetworkInterface> interfaces;
        try {
          interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
          return null;
        }
        while (interfaces.hasMoreElements()) {
          NetworkInterface element = interfaces.nextElement();
          Enumeration<InetAddress> addrs = element.getInetAddresses();
          while (addrs.hasMoreElements()) {
            InetAddress addr = addrs.nextElement();
            if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
              return addr.getHostAddress();
            }
          }
        }
      } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Service.WIFI_SERVICE);
        return intIP2StringIP(wifiMgr.getConnectionInfo().getIpAddress());
      }
    }
    return null;
  }

  private static String intIP2StringIP(int ip) {
    return (ip & 0xFF) + "." +
      ((ip >> 8) & 0xFF) + "." +
      ((ip >> 16) & 0xFF) + "." +
      (ip >> 24 & 0xFF);
  }

  public static JSONObject success(JSONArray data) throws JSONException {
    final JSONObject json = new JSONObject();
    json.put("errorCode", 200);
    json.put("errorMsg", null);
    json.put("data", data);
    json.put("isSuccess", true);
    return json;
  }

  public static JSONObject error(String msg) throws JSONException {
    final JSONObject json = new JSONObject();
    json.put("errorCode", 404);
    json.put("errorMsg", msg);
    json.put("data", null);
    json.put("isSuccess", false);
    return json;
  }
}
