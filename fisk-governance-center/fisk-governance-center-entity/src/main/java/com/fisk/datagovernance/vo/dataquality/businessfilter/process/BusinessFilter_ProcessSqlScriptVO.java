package com.fisk.datagovernance.vo.dataquality.businessfilter.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BusinessFilter_ProcessSqlScriptVO {
    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    public int id;

    /**
     * tb_bizfilter_rule表主键ID
     */
    @ApiModelProperty(value = "tb_bizfilter_rule表主键ID")
    public int ruleId;

    /**
     * tb_bizfilter_process_task表task_code
     */
    @ApiModelProperty(value = "tb_bizfilter_process_task表task_code")
    public String taskCode;

    /**
     * sql脚本
     */
    @ApiModelProperty(value = "sql脚本")
    public String sqlScript;

    /**
     * 自定义描述
     */
    @ApiModelProperty(value = "自定义描述")
    public String customDescribe;
}
