package com.tallence.core.redirects.cae.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This special adaptation of a {@link ThreadPoolExecutor} is able to control a second {@link PausableThreadPoolExecutorService},
 * so that it will be paused, while tasks are running in this pool.
 */
public class ControllingThreadPoolExecutorService extends ThreadPoolExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(ControllingThreadPoolExecutorService.class);

  /**
   * We have to keep count here, because {@link ThreadPoolExecutor#getActiveCount()} only returns an estimate and not the
   * precise count.
   */
  private final AtomicInteger activeThreadCount = new AtomicInteger(0);
  private final PausableThreadPoolExecutorService pausableThreadPoolExecutorService;

  public ControllingThreadPoolExecutorService(int corePoolSize,
                                              int maximumPoolSize,
                                              long keepAliveTime,
                                              TimeUnit unit,
                                              BlockingQueue<Runnable> workQueue,
                                              ThreadFactory threadFactory,
                                              PausableThreadPoolExecutorService pausableThreadPoolExecutorService) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    this.pausableThreadPoolExecutorService = pausableThreadPoolExecutorService;
  }

  @Override
  protected void beforeExecute(Thread t, Runnable r) {
    super.beforeExecute(t, r);
    activeThreadCount.incrementAndGet();
    pausableThreadPoolExecutorService.pause();
  }

  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    super.afterExecute(r, t);
    resumeIfNoMoreThreads();
  }

  /**
   * Resumes the {@code pausableThreadPoolExecutorService}, if there are no more threads running.
   * <p>
   * This method is synchronized, because the check and the resume call need to be atomic.
   */
  private synchronized void resumeIfNoMoreThreads() {
    if (activeThreadCount.decrementAndGet() == 0) {
      LOG.info("Site indexing finished, resumed item update queue");
      pausableThreadPoolExecutorService.resume();
    }
  }
}
