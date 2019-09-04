package com.shl.jdktest.volatiletest;
import java.util.concurrent.TimeUnit;

/**
 * 内存可见性
 * @author songhengliang
 * @date 2019/9/4
 */
public class VolatileVisbilityTest {
    private volatile static boolean flag = false;

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            while (!flag) {
            }
            System.out.println("thread execute over ... ");
        }).start();

        TimeUnit.SECONDS.sleep(1);

        new Thread(() -> {
            flag = true;
        }).start();
    }
}
