package com.binlee.learning.http.download;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;
import java.util.Objects;

/**
 * Created on 18-9-3.
 *
 * @author leebin
 */
public abstract class DownloadTask implements Comparable<DownloadTask> {

  public static final String IDLE = "custom_state_idle";
  public static final String PAUSED = "custom_state_paused";
  public static final String CANCELED = "custom_state_canceled";

  @StringDef({ IDLE, PAUSED, CANCELED })
  public @interface CustomState {
  }

  private final DownloadCallback mDownloadCallback;
  @CustomState
  private volatile String mState = IDLE;
  private int mSequence;
  private DownloadTaskQueue mDownloadQueue;

  protected DownloadTask(DownloadCallback downloadCallback) {
    mDownloadCallback = downloadCallback;
  }

  public void pause() {
    mState = PAUSED;
  }

  public void cancel() {
    mState = CANCELED;
  }

  public void download() {
    if (Objects.equals(mState, CANCELED)) {
      mDownloadCallback.onComplete();
      return;
    } else if (Objects.equals(mState, PAUSED)) {
      mDownloadCallback.onPause();
    }
    mDownloadCallback.onStart(0L);
    if (Objects.equals(mState, CANCELED)) {
      mDownloadCallback.onComplete();
    } else if (Objects.equals(mState, PAUSED)) {
      mDownloadCallback.onPause();
    } else {
      mDownloadCallback.onStart(0L);
    }
  }

  @CustomState
  public String getState() {
    return mState;
  }

  public boolean isCanceled() {
    return Objects.equals(mState, CANCELED) || Objects.equals(mState, PAUSED);
  }

  @Override
  public int compareTo(@NonNull DownloadTask another) {
    int left = this.getPriority();
    int right = another.getPriority();
    // High-priority requests are "lesser" so they are sorted to the front.
    // Equal priorities are sorted by sequence number to provide FIFO ordering.
    return left == right ?
      this.mSequence - another.mSequence :
      right - left;
  }

  protected abstract int getPriority();

  protected void finish() {
    if (mDownloadQueue != null) {
      mDownloadQueue.finishTask(this);
    }
  }

  public void setDownloadQueue(DownloadTaskQueue queue) {
    mDownloadQueue = queue;
  }

  public void setSequenceNumber(int sequence) {
    mSequence = sequence;
  }
}
