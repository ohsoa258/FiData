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

    @Test
    public void test() throws Exception {
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
}
