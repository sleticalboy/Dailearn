package com.binlee.sample.util;

import android.util.Log;

/**
 * Created on 21-2-4.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class Glog {

  private static Config sConfig;

  private Glog() {
    sConfig = new Config(true, true, true);
  }

  public static final class Config {

    private final boolean mVerbose;
    private final boolean mDebug;
    private final boolean mInfo;

    public Config(boolean verbose, boolean debug, boolean info) {
      mVerbose = verbose;
      mDebug = debug;
      mInfo = info;
    }
  }

  public static void setConfig(Config config) {
    if (config != null) sConfig = config;
  }

  public static boolean isLoggable() {
    return sConfig.mVerbose && (sConfig.mDebug || sConfig.mInfo);
  }

  public static void v(String tag, String msg) {
    if (sConfig.mVerbose) log(tag, msg, Log.VERBOSE);
  }

  public static void d(String tag, String msg) {
    if (sConfig.mDebug) log(tag, msg, Log.DEBUG);
  }

  public static void i(String tag, String msg) {
    if (sConfig.mInfo) log(tag, msg, Log.INFO);
  }

  public static void w(String tag, String msg) {
    log(tag, msg, Log.WARN);
  }

  public static void w(String tag, String msg, Throwable t) {
    if (t != null) {
      log(tag, msg + "\n" + Log.getStackTraceString(t), Log.WARN);
    } else {
      log(tag, msg, Log.WARN);
    }
  }

  public static void e(String tag, String msg) {
    log(tag, msg, Log.ERROR);
  }

  public static void e(String tag, String msg, Throwable t) {
    if (t != null) {
      log(tag, msg + "\n" + Log.getStackTraceString(t), Log.ERROR);
    } else {
      log(tag, msg, Log.ERROR);
    }
  }

  public static String wrapTag(String tag) {
    return !tag.startsWith("Arch-") ? String.format("Arch-%s", tag) : tag;
  }

  private static void log(String tag, String msg, int priority) {
    Log.println(priority, tag, msg);
  }
}
