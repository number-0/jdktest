package com.shl.jdktest.myratelimiter.guava;

/**
 * @author songhengliang
 * @date 2019/8/24
 */
public class TokenBucketTest {

    public static void main(String[] args) {
        final TokenBucket tokenBucket = new TokenBucket();
        for (int i = 0; i < 200; i++) {
            new Thread(tokenBucket::buy).start();
        }
    }

}
