package com.binlee.sample.event;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;

/**
 * Created on 21-2-4.
 *
 * @author binlee sleticalboy@gmail.com
 */
public interface AsyncEvent extends Runnable, IEvent {

  // connect status
  int STATUS_CONNECTED = 0x01;
  int STATUS_NOT_CONNECTED = 0x02;
  int STATUS_CONNECTING = 0x03;
  int STATUS_DISCONNECTING = 0x04;
  int STATUS_CONFIG_START = 0x05;
  int STATUS_CONFIG_SECOND = 0x06;
  int STATUS_CONFIG_OVER = 0x07;

  // finish reason
  int REASON_CONNECT_DONE = 0x01;
  int REASON_DISCONNECT_DONE = 0x02;
  int REASON_TIMEOUT = 0x03;
  int REASON_FAILED = 0x04;
  int REASON_ABORTED = 0x05;

  BluetoothDevice target();

  boolean isFinished();

  void onFinish(int reason);

  @Override
  int type();

  @Override
  void run();

  default void setContext(Context context) {
  }

  default void setHandler(Handler handler) {
  }

  default String getStatus(int status) {
    if (status == STATUS_CONNECTED) return String.format("STATUS_CONNECTED(%02x)", status);
    if (status == STATUS_NOT_CONNECTED) return String.format("STATUS_NOT_CONNECTED(%02x)", status);
    if (status == STATUS_CONNECTING) return String.format("STATUS_CONNECTING(%02x)", status);
    if (status == STATUS_DISCONNECTING) return String.format("STATUS_DISCONNECTING(%02x)", status);
    if (status == STATUS_CONFIG_START) return String.format("STATUS_CONFIG_START(%02x)", status);
    if (status == STATUS_CONFIG_OVER) return String.format("STATUS_CONFIG_OVER(%02x)", status);
    return String.format("Unknown status(%02x)", status);
  }

  default String getReason(int reason) {
    if (reason == REASON_CONNECT_DONE) return String.format("REASON_CONNECT_DONE(%02x)", reason);
    if (reason == REASON_DISCONNECT_DONE) {
      return String.format("REASON_DISCONNECT_DONE(%02x)", reason);
    }
    if (reason == REASON_TIMEOUT) return String.format("REASON_TIMEOUT(%02x)", reason);
    if (reason == REASON_FAILED) return String.format("REASON_FAILED(%02x)", reason);
    if (reason == REASON_ABORTED) return String.format("REASON_ABORTED(%02x)", reason);
    return String.format("Unknown status(%02x)", reason);
  }
}
