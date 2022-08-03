package com.binlee.learning.http;

import android.os.Process;
import androidx.annotation.NonNull;
import java.io.Serializable;
import java.util.Locale;

/**
 * Created on 18-9-19.
 *
 * @author leebin
 */
public abstract class Task implements Runnable, Serializable, Comparable<Task> {

  private static final long serialVersionUID = -1786506507083942452L;

  private final String name;
  private int sequenceId;

  public Task(String format, Object... args) {
    this.name = String.format(Locale.CHINA, format, args);
  }

  @Override
  public final void run() {
    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
    final String oldName = Thread.currentThread().getName();
    Thread.currentThread().setName(name);
    try {
      execute();
    } finally {
      Thread.currentThread().setName(oldName);
    }
  }

  public abstract void execute();

  public Task get() {
    return this;
  }

  public abstract void cancel();

  public abstract boolean isCanceled();

  @Override
  public final int compareTo(@NonNull final Task o) {
    return 0;
  }
}
