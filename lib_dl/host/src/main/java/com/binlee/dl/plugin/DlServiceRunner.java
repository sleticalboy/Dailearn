package com.binlee.dl.plugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.NonNull;
import com.binlee.dl.DlConst;
import com.binlee.dl.host.proxy.ProxyService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created on 2022/9/28
 *
 * @author binlee
 */
public final class DlServiceRunner {

  private static final ServiceQueue SERVICE_QUEUE = new ServiceQueue();

  public static void start(Context context, ComponentName target) {
    SERVICE_QUEUE.enqueue(new RunnerSpec(context, target, RunnerSpec.CMD_START));
  }

  public static void stop(Context context, ComponentName target) {
    SERVICE_QUEUE.enqueue(new RunnerSpec(context, target, RunnerSpec.CMD_STOP));
  }

  public static void bind(Context context, ComponentName target, ServiceConnection connection) {
    final RunnerSpec bind = new RunnerSpec(context, target, RunnerSpec.CMD_BIND);
    bind.mConnection = connection;
    SERVICE_QUEUE.enqueue(bind);
  }

  public static void unbind(Context context, ComponentName target, ServiceConnection connection) {
    final RunnerSpec unbind = new RunnerSpec(context, target, RunnerSpec.CMD_UNBIND);
    unbind.mConnection = connection;
    SERVICE_QUEUE.enqueue(unbind);
  }

  public static void scheduleNext() {
    SERVICE_QUEUE.scheduleNext();
  }

  public static String currentService() {
    final RunnerSpec spec = SERVICE_QUEUE.getCurrent();
    return spec == null ? null : spec.mTarget.getClassName();
  }

  private static class ServiceQueue {

    private final List<RunnerSpec> mServices = new ArrayList<RunnerSpec>() {
      @Override public boolean add(RunnerSpec runner) {
        return !contains(runner) && super.add(runner);
      }
    };
    private RunnerSpec mCurrent;

    void enqueue(RunnerSpec item) {
      mServices.add(item);
      tryExecuteNext();
    }

    void scheduleNext() {
      mCurrent = null;
      tryExecuteNext();
    }

    RunnerSpec getCurrent() {
      return mCurrent;
    }

    private void tryExecuteNext() {
      dump();
      if (mServices.size() > 0 && mCurrent == null) {
        mCurrent = mServices.remove(0);
        mCurrent.execute();
      }
    }

    private void dump() {
      Log.wtf("ServiceQueue", "mCurrent=" + mCurrent + ", mServices=" + mServices);
    }
  }

  private static class RunnerSpec implements ServiceConnection {

    private static final String TAG = "RunnerSpec";

    private static final int CMD_START = 0x01;
    private static final int CMD_STOP = 0x02;
    private static final int CMD_BIND = 0x03;
    private static final int CMD_UNBIND = 0x04;

    private final int mCmd;
    private final Context mContext;
    private final ComponentName mTarget;
    private ServiceConnection mConnection;

    private RunnerSpec(Context context, ComponentName target, int cmd) {
      mContext = context;
      mTarget = target;
      mCmd = cmd;
    }

    void execute() {
      final Intent service = new Intent(mContext, ProxyService.class)
        .putExtra(DlConst.REAL_COMPONENT, mTarget);
      switch (mCmd) {
        case CMD_START:
          final ComponentName component = mContext.startService(service);
          Log.d(TAG, "startService() component: " + component);
          break;
        case CMD_BIND:
          final boolean bound = mContext.bindService(service, this, Context.BIND_AUTO_CREATE);
          Log.d(TAG, "bindService() bound: " + bound + ", connection: " + this);
          break;
        case CMD_STOP:
          final boolean stopped = mContext.stopService(service);
          Log.d(TAG, "stopService() stopped: " + stopped);
          break;
        case CMD_UNBIND:
          mContext.unbindService(this);
          Log.d(TAG, "unbindService() connection: " + this);
          break;
        default:
          Log.d(TAG, "run() invalid cmd: " + mCmd);
          break;
      }
    }

    @Override public void onServiceConnected(ComponentName name, IBinder service) {
      Log.d(TAG, "onServiceConnected() called with: name = [" + name + "], service = [" + service + "]");
      if (mConnection != null) {
        mConnection.onServiceConnected(name, service);
      }
    }

    @Override public void onServiceDisconnected(ComponentName name) {
      Log.d(TAG, "onServiceDisconnected() called with: name = [" + name + "]");
      if (mConnection != null) {
        mConnection.onServiceDisconnected(name);
      }
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof RunnerSpec)) return false;
      RunnerSpec that = (RunnerSpec) o;
      return mCmd == that.mCmd &&
        Objects.equals(mTarget, that.mTarget) &&
        Objects.equals(mContext, that.mContext);
    }

    @Override public int hashCode() {
      return Objects.hash(mCmd, mTarget, mContext);
    }

    @NonNull @Override public String toString() {
      return "RunnerSpec{" +
        "mCmd=" + cmdString(mCmd) +
        ", mContext=" + mContext +
        ", mTarget=" + mTarget +
        '}';
    }

    private static String cmdString(int cmd) {
      if (cmd == CMD_START) return "CMD_START";
      if (cmd == CMD_STOP) return "CMD_STOP";
      if (cmd == CMD_BIND) return "CMD_BIND";
      if (cmd == CMD_UNBIND) return "CMD_UNBIND";
      return String.format("Invalid cmd: 0x%x", cmd);
    }
  }

  public interface Callbacks {
    default void onServiceStarted(ComponentName component) {
    }

    default void onServiceStopped(ComponentName component) {
    }

    default void onServiceBound(ComponentName component) {
    }

    default void onServiceUnbound(ComponentName component) {
    }
  }
}
