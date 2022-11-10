package com.fisk.mdm.job;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
import com.fisk.mdm.service.IModelService;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author WangYan
 * @Date 2022/6/22 10:58
 * @Version 1.0
 */
@Slf4j
@Component
public class TimedTask {

    @Autowired
    IModelService modelService;

    @Resource
    UserClient userClient;

    @Scheduled(cron = "${timed.setDataStructure}")
    public void setDataStructure(){
        // 开始日志
        log.info("------------【" + DateTimeUtils.getNow() + "】" + ResultEnum.MANDATE_TIMESTAMP_START.getMsg() +
                "------------");

        ResultEntity<DataSourceDTO> result = userClient.getFiDataDataSourceById(3);
        if (result.code == ResultEnum.SUCCESS.getCode()) {

            FiDataMetaDataReqDTO reqDto = new FiDataMetaDataReqDTO();
            // 3: mdm数据源
            reqDto.setDataSourceId(String.valueOf(result.data.id));
            reqDto.setDataSourceName(result.data.conDbname);
            // 刷新主数据结构
            modelService.setDataStructure(reqDto);
        }

        // 结束日志
        log.info("------------【" + DateTimeUtils.getNow() + "】" + ResultEnum.MANDATE_TIMESTAMP_SUCCESS.getMsg() +
                "------------");
    }
}
