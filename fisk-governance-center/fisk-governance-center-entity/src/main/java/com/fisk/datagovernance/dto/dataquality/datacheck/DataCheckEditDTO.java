package com.fisk.datagovernance.dto.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验编辑DTO
 * @date 2022/3/24 13:31
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DataCheckEditDTO extends DataCheckDTO
{
    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    public int id;
}
