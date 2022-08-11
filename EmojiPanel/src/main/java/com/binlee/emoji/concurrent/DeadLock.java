package com.binlee.emoji.concurrent;

import android.os.SystemClock;
import android.util.Log;

/**
 * Created on 20-9-22.
 *
 * @author binlee sleticalboy@gmail.com
 */
final public class DeadLock {

  private static final String TAG = "DeadLock";

  private final Object mLockA, mLockB;
  private int mNumber = 100;

  private DeadLock() {
    mLockA = new Object();
    mLockB = new Object();
  }

  public static void run() {
    final DeadLock lock = new DeadLock();
    new Thread("ThreadA") {
      @Override
      public void run() {
        lock.runThreadA(getName());
      }
    }.start();
    new Thread("ThreadB") {
      @Override
      public void run() {
        lock.runThreadB(getName());
      }
    }.start();
  }

  private void runThreadA(String name) {
    Log.d(TAG, name + " is running and try to acquire mLockA");
    while (mNumber >= 0) {
      synchronized (mLockA) {
        Log.d(TAG, name + " is holding mLockA.");
        mNumber++;
        Log.d(TAG, name + " mNumber++: " + mNumber);
        SystemClock.sleep(100L);
        Log.d(TAG, name + " try to acquire mLockB");
        synchronized (mLockB) {
          Log.d(TAG, name + " is holding mLockB.");
          mNumber--;
          Log.d(TAG, name + " mNumber:--" + mNumber);
          Log.d(TAG, name + " release mLockB.");
        }
        Log.d(TAG, name + " release mLockA.");
      }
    }
  }

  private void runThreadB(String name) {
    Log.d(TAG, name + " is running and try to acquire mLockB");
    while (mNumber >= 0) {
      synchronized (mLockB) {
        Log.d(TAG, name + " is holding mLockB.");
        mNumber--;
        Log.d(TAG, name + " mNumber--: " + mNumber);
        SystemClock.sleep(100L);
        Log.d(TAG, name + " try to acquire mLockA");
        synchronized (mLockA) {
          Log.d(TAG, name + " is holding mLockA.");
          mNumber++;
          Log.d(TAG, name + " mNumber++: " + mNumber);
          SystemClock.sleep(100L);
          Log.d(TAG, name + " release mLockA.");
        }
        Log.d(TAG, name + " release mLockB.");
      }
    }
  }
}
