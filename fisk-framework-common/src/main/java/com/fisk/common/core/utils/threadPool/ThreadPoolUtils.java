package com.fisk.common.core.utils.threadPool;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author gy
 * @version 1.0
 * @description 线程池帮助类
 * @date 2022/12/28 11:53
 */
@Slf4j
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
        log.info(
                String.format("线程池创建，参数：【核心线程数：%s】【最大线程数：%s】【线程存活时长：%ss】【最大任务排队数：%s】【线程拒绝策略：%s】",
                        maxNum, maxTask, keepAliveTime, maxTask, "线程池任务队列超过最大值之后,并且已经开启到最大线程数时，拒绝创建新任务")
        );
        return new ThreadPoolExecutor(coreNum,
                maxNum,
                keepAliveTime,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(maxTask),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }
}
