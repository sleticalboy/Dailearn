package com.binlee.dl.plugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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

  private enum Command {
    START, STOP, BIND, UNBIND
  }

  private enum Result {
    STARTED, STOPPED, BOUND, UNBOUND, NONE
  }

  private static final ServiceQueue sQueue = new ServiceQueue();

  public static void start(Context context, ComponentName target) {
    sQueue.enqueue(new RunnerSpec(context, target, Command.START));
  }

  public static void stop(Context context, ComponentName target) {
    sQueue.enqueue(new RunnerSpec(context, target, Command.STOP));
  }

  public static void bind(Context context, ComponentName target, ServiceConnection connection) {
    final RunnerSpec bind = new RunnerSpec(context, target, Command.BIND);
    bind.mConnection = connection;
    sQueue.enqueue(bind);
  }

  public static void unbind(Context context, ComponentName target, ServiceConnection connection) {
    final RunnerSpec unbind = new RunnerSpec(context, target, Command.UNBIND);
    unbind.mConnection = connection;
    sQueue.enqueue(unbind);
  }

  public static void scheduleNext() {
    sQueue.scheduleNext();
  }

  public static String currentName() {
    final RunnerSpec spec = sQueue.getCurrent();
    return spec == null ? null : spec.mTarget.getClassName();
  }

  private static class ServiceQueue {

    private static final String TAG = "ServiceQueue";

    private final List<RunnerSpec> mServices = new ArrayList<RunnerSpec>() {
      @Override public boolean add(RunnerSpec runner) {
        return !contains(runner) && super.add(runner);
      }
    };
    // 已绑定的 service
    private final List<ServiceConnection> mConnections = new ArrayList<>();
    private RunnerSpec mCurrent;

    void enqueue(RunnerSpec spec) {
      spec.throwIfInvalid();
      if (spec.mCommand == Command.BIND && mConnections.contains(spec.mConnection)) {
        Log.w(TAG, "enqueue() already bound: " + spec);
        return;
      }
      if (spec.mCommand == Command.UNBIND && !mConnections.contains(spec.mConnection)) {
        Log.w(TAG, "enqueue() already unbound: " + spec);
        return;
      }
      mServices.add(spec);
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
        final Result result = mCurrent.execute();
        if (result == Result.BOUND) {
          mConnections.add(mCurrent.mConnection);
        } else if (result == Result.UNBOUND) {
          mConnections.remove(mCurrent.mConnection);
        }
        Log.d(TAG, "tryExecuteNext() result: " + result);
      }
    }

    private void dump() {
      Log.wtf("ServiceQueue", "mCurrent=" + mCurrent
        + ", mServices=" + mServices
        + ", mConnections=" + mConnections
      );
    }
  }

  private static class RunnerSpec {

    private static final String TAG = "RunnerSpec";

    private final Command mCommand;
    private final Context mContext;
    private final ComponentName mTarget;
    private ServiceConnection mConnection;

    private RunnerSpec(Context context, ComponentName target, Command command) {
      mContext = context;
      mTarget = target;
      mCommand = command;
    }

    Result execute() {
      final Intent service = new Intent(mContext, ProxyService.class)
        .putExtra(DlConst.REAL_COMPONENT, mTarget);
      switch (mCommand) {
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
          Log.d(TAG, "run() invalid command: " + mCommand);
          return Result.NONE;
      }
    }

    void throwIfInvalid() {
      if ((mCommand == Command.BIND || mCommand == Command.UNBIND) && mConnection == null) {
        throw new IllegalArgumentException("connection is null");
      }
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof RunnerSpec)) return false;
      RunnerSpec that = (RunnerSpec) o;
      return mCommand == that.mCommand &&
        Objects.equals(mTarget, that.mTarget) &&
        Objects.equals(mContext, that.mContext);
    }

    @Override public int hashCode() {
      return Objects.hash(mCommand, mTarget, mContext);
    }

    @NonNull @Override public String toString() {
      return "RunnerSpec{" +
        "mCommand=" + mCommand +
        ", mContext=" + mContext +
        ", mTarget=" + mTarget +
        '}';
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
