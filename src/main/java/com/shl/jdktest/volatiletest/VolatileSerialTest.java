package com.shl.jdktest.volatiletest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 有序性：禁止指令重排序
 *
 *
 * 观察如下代码，只看代码，发现永远都不会出现"a=1和b=1"，
 *
 * 加上volatile可以保证有序性，volatile是通过内存屏障实现的
 * @author songhengliang
 * @date 2019/9/4
 */
public class VolatileSerialTest {
    //加上volatile可以保证有序性
    volatile static int x = 0;
    volatile static int y = 0;

    public static void main(String[] args) throws InterruptedException {
        Set<String> resultSet = new HashSet<>();
        Map<String, Integer> resultMap = new HashMap<>();

        for (int i = 0; i < 1000000; i++) {
            x=0;
            y=0;
            resultMap.clear();

            Thread one = new Thread(new Runnable(){
                @Override
                public void run() {
                    //重排序只要求最终的一致性
                    int a = y; //4
                    x = 1; //1
                    resultMap.put("a", a);
                }
            });

            Thread other = new Thread(new Runnable(){
                @Override
                public void run() {
                    int b = x; //3
                    y = 1; //2
                    resultMap.put("b", b);
                }
            });

            one.start();
            other.start();
            one.join();
            other.join();

            resultSet.add("a=" + resultMap.get("a") + " b=" + resultMap.get("b"));
            System.out.println(resultSet);
        }
    }

}
