package com.binlee.sample.view;

import com.binlee.sample.model.ArchDevice;

/**
 * Created on 21-2-23.
 *
 * @author binlee sleticalboy@gmail.com
 */
public interface IView {

  void onDeviceChanged(ArchDevice device, boolean removed);

  void onScanTimeout();

  void onConnectTimeout();

  void onClearInfo(ArchDevice device, boolean remote);
}
