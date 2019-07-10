package com.shl.jdktest.simpledateformat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 * SimpleDateFormat线程不安全案例
 * @author songhengliang
 * @date 2016/4/10
 */
public class SimpleDateFormatUnsafeTest {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

    public static String format(Date date) {
        return SDF.format(date);
    }

    public static Date parse(String strDate) throws ParseException {
        return SDF.parse(strDate);
    }

    @Test
    public void singleThreadParse() throws ParseException {
        System.out.println(parse("2019-04-10 17:37:02,609"));
    }

    /**
     * parse不安全
     * @throws ParseException
     * @throws InterruptedException
     */
    @Test
    public void multiThreadParse() throws ParseException, InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(50);

        for (int i = 0; i < 200; i++) {
            service.execute(() -> {
                try {
                    System.out.println(parse("2019-04-10 17:37:02,609"));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
        }

        TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
    }

    /**
     * format不安全
     * @throws ParseException
     * @throws InterruptedException
     */
    @Test
    public void multiThreadFormat() throws ParseException, InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(20);

        Date date1 = new Date(1546272000000L);
        Date date2 = new Date(1548950400000L);

        for (int i = 0; i < 200000; i++) {
            service.execute(() -> {
                try {
                    if (Thread.currentThread().getName().equals("pool-1-thread-1")) {
                        System.out.println(Thread.currentThread().getName() + " - date1:"+ date1 +" - " + format(date1));
                    } else {
                        format(date2);
                    }

                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
        }

        TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
    }

}
