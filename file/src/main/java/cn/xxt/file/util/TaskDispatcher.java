package cn.xxt.file.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 信号量控制网络并发量：多任务，将每个任务，加入到控制器中
 *
 * final TaskDispatcher td = TaskDispatcher.getInstance();
   td.doTask(new Runnable() {
        @Override
        public void run() {
            //do work
        }
   });
 *
 * Created by zyj on 2017/9/21.
 */

public class TaskDispatcher {
    /** 任务队列 */
    private LinkedList<Runnable> mTaskList;
    /** 线程池 */
    private ThreadPoolExecutor mThreadPool;
    /** 轮询线程 */
    private Thread mPollingThead;
    /** 轮询线程中的Handler */
    private Handler mPollingHanler;
    /** 线程池的线程数量，默认为3 */
    private static int mThreadCount = 3;
    private int maximumPoolSize = 20;
    private long keepAliveTime = 30;
    /** 队列的调度方式，默认为LIFO */
    private Type mType = Type.FIFO;
    /** 信号量，由于线程池内部也有一个阻塞线程，若加入任务的速度过快，LIFO效果不明显 */
    private volatile Semaphore mPollingSemaphore;
    /** 信号量，防止mPoolThreadHander未初始化完成 */
    private volatile Semaphore mSemaphore = new Semaphore(0);

    private static TaskDispatcher mInstance;

    public enum Type { FIFO, LIFO }

    /**
     * 单例获得实例对象
     * @return   实例对象
     */
    public static TaskDispatcher getInstance() {
        if (mInstance == null) {
            synchronized (TaskDispatcher.class) {
                if (mInstance == null) {
                    mInstance = new TaskDispatcher(mThreadCount, Type.FIFO);
                }
            }
        }
        return mInstance;
    }

    /**
     * 单例获得实例对象
     * @param threadCount    线程池的线程数量
     * @param type           队列的调度方式
     * @return   实例对象
     */
    public static TaskDispatcher getInstance(int threadCount, Type type) {
        if (mInstance == null) {
            synchronized (TaskDispatcher.class) {
                if (mInstance == null) {
                    mInstance = new TaskDispatcher(threadCount, type);
                }
            }
        }
        return mInstance;
    }

    /**
     * 构造函数
     * @param threadCount    线程池的线程数量
     * @param type           队列的调度方式
     */
    private TaskDispatcher(int threadCount, Type type) {
        init(threadCount, type);
    }

    /**
     * 初始化
     * @param threadCount    线程池的线程数量
     * @param type           队列的调度方式
     */
    private void init(int threadCount, Type type) {

        mThreadPool = new ThreadPoolExecutor(threadCount,
                maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>());
        mPollingSemaphore = new Semaphore(threadCount);
        mTaskList = new LinkedList<Runnable>();
        if (type == null) {
            mType = Type.LIFO;
        } else {
            mType = type;
        }

        // 开启轮询线程
        mPollingThead = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mPollingHanler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        try {
                            Thread.sleep(600);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mThreadPool.submit(getTask());
                        try {
                            mPollingSemaphore.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                mSemaphore.release();   // 释放一个信号量
                Looper.loop();
            }
        };
        mPollingThead.start();
    }

    /**
     * 添加一个任务
     * @param task   任务
     */
    private synchronized void addTask(Runnable task) {
        try {
            // mPollingHanler为空时，请求信号量，因为mPollingHanler创建完成会释放一个信号量
            if (mPollingHanler == null) {
                mSemaphore.acquire();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!mTaskList.contains(task)) {
            mTaskList.add(task);
        }

        mPollingHanler.sendEmptyMessage(0x110);
    }

    /**
     * 取出一个任务
     * @return 需要执行的任务
     */
    private synchronized Runnable getTask() {
        if (mType == Type.LIFO) {
            return mTaskList.removeLast();
        } else if (mType == Type.FIFO) {
            return mTaskList.removeFirst();
        }
        return null;
    }

    /**
     * 执行自定义的任务
     */
    public void doTask(Runnable runnable) {
        addTask(runnable);
    }

    public void releaseSemaphore() {
        mPollingSemaphore.release();
    }
}
