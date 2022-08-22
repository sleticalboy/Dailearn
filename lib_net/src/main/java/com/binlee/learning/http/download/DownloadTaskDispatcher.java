package com.binlee.learning.http.download;

import android.os.Process;
import java.util.concurrent.BlockingQueue;

/**
 * Created on 18-9-3.
 *
 * @author leebin
 */
public class DownloadTaskDispatcher extends Thread {

  private final BlockingQueue<DownloadTask> mQueue;
  private volatile boolean mQuit;

  public DownloadTaskDispatcher(BlockingQueue<DownloadTask> queue) {
    super("DownloadTaskDispatcher");
    mQueue = queue;
  }

  public void quit() {
    mQuit = true;
    interrupt();
  }

  @Override
  public void run() {
    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
    DownloadTask downloadTask;
    while (true) {
      try {
        downloadTask = mQueue.take();
      } catch (InterruptedException e) {
        if (mQuit) {
          return;
        }
        continue;
      }
      downloadTask.download();
    }
  }
}
