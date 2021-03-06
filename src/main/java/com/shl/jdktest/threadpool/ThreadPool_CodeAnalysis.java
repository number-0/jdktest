package com.shl.jdktest.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 线程池源码分析
 * @author songhengliang
 * @date 2020/9/1
 */
public class ThreadPool_CodeAnalysis {

    private static String name = "test-task";

    private static ThreadPoolExecutor threadPool =
            new ThreadPoolExecutor(10, 10,
                    0, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(),
                    new ThreadFactoryBuilder().setNameFormat(name + "-%d").build(),
                    new AbortPolicy());


    public static void main(String[] args) {

        for (int i = 0; i < 100; i++) {
            threadPool.execute(new MyTask(i, "任务"+i));
        }


//        adjustArgs();

        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void adjustArgs() {
        int corePoolSize = threadPool.getCorePoolSize();
        int maximumPoolSize = threadPool.getMaximumPoolSize();
        long keepAliveTime = threadPool.getKeepAliveTime(TimeUnit.SECONDS);
        System.out.println("before adjust, corePoolSize:"+corePoolSize + ", maximumPoolSize:" + maximumPoolSize + ", keepAliveTime:" + keepAliveTime);

        threadPool.setCorePoolSize(20);
        threadPool.setMaximumPoolSize(30);
        threadPool.setKeepAliveTime(10, TimeUnit.SECONDS);

        corePoolSize = threadPool.getCorePoolSize();
        maximumPoolSize = threadPool.getMaximumPoolSize();
        keepAliveTime = threadPool.getKeepAliveTime(TimeUnit.SECONDS);
        System.out.println("after adjust, corePoolSize:" + corePoolSize + ", maximumPoolSize:" + maximumPoolSize + ", keepAliveTime:" + keepAliveTime);
    }


    @Setter
    @Getter
    @AllArgsConstructor
    public static class MyTask implements Runnable {
        private int taskId;
        private String taskName;

        @Override
        public void run() {
            try {
                System.out.println("run taskId =" + this.taskId);
                Thread.sleep(5*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
