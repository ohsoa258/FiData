package com.fisk.task.listener.governance;

import com.alibaba.fastjson.JSON;
import com.fisk.datagovernance.enums.dataquality.DataQualityRequestDTO;
import com.fisk.datagovernance.enums.dataquality.TemplateModulesTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验-强规则模板消费类
 * @date 2022/4/12 11:11
 */
@Component
@Slf4j
public class BuildFieldStrongTaskListener {
    public void msg(String dataInfo, Acknowledgment acke) {
        log.info("执行 数据治理-数据质量-数据校验-强规则模板消费类");
        log.info("dataInfo:" + dataInfo);
        DataQualityRequestDTO requestDTO= JSON.parseObject(dataInfo,DataQualityRequestDTO.class);
        if (requestDTO==null || requestDTO.getId()==0
                || requestDTO.getTemplateModulesType()!= TemplateModulesTypeEnum.DATACHECK_MODULE){
            log.info("强规则模板消费参数异常");
            return;
        }

    }
}
