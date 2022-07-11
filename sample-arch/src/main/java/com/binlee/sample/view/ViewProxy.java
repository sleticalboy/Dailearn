package com.binlee.sample.view;

import android.os.Handler;
import android.os.Looper;
import com.binlee.sample.model.ArchDevice;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created on 21-2-23.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class ViewProxy implements IView {

  private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

  private final List<IView> mTargets;

  public ViewProxy() {
    mTargets = new CopyOnWriteArrayList<>();
  }

  public void attach(IView target) {
    if (target == null) return;
    mTargets.add(target);
  }

  public void detach(IView target) {
    if (target == null) return;
    mTargets.remove(target);
  }

  @Override
  public void onDeviceChanged(ArchDevice device, boolean removed) {
    if (mTargets.size() == 0) return;
    MAIN_HANDLER.post(() -> {
      for (final IView view : mTargets) view.onDeviceChanged(device, removed);
    });
  }

  @Override
  public void onScanTimeout() {
    if (mTargets.size() == 0) return;
    MAIN_HANDLER.post(() -> {
      for (final IView view : mTargets) view.onScanTimeout();
    });
  }

  @Override
  public void onConnectTimeout() {
    if (mTargets.size() == 0) return;
    MAIN_HANDLER.post(() -> {
      for (final IView view : mTargets) view.onConnectTimeout();
    });
  }

  @Override
  public void onClearInfo(ArchDevice device, boolean remote) {
    if (mTargets.size() == 0) return;
    MAIN_HANDLER.post(() -> {
      for (final IView view : mTargets) view.onClearInfo(device, remote);
    });
  }
}
