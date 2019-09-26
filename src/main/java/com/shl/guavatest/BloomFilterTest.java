package com.shl.guavatest;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.apache.commons.codec.Charsets;

/**
 * @author songhengliang
 * @date 2019/9/26
 */
public class BloomFilterTest {

    public static void main(String[] args) {
        int size = 10000;

        //fpp和误识别率有关：其实就是 扩大二进制向量数组或增加映射函数
        BloomFilter<String> bloomFilter = BloomFilter
                .create(Funnels.stringFunnel(Charsets.UTF_8), size, 0.00000001);
        for (int i = 0; i < size; i++) {
            bloomFilter.put("BloomFilterTest" + i);
        }

        int errorCount = 0;
        for (int i = 0; i < size; i++) {
            if (bloomFilter.mightContain("aa" + i)) {
                errorCount++;
            }
        }

        System.out.println("误识别率：" + errorCount);


    }

}
