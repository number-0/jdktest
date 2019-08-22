package com.shl.jdktest.threadlocal;

import java.lang.ThreadLocal.ThreadLocalMap;
import java.lang.ThreadLocal.ThreadLocalMap.Entry;
import java.lang.ref.WeakReference;

/**
 * ThreadLocal源码分析
 * @author songhengliang
 * @date 2019/8/22
 */
public class ThreadLocal_CodeAnalysis {


    // ************************* ThreadLocal ************************

    /**
     * get()整体逻辑：
     * （1）获取当前线程内部的ThreadLocalMap
     * （2）map存在则获取当前ThreadLocal对应的value值
     * （3）map不存在或者找不到value值，则调用setInitialValue，进行初始化
     * @return
     */
    public T get() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                return result;
            }
        }
        return setInitialValue();
    }

    ThreadLocalMap getMap(Thread t) {
        return t.threadLocals;
    }

    /**
     * setInitialValue()整体逻辑：
     *  调用initialValue方法，获取初始化值【调用者通过覆盖该方法，设置自己的初始化值】
     *  获取当前线程内部的ThreadLocalMap
     *  map存在则把当前ThreadLocal和value添加到map中
     *  map不存在则创建一个ThreadLocalMap，保存到当前线程内部
     * @return
     */
    private T setInitialValue() {
        T value = initialValue();
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
        return value;
    }

    void createMap(Thread t, T firstValue) {
        t.threadLocals = new ThreadLocal.ThreadLocalMap(this, firstValue);
    }


    /**
     * set(T value)
     *  获取当前线程内部的ThreadLocalMap
     *  map存在则把当前ThreadLocal和value添加到map中
     *  map不存在则创建一个ThreadLocalMap，保存到当前线程内部
     * @param value
     */
    public void set(T value) {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
    }

    /**
     * remove()
     * 获取当前线程内部的ThreadLocalMap，存在则从map中删除这个ThreadLocal对象
     */
    public void remove() {
        ThreadLocalMap m = getMap(Thread.currentThread());
        if (m != null)
            //ThreadLocalMap#remove
            m.remove(this);
    }



    //start ************************* ThreadLocal#ThreadLocalMap ************************

    /**
     * ThreadLocalMap是一个自定义的hash map，专门用来保存线程的thread local变量
     * key：ThreadLocal弱引用，value：val
     * key使用WeakReferences，是不想因为自己存储了ThreadLocal对象，而影响到它的垃圾回收
     * hash表运行空间不足时，key为null的entry就会被清理掉
     *
     * 初始化大小为16，阈值threshold为数组长度的2/3，Entry类型为WeakReference，有一个弱引用指向ThreadLocal对象
     */
    static class ThreadLocalMap {

        // hash map中的entry继承自弱引用WeakReference，指向threadLocal对象
        // 对于key为null的entry，说明不再需要访问，会从table表中清理掉
        // 这种entry被成为“stale entries”
        static class Entry extends WeakReference<ThreadLocal<?>> {
            /** The value associated with this ThreadLocal. */
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }

        private static final int INITIAL_CAPACITY = 16;

        private Entry[] table;

        private int size = 0;

        private int threshold; // Default to 0

        private void setThreshold(int len) {
            threshold = len * 2 / 3;
        }

        ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
            table = new Entry[INITIAL_CAPACITY];
            int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
            table[i] = new Entry(firstKey, firstValue);
            size = 1;
            setThreshold(INITIAL_CAPACITY);
        }

        private void set(ThreadLocal<?> key, Object value) {
            ThreadLocal.ThreadLocalMap.Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);

            for (ThreadLocal.ThreadLocalMap.Entry e = tab[i];
                    e != null;
                    e = tab[i = nextIndex(i, len)]) {
                ThreadLocal<?> k = e.get();

                if (k == key) {
                    e.value = value;
                    return;
                }

                if (k == null) {
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }

            tab[i] = new ThreadLocal.ThreadLocalMap.Entry(key, value);
            int sz = ++size;
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
                rehash();
        }

        /**
         * Remove the entry for key.
         */
        private void remove(ThreadLocal<?> key) {
            ThreadLocal.ThreadLocalMap.Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);
            for (ThreadLocal.ThreadLocalMap.Entry e = tab[i];
                    e != null;
                    e = tab[i = nextIndex(i, len)]) {
                if (e.get() == key) {
                    e.clear();
                    expungeStaleEntry(i);
                    return;
                }
            }
        }
    }

    //end ************************* ThreadLocal#ThreadLocalMap ************************
}
