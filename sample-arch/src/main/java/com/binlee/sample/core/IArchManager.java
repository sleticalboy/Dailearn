package com.binlee.sample.core;

import android.content.Context;
import android.os.Handler;
import com.binlee.sample.event.IEvent;
import com.binlee.sample.view.IView;

/**
 * Created on 21-2-4.
 *
 * @author binlee sleticalboy@gmail.com
 */
public interface IArchManager {

  void onCreate(Context context);

  void onStart();

  void onDestroy();

  Handler handler();

  void postEvent(IEvent event);

  void attachView(IView view, boolean refresh);

  void detachView(IView view, boolean abort);
}
