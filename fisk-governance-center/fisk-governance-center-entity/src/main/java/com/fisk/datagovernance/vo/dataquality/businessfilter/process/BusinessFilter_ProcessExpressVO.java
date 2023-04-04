package com.fisk.datagovernance.vo.dataquality.businessfilter.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class BusinessFilter_ProcessExpressVO {
    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    public int id;

    /**
     * 数据标识
     */
    @ApiModelProperty(value = "数据标识")
    public String dataCode;

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
     * 表达式规则
     */
    @ApiModelProperty(value = "表达式规则")
    public List<BusinessFilter_ProcessFieldRuleVO> expressRuleList;

    /**
     * 表达式规则之间逻辑关系
     */
    @ApiModelProperty(value = "表达式规则之间逻辑关系")
    public String expressRuleRelation;

    /**
     * 表达式预览文本
     */
    @ApiModelProperty(value = "表达式预览文本")
    public String expressPreviewText;

    /**
     * 表达式SQL
     */
    @ApiModelProperty(value = "表达式SQL")
    public String expressSql;

    /**
     * 自定义描述
     */
    @ApiModelProperty(value = "自定义描述")
    public String customDescribe;
}
