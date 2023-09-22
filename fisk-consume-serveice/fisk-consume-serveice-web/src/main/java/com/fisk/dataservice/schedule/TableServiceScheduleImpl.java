package com.fisk.dataservice.schedule;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
import com.fisk.dataservice.service.ITableService;
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

@EnableScheduling
@Component
@Slf4j
public class TableServiceScheduleImpl implements SchedulingConfigurer {

    @Value("${table-service-job.schedule}")
    private String cron;
    @Value("${table-service-job.enabled}")
    private boolean enabled;
    @Resource
    private ITableService tableService;

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addTriggerTask(doTask(), getTrigger());
    }

    private Runnable doTask() {
        return new Runnable() {
            @Override
            public void run() {
                // 业务逻辑
                if (enabled) {
                    log.info("数据分发服务写入数据结构到redis: 开始");
                    loadFiDataDataServiceData();
                    log.info("数据分发服务写入数据结构到redis: 结束");
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

    /**
     * 加载数据接入结构,并存入redis
     *
     * @description 加载数据接入结构, 并存入redis
     * @author Lock
     * @date 2022/6/20 14:55
     */
    private void loadFiDataDataServiceData() {
        try {
            FiDataMetaDataReqDTO reqDto = new FiDataMetaDataReqDTO();
            // 2: ods数据源
            reqDto.setDataSourceId("-19");
            reqDto.setDataSourceName("数据分发服务");
                tableService.setDataServiceStructure(reqDto);
        } catch (Exception e) {
            throw new FkException(ResultEnum.LOAD_FIDATA_DATA_SERVICE_DATA_ERROR, e);
        }
    }
}
