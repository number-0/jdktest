package com.shl.jdktest.myratelimiter;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

/**
 * 限流：令牌桶算法实现
 *
 * 令牌桶算法关键点：计数、定时器(每隔200ms放)，间隔(200ms)
 * 计数可以用Semaphore来实现，Semaphore可增可减
 * 定时器可以用Timer
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
                //Semaphore#availablePermits()：获取可用的许可数，此方法一般用于调试
                if (sem.availablePermits() < limit) {
                    //Semaphore#release()：释放一个许可，将其返回给信号量，许可数增加1
                    sem.release();
                }
            }
        }, period, period);
    }

    //todo 获取令牌 Semaphore#acquire()：从此信号量获取一个许可，在提供一个许可前一直将线程阻塞，否则线程被中断，可用的许可数减1
}
