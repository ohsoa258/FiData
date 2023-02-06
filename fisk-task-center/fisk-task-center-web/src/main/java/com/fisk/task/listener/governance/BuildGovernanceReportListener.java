package com.fisk.task.listener.governance;

import com.alibaba.fastjson.JSON;
import com.fisk.datagovernance.client.DataGovernanceClient;
import com.fisk.task.dto.datagovernance.ReportDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author dick
 * @version 1.0
 * @description 数据治理消费类
 * @date 2022/4/12 11:11
 */
@Component
@Slf4j
public class BuildGovernanceReportListener {
    @Resource
    DataGovernanceClient dataGovernanceClient;

    public void dataQuality_QualityReport_Msg(String dataInfo, Acknowledgment acke) {
        log.info("数据质量质量报告消费开始");
        log.info("数据质量质量报告消费参数-dataInfo:" + dataInfo);
        if (StringUtils.isEmpty(dataInfo)){
            log.info("数据质量质量报告消费参数异常");
            return;
        }
        ReportDTO requestDTO = JSON.parseObject(dataInfo, ReportDTO.class);
        if (requestDTO == null || requestDTO.getId() == 0) {
            log.info("数据质量质量报告消费参数异常");
            return;
        }
        try {
            dataGovernanceClient.createQualityReport(requestDTO.getId());
        } catch (Exception ex) {
            log.error("数据质量质量报告消费异常:" + ex);
        }
    }

    public void dataSecurity_IntelligentDiscoveryReport_Msg(String dataInfo, Acknowledgment acke) {
        log.info("数据安全智能发现报告消费开始");
        log.info("数据安全智能发现报告消费参数-dataInfo:" + dataInfo);
        if (StringUtils.isEmpty(dataInfo)){
            log.info("数据安全智能发现报告消费参数异常");
            return;
        }
        ReportDTO requestDTO = JSON.parseObject(dataInfo, ReportDTO.class);
        if (requestDTO == null || requestDTO.getId() == 0) {
            log.info("数据安全智能发现报告消费参数异常");
            return;
        }
        try {
            dataGovernanceClient.createScanReport(requestDTO.getId());
        } catch (Exception ex) {
            log.error("数据安全智能发现报告消费异常:" + ex);
        }
    }
}
