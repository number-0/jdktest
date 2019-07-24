package com.shl.jdktest.aqs;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AQS
 * @author songhengliang
 * @date 2019/7/22
 */
public class AqsTest {

    private static ReentrantLock lock = new ReentrantLock(true);

    public static void test() {

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


    public static void main(String[] args) {
        new Thread(AqsTest::test, "线程一").start();

        new Thread(AqsTest::test, "线程二").start();

        try {
            TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }





}
