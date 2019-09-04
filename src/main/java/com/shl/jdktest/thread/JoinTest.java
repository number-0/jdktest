package com.shl.jdktest.thread;

/**
 * Thread#join() test
 *
 * join方法只有在继承了Thread类的线程中才有
 * 线程必须要start() 后再join才能起作用
 * 另外一个线程join到当前线程，则需要等到join进来的线程执行完才会继续执行当前线程
 * join()会让调用线程等待被调用线程结束后，才会继续执行。使用的场景为我们需要等待某个线程执行完成后才可继续执行的场景。
 * @author songhengliang
 * @date 2019/9/4
 */
public class JoinTest {

    public static void main(String[] args) throws InterruptedException {

        Thread thread = new Thread(() -> {
            for (int i = 0; i < 1000000; i++) {
                System.out.println("thread is executing ... ");
            }
        });

        thread.start();
        thread.join();

        System.out.println("main is over ... ");
    }

}
