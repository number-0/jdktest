package com.shl.jdktest.myratelimiter.guava;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 令牌桶算法
 *
 * @author songhengliang
 * @date 2019/8/24
 */
public class TokenBucket {

    /**
     * 流水号
     */
    private AtomicInteger phoneNumbers = new AtomicInteger(0);

    private final static int LIMIT = 100;

    /**
     * 1秒钟允许10个操作，100ms处理一个操作
     */
    private RateLimiter rateLimiter = RateLimiter.create(10);

    private final int saleLimit;

    public TokenBucket() {
        this(LIMIT);
    }

    public TokenBucket(int limit) {
        this.saleLimit = limit;
    }

    public int buy() {
        Stopwatch started = Stopwatch.createStarted();
        boolean success = rateLimiter.tryAcquire(10, TimeUnit.SECONDS);
        if (success) {
            if (phoneNumbers.get() >= saleLimit) {
                throw new IllegalStateException(
                        "Not any phone can be sale, please wait to next time.");
            }
            int phoneNo = phoneNumbers.getAndIncrement();
            handleOrder();
            System.out.println(
                    Thread.currentThread() + " user get the Mi phone: " + phoneNo + ",ELT:" + started
                            .stop());
            return phoneNo;
        } else {
            started.stop();
            throw new RuntimeException("Sorry, occur exception when buy phone");
        }
    }

    private void handleOrder() {
        try {
            TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(10));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
