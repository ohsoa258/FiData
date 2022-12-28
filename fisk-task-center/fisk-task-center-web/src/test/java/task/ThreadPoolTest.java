package task;

import com.fisk.task.server.TaskThreadPool;
import org.junit.Test;

/**
 * @author gy
 * @version 1.0
 * @description TODO
 * @date 2022/12/28 12:27
 */
public class ThreadPoolTest {

    /**
     * 测试 任务超过上限，继续添加任务是否报错
     */
    @Test
    public void test_max_limit() throws Exception {
        for (int i = 0; i < 25; i++) {
            int finalI = i;
            TaskThreadPool.taskPool.submit(() -> {
                System.out.printf("任务：%s，线程id：{%s}%n", finalI, Thread.currentThread().getId());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            Thread.sleep(100);
        }
        Thread.sleep(10000);
    }

    /**
     * 测试
     */
    @Test
    public void test() throws Exception {
        TaskThreadPool.taskPool.submit(() -> {
            System.out.printf("任务：%s，线程id：{%s}%n", 1, Thread.currentThread().getId());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        TaskThreadPool.taskPool.submit(() -> {
            System.out.printf("任务：%s，线程id：{%s}%n", 2, Thread.currentThread().getId());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        TaskThreadPool.taskPool.submit(() -> {
            System.out.printf("任务：%s，线程id：{%s}%n", 3, Thread.currentThread().getId());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread.sleep(10000);
    }
}
