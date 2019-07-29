package com.shl.jdktest.aqs;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.Test;

/**
 * AQS
 * @author songhengliang
 * @date 2019/7/22
 */
public class AqsTest {

    @Test
    public void unFairLock() {
        ReentrantLock lock = new ReentrantLock(false);

        lock.lock();
        System.out.println("---------获取锁成功");


        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        lock.unlock();
        System.out.println("---------释放锁成功");
    }






    private static void test() {
        //默认创建的是独占锁，只允许一个线程操作

        lock.lock();
        System.out.println("---------第1次获取锁成功");


        lock.lock();
        System.out.println("---------重入，获取锁成功");
        lock.unlock();
        System.out.println("---------重入，释放锁成功");

        lock.unlock();
        System.out.println("---------释放锁成功");

    }
    private static ReentrantLock lock = new ReentrantLock(true);
    @Test
    public void fairLock() {
        new Thread(AqsTest::test, "线程一").start();

        new Thread(AqsTest::test, "线程二").start();

        try {
            TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
