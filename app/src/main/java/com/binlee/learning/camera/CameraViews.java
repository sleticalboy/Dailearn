package com.binlee.learning.camera;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.widget.ListPopupWindow;
import com.binlee.learning.camera.v1.CameraV1;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created on 2023/6/27
 *
 * @author binlee
 */
public final class CameraViews {

  private static final String TAG = "CameraViews";

  private static Method sBuildDropDownMethod;

  private CameraViews() {
    //no instance
  }

  private static boolean ensureBuilt() {
    if (sBuildDropDownMethod != null) return true;
    try {
      final Method method = ListPopupWindow.class.getDeclaredMethod("buildDropDown");
      method.setAccessible(true);
      sBuildDropDownMethod = method;
      return true;
    } catch (NoSuchMethodException e) {
      sBuildDropDownMethod = null;
    }
    return false;
  }

  // Toast.makeText(this, "Open Exposure!", Toast.LENGTH_SHORT).show()
  // 曝光
  // exposure-compensation=0
  // max-exposure-compensation=12
  // min-exposure-compensation=-12
  // exposure-compensation-step=0.166667
  // auto-exposure-lock=false
  // auto-exposure-lock-supported=true
  public static void showExposure(Context context, View anchor, CameraV1 camera) {
    final Camera.Parameters params = camera.getInitialParams();
    if (params == null) return;
    final List<String> items = params.getSupportedWhiteBalance();
    final String value = camera.getWhiteBalance();
    int selected = items.indexOf(value);
    Log.d(TAG, "showWhiteBalances() flash mode: " + value + ", selected: " + selected);
    final ListPopupWindow popView = createPopView(context, anchor, items, selected, position -> {
      camera.setWhiteBalance(items.get(position));
    });
    if (popView != null) popView.show();
  }

  // 白平衡
  // auto-whitebalance-lock=false
  // auto-whitebalance-lock-supported=true
  public static void showWhiteBalance(Context context, View anchor, CameraV1 camera) {
    final Camera.Parameters params = camera.getInitialParams();
    if (params == null) return;
    final List<String> items = params.getSupportedWhiteBalance();
    final String value = camera.getWhiteBalance();
    int selected = items.indexOf(value);
    Log.d(TAG, "showWhiteBalances() flash mode: " + value + ", selected: " + selected);
    final ListPopupWindow popView = createPopView(context, anchor, items, selected, position -> {
      camera.setWhiteBalance(items.get(position));
    });
    if (popView != null) {
      popView.setContentWidth(anchor.getWidth() * 2);
      popView.show();
    }
  }

  // 闪光模式
  // flash-mode=off
  // flash-mode-values=off,auto,on
  public static void showFlashMode(Context context, View anchor, CameraV1 camera) {
    final Camera.Parameters params = camera.getInitialParams();
    if (params == null) return;
    final List<String> items = params.getSupportedFlashModes();
    final String mode = camera.getFlashMode();
    int selected = items.indexOf(mode);
    Log.d(TAG, "showFlashModes() flash mode: " + mode + ", selected: " + selected);
    final ListPopupWindow popView = createPopView(context, anchor, items, selected, position -> {
      camera.setFlashMode(items.get(position));
    });
    if (popView != null) {
      popView.setContentWidth((int) (anchor.getWidth() * 1.2));
      popView.show();
    }
  }

  private static ListPopupWindow createPopView(Context context, View anchor, List<String> items,
    int pos, OnItemClickListener l) {
    ListPopupWindow window = new ListPopupWindow(context);
    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    window.setAnchorView(anchor);
    window.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_checked, items));
    window.setOnItemClickListener((parent, view, position, id) -> {
      if (l != null) l.onItemClick(position);
      parent.postDelayed(window::dismiss, 500L);
    });
    if (ensureBuilt()) {
      try {
        sBuildDropDownMethod.invoke(window);
      } catch (IllegalAccessException | InvocationTargetException e) {
        return null;
      }
      final ListView listView = window.getListView();
      if (listView != null) {
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        if (pos >= 0) listView.setItemChecked(pos, true);
      }
    }
    return window;
  }

  private interface OnItemClickListener {
    void onItemClick(int position);
  }
}
