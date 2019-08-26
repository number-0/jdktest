package com.shl.jdktest.myratelimiter.guava;

import com.google.common.util.concurrent.Monitor;
import com.google.common.util.concurrent.RateLimiter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 漏桶算法
 *
 * @author songhengliang
 * @date 2019/8/24
 */
public class LeakyBucket {
    /**
     * 定义漏桶
     */
    private final ConcurrentLinkedQueue<Integer> container = new ConcurrentLinkedQueue<>();

    /**
     * 桶的最大量
     */
    private static final int BUCKET_LIMIT = 1000;

    /**
     * 1秒钟允许10个操作，100ms处理一个操作
     */
    private final RateLimiter limiter = RateLimiter.create(10d);

    /**
     * 往桶里面放数据时，判断是否超过桶的最大量1000
     */
    private final Monitor offerMonitor = new Monitor();

    /**
     * 从桶里消费数据时，桶里必须存在数据
     */
    private final Monitor pollMonitor = new Monitor();

    /**
     * 往桶里面写数据，即提交请求，桶里最多放1000
     */
    public void submit(Integer data) {
        if (offerMonitor.enterIf(offerMonitor.newGuard(() -> container.size() < BUCKET_LIMIT))) {
            try {
                container.offer(data);
                System.out.println(
                        Thread.currentThread() + " submit data " + data + ",current size:"
                                + container.size());
            } finally {
                offerMonitor.leave();
            }
        } else {
            //这里时候采用降级策略了。消费速度跟不上产生速度时，而且桶满了，抛出异常
            //或者存入MQ DB等后续处理
            throw new IllegalStateException("The bucket is full.");
        }
    }

    /**
     * 从桶里面消费数据
     */
    public void takeThenConsume(Consumer<Integer> consumer) {
        if (pollMonitor.enterIf(pollMonitor.newGuard(() -> !container.isEmpty()))) {
            try {
                //不打印时 写 consumerRate.acquire();
                System.out.println(Thread.currentThread() + " waiting " + limiter.acquire());
                //container.peek() 只是去取出来不会删掉
                consumer.accept(container.poll());
            } finally {
                pollMonitor.leave();
            }
        }
    }

}
