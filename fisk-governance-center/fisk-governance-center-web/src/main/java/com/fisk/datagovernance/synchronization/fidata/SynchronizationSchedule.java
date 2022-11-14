package com.fisk.datagovernance.synchronization.fidata;

import com.fisk.datagovernance.service.impl.dataops.DataOpsDataSourceManageImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author dick
 */
@EnableScheduling
@Component
@Slf4j
public class SynchronizationSchedule implements SchedulingConfigurer {

    @Resource
    DataOpsDataSourceManageImpl dataOpsDataSourceManageImpl;

    @Value("${dataops.schedule}")
    private String cron;
    @Value("${dataops.enabled}")
    private boolean enabled;

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addTriggerTask(doTask(), getTrigger());
    }

    private Runnable doTask() {
        return new Runnable() {
            @Override
            public void run() {
                // 业务逻辑
               if (enabled)
               {
                   log.info("数据运维写入pg数据源到redis 开始");
                   //数据运维写入数据源到redis
                   dataOpsDataSourceManageImpl.setDataOpsDataSource_v1();
                   log.info("数据运维写入pg数据源到redis 结束");
               }
            }
        };
    }

    private Trigger getTrigger() {
        return new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                // 触发器
                CronTrigger trigger = new CronTrigger(getCron());
                return trigger.nextExecutionTime(triggerContext);
            }
        };
    }

    public String getCron() {
        return cron;
    }

}
