package com.fisk.dataaccess.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Lock
 * @version 1.3
 * @description 生成pdf文档实体类
 * @date 2022/2/22 16:40
 */
@Data
public class GenerateDocDTO {

    @ApiModelProperty(value = "实时apiId",required = true)
    @NotNull()
    public Long apiId;
}
