package com.tallence.core.redirects.cae.service.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A pausable executor service.
 *
 * After https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ThreadPoolExecutor.html
 */
public class PausableThreadPoolExecutorService extends ThreadPoolExecutor {

  private boolean isPaused = false;
  private ReentrantLock pauseLock = new ReentrantLock();
  private Condition unpaused = pauseLock.newCondition();

  public PausableThreadPoolExecutorService(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
  }

  @Override
  protected void beforeExecute(Thread thread, Runnable runnable) {
    super.beforeExecute(thread, runnable);
    pauseLock.lock();
    try {
      while (isPaused) {
        unpaused.await();
      }
    } catch (InterruptedException e) {
      thread.interrupt();
    } finally {
      pauseLock.unlock();
    }
  }

  public void pause() {
    pauseLock.lock();
    try {
      isPaused = true;
    } finally {
      pauseLock.unlock();
    }
  }

  public void resume() {
    pauseLock.lock();
    try {
      isPaused = false;
      unpaused.signalAll();
    } finally {
      pauseLock.unlock();
    }
  }
}
