package com.fisk.dataservice.vo.api;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description api VO
 * @date 2022/1/6 14:51
 */
public class ApiRegisterDetailVO {
    /**
     * api信息
     */
    @ApiModelProperty(value = "api信息")
    public ApiConfigVO apiVO;

    /**
     * 字段列表
     */
    @ApiModelProperty(value = "字段列表")
    public List<FieldConfigVO> fieldVO;

    /**
     * 条件列表
     */
    @ApiModelProperty(value = "条件列表")
    public List<FilterConditionConfigVO> whereVO;

    /**
     * 参数列表
     */
    @ApiModelProperty(value = "参数列表")
    public List<ParmConfigVO> parmVO;
}
