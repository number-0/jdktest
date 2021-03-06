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
    private AtomicInteger serialNumber = new AtomicInteger(0);

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
            if (serialNumber.get() >= saleLimit) {
                throw new IllegalStateException(
                        "Not any phone can be sale, please wait to next time.");
            }
            int phoneNo = serialNumber.getAndIncrement();
            handleOrder();
            System.out.println(
                    Thread.currentThread() + " user get the Mi phone: " + phoneNo + ",ELT:" + started
                            .stop());
            return phoneNo;
        } else {
            started.stop();
            //超时后 同一时间，很大的流量来强时，超时快速失败。
            throw new RuntimeException("Sorry, occur exception when buy phone");
        }
    }

    /**
     * 模拟处理订单的流程
     */
    private void handleOrder() {
        try {
            TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(10));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
