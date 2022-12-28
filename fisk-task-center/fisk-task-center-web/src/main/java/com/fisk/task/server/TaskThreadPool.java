package com.fisk.task.server;

import com.fisk.common.core.utils.threadPool.ThreadPoolUtils;

import java.util.concurrent.ExecutorService;

/**
 * @author gy
 * @version 1.0
 * @description 线程池
 * @date 2022/12/28 12:25
 */
public class TaskThreadPool {

    public final static ExecutorService TASK_POOL;

    static {
        TASK_POOL = ThreadPoolUtils.buildThreadPool();
    }
}
