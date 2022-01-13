package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description 参数 DTO
 * @date 2022/1/6 14:51
 */
@Data
public class ParmConfigDTO
{
    /**
     * 参数名称
     */
    @ApiModelProperty(value = "参数名称")
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    public String parmName;

    /**
     * 参数描述
     */
    @ApiModelProperty(value = "参数描述")
    @Length(min = 0, max = 255, message = "长度最多255")
    public String parmDesc;

    /**
     * 参数值
     */
    @ApiModelProperty(value = "参数值")
    @Length(min = 0, max = 50, message = "长度最多50")
    public String parmValue;
}
