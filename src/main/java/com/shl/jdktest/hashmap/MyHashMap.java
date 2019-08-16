package com.shl.jdktest.hashmap;

import java.util.ArrayList;
import java.util.List;

public class MyHashMap<K, V> implements MyMap<K, V> {

    private static int defaultLength = 16;

    private static double defaultLoader = 0.75;

    private Entry<K, V>[] table = null;

    private int size = 0;

    public MyHashMap(int length, double loader) {
        defaultLength = length;
        defaultLoader = loader;

        table = new Entry[defaultLength];
    }

    public MyHashMap() {
        this(defaultLength, defaultLoader);
    }

    /**
     * 整体逻辑：
     * V put：
     * （1）判断是否需要扩容，如果需要扩容，resize两倍，每个元素都要rehash，然后从新放入数组(相当于map.put执行一遍)
     * （2）计算index = hash(key) % size，
     *      如果index位置无元素了，则直接放入即可
     *      如果index位置有元素，则判断key是否equals，
     *              如果相等，则新value覆盖旧value
     *              如果不相等，则index位置用新元素替代，next指针指向老的元素
     * V get：
     * （1）计算index = hash(key) % size
     * （2）arr[index]获取值：
     *      遍历链表，（key == key || key.equals(key) 返回value
     * @param k
     * @param v
     * @return
     */
    @Override
    public V put(K k, V v) {

        //在这里要判断一下，size是否达到了一个扩容的一个标准
        if (size >= defaultLength * defaultLoader) {
            up2size();
        }

        //1、   创建一个hash函数，根据key和hash函数算出数组下标
        int index = getIndex(k);

        Entry<K, V> entry = table[index];

        if (entry == null) {
            //如果entry为null，说明table的index位置上没有元素
            table[index] = newEntry(k, v, null);
            size++;
        } else {
            //如果index位置不为空，说明index位置有元素，那么就要进行一个替换，然后next指针指向老数据
            table[index] = newEntry(k, v, entry);
        }
        return table[index].getValue();
    }

    private void up2size() {
        Entry<K, V>[] newTable = new Entry[2 * defaultLength];

        //新创建数组以后，以前老数组里面的元素要对新数组进行再散列
        againHash(newTable);
    }

    //新创建数组以后，以前老数组里面的元素要对新数组进行再散列
    private void againHash(Entry<K, V>[] newTable) {

        List<Entry<K, V>> list = new ArrayList<Entry<K, V>>();

        for (int i = 0; i < table.length; i++) {
            if (table[i] == null) {
                continue;
            }
            findEntryByNext(table[i], list);
        }

        if (list.size() > 0) {
            //要进行一个新数组的再散列
            size = 0;
            defaultLength = defaultLength * 2;
            table = newTable;

            for (Entry<K, V> entry : list) {
                if (entry.next != null) {
                    entry.next = null;
                }
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void findEntryByNext(Entry<K, V> entry, List<Entry<K, V>> list) {

        if (entry != null && entry.next != null) {
            list.add(entry);
            findEntryByNext(entry.next, list);
        } else {
            list.add(entry);
        }
    }

    private Entry<K, V> newEntry(K k, V v, Entry<K, V> next) {
        return new Entry(k, v, next);
    }

    private int getIndex(K k) {

        int m = defaultLength;

        int index = k.hashCode() % m;

        return index >= 0 ? index : -index;
    }

    /**
     * 整体逻辑：
     * V put：
     * （1）判断是否需要扩容，如果需要扩容，resize两倍，每个元素都要rehash，然后从新放入数组(相当于map.put执行一遍)
     * （2）计算index = hash(key) % size，
     *      如果index位置无元素了，则直接放入即可
     *      如果index位置有元素，则判断key是否equals，
     *              如果相等，则新value覆盖旧value
     *              如果不相等，则index位置用新元素替代，next指针指向老的元素
     * V get：
     * （1）计算index = hash(key) % size
     * （2）arr[index]获取值：
     *      遍历链表，（key == key || key.equals(key) 返回value
     * @param k
     * @param v
     * @return
     */
    @Override
    public V get(K k) {

        //1、   创建一个hash函数，根据key和hash函数算出数组下标
        int index = getIndex(k);

        if (table[index] == null) {
            return null;
        }

        return findValueByEqualKey(k, table[index]);
    }

    public V findValueByEqualKey(K k, Entry<K, V> entry) {

        if (k == entry.getKey() || k.equals(entry.getKey())) {
            return entry.getValue();
        } else {
            if (entry.next != null) {
                return findValueByEqualKey(k, entry.next);
            }
        }

        return null;
    }

    @Override
    public int size() {
        return size;
    }

    class Entry<K, V> implements MyMap.Entry<K, V> {

        K k;

        V v;

        Entry<K, V> next;

        public Entry(K k, V v, Entry<K, V> next) {
            this.k = k;
            this.v = v;
            this.next = next;
        }

        @Override
        public K getKey() {
            return k;
        }

        @Override
        public V getValue() {
            return v;
        }

    }
}