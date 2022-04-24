package com.fisk.common.core.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import org.springframework.scheduling.support.CronSequenceGenerator;

/**
 * @author dick
 * @version 1.0
 * @description Cron 表达式工具类
 * @date 2022/4/24 19:36
 */
public class CronUtils {

    /**
     * @description 获取cron表达式下次执行时间
     * @author dick
     * @date 2022/4/24 19:48
     * @version v1.0
     * @params jobCronExpress
     * @return java.lang.String
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
            throw new FkException(ResultEnum.ERROR, "【getCronExpress】：" + ex);
        }
        return dateString;
    }

}
