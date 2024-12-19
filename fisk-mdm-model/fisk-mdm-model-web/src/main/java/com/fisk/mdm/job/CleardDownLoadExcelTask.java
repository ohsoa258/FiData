package com.fisk.mdm.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @Author: wangjian
 * @Date: 2024-12-18
 * @Description:
 */
@Slf4j
@Component
public class CleardDownLoadExcelTask {

    @Value("${downloadpath}")
    private String filePath;

    // 每天凌晨1点执行任务
    @Scheduled(cron = "0 0 1 * * ?")
    public void doTask() {
        // 获取文件夹路径
        File directory = new File(filePath);

        // 判断文件夹是否存在
        if (directory.exists() && directory.isDirectory()) {
            // 获取目录下的所有文件
            File[] files = directory.listFiles((dir, name) -> name.endsWith(".xlsx"));

            // 如果有符合条件的文件
            if (files != null && files.length > 0) {
                for (File file : files) {
                    try {
                        // 删除文件
                        boolean deleted = file.delete();
                        if (deleted) {
                            log.info("文件删除成功: {}", file.getName());
                        } else {
                            log.warn("文件删除失败: {}", file.getName());
                        }
                    } catch (Exception e) {
                        log.error("删除文件时发生错误: {}", file.getName(), e);
                    }
                }
            } else {
                log.info("目录中未找到 .xlsx 文件。");
            }
        } else {
            log.warn("指定的目录不存在或不是有效的目录: {}", filePath);
        }
    }
}
