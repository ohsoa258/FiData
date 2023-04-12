package com.fisk.datagovernance.dto.dataquality.businessfilter;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗编辑DTO
 * @date 2022/3/24 13:48
 */
@EqualsAndHashCode(callSuper = true)
public class BusinessFilterEditDTO extends BusinessFilterDTO {
    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    public int id;
}
