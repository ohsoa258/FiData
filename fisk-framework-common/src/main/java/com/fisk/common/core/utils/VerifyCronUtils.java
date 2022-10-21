package com.fisk.common.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.quartz.impl.triggers.CronTriggerImpl;

import java.util.Date;

/**
 * @author JianWenYang
 */
@Slf4j
public class VerifyCronUtils {

    /**
     * 校验cron格式是否正确
     *
     * @param cronExpression
     * @return
     */
    public static boolean isValidExpression(String cronExpression) {
        CronTriggerImpl trigger = new CronTriggerImpl();
        try {
            trigger.setCronExpression(cronExpression);
            Date date = trigger.computeFirstFireTime(null);
            return date != null && date.after(new Date());
        } catch (Exception e) {
            log.error("[VerifyCronUtils.isValidExpression]:failed. throw ex:", e);
        }
        return false;
    }

}
