package com.fisk.datamodel.service.impl;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
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
 * @author Lock
 * @version 2.6
 * @description
 * @date 2022/6/20 15:31
 */
@EnableScheduling
@Component
@Slf4j
public class DataModelScheduleImpl implements SchedulingConfigurer {

    @Value("${data-model-job.schedule}")
    private String cron;
    @Value("${data-model-job.enabled}")
    private boolean enabled;
    @Resource
    private BusinessAreaImpl businessAreaImpl;

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
                    log.info("数据建模写入数据结构到redis: 开始");
                    loadFiDataMetaData();
                    log.info("数据接入写入数据结构到redis: 结束");
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
     * 加载数据建模结构,并存入redis
     *
     * @return void
     * @description 加载数据接入结构, 并存入redis
     * @author Lock
     * @date 2022/6/20 14:55
     * @version v1.0
     * @params
     */
    private void loadFiDataMetaData() {
        try {
            FiDataMetaDataReqDTO reqDto = new FiDataMetaDataReqDTO();
            // 1: dw数据源
            reqDto.setDataSourceId("1");
            businessAreaImpl.setDataModelStructure(reqDto);
            // 4: olap数据源
            reqDto.setDataSourceId("4");
            businessAreaImpl.setDataModelStructure(reqDto);
        } catch (Exception e) {
            throw new FkException(ResultEnum.LOAD_FIDATA_METADATA_ERROR, e);
        }
    }

}
