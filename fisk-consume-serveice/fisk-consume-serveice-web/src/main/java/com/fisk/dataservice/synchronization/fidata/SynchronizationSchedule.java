package com.fisk.dataservice.synchronization.fidata;

import com.fisk.dataservice.service.impl.DataSourceConManageImpl;
import com.fisk.dataservice.service.impl.IATVServiceAnalyseImpl;
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
    DataSourceConManageImpl dataSourceConManageImpl;

    @Resource
    IATVServiceAnalyseImpl atvServiceAnalyse;

    @Value("${dataservice.datasource.schedule}")
    private String cron;
    @Value("${dataservice.datasource.enabled}")
    private boolean enabled;

    @Value("${dataservice.scan.schedule}")
    private String scanCron;
    @Value("${dataservice.scan.enabled}")
    private boolean scanEnabled;

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addTriggerTask(doTask(), getTrigger());
        scheduledTaskRegistrar.addTriggerTask(scanDoTask(), getScanTrigger());
    }

    private Runnable scanDoTask() {
        return new Runnable() {
            @Override
            public void run() {
                // 业务逻辑
                if (scanEnabled) {
                    // 扫描数据服务是否熔断
                    atvServiceAnalyse.scanDataServiceApiIsFuSing();
                }
            }
        };
    }

    private Trigger getScanTrigger() {
        return new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                // 触发器
                CronTrigger trigger = new CronTrigger(getScanCron());
                return trigger.nextExecutionTime(triggerContext);
            }
        };
    }

    private Runnable doTask() {
        return new Runnable() {
            @Override
            public void run() {
                // 业务逻辑
                if (enabled) {
                    //写入数据源到redis
                    dataSourceConManageImpl.setMetaDataToRedis();
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

    public String getScanCron() {
        return scanCron;
    }

    public String getCron() {
        return cron;
    }
}
