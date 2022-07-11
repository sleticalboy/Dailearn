package com.binlee.sample.core;

import com.binlee.sample.event.AsyncEvent;
import com.binlee.sample.util.Glog;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created on 21-2-4.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class EventExecutor extends Thread implements IComponent {

  private static final String TAG = "Dispatcher";

  private static final int CAPACITY = 2;

  private final ArrayBlockingQueue<AsyncEvent> mTasks = new ArrayBlockingQueue<>(CAPACITY);
  private final AtomicBoolean mReleased;
  private AsyncEvent mEvent;

  public EventExecutor() {
    mReleased = new AtomicBoolean(false);
  }

  @Override
  public void onStart() {
    if (!mReleased.getAndSet(false)) super.start();
  }

  @Override
  public void onDestroy() {
    mReleased.getAndSet(true);
    interrupt();
  }

  @Override
  public void run() {
    while (!mReleased.get() && !isInterrupted()) {
      AsyncEvent event = null;
      try {
        event = mTasks.take();
      } catch (InterruptedException e) {
        Glog.e(TAG, "run() take error.", e);
      }
      if (event == null) continue;
      Glog.v(TAG, "run() take and exec " + event + " from queue");
      mEvent = event;
      try {
        event.run();
      } catch (Throwable e) {
        Glog.e(TAG, "exec event " + event.action() + " error");
      }
      Glog.v(TAG, "run() wait " + event.action() + " 40s for timeout...");
      synchronized (mTasks) {
        try {
          mTasks.wait(40000L);
        } catch (InterruptedException e) {
          Glog.e(TAG, "queue wait error.", e);
        }
      }
      if (event.equals(mEvent) && !event.isFinished()) {
        Glog.v(TAG, "run() after 40s, " + event.action() + " timeout...");
        event.onFinish(AsyncEvent.REASON_TIMEOUT);
      }
    }
  }

  public boolean submit(AsyncEvent event) {
    if (event == null || mEvent.equals(event) || mTasks.contains(event)) {
      return false;
    }
    try {
      mTasks.put(event);
      return true;
    } catch (InterruptedException e) {
      Glog.e(TAG, "submit() error.", e);
    }
    return false;
  }

  public void finish(AsyncEvent event, int reason) {
    if (event == null || mEvent == null) return;
    if (event.equals(mEvent) && !event.isFinished()) {
      synchronized (mTasks) {
        event.onFinish(reason);
        mEvent = null;
        mTasks.notifyAll();
      }
    }
  }

  public void abortAll() {
    finish(mEvent, AsyncEvent.REASON_ABORTED);
    mTasks.clear();
  }
}
