package com.fisk.datamanagement.synchronization.fidata;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author JianWenYang
 */
@Component
public class SynchronizationSchedule implements SchedulingConfigurer {

    @Resource
    SynchronizationPgData synchronizationPgData;
    @Resource
    SynchronizationPgKinShip synchronizationPgKinShip;

    @Value("${spring.schedule}")
    private String cron;
    @Value("${scheduling.enabled}")
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
                   //同步元数据对象
                   synchronizationPgData.synchronizationPgData();
                   //同步元数据对象血缘
                   synchronizationPgKinShip.synchronizationKinShip();
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
