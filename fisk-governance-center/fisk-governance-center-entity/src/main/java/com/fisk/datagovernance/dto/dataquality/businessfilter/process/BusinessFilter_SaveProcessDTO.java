package com.fisk.datagovernance.dto.dataquality.businessfilter.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class BusinessFilter_SaveProcessDTO {
    /**
     * tb_bizfilter_rule表主键ID
     */
    @ApiModelProperty(value = "tb_bizfilter_rule表主键ID")
    public int ruleId;

    /**
     * 工作区流程任务列表
     */
    @ApiModelProperty(value = "工作区流程任务列表")
    public List<BusinessFilter_ProcessTaskDTO> processTaskList;
}
