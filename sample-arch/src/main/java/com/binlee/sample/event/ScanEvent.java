package com.binlee.sample.event;

/**
 * Created on 21-2-7.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class ScanEvent implements IEvent {

  @Type
  private final int mType;

  public ScanEvent(@Type int type) {
    mType = type;
  }

  @Override
  public int type() {
    return mType;
  }

  public boolean useUltra() {
    return mType == ULTRA_SCAN;
  }

  public long duration() {
    return mType == ULTRA_SCAN ? 10000L : mType == REBOOT_SCAN ? 6000L : 0L;
  }

  public long interval() {
    return mType == ULTRA_SCAN ? 10000L : 0L;
  }

  public int repetition() {
    return mType == ULTRA_SCAN || mType == USB_SCAN ? 2 : 0;
  }
}
