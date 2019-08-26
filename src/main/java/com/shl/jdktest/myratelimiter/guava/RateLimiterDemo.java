package com.shl.jdktest.myratelimiter.guava;

import com.google.common.util.concurrent.RateLimiter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author songhengliang
 * @date 2019/8/24
 */
public class RateLimiterDemo {


    /**
     * RateLimiter#create(double permitsPerSecond)：每秒钟允许多少个许可
     * 0.5：1秒钟允许有0.5个许可，那就是2秒钟获取1个许可
     *
     * RateLimiter#acquire()：匀速获取许可，会阻塞再没有到达获取时间的时候，因为是匀速
     */
    private static final RateLimiter RATE_LIMITER = RateLimiter.create(0.5);

    private static void testLimiter() {
        System.out.println(Thread.currentThread() + " waiting " + RATE_LIMITER.acquire() );
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executorService.submit(RateLimiterDemo::testLimiter );
        }
    }
}
