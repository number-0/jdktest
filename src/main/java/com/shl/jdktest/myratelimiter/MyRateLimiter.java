package com.shl.jdktest.myratelimiter;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

/**
 * 限流：令牌桶算法实现
 *
 * @author songhengliang
 * @date 2019/5/28
 */
public class MyRateLimiter {
    private Semaphore sem;
    private int limit;
    private Timer timer;

    public MyRateLimiter(int limit) {
        this.limit = limit;
        this.sem = new Semaphore(limit);

        timer = new Timer();

        //放入令牌的时间间隔
        long period = 1000L / limit;

        //通过定时器，定时放入令牌
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (sem.availablePermits() < limit) {
                    sem.release();
                }
            }
        }, period, period);
    }

    public void acuqire() throws InterruptedException {
        this.sem.acquire();
    }

    public boolean tryAcquire() {
        return this.sem.tryAcquire();
    }

    public int availablePermits() {
        return this.sem.availablePermits();
    }
}
