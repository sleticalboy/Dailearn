package com.binlee.sample.core;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import androidx.annotation.NonNull;
import com.binlee.sample.event.AsyncEvent;
import com.binlee.sample.event.ConnectEvent;
import com.binlee.sample.event.DisconnectEvent;
import com.binlee.sample.event.IEvent;
import com.binlee.sample.event.ScanEvent;
import com.binlee.sample.model.ArchDevice;
import com.binlee.sample.util.Glog;
import com.binlee.sample.view.IView;
import com.binlee.sample.view.ViewProxy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created on 21-2-5.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class ArchManager implements IArchManager, Handler.Callback,
  EventDispatcher.OnUnhandledCallback {

  private static final String TAG = "ArchManager";

  private final ViewProxy mViewProxy;
  private final Handler mWorker;
  private final DataSource mSource;
  private Context mContext;
  // IComponents
  private LifecycleDetector mDetector;
  private EventExecutor mEventExecutor;
  private EventCenter mObserver;
  private NrfStateReader mStateReader;
  // other component
  private EventDispatcher mEventDispatcher;
  private BleScanner mScanner;
  private ConfigAssigner mAssigner;
  // some flag
  private boolean mForeground;

  public ArchManager() {
    HandlerThread thread = new HandlerThread(TAG);
    thread.start();
    mWorker = new InjectableHandler(thread.getLooper(), this);
    mSource = DataSource.get();
    mViewProxy = new ViewProxy();
  }

  @Override
  public void onCreate(Context context) {
    mContext = context;

    initDispatchers();
    initComponents();

    mScanner = new BleScanner(mContext, mWorker);
  }

  @Override
  public Handler handler() {
    return mWorker;
  }

  @Override
  public void postEvent(IEvent event) {
    mWorker.obtainMessage(IWhat.POST_EVENT, event).sendToTarget();
  }

  @Override
  public void onStart() {
    mDetector.onStart();
    mObserver.onStart();
    mEventExecutor.onStart();
    mStateReader.onStart();
  }

  @Override
  public void onDestroy() {
    mDetector.onDestroy();
    mObserver.onDestroy();
    mEventExecutor.onDestroy();
    mStateReader.onDestroy();
  }

  @Override
  public void attachView(IView view, boolean refresh) {
    mViewProxy.attach(view);

    // in worker handler
    if (refresh) mSource.fetchCaches(mWorker);
  }

  @Override
  public void detachView(IView view, boolean abort) {
    mViewProxy.detach(view);
    if (!abort) return;
    mEventExecutor.abortAll();
  }

  @Override
  public void onUnhandled(IEvent event) {
    Glog.w(TAG, "onUnhandledEvent() " + event);
  }

  @Override
  public boolean handleMessage(@NonNull Message msg) {
    switch (msg.what) {
      case IWhat.SCAN_RESULT:
        onScanResult(((BluetoothDevice) msg.obj), msg.arg1);
        return true;
      case IWhat.SCAN_FAILED:
        onScanFailed(((String) msg.obj), msg.arg1);
        return true;
      case IWhat.BONDED_CHANGED:
        onBondedChanged(((BluetoothDevice) msg.obj), msg.arg1, msg.arg2);
        return true;
      case IWhat.GATT_CREATE_BOND:
        onGattCreateBond(((BluetoothDevice) msg.obj));
        return true;
      case IWhat.HID_PROFILE_CHANGED:
        onHidProfileChanged(((BluetoothDevice) msg.obj), msg.arg1);
        return true;
      case IWhat.LOCALE_CHANGED:
        onLocaleChanged();
        return true;
      case IWhat.GATT_START_CONFIG:
        onGattStartConfig(((BluetoothDevice) msg.obj));
        return true;
      case IWhat.CONNECT_STATUS_CHANGE:
        onConnectStatusChanged(((ConnectEvent) msg.obj), msg.arg1);
        return true;
      case IWhat.CACHE_FETCHED:
        onCacheFetched(((List<?>) msg.obj));
        return true;
      case IWhat.LIFECYCLE_CHANGE:
        onLifecycleChanged(msg.arg1 == 1);
        return true;
      case IWhat.POST_EVENT:
        onPostEvent(((IEvent) msg.obj));
        return true;
    }
    return false;
  }

  private void onCacheFetched(List<?> list) {
    for (final Object obj : list) {
      ArchDevice device = (ArchDevice) obj;
      mViewProxy.onDeviceChanged(device, false);
    }
  }

  private void onPostEvent(IEvent event) {
    if (event instanceof AsyncEvent && !prepareEvent(((AsyncEvent) event))) return;
    mEventDispatcher.deliver(event);
  }

  private void onLifecycleChanged(boolean foreground) {
    mForeground = foreground;
    Glog.v(TAG, "onLifecycleChanged() foreground: " + foreground);
  }

  private void onConnectStatusChanged(ConnectEvent event, int status) {
    if (status == AsyncEvent.STATUS_CONFIG_OVER) {
      //
    }
  }

  private void onGattStartConfig(BluetoothDevice ble) {
    DataSource.Record r = mSource.getRecord(ble.getAddress());
    if (r == null || !(r.mEvent instanceof ConnectEvent)) return;

    if (mAssigner == null) mAssigner = new ConfigAssigner();
    if (mAssigner.assign(r.mDevice)) {
      if (((ConnectEvent) r.mEvent).startConfig(0, "a00248")) {
        Glog.v(TAG, "onGattStartConfig() ...");
      }
    }
  }

  private void onLocaleChanged() {
    // 更新 dialog 等
  }

  private void onHidProfileChanged(BluetoothDevice ble, int state) {
    if (state != BluetoothProfile.STATE_CONNECTED || ble == null) return;
    String name = ble.getName();
    int type = ble.getType();
    if (type != BluetoothDevice.DEVICE_TYPE_LE) return;
    DataSource.Record r = mSource.getRecord(ble.getAddress());
    if (r == null || r.mEvent == null) return;
    if (r.mEvent instanceof ConnectEvent) {
      ((ConnectEvent) r.mEvent).connectGatt();
    }
  }

  private void onGattCreateBond(BluetoothDevice ble) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      ble.createBond();
      return;
    }
    try {
      Method m = BluetoothDevice.class.getDeclaredMethod("createBond", int.class);
      m.setAccessible(true);
      m.invoke(ble, BluetoothDevice.TRANSPORT_LE);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  private void onBondedChanged(BluetoothDevice ble, int state, int reason) {
    if (state == BluetoothDevice.BOND_BONDED) {
      //
    } else if (state == BluetoothDevice.BOND_NONE) {
      // check reason
    }
  }

  private void onScanResult(BluetoothDevice ble, int arg1) {
    int type;
    if (arg1 == IEvent.ULTRA_SCAN) {
      type = IEvent.ULTRA_CONNECT;
    } else if (arg1 == IEvent.USB_SCAN) {
      type = IEvent.USB_CONNECT;
    } else if (arg1 == IEvent.REBOOT_SCAN) {
      type = IEvent.REBOOT_CONNECT;
    } else {
      return;
    }
    onPostEvent(new ConnectEvent(ble, type));
  }

  private void onScanFailed(String reason, int error) {
    Glog.w(TAG, "onScanFailed() " + reason + ", " + error);
  }

  private void initComponents() {
    mDetector = new LifecycleDetector(mContext, mWorker);
    mEventExecutor = new EventExecutor();
    mObserver = new EventCenter(mContext, mWorker);
    mStateReader = new NrfStateReader(this);
  }

  private void initDispatchers() {
    EventDispatcher handler = new AsyncEventDispatcher();
    handler.setOnUnhandledCallback(this);

    handler = new ScanEventDispatcher(handler);
    handler.setOnUnhandledCallback(this);
    mEventDispatcher = handler;
  }

  private boolean prepareEvent(AsyncEvent event) {
    DataSource.Record r = mSource.getRecord(event.target().getAddress());
    if (r != null && event.equals(r.mEvent)) return false;

    if (r == null) mSource.put(new DataSource.Record(new ArchDevice(event.target())));

    if (event instanceof DisconnectEvent) {
      setFlags();
    } else if (event instanceof ConnectEvent) {
      setFlags();
    }
    event.setContext(mContext);
    event.setHandler(mWorker);
    return true;
  }

  private void setFlags() {
  }

  // 处理扫描事件
  private final class ScanEventDispatcher extends EventDispatcher {

    private ScanEventDispatcher(EventDispatcher next) {
      super(next);
    }

    @Override
    public boolean onProcess(IEvent event) {
      if (!(event instanceof ScanEvent)) return false;
      mScanner.start(((ScanEvent) event));
      return true;
    }
  }

  // 处理连接 && 断开事件
  private final class AsyncEventDispatcher extends EventDispatcher {

    public AsyncEventDispatcher() {
      super(null);
    }

    @Override
    protected boolean onProcess(IEvent event) {
      if (!(event instanceof AsyncEvent)) return false;
      execEvent(((AsyncEvent) event));
      return true;
    }

    private void execEvent(AsyncEvent event) {
      if (mEventExecutor.submit(event) && event instanceof ConnectEvent) {
        // start fast loop read
        mStateReader.startIfNeeded();
      }
    }
  }
}
