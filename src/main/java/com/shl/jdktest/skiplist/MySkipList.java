package com.shl.jdktest.skiplist;

import java.util.Random;

/**
 * 跳表
 *
 * @author songhengliang
 * @date 2019/8/13
 */
public class MySkipList {

    private static class Node {

        private Integer value;
        //指针：上
        private Node up;
        //指针：下
        private Node down;
        //指针：左
        private Node left;
        //指针：右
        private Node right;
        //节点类型
        private byte type;

        public Node(Integer value, byte type) {
            this.value = value;
            this.type = type;
        }

        public Node(Integer value) {
            this(value, DATA_NODE);
        }
    }


    //节点类型：head节点
    private final static byte HEAD_NODE = (byte) -1;
    //节点类型：数据节点
    private final static byte DATA_NODE = (byte) 0;
    //节点类型：tail节点
    private final static byte TAIL_NODE = (byte) 1;

    //head节点
    private Node head;
    //tail节点
    private Node tail;
    //跳表size
    private int size;
    //跳表高度
    private int height = 1;
    //随机算法
    private Random random;

    public MySkipList() {
        //初始为一个空的跳表
        this.head = new Node(null, HEAD_NODE);
        this.tail = new Node(null, TAIL_NODE);

        head.right = tail;
        tail.left = head;

        this.random = new Random(System.currentTimeMillis());
    }

    /**
     * find方法只能找到一个位置
     * 找的时候是：向右找，向下找
     * 找到的是最底层链表的一个位置
     * @param element
     * @return
     */
    private Node find(Integer element) {
        Node temp = head;

        //向右找(只有当要找的元素大于等于右边的元素，才会继续向右找)，向下找
        for (; ; ) {
            while (temp.right.type != TAIL_NODE && element >= temp.right.value) {
                temp = temp.right;
            }

            if (temp.down != null) {
                temp = temp.down;
            } else {
                break;
            }
        }

        //必定有 temp <= element < temp.right.value
        return temp;
    }

    public boolean contains(Integer element) {
        return this.find(element).value.intValue() == element;
    }

    public Integer get(Integer element) {
        Node node = this.find(element);
        return node.value.intValue() == element ? node.value : null;
    }

    /**
     * 整体逻辑：
     * （1）找到元素在最底层链表的附件位置，最底层链表插入元素(指针连接)
     * （2）算法拔高，在当前层链表插入元素(指针连接)
     * @param element
     */
    public void add(Integer element) {
        //找到要插入元素的附近位置
        Node nearNode = this.find(element);

        //要插入的节点
        Node newNode = new Node(element);

        //指针连接
        newNode.right = nearNode.right; //新节点的右指针
        newNode.left = nearNode; //新节点的左指针

        nearNode.right = newNode; //新节点左边元素的右指针
        nearNode.right.left = newNode; //新节点右边元素的左指针

        //随机算法拔高度
        int currentLevel = 1;
        while (random.nextDouble() < 0.5d) {
            if (currentLevel >= height) {
                height++;

                Node dummyHead = new Node(null, HEAD_NODE);
                Node dummyTail = new Node(null, TAIL_NODE);

                dummyHead.right = dummyTail;
                dummyHead.down = head;
                head.up = dummyHead;

                dummyTail.left = dummyHead;
                dummyTail.down = tail;
                tail.up = dummyTail;

                head = dummyHead;
                tail = dummyTail;
            }

            //up节点不为空为止，这样newNode的up节点才能将left指针有指向
            while (nearNode.up == null) {
                nearNode = nearNode.left;
            }
            Node upNearNode = nearNode.up;
            Node upNewNode = new Node(element);
            upNewNode.left = upNearNode;
            upNewNode.right = upNearNode.right;
            upNewNode.down = newNode;

            upNearNode.right = upNewNode;
            upNearNode.right.left = upNearNode;

            newNode.up = upNewNode;

            currentLevel++;
        }

        //add后，跳表的size+1
        size++;
    }

    /**
     * 打印整个跳表：向右向下打印
     */
    public void printSkipList() {
        StringBuilder txt = new StringBuilder();

        Node temp = head;
        for (int i = height; i >= 1; i--) {
            txt.append("totalHeight:" + height + ", currentHeight:" + i);

            Node node = temp.right;

            txt.append(" [");
            while (node.type == DATA_NODE) {
                txt.append(node.value).append(" -> ");
                node = node.right;
            }
            txt.append(" ]").append("\n");

            temp = temp.down;
        }

        System.out.println(txt.toString());
    }

    public boolean delete() {
        // 和add类似
        return true;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public static void main(String[] args) {
        MySkipList mySkipList = new MySkipList();
        mySkipList.add(10);
        mySkipList.printSkipList();
    }
}
