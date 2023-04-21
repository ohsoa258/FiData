package com.fisk.datamodel.dto.datadomain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/9/10 14:40
 * 业务过程 二级
 */
@Data
public class BusinessProcessDimDTO {
    @ApiModelProperty(value = "业务进程Id")
    public Long businessProcessId;

    @ApiModelProperty(value = "业务进程中午名称")
    public String businessProcessCnName;
    /**
     * 事实表 三级
     */
    @ApiModelProperty(value = "事实列表")
    public List<FactDimDTO> factList;

    /**
     * 7.业务域  8.维度  9.业务过程  10.事实表
     */
    @ApiModelProperty(value = "7.业务域  8.维度  9.业务过程  10.事实表")
    public Integer flag;
    /**
     * 上一级id
     */
    @ApiModelProperty(value = "上一级id")
    public Long pid;
}
