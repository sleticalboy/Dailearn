package com.binlee.sample.core;

import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import com.binlee.sample.util.Glog;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created on 21-2-25.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class NrfStateReader implements IComponent {

  private static final String TAG = Glog.wrapTag("StateReader");

  private static final int MAX_CHECK_COUNT = 6;
  private static final int ARRAY_CAPACITY = 2;

  private final IArchManager mArch;
  private final SparseIntArray mCheckArray = new SparseIntArray(2);
  // guarded by mStateArray
  private final SparseBooleanArray mStateArray = new SparseBooleanArray(2);
  private Timer mTimer;
  private TimerTask mTask;
  private Runnable mSlowLoopStarter;

  public NrfStateReader(IArchManager arch) {
    mArch = arch;
    for (int i = 0; i < ARRAY_CAPACITY; i++) {
      mStateArray.put(i, false);
    }
  }

  @Override
  public void onStart() {
    mTimer = new Timer(TAG);
    if (DataSource.get().getCaches().size() > 0) startLoop(false);
  }

  @Override
  public void onDestroy() {
    cancelTask();
    mTimer.cancel();
    mTimer.purge();
  }

  public void startIfNeeded() {
    startLoop(true);
  }

  private void startLoop(boolean fast) {
    cancelTask();
    mTimer.schedule(mTask = new ReadTask(this), fast ? 500L : 1000L, fast ? 500L : 1000L);
    if (!fast) return;
    if (mSlowLoopStarter == null) mSlowLoopStarter = () -> startLoop(false);
    mArch.handler().postDelayed(mSlowLoopStarter, 10000L);
  }

  private void cancelTask() {
    if (mTask == null) return;
    mTask.cancel();
    mTask = null;
  }

  private void readOnce() {
    checkLost();
  }

  private void checkLost() {
    for (int i = 0; i < MAX_CHECK_COUNT - 1; i++) {
      if (i == MAX_CHECK_COUNT - 2) {

      }
    }
  }

  private static final class ReadTask extends TimerTask {

    private boolean mCanceled = false;
    private final NrfStateReader mReader;

    public ReadTask(NrfStateReader reader) {
      mReader = reader;
    }

    @Override
    public void run() {
      if (!mCanceled && mReader != null) mReader.readOnce();
    }

    @Override
    public boolean cancel() {
      return mCanceled = super.cancel();
    }

    @Override
    public String toString() {
      return "ReadTask{mCanceled=" + mCanceled + '}';
    }
  }
}
