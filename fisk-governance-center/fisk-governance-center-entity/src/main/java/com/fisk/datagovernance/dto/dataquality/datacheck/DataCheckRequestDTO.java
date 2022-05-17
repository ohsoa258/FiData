package com.fisk.datagovernance.dto.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验请求DTO
 * @date 2022/5/16 20:44
 */
public class DataCheckRequestDTO {
    /**
     * 表名称
     */
    @ApiModelProperty(value = "验证的表名称")
    @NotNull()
    public String tableName;

    /**
     * 验证的内容，json格式
     */
    @ApiModelProperty(value = "验证的内容，json格式")
    public String body;
}
