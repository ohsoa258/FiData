package com.fisk.datagovernance.vo.dataquality.businessfilter.filterresult;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗结果VO
 * @date 2023/3/30 15:22
 */
@Data
public class BusinessFilterResultVO {
    /**
     * 规则ID
     */
    @ApiModelProperty(value = "规则ID")
    public int ruleId;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    public int ruleName;

    /**
     * 组件ID
     */
    @ApiModelProperty(value = "组件ID")
    public int assemblyId;

    /**
     * 组件名称
     */
    @ApiModelProperty(value = "组件名称")
    public int assemblyName;

    /**
     * 任务code
     */
    @ApiModelProperty(value = "任务code")
    public String taskCode;

    /**
     * 任务名称
     */
    @ApiModelProperty(value = "任务名称")
    public String taskName;

    /**
     * 清洗的表名称
     */
    @ApiModelProperty(value = "清洗的表名称")
    public String tableName;

    /**
     * 清洗结果
     */
    @ApiModelProperty(value = "清洗结果")
    public boolean filterResult;

    /**
     * 清洗数据数量
     */
    @ApiModelProperty(value = "清洗数据数量")
    public int filterCount;
}
