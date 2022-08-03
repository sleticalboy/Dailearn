package com.binlee.learning.http.download;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 18-9-3.
 *
 * @author leebin
 */
public final class DownloadTaskQueue {

  private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 1;
  private final Set<DownloadTask> mCurrentQueue = new HashSet<>();
  private final PriorityBlockingQueue<DownloadTask> mDownloadQueue = new PriorityBlockingQueue<>();
  private final AtomicInteger mSequenceGenerator = new AtomicInteger();
  private DownloadTaskDispatcher[] mDispatchers;

  public DownloadTaskQueue() {
    this(DEFAULT_NETWORK_THREAD_POOL_SIZE);
  }

  public DownloadTaskQueue(int threadPoolSize) {
    mDispatchers = new DownloadTaskDispatcher[threadPoolSize];
  }

  public void finishTask(DownloadTask task) {
    synchronized (mCurrentQueue) {
      task.cancel();
      mCurrentQueue.remove(task);
    }
  }

  public void addTask(DownloadTask task) {
    task.setDownloadQueue(this);
    synchronized (mCurrentQueue) {
      mCurrentQueue.add(task);
    }
    task.setSequenceNumber(getSequenceNumber());
    mDownloadQueue.add(task);
  }

  public int getSequenceNumber() {
    return mSequenceGenerator.incrementAndGet();
  }

  public void startTask() {
    stopTask();
    for (int i = 0; i < mDispatchers.length; i++) {
      mDispatchers[i] = new DownloadTaskDispatcher(mDownloadQueue);
      mDispatchers[i].start();
    }
  }

  public void stopTask() {
    for (final DownloadTaskDispatcher dispatcher : mDispatchers) {
      if (dispatcher != null) {
        dispatcher.quit();
      }
    }
  }

  public void stopAllTask() {
    synchronized (mCurrentQueue) {
      for (final DownloadTask task : mCurrentQueue) {
        task.pause();
      }
    }
  }

  public void cancelAllTask(TaskFilter filter) {
    synchronized (mCurrentQueue) {
      for (final DownloadTask task : mCurrentQueue) {
        if (filter.apply(task)) {
          task.cancel();
        }
      }
    }
  }

  public interface TaskFilter {
    boolean apply(DownloadTask task);
  }
}
