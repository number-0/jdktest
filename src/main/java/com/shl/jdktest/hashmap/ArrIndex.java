package com.shl.jdktest.hashmap;

/**
 * 数组位置计算步骤
 * @author songhengliang
 * @date 2019/8/16
 */
public class ArrIndex {

    /**
     * 数组位置计算分为2步：
     * （1）计算hash值并扰动处理
     * （2）根据hash值再计算得出最后数组位置：h & (length - 1);
     */
    // a. 根据键值key计算hash值并扰动处理 ->> 分析1
    int hash = hash(key);
    // b. 根据hash值 最终获得 key对应存放的数组Table中位置 ->> 分析2
    int i = indexFor(hash, table.length);

    /**
     * 步骤1：hash(key) 该函数在JDK 1.7 和 1.8 中的实现不同，但原理一样 = 扰动函数 = 使得根据key生成的哈希码（hash值）分布更加均匀、更具备随机性，避免出现hash值冲突（即指不同key但生成同1个hash值）
     * JDK 1.7 做了9次扰动处理 = 4次位运算 + 5次异或运算 JDK 1.8 简化了扰动函数 = 只做了2次扰动 = 1次位运算 + 1次异或运算
     */

    // JDK 1.7实现：将 键key 转换成 哈希码（hash值）操作  = 使用hashCode() + 4次位运算 + 5次异或运算（9次扰动）
    static final int hash(int h) {
        h ^= k.hashCode();
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    // JDK 1.8实现：将 键key 转换成 哈希码（hash值）操作 = 使用hashCode() + 1次位运算 + 1次异或运算（2次扰动）
    // 1. 取hashCode值： h = key.hashCode()
    //  2. 高位参与低位的运算：h ^ (h >>> 16)
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
        // a. 当key = null时，hash值 = 0，所以HashMap的key 可为null
        // 注：对比HashTable，HashTable对key直接hashCode（），若key为null时，会抛出异常，所以HashTable的key不可为null
        // b. 当key ≠ null时，则通过先计算出 key的 hashCode()（记为h），然后 对哈希码进行 扰动处理： 按位 异或（^） 哈希码自身右移16位后的二进制
    }

    /**
     * 步骤2：indexFor(hash, table.length) JDK 1.8中实际上无该函数，但原理相同，即具备类似作用的函数
     */
    static int indexFor(int h, int length) {
        return h & (length - 1); //本质就是取模运算，位运算比取模效率更高
        // 将对哈希码扰动处理后的结果 与运算(&) （数组长度-1），最终得到存储在数组table的位置（即数组下标、索引）
    }


}
