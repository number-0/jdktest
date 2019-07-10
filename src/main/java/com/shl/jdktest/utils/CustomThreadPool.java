package com.shl.jdktest.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

/**
 * 自定义线程池
 */
public class CustomThreadPool {
    /**
     * 核心线程数：线程池维护线程的最少数量
     * 默认情况下核心线程会一直存活，即使处于闲置状态也不会受存keepAliveTime限制。除非将allowCoreThreadTimeOut设置为true
     */
    private static final int CORE_POOL_SIZE = 2;

    /**
     * CPU核数
     */
    private static int availableProcessors = Runtime.getRuntime().availableProcessors();

    /**
     * 最大线程数量
     */
    private static final int MAXINUM_POOL_SIZE = availableProcessors;

    /**
     * 线程池维护线程所允许的空闲时间：线程池中每一个线程保持空闲的时间，空闲线程活着的时间，不被销毁
     */
    private static final long KEEP_ALIVE_TIME = 4;

    /**
     * keepAliveTime的时间单位：线程池维护线程所允许的空闲时间的单位
     */
    private static final TimeUnit UNIT = TimeUnit.SECONDS;

    /**
     * 线程池所使用的缓冲队列,这里队列大小为3
     */
    private static final BlockingQueue<Runnable> WORKQUEUE = new ArrayBlockingQueue<Runnable>(3);

    /**
     * 线程池对拒绝任务的处理策略：
     *      AbortPolicy为抛出异常；
     *      CallerRunsPolicy为重试添加当前的任务，他会自动重复调用execute()方法；
     *      DiscardOldestPolicy为抛弃旧的任务
     *      DiscardPolicy为抛弃当前的任务
     */
    private static final AbortPolicy HANDLER = new ThreadPoolExecutor.AbortPolicy();

    /**
     *
     */
    private static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE,
            MAXINUM_POOL_SIZE, KEEP_ALIVE_TIME, UNIT, WORKQUEUE, HANDLER);

    /**
     * 加入到线程池中执行
     */
    public static void runInThread(Runnable runnable) {
        threadPool.execute(runnable);
    }
}