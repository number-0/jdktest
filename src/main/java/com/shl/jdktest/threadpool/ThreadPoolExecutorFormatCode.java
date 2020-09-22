package com.shl.jdktest.threadpool;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.Worker;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author songhengliang
 * @date 2020/9/2
 */
public class ThreadPoolExecutorFormatCode {
    // TODO: 2020/9/8 有效线程数(workerCount)的定义是什么？ 

    //ctl：高3位保存线程池的运行状态(runState)，低29位保存线程池有效线程的数量(workerCount)
    private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
    //COUNT_BITS = 29
    private static final int COUNT_BITS = Integer.SIZE - 3;
    //CAPACITY = 1左移29位减1(29个1), 大约5亿多
    private static final int CAPACITY   = (1 << COUNT_BITS) - 1;

    // 线程池运行状态：runState is stored in the high-order bits
    private static final int RUNNING    = -1 << COUNT_BITS;
    private static final int SHUTDOWN   =  0 << COUNT_BITS;
    private static final int STOP       =  1 << COUNT_BITS;
    private static final int TIDYING    =  2 << COUNT_BITS;
    private static final int TERMINATED =  3 << COUNT_BITS;


    //根据ctl获取运行状态
    private static int runStateOf(int c)     { return c & ~CAPACITY; }
    //根据ctl获取活动线程数
    private static int workerCountOf(int c)  { return c & CAPACITY; }
    //获取运行状态和活动线程数的值
    private static int ctlOf(int rs, int wc) { return rs | wc; }

    ThreadPoolExecutor (
        int corePoolSize,   //核心线程数，
        int maximumPoolSize,  //最大线程数量
        long keepAliveTime,   //核心线程外的线程保持空闲的时间，空闲线程活着的时间，不被销毁
        TimeUnit unit,   //keepAliveTime的时间单位
        BlockingQueue<Runnable> workQueue,   //当线程数不够用的时候，新进来的任务就会放在此队列
        ThreadFactory threadFactory, //ThreadFactory类型的变量，用来创建新线程
        RejectedExecutionHandler handler  //拒绝策略
    )




    private boolean addWorker(Runnable firstTask, boolean core) {
        /*
         * 第1个for循环：用于判断线程池运行状态。
         *              线程状态异常则结束执行流程，不创建新线程
         *                  rs > SHUTDOWN，至少表示不再接受新任务也不处理阻塞队列中的任务，结束执行流程，不需要创建新线程
         *                  rs = SHUTDOWN && firstTask != null, 结束执行流程，不需要创建新线程
         *                  rs = SHUTDOWN && firstTask == null && 阻塞队列为空，结束执行流程，不需要创建新线程
         * 第2个for循环(在第1个for内部)：workCount工作线程的数量加1，通过cas操作加1，加1成功才创建线程
         *              内部会比较workCount工作线程数和coreSize、maxSize：workCount大于coreSize、maxSize，则结束流程，不需要创建新线程
         */
        retry:
        for (;;) {
            int c = ctl.get();
            // 获取运行状态
            int rs = runStateOf(c);

            /*
             * 这个if判断
             * 如果rs >= SHUTDOWN，则表示此时不再接收新任务；
             * 接着判断以下3个条件，只要有1个不满足，则返回false：
             * 1. rs == SHUTDOWN，这时表示关闭状态，不再接受新提交的任务，但却可以继续处理阻塞队列中已保存的任务
             * 2. firsTask为空
             * 3. 阻塞队列不为空
             *
             * 首先考虑rs == SHUTDOWN的情况
             * 这种情况下不会接受新提交的任务，所以在firstTask不为空的时候会返回false；
             * 然后，如果firstTask为空，并且workQueue也为空，则返回false，
             * 因为队列中已经没有任务了，不需要再添加线程了
             */
            // Check if queue empty only if necessary.
            if (rs >= SHUTDOWN &&
                    ! (rs == SHUTDOWN &&
                            firstTask == null &&
                            ! workQueue.isEmpty()))
                return false;

            for (;;) {
                // 获取线程数
                int wc = workerCountOf(c);
                // 如果wc超过CAPACITY，也就是ctl的低29位的最大值（二进制是29个1），返回false；
                // 这里的core是addWorker方法的第二个参数，如果为true表示根据corePoolSize来比较，
                // 如果为false则根据maximumPoolSize来比较。
                if (wc >= CAPACITY ||
                        wc >= (core ? corePoolSize : maximumPoolSize))
                    return false;
                // 尝试增加workerCount，如果成功，则跳出第一个for循环
                if (compareAndIncrementWorkerCount(c))
                    break retry;
                // 如果增加workerCount失败，则重新获取ctl的值
                c = ctl.get();  // Re-read ctl
                // 如果当前的运行状态不等于rs，说明状态已被改变，返回第一个for循环继续执行
                if (runStateOf(c) != rs)
                    continue retry;
                // else CAS failed due to workerCount change; retry inner loop
            }
        }


        /*
         * 以下代码执行流程为：
         *     创建Worker对象，Worker内部会创建Thread但不启动Thread
         *     将Worker对象添加到成员变量workers(HashSet)中，将workerAdded置为true; 成员变量largestPoolSize记录线程池中出现过的最大线程数量
         *     workerAdded为true，启动线程，并将workerStarted置为true
         */
        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
            // 根据firstTask来创建Worker对象
            w = new Worker(firstTask);
            // 每一个Worker对象都会创建一个线程
            final Thread t = w.thread;
            if (t != null) {
                final ReentrantLock mainLock = this.mainLock;
                mainLock.lock();
                try {
                    // Recheck while holding lock.
                    // Back out on ThreadFactory failure or if
                    // shut down before lock acquired.
                    int rs = runStateOf(ctl.get());
                    // rs < SHUTDOWN表示是RUNNING状态；
                    // 如果rs是RUNNING状态或者rs是SHUTDOWN状态并且firstTask为null，向线程池中添加线程。
                    // 因为在SHUTDOWN时不会在添加新的任务，但还是会执行workQueue中的任务
                    if (rs < SHUTDOWN ||
                            (rs == SHUTDOWN && firstTask == null)) {
                        if (t.isAlive()) // precheck that t is startable
                            throw new IllegalThreadStateException();
                        // workers是一个HashSet
                        workers.add(w);
                        int s = workers.size();
                        // 成员变量largestPoolSize记录着线程池中出现过的最大线程数量
                        if (s > largestPoolSize)
                            largestPoolSize = s;
                        workerAdded = true;
                    }
                } finally {
                    mainLock.unlock();
                }
                if (workerAdded) {
                    // 启动线程
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            if (! workerStarted)
                addWorkerFailed(w);
        }
        return workerStarted;
    }

    private final class Worker
            extends AbstractQueuedSynchronizer
            implements Runnable
    {
        private static final long serialVersionUID = 6138294804551838833L;

        /** Thread this worker is running in.  Null if factory fails. */
        final Thread thread;
        /** Initial task to run.  Possibly null. */
        Runnable firstTask;
        /** Per-thread task counter */
        volatile long completedTasks;

        /**
         * Creates with given first task and thread from ThreadFactory.
         * @param firstTask the first task (null if none)
         */
        Worker(Runnable firstTask) {
            setState(-1); // inhibit interrupts until runWorker
            this.firstTask = firstTask;
            this.thread = getThreadFactory().newThread(this);
        }

        /** Delegates main run loop to outer runWorker  */
        public void run() {
            runWorker(this);
        }

        // Lock methods
        //
        // The value 0 represents the unlocked state.
        // The value 1 represents the locked state.

        protected boolean isHeldExclusively() {
            return getState() != 0;
        }

        protected boolean tryAcquire(int unused) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        protected boolean tryRelease(int unused) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        public void lock()        { acquire(1); }
        public boolean tryLock()  { return tryAcquire(1); }
        public void unlock()      { release(1); }
        public boolean isLocked() { return isHeldExclusively(); }

        void interruptIfStarted() {
            Thread t;
            if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                }
            }
        }
    }


    /**
     * 执行流程：
     * (1) 先执行firstTask，然后while循环不断地通过getTask()方法获取任务；
     * (2) getTask()方法从阻塞队列中取任务；
     * (3) 如果线程池正在停止，那么要保证当前线程是中断状态，否则要保证当前线程不是中断状态；
     * (4) 调用task.run()执行任务；
     * (5) 如果task为null则跳出循环，执行processWorkerExit()方法；
     * (6) runWorker方法执行完毕，也代表着Worker中的run方法执行完毕，销毁线程。
     * @param w
     */
    final void runWorker(ThreadPoolExecutor.Worker w) {
        Thread wt = Thread.currentThread();
        // 获取第一个任务
        Runnable task = w.firstTask;
        w.firstTask = null;
        // 允许中断, 将state置为0
        w.unlock(); // allow interrupts
        // 是否因为异常退出循环
        boolean completedAbruptly = true;
        try {
            // 如果task为空，则通过getTask从队列来获取任务
            while (task != null || (task = getTask()) != null) {
                w.lock();
                // If pool is stopping, ensure thread is interrupted;
                // if not, ensure thread is not interrupted.  This
                // requires a recheck in second case to deal with
                // shutdownNow race while clearing interrupt
                /*
                 * 如果线程池正在停止，那么要保证当前线程是中断状态；
                 * 如果不是的话，则要保证当前线程不是中断状态；
                 */
                if ((runStateAtLeast(ctl.get(), STOP) ||
                        (Thread.interrupted() &&
                                runStateAtLeast(ctl.get(), STOP))) &&
                        !wt.isInterrupted())
                    wt.interrupt();
                try {
                    beforeExecute(wt, task);
                    Throwable thrown = null;
                    try {
                        task.run();
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {
                        afterExecute(task, thrown);
                    }
                } finally {
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                }
            }
            completedAbruptly = false;
        } finally {
            processWorkerExit(w, completedAbruptly);
        }
    }

    /**
     * 执行流程
     * (1) "线程池状态为SHUTDOWN 并且 队列为空" 或者 "线程池状态为STOP"，则工作线程数workerCount减1并返回null
     * (2) timed用于判断是否允许超时，"allowCoreThreadTimeOut || wc > corePoolSize"则timed为true
     * (3) 如果通过setMaximumPoolSize方法设置导致wc > maximumPoolSize，那么将工作线程数workerCount减1，并返回null
     *     如果上次从队列获取任务超时并且队列为空了，那么将工作线程数workerCount减1，并返回null
     * (4) 从队列获取任务
     *     根据timed来判断，如果为true，则通过阻塞队列的poll方法进行超时控制，如果在keepAliveTime时间内没有获取到任务，则返回null；
     *     否则通过take方法，如果这时队列为空，则take方法会阻塞直到队列不为空。
     * @return
     */
    private Runnable getTask() {
        // timeOut变量的值表示上次从阻塞队列中取任务时是否超时
        boolean timedOut = false; // Did the last poll() time out?

        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);

            // Check if queue empty only if necessary.
            /*
             * 如果线程池状态rs >= SHUTDOWN，也就是非RUNNING状态，再进行以下判断：
             * 1. rs >= STOP，线程池是否正在stop；
             * 2. 阻塞队列是否为空。
             * 如果以上条件满足，则将workerCount减1并返回null。
             * 因为如果当前线程池状态的值是SHUTDOWN或以上时，不允许再向阻塞队列中添加任务。
             *
             * simple：
             * "线程池状态为SHUTDOWN 并且 队列为空" 或者 "线程池状态为STOP"，则工作线程数workerCount减1并返回null。
             */
            if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
                //工作线程数workerCount减1并返回null。
                decrementWorkerCount();
                return null;
            }

            int wc = workerCountOf(c);

            // Are workers subject to culling?
            /*
             * timed变量用于判断是否需要进行超时控制。
             * allowCoreThreadTimeOut默认是false，也就是核心线程不允许进行超时；
             * wc > corePoolSize，表示当前线程池中的线程数量大于核心线程数量；
             * 对于超过核心线程数量的这些线程，需要进行超时控制
             *
             * simple：timed用于判断是否允许超时，"allowCoreThreadTimeOut || wc > corePoolSize"则timed为true
             */
            boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;

            /*
             * wc > maximumPoolSize的情况是因为可能在此方法执行阶段同时执行了setMaximumPoolSize方法；
             * timed && timedOut 如果为true，表示当前操作需要进行超时控制，并且上次从阻塞队列中获取任务发生了超时
             * 接下来判断，如果有效线程数量大于1，或者阻塞队列是空的，那么尝试将workerCount减1；
             * 如果减1失败，则返回重试。
             * 如果wc == 1时，也就说明当前线程是线程池中唯一的一个线程了。
             *
             * simple：
             *  如果通过setMaximumPoolSize方法设置导致wc > maximumPoolSize，那么将工作线程数workerCount减1，并返回null
             *  如果上次从队列获取任务超时并且队列为空了，那么将工作线程数workerCount减1，并返回null
             */
            if ((wc > maximumPoolSize || (timed && timedOut))
                    && (wc > 1 || workQueue.isEmpty())) {
                if (compareAndDecrementWorkerCount(c))
                    return null;
                continue;
            }

            try {
                /*
                 * 根据timed来判断，如果为true，则通过阻塞队列的poll方法进行超时控制，如果在keepAliveTime时间内没有获取到任务，则返回null；
                 * 否则通过take方法，如果这时队列为空，则take方法会阻塞直到队列不为空。
                 */
                Runnable r = timed ?
                        workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                        workQueue.take();
                if (r != null)
                    return r;
                // 如果 r == null，说明已经超时，timedOut设置为true
                timedOut = true;
            } catch (InterruptedException retry) {
                // 如果获取任务时当前线程发生了中断，则设置timedOut为false并返回循环重试
                timedOut = false;
            }
        }
    }

    /**
     * 执行流程：
     * (1) completedAbruptly值为true，说明线程执行时出现了异常，workerCount需要减1; 为false，已经在getTask()中减1了
     * (2) 销毁当前线程：将worker从HashSet中移除
     * (3) tryTerminate()：根据线程池状态进行判断是否结束线程池
     * @param w
     * @param completedAbruptly
     */
    private void processWorkerExit(ThreadPoolExecutor.Worker w, boolean completedAbruptly) {
        // 如果completedAbruptly值为true，则说明线程执行时出现了异常，需要将workerCount减1；
        // 如果线程执行时没有出现异常，说明在getTask()方法中已经已经对workerCount进行了减1操作，这里就不必再减了。
        if (completedAbruptly) // If abrupt, then workerCount wasn't adjusted
            decrementWorkerCount();

        //加锁是因为HashSet线程不安全
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            //统计完成的任务数，赋值给成员变量completedTaskCount
            completedTaskCount += w.completedTasks;
            // 从workers(HashSet)中移除，也就表示着从线程池中移除了一个工作线程
            workers.remove(w);
        } finally {
            mainLock.unlock();
        }

        // 根据线程池状态进行判断是否结束线程池
        tryTerminate();

        int c = ctl.get();
        /*
         * 当线程池是RUNNING或SHUTDOWN状态时，如果worker是异常结束，那么会直接addWorker；
         * 如果allowCoreThreadTimeOut=true，并且等待队列有任务，至少保留一个worker；
         * 如果allowCoreThreadTimeOut=false，workerCount不少于corePoolSize。
         */
        if (runStateLessThan(c, STOP)) {
            if (!completedAbruptly) {
                int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
                if (min == 0 && ! workQueue.isEmpty())
                    min = 1;
                if (workerCountOf(c) >= min)
                    return; // replacement not needed
            }
            addWorker(null, false);
        }
    }

    /**
     * 执行流程：tryTerminate方法根据线程池状态进行判断是否结束线程池
     * 先将线程的状态设置为TIDYING，然后调用terminated()后(什么也不做，留给子类实现)，再将线程的状态设置为TERMINATED
     * (1) 以下情况不结束线程池，直接返回
     *      RUNNING，因为还在运行中，不能停止；
     *      TIDYING或TERMINATED，因为线程池中已经没有正在运行的线程了；
     *      SHUTDOWN并且等待队列非空，这时要执行完workQueue中的task；
     * (2) 走到此处，线程池的运行状态为：
     *      SHUTDOWN 并且 队列为空
     *      STOP
     * (3) 工作线程数大于0，则执行interruptIdleWorkers，然后返回，不再执行后续流程
     * (4) 执行结束线程池操作
     *      先将线程的状态设置为TIDYING，然后调用terminated()后(什么也不做，留给子类实现)，再将线程的状态设置为TERMINATED
     */
    final void tryTerminate() {
        for (;;) {
            int c = ctl.get();
            /*
             * 当前线程池的状态为以下几种情况时，直接返回：
             * 1. RUNNING，因为还在运行中，不能停止；
             * 2. TIDYING或TERMINATED，因为线程池中已经没有正在运行的线程了；
             * 3. SHUTDOWN并且等待队列非空，这时要执行完workQueue中的task；
             */
            if (isRunning(c) ||
                    runStateAtLeast(c, TIDYING) ||
                    (runStateOf(c) == SHUTDOWN && ! workQueue.isEmpty()))
                return;

            /*
             * 走到下面，线程池的运行状态为：
             * 1. SHUTDOWN 并且 队列为空
             * 2. STOP
             */

            // 如果线程数量不为0，则中断一个空闲的工作线程，并返回
            if (workerCountOf(c) != 0) { // Eligible to terminate
                interruptIdleWorkers(ONLY_ONE);
                return;
            }

            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                // 这里尝试设置状态为TIDYING，如果设置成功，则调用terminated方法
                if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
                    try {
                        // terminated方法默认什么都不做，留给子类实现
                        terminated();
                    } finally {
                        // 设置状态为TERMINATED
                        ctl.set(ctlOf(TERMINATED, 0));
                        termination.signalAll();
                    }
                    return;
                }
            } finally {
                mainLock.unlock();
            }
            // else retry on failed CAS
        }
    }

    /**
     * 中断一个空闲的线程
     *      遍历workers(HashSet)中所有的工作线程，若线程处于空闲状态tryLock成功，就中断该线程。
     * @param onlyOne
     */
    private void interruptIdleWorkers(boolean onlyOne) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (ThreadPoolExecutor.Worker w : workers) {
                Thread t = w.thread;
                if (!t.isInterrupted() && w.tryLock()) {
                    try {
                        t.interrupt();
                    } catch (SecurityException ignore) {
                    } finally {
                        w.unlock();
                    }
                }
                if (onlyOne)
                    break;
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * shutdown方法要将线程池切换到SHUTDOWN状态，并调用interruptIdleWorkers方法请求中断所有空闲的worker，最后调用tryTerminate尝试结束线程池。
     */
    public void shutdown() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            // 安全策略判断
            checkShutdownAccess();
            // 切换状态为SHUTDOWN
            advanceRunState(SHUTDOWN);
            // 中断所有的空闲线程
            interruptIdleWorkers();
            onShutdown(); // hook for ScheduledThreadPoolExecutor
        } finally {
            mainLock.unlock();
        }
        // 尝试结束线程池
        tryTerminate();
    }

    public List<Runnable> shutdownNow() {
        List<Runnable> tasks;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            // 安全策略判断
            checkShutdownAccess();
            // 切换状态为STOP
            advanceRunState(STOP);
            // 中断所有工作线程，无论是否空闲
            interruptWorkers();
            // 取出队列中没有被执行的任务
            tasks = drainQueue();
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
        return tasks;
    }
}


