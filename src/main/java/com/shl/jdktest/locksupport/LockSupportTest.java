package com.shl.jdktest.locksupport;

import java.util.concurrent.locks.LockSupport;

/**
 * @author songhengliang
 * @date 2019/7/24
 */
public class LockSupportTest {

    public static void main(String[] args) {

        LockSupport.park();

        System.out.println("park");

        LockSupport.unpark(Thread.currentThread());

        System.out.println("unpark");
    }

}
