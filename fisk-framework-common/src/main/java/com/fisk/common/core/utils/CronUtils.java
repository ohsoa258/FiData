package com.fisk.common.core.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronSequenceGenerator;

/**
 * @author dick
 * @version 1.0
 * @description Cron 表达式工具类
 * @date 2022/4/24 19:36
 */
@Slf4j
public class CronUtils {

    /**
     * @return java.lang.String
     * @description 获取cron表达式下次执行时间
     * @author dick
     * @date 2022/4/24 19:48
     * @version v1.0
     * @params jobCronExpress
     */
    public static String getCronExpress(String jobCronExpress) {
        String dateString = null;
        try {
            CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(jobCronExpress);
            Date now = new Date();
            // 任务下次执行时间
            Date nextTime = cronSequenceGenerator.next(now);
            SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateString = dataFormat.format(nextTime);
        } catch (Exception ex) {
            log.error("【getCronExpress error】：" + ex);
            dateString = "cron表达式解析异常";
        }
        return dateString;
    }

}
