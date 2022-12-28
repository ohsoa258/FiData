package com.fisk.common.core.utils.threadPool;

import java.util.concurrent.*;

/**
 * @author gy
 * @version 1.0
 * @description 线程池帮助类
 * @date 2022/12/28 11:53
 */
public class ThreadPoolUtils {

    public static ExecutorService buildThreadPool() {
        return buildThreadPool(3, 10, 30, 10);
    }

    /**
     * 根据配置创建线程池
     * 创建的线程池核心线程为？个，任务队列为10，任务队列满了之后再有新任务会创建额外线程。
     * 最大线程数量为？个。如果线程池任务队列超过最大值之后,并且已经开启到最大线程数时，拒绝创建新任务
     * 使用默认线程工厂模式。
     *
     * @param coreNum       核心线程数
     * @param maxNum        最大线程数
     * @param keepAliveTime 线程存活时长
     * @param maxTask       最大任务数
     * @return 线程池
     */
    public static ExecutorService buildThreadPool(int coreNum, int maxNum, int keepAliveTime, int maxTask) {
        return new ThreadPoolExecutor(coreNum,
                maxNum,
                keepAliveTime,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(maxTask),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }
}
