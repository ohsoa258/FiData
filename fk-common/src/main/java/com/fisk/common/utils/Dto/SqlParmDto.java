package com.fisk.common.utils.Dto;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description 请求参数拼接sql DTO
 * @date 2022/1/16 14:12
 */
public class SqlParmDto {
    /**
     * 参数名称
     */
    public String parmName;

    /**
     * 参数值
     */
    public String parmValue;
}
