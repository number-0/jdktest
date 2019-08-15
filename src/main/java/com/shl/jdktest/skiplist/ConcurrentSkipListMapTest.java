package com.shl.jdktest.skiplist;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

import java.util.concurrent.ConcurrentSkipListMap;
import org.junit.Test;

/**
 * ConcurrentSkipListMap
 * @author songhengliang
 * @date 2019/8/14
 */
public class ConcurrentSkipListMapTest {


    /**
     * ceiling取上限，4.1 --> 5
     * ceiling在跳表是是指找到最接近的上限元素
     */
    @Test
    public void testCeiling() {
        ConcurrentSkipListMap map = new ConcurrentSkipListMap<Integer, String>();
        map.put(1, "C");
        map.put(5, "Java");
        map.put(10, "C++");

        assertThat(map.size(), equalTo(3));
        assertThat(map.ceilingKey(2), equalTo(5) );
        assertThat(map.ceilingEntry(2).getValue(), equalTo("Java"));
        assertThat(map.ceilingEntry(5).getValue(), equalTo("Java"));
    }

    /**
     * floor取下限，4.1 --> 4
     * floor在跳表是是指找到最接近的下限元素
     */
    @Test
    public void testFloor() {
        ConcurrentSkipListMap map = new ConcurrentSkipListMap<Integer, String>();
        map.put(1, "C");
        map.put(5, "Java");
        map.put(10, "C++");

        assertThat(map.floorKey(2), equalTo(1) );
        assertThat(map.floorEntry(2).getValue(), equalTo("C"));
        assertThat(map.floorEntry(1 ).getValue(), equalTo("C"));
    }

    /**
     * first第一个元素
     */
    @Test
    public void testFirst() {
        ConcurrentSkipListMap map = new ConcurrentSkipListMap<Integer, String>();
        map.put(1, "C");
        map.put(5, "Java");
        map.put(10, "C++");

        assertThat(map.firstKey(), equalTo(1) );
        assertThat(map.firstEntry().getValue(), equalTo("C"));
    }

    /**
     * last最后一个元素
     */
    @Test
    public void testLast() {
        ConcurrentSkipListMap map = new ConcurrentSkipListMap<Integer, String>();
        map.put(1, "C");
        map.put(5, "Java");
        map.put(10, "C++");

        assertThat(map.lastKey(), equalTo(10) );
        assertThat(map.lastEntry().getValue(), equalTo("C++"));
    }
}
