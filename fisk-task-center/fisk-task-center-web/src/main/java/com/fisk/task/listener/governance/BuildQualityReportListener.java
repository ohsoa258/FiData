package com.fisk.task.listener.governance;

import com.alibaba.fastjson.JSON;
import com.fisk.datagovernance.client.DataQualityClient;
import com.fisk.task.dto.datagovernance.QualityReportDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author dick
 * @version 1.0
 * @description 数据质量-质量报告
 * @date 2022/4/12 11:11
 */
@Component
@Slf4j
public class BuildQualityReportListener {
    @Resource
    DataQualityClient dataQualityClient;

    public void msg(String dataInfo, Acknowledgment acke) {
        log.info("质量报告消费开始");
        log.info("质量报告消费参数-dataInfo:" + dataInfo);
        if (StringUtils.isEmpty(dataInfo)){
            log.info("质量报告消费参数异常");
            return;
        }
        QualityReportDTO requestDTO = JSON.parseObject(dataInfo, QualityReportDTO.class);
        if (requestDTO == null || requestDTO.getId() == 0) {
            log.info("质量报告消费参数异常");
            return;
        }
        try {
            dataQualityClient.createQualityReport(requestDTO.getId());
        } catch (Exception ex) {
            log.error("质量报告消费异常:" + ex);
        }
    }
}
