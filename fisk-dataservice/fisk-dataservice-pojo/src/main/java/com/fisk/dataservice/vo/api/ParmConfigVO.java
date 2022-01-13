package com.fisk.dataservice.vo.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 参数 DTO
 * @date 2022/1/6 14:51
 */
@Data
public class ParmConfigVO
{
    /**
     * Id
     */
    @ApiModelProperty(value = "主键")
    public int id;

    /**
     * apiId
     */
    @ApiModelProperty(value = "apiId")
    public Integer apiId;

    /**
     * 参数名称
     */
    @ApiModelProperty(value = "参数名称")
    public String parmName;

    /**
     * 参数描述
     */
    @ApiModelProperty(value = "参数描述")
    public String parmDesc;

    /**
     * 参数值
     */
    @ApiModelProperty(value = "参数值")
    public String parmValue;
}
