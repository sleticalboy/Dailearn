package com.binlee.dl.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import androidx.annotation.NonNull;
import com.binlee.dl.DlConst;
import com.binlee.dl.proxy.ProxyService;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Created on 2022/9/28
 *
 * @author binlee
 */
public final class DlServices {

  public enum Action {
    START, STOP, BIND, UNBIND
  }

  public enum Result {
    STARTED, STOPPED, BOUND, UNBOUND, NONE
  }

  public interface IMonitor {
    void onResult(ComponentName component, Action action, Result result);
  }

  private static final ServiceQueue sQueue = new ServiceQueue();
  private static IMonitor sMonitor;

  private DlServices() {
    //no instance
  }

  ///////////////////////////////////////////////////////////////////////////
  // public api
  ///////////////////////////////////////////////////////////////////////////

  public static void setMonitor(IMonitor monitor) {
    sMonitor = monitor;
  }

  public static void start(Context context, ComponentName target) {
    sQueue.enqueue(new Service(context, target, Action.START));
  }

  public static void stop(Context context, ComponentName target) {
    sQueue.enqueue(new Service(context, target, Action.STOP));
  }

  public static void bind(Context context, ComponentName target, ServiceConnection connection) {
    final Service bind = new Service(context, target, Action.BIND);
    bind.mConnection = connection;
    sQueue.enqueue(bind);
  }

  public static void unbind(Context context, ComponentName target, ServiceConnection connection) {
    final Service unbind = new Service(context, target, Action.UNBIND);
    unbind.mConnection = connection;
    sQueue.enqueue(unbind);
  }

  ///////////////////////////////////////////////////////////////////////////
  // internal api
  ///////////////////////////////////////////////////////////////////////////

  public static void scheduleNext() {
    sQueue.scheduleNext();
  }

  public static String currentName() {
    final Service spec = sQueue.getCurrent();
    return spec == null ? null : spec.mTarget.getClassName();
  }

  private static class ServiceQueue {

    private static final String TAG = "ServiceQueue";

    private final Deque<Service> mServices = new LinkedList<Service>() {
      @Override public boolean add(Service runner) {
        return !contains(runner) && super.add(runner);
      }
    };
    // 已绑定的 service
    private final List<ServiceConnection> mConnections = new ArrayList<>();
    private Service mCurrent;

    void enqueue(Service spec) {
      spec.throwIfInvalid();
      if (spec.mAction == Action.BIND && mConnections.contains(spec.mConnection)) {
        Log.w(TAG, "enqueue() already bound: " + spec);
        return;
      }
      if (spec.mAction == Action.UNBIND && !mConnections.contains(spec.mConnection)) {
        Log.w(TAG, "enqueue() already unbound: " + spec);
        return;
      }
      mServices.addLast(spec);
      tryExecuteNext();
    }

    void scheduleNext() {
      mCurrent = null;
      tryExecuteNext();
    }

    Service getCurrent() {
      return mCurrent;
    }

    private void tryExecuteNext() {
      dump("before executing ");
      if (mServices.size() > 0 && mCurrent == null) {
        mCurrent = mServices.removeFirst();
        final Result result = mCurrent.execute();
        if (result == Result.BOUND) {
          mConnections.add(mCurrent.mConnection);
        } else if (result == Result.UNBOUND) {
          mConnections.remove(mCurrent.mConnection);
        }
        Log.d(TAG, "tryExecuteNext() result: " + result);
        if (sMonitor != null) {
          sMonitor.onResult(mCurrent.mTarget, mCurrent.mAction, result);
        }
      }
      dump("after executing ");
    }

    private void dump(String prefix) {
      if (mServices.size() != 0) {
        Log.wtf("ServiceQueue", prefix + "current=" + mCurrent
          + ", services=" + mServices
          + ", connections=" + mConnections
        );
      }
    }
  }

  private static class Service {

    private static final String TAG = "Service";

    private final Action mAction;
    private final Context mContext;
    private final ComponentName mTarget;
    private ServiceConnection mConnection;

    private Service(Context context, ComponentName target, Action action) {
      mContext = context;
      mTarget = target;
      mAction = action;
    }

    Result execute() {
      final Intent service = new Intent(mContext, ProxyService.class)
        .putExtra(DlConst.REAL_COMPONENT, mTarget);
      switch (mAction) {
        case START:
          final ComponentName component = mContext.startService(service);
          Log.d(TAG, "startService() component: " + component);
          return Result.STARTED;
        case BIND:
          final boolean bound = mContext.bindService(service, mConnection, Context.BIND_AUTO_CREATE);
          Log.d(TAG, "bindService() bound: " + bound + ", connection: " + mConnection);
          return Result.BOUND;
        case STOP:
          final boolean stopped = mContext.stopService(service);
          Log.d(TAG, "stopService() stopped: " + stopped);
          return Result.STOPPED;
        case UNBIND:
          mContext.unbindService(mConnection);
          Log.d(TAG, "unbindService() connection: " + mConnection);
          return Result.UNBOUND;
        default:
          Log.d(TAG, "run() invalid action: " + mAction);
          return Result.NONE;
      }
    }

    void throwIfInvalid() {
      if (mConnection == null && (mAction == Action.BIND || mAction == Action.UNBIND)) {
        throw new IllegalArgumentException("connection is null when action: " + mAction);
      }
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Service)) return false;
      Service that = (Service) o;
      return mAction == that.mAction &&
        Objects.equals(mTarget, that.mTarget) &&
        Objects.equals(mContext, that.mContext);
    }

    @Override public int hashCode() {
      return Objects.hash(mAction, mTarget, mContext);
    }

    @NonNull @Override public String toString() {
      return "Service{" +
        "action=" + mAction +
        ", context=" + mContext +
        ", target=" + mTarget +
        '}';
    }
  }
}
