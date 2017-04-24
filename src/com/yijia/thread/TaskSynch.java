package com.yijia.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class TaskSynch {
    /**
     * 同步信号量
     */
//    private static final Semaphore semaphore = new Semaphore(1);

    /**
     * 每次同步的时间间隔 单位：秒
     */
    public static final int time = 60;
    /**
     * 定时执行
     */
    private static ScheduledExecutorService scheduler = null;

    private TaskSynch() {
    }

    /**
     * 定时操作线程
     */
    private final static Runnable TodoOperation = new Runnable() {
        public void run() {
            	new AutoSynchThread().start();
        }
    };

    /**
     * 启动定时器
     */
    public static void start() {

        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            synchronized (scheduler) {
                scheduler.scheduleAtFixedRate(TodoOperation, 0, time, TimeUnit.SECONDS);
                System.out.println("启动TODO定时器!");
            }

        } else {
            System.out.println("TODO定时器正在运行!");
        }

    }

    /**
     * 停止定时器
     */
    public static void stop() {
        synchronized (scheduler) {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                scheduler = null;
                System.out.println("关闭TODO定时器!");
            }
        }
    }

    /**
     * 重启定时器
     */
    public static void restart() {
        stop();
        start();
    }

    /**
     * 手动触发
     */
    public static void trigger() {
        TodoOperation.run();
    }
}
