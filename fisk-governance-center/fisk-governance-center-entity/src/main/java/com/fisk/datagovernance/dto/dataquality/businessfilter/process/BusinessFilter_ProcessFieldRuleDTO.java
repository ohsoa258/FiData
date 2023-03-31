package com.fisk.datagovernance.dto.dataquality.businessfilter.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BusinessFilter_ProcessFieldRuleDTO {
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
     * tb_bizfilter_process_express表数据标识
     * 或者
     * tb_bizfilter_process_field_assign表数据标识
     */
    @ApiModelProperty(value = "tb_bizfilter_process_express表数据标识\n" +
            "或者\n" +
            "tb_bizfilter_process_field_assign表数据标识")
    public String fkDataCode;

    /**
     * 随机名称
     */
    @ApiModelProperty(value = "随机名称")
    public String randomName;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    public String fieldName;

    /**
     * 字段符号
     */
    @ApiModelProperty(value = "字段符号")
    public String fieldSymbol;

    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型")
    public String fieldType;

    /**
     * 字段值
     */
    @ApiModelProperty(value = "字段值")
    public String fieldValue;
}
