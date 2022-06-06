package com.fisk.datagovernance.dto.dataquality;

import com.fisk.datagovernance.enums.dataquality.ModuleTypeEnum;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description Kafka数据质量请求DTO
 * @date 2022/4/12 11:05
 */
@Data
public class DataQualityRequestDTO {

    /**
     * id标识
     */
    public int id;

    /**
     * 模块类型
     */
    public ModuleTypeEnum moduleTypeEnum;
}