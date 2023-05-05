package com.fisk.datamodel.dto.datadomain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/12 17:17
 * 业务过程 二级
 */
@Data
public class BusinessProcessNameDTO {

    @ApiModelProperty(value = "业务进程Id")
    public Long businessProcessId;

    @ApiModelProperty(value = "业务进程中文名")
    public String businessProcessCnName;
    /**
     * 事实表 三级
     */
    @ApiModelProperty(value = "事实表 三级")
    public List<FactNameDTO> factList;
    /**
     * 3.业务域 4.业务流程  5.事实表
     */
    @ApiModelProperty(value = "3.业务域 4.业务流程  5.事实表")
    public Integer flag;
    /**
     * 业务域的id
     */
    @ApiModelProperty(value = "业务域的id")
    public Long pid;
}
