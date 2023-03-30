package com.fisk.datagovernance.vo.dataquality.businessfilter.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BusinessFilter_ProcessFieldRuleVO {
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
     * tb_bizfilter_process_express表主键ID
     * 或者
     * tb_bizfilter_process_field_assign表主键ID
     */
    @ApiModelProperty(value = "tb_bizfilter_process_express表主键ID\n" +
            "或者\n" +
            "tb_bizfilter_process_field_assign表主键ID")
    public int businessId;

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
