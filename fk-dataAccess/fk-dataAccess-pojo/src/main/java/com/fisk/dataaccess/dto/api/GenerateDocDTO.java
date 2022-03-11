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

    @ApiModelProperty(value = "api下的物理表json结构")
    public String pushDataJson;

    @ApiModelProperty(value = "true: 当前api下存在表;false: 当前api下没有表")
    public boolean tableIsEmpty;
}
