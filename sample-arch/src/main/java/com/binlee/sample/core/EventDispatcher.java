package com.binlee.sample.core;

import com.binlee.sample.event.IEvent;

/**
 * Created on 21-2-5.
 *
 * @author binlee sleticalboy@gmail.com
 */
public abstract class EventDispatcher {

  private final EventDispatcher mNext;
  private OnUnhandledCallback mCallback;

  public EventDispatcher(EventDispatcher next) {
    mNext = next;
  }

  public final void deliver(IEvent event) {
    EventDispatcher next = this;
    while (next != null) {
      if (next.onProcess(event)) {
        return;
      }
      next = next.mNext;
    }
    if (mCallback != null) {
      mCallback.onUnhandled(event);
    }
  }

  public final void setOnUnhandledCallback(OnUnhandledCallback callback) {
    mCallback = callback;
  }

  protected abstract boolean onProcess(IEvent event);

  public interface OnUnhandledCallback {

    void onUnhandled(IEvent event);
  }
}
