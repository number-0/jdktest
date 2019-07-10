package com.shl.jdktest.simpledateformat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 * 解决SimpleDateFormat线程不安全问题
 * @author songhengliang
 * @date 2016/4/10
 */
public class SimpleDateFormatSafeTest {
    /*start 解决方案一：每次使用都创建新实例， 会频繁地创建和销毁对象，效率较低*/
    public static String format(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        return sdf.format(date);
    }
    public static Date parse(String strDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        return sdf.parse(strDate);
    }
    /*end 解决方案一：每次使用都创建新实例*/



    /*start 解决方案二：使用锁，缺点并发量大的时候会对性能有影响，线程阻塞*/
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

    public static String format2(Date date) {
        synchronized(SDF){
            return SDF.format(date);
        }
    }

    public static Date parse2(String strDate) throws ParseException {
        synchronized(SDF){
            return SDF.parse(strDate);
        }
    }
    /*end 解决方案二：使用锁*/



    /*start 解决方案三：ThreadLocal*/
    private static ThreadLocal<SimpleDateFormat> SDF_THREADLOCAL = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        }
    };

    public static String format3(Date date) {
        return SDF_THREADLOCAL.get().format(date);
    }

    public static Date parse3(String dateStr) throws ParseException {
        return SDF_THREADLOCAL.get().parse(dateStr);
    }
    /*end 解决方案三：ThreadLocal*/



    /*start 解决方案四：java8，新日期时间*/
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

    public static String format4(LocalDateTime localDateTime) {
        return localDateTime.format(DTF);
    }

    public static LocalDateTime parse4(String dateStr) {
        return LocalDateTime.parse(dateStr, DTF);
    }

    @Test
    public void dateTimeFormatterTest() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(1);

        for (int i = 0; i < 1; i++) {
            service.execute(() -> {
                try {
                    String formatTime = format4(LocalDateTime.now());
                    System.out.println(formatTime);
                    System.out.println(parse4(formatTime));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
    }
    /*end 解决方案四：java8，新日期时间*/

}
