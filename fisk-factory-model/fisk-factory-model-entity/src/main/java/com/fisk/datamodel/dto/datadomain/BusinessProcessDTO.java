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
public class BusinessProcessDTO {

    @ApiModelProperty(value = "业务进程Id")
    public Long businessProcessId;

    @ApiModelProperty(value = "业务进程中文名")
    public String businessProcessCnName;
    /**
     * 事实表 三级
     */
    @ApiModelProperty(value = "事实表 三级")
    public List<FactDTO> factList;

    /**
     * 是否维度 0 否  1 是维度
     */
    @ApiModelProperty(value = "是否维度 0 否  1 是维度")
    public int dimension;
}
