package com.binlee.learning.ffmpeg;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created on 3/18/23
 *
 * @author binlee
 */
public class CodecQueue<T> {

  private final Deque<T> mQueue;

  public CodecQueue() {
    this(false);
  }

  public CodecQueue(boolean async) {
    mQueue = async ? new LinkedBlockingDeque<>() : new LinkedList<>();
  }

  public void enqueue(T e) {
    if (mQueue instanceof BlockingDeque) mQueue.addFirst(e);

    synchronized (mQueue) {
      mQueue.addFirst(e);
    }
  }

  public T dequeue() {
    if (mQueue instanceof BlockingDeque) {
      try {
        return ((BlockingDeque<T>) mQueue).takeLast();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    synchronized (mQueue) {
      return mQueue.removeLast();
    }
  }

  public int size() {
    if (mQueue instanceof BlockingDeque) return mQueue.size();

    synchronized (mQueue) {
      return mQueue.size();
    }
  }
}
