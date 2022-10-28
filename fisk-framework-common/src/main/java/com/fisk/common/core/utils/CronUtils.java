package com.fisk.common.core.utils;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.Dto.cron.NextCronTimeDTO;
import com.fisk.common.framework.exception.FkException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.TriggerUtils;
import org.quartz.impl.triggers.CronTriggerImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author JianWenYang
 */
@Slf4j
public class CronUtils {

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

    /**
     * 获取下次cron执行时间
     *
     * @param dto
     * @return
     */
    public static List<String> nextCronExeTime(NextCronTimeDTO dto) {
        //校验cron格式是否正确
        if (StringUtils.isEmpty(dto.cronExpression) || !isValidExpression(dto.cronExpression)) {
            throw new FkException(ResultEnum.CRON_ERROR);
        }
        List<String> data = new ArrayList<>();
        try {
            CronTriggerImpl cronTriggerImpl = new CronTriggerImpl();
            //这里写要准备猜测的cron表达式
            cronTriggerImpl.setCronExpression(dto.cronExpression);
            Calendar calendar = Calendar.getInstance();
            Date now = calendar.getTime();
            //把统计的区间段设置为从现在到2年后的今天（主要是为了方法通用考虑，如那些1个月跑一次的任务，如果时间段设置的较短就不足20条)
            calendar.add(Calendar.YEAR, 2);
            //这个是重点，一行代码搞定~~
            List<Date> dates = TriggerUtils.computeFireTimesBetween(cronTriggerImpl, null, now, calendar.getTime());
            System.out.println(dates.size());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < dates.size(); i++) {
                //这个是提示的日期个数
                if (i >= dto.number) {
                    break;
                }
                data.add(dateFormat.format(dates.get(i)));
            }
        } catch (ParseException e) {
            log.error("nextCronExeTime ex:{}", e);
        }
        return data;
    }

}
