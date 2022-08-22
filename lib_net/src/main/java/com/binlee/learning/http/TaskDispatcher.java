package com.binlee.learning.http;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created on 18-9-19.
 *
 * @author leebin
 */
public final class TaskDispatcher {

  private int maxRequests = 25;
  private final Deque<Task> readyTasks = new ArrayDeque<>();
  private final Deque<Task> runningTasks = new ArrayDeque<>();
  private ExecutorService executorService;
  private Runnable idleCallback;

  public TaskDispatcher() {
  }

  public TaskDispatcher(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public synchronized void cancelAll() {
    for (final Task task : readyTasks) {
      task.get().cancel();
    }
    for (final Task task : runningTasks) {
      task.get().cancel();
    }
  }

  public synchronized void enqueue(Task task) {
    if (runningTasks.size() < maxRequests) {
      runningTasks.add(task);
      executorService().execute(task);
    } else {
      readyTasks.add(task);
    }
  }

  public synchronized void setIdleCallback(Runnable idleCallback) {
    this.idleCallback = idleCallback;
  }

  public synchronized void setMaxRequests(final int maxRequests) {
    if (maxRequests < 1) {
      throw new IllegalArgumentException("max < 1: " + maxRequests);
    }
    this.maxRequests = maxRequests;
    promoteTasks();
  }

  public void finished(Task task) {
    int runningTaskCount;
    Runnable idleCallback;
    synchronized (this) {
      if (!runningTasks.remove(task)) {
        throw new AssertionError("");
      }
      promoteTasks();
      runningTaskCount = runningTasks.size();
      idleCallback = this.idleCallback;
    }
    if (runningTaskCount == 0 && idleCallback != null) {
      idleCallback.run();
    }
  }

  private void promoteTasks() {
    if (runningTasks.size() > maxRequests) {
      return; // 达到上限了
    }
    if (readyTasks.size() == 0) {
      return; // 没有空闲的
    }
    for (Iterator<Task> it = readyTasks.iterator(); it.hasNext(); ) {
      final Task task = it.next();
      it.remove();
      runningTasks.add(task);
      executorService().execute(task);
      if (runningTasks.size() >= maxRequests) {
        return; // 到达上限了
      }
    }
  }

  public synchronized ExecutorService executorService() {
    if (executorService == null) {
      executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
        new SynchronousQueue<>(), OkUtils.threadFactory("Task Dispatcher", false));
    }
    return executorService;
  }

  public synchronized List<Task> runningTasks() {
    return Collections.unmodifiableList(new ArrayList<>(runningTasks));
  }

  public synchronized List<Task> readyTasks() {
    return Collections.unmodifiableList(new ArrayList<>(readyTasks));
  }
}
