package com.shl.jdktest.myratelimiter.guava;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * 漏桶算法test
 * @author songhengliang
 * @date 2019/8/24
 */
public class LeakyBucketTest {
    public static void main(String[] args) {
        final LeakyBucket bucket = new LeakyBucket();
        final AtomicInteger DATA_CREATOR = new AtomicInteger(0);

        //1. 往桶中写数据，即提交请求
        IntStream.range(0, 5).forEach(i -> {
            new Thread(() -> {
                for (; ; ) {
                    int data = DATA_CREATOR.getAndIncrement();
                    bucket.submit(data);
                    try {
                        //1s往桶中写5个数据
                        TimeUnit.MILLISECONDS.sleep(200L);
                    } catch (Exception e) {
                        if (e instanceof IllegalStateException) {
                            System.out.println(e.getMessage());
                        }
                    }
                } //25 = 5个线程，每个线程1s往桶中写5个数据
                //RateLimiter定义的1s可以处理10个请求
                //25:10 = 5:2
            }).start();
        });


        //2. 从桶里面消费数据
        IntStream.range(0, 5).forEach(i -> new Thread(() -> {
            for (; ; ) {
                bucket.takeThenConsume(x -> System.out.println(Thread.currentThread() + " W " + x));
            }
        }).start());
    }
}
