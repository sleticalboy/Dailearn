package com.crazyview.crazycamera2;

import android.util.Log;

/**
 * Auther: Crazy.Mo
 * DateTime: 2017/5/15 11:23
 * Summary:
 */
public class LogUtil {

  private final static boolean DEBUG = true;

  private static String getTraceInfo() {
    StringBuffer sb = new StringBuffer();

    StackTraceElement[] stacks = new Throwable().getStackTrace();
    int stacksLen = stacks.length;
    sb.append("class: ").append(stacks[1].getClassName()).append("; method: ")
      .append(stacks[1].getMethodName()).append("; number: ")
      .append(stacks[1].getLineNumber());
    String _methodName =
      new Exception().getStackTrace()[1].getMethodName();// 获得调用者的方法名

    String _thisMethodName =
      new Exception().getStackTrace()[0].getMethodName();// 获得当前的方法名
    return sb.toString();
  }

  public static void showErroLog(String messge) {
    if (DEBUG) {
      StringBuffer sb = new StringBuffer();
      StackTraceElement[] stacks = new Throwable().getStackTrace();
      if (stacks.length > 0 && stacks[1] != null) {
        Log.e(stacks[1].getClassName().toString(), stacks[1].getMethodName()
          + "cmo --->" + "##  " + messge + "  ##");
      } else {
        Log.e("cmo", "**" + messge + "##");
      }
    }
  }

  public static void showDebugLog(String messge) {
    if (DEBUG) {
      StringBuffer sb = new StringBuffer();
      StackTraceElement[] stacks = new Throwable().getStackTrace();
      if (stacks.length > 0 && stacks[1] != null) {
        Log.e(stacks[1].getClassName().toString(), stacks[1].getMethodName()
          + "cmo -->" + "**  " + messge + "  **");
      } else {
        Log.e("cmo -->", "**  " + messge + " ##");
      }
    }
  }

  public static void showModelLog(String messge) {
    if (DEBUG) {
      StringBuffer sb = new StringBuffer();
      StackTraceElement[] stacks = new Throwable().getStackTrace();
      if (stacks.length > 0 && stacks[1] != null) {
        Log.e(stacks[1].getClassName().toString(), stacks[1].getMethodName()
          + "faceapi -->" + "**  " + messge + "  **");
      } else {
        Log.e("cmo -->", "**  " + messge + " ##");
      }
    }
  }
}
