package com.fisk.mdm.job;

import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
import com.fisk.mdm.service.IModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    @Scheduled(cron = "${timed.setDataStructure}")
    public void Test1(){
        FiDataMetaDataReqDTO dto = new FiDataMetaDataReqDTO();
        dto.setDataSourceId("3");
        modelService.setDataStructure(dto);
    }
}
