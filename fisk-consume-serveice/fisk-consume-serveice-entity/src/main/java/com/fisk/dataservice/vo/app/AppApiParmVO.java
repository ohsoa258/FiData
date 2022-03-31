package com.fisk.dataservice.vo.app;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version v1.0
 * @description 应用订阅API内置参数 VO
 * @date 2022/1/10 17:51
 */
public class AppApiParmVO
{
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

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

    /**
     * 是否是内置参数 1是、0否
     */
    @ApiModelProperty(value = "是否是内置参数 1是、0否")
    public int parmIsbuiltin;
}
