package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.enums.DataSourceTypeEnum;
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
import java.util.List;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/5/31 16:59
 */
@EnableScheduling
@Component
@Slf4j
public class DataAccessScheduleImpl implements SchedulingConfigurer {

    @Value("${data-access-job.schedule}")
    private String cron;
    @Value("${data-access-job.enabled}")
    private boolean enabled;
    @Resource
    private AppDataSourceImpl dataSourceImpl;
    @Resource
    private AppRegistrationImpl appRegistrationImpl;

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
                    log.info("数据接入写入数据结构到redis: 开始");
                    loadFiDataMetaData();
                    log.info("数据接入写入数据结构到redis: 结束");
                    log.info("数据接入写入物理表功能的表及视图详情到redis: 开始");
                    // 重新加载所有数据源以及数据库、表数据,并存入redis
                    loadDataSourceMeta();
                    log.info("数据接入写入物理表功能的表及视图详情到redis: 结束");
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
     * 重新加载所有数据源以及数据库、表数据,并存入redis
     *
     * @description 重新加载所有数据源以及数据库、表数据
     * @author Lock
     * @date 2022/5/31 17:04
     */
    private void loadDataSourceMeta() {
        try {
            List<AppDataSourcePO> dataSourcePoList = dataSourceImpl.query()
                    .eq("drive_type", DataSourceTypeEnum.MYSQL.getName())
                    .or()
                    .eq("drive_type", DataSourceTypeEnum.SQLSERVER.getName())
                    .or()
                    .eq("drive_type", DataSourceTypeEnum.ORACLE.getName())
                    .list();
            if (CollectionUtils.isNotEmpty(dataSourcePoList)) {
                dataSourcePoList.forEach(e -> dataSourceImpl.setDataSourceMeta(e.appId));
            }
        } catch (Exception e) {
            throw new FkException(ResultEnum.LOAD_DATASOURCE_META, e);
        }
    }

    /**
     * 加载数据接入结构,并存入redis
     *
     * @description 加载数据接入结构, 并存入redis
     * @author Lock
     * @date 2022/6/20 14:55
     */
    private void loadFiDataMetaData() {
        try {
            FiDataMetaDataReqDTO reqDto = new FiDataMetaDataReqDTO();
            // 2: ods数据源
            reqDto.setDataSourceId("2");
            appRegistrationImpl.setDataAccessStructure(reqDto);
        } catch (Exception e) {
            throw new FkException(ResultEnum.LOAD_FIDATA_METADATA_ERROR, e);
        }
    }
}
