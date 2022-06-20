package com.fisk.datagovernance.vo.dataquality.rule;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据质量规则信息
 * @date 2022/6/16 21:28
 */
@Data
public class TableRuleInfoVO {
    public TableRuleInfoVO(){
        checkRules=new ArrayList<>();
        filterRules=new ArrayList<>();
        lifecycleRules=new ArrayList<>();
        noticeRules=new ArrayList<>();
    }
    /**
     * 表/字段 名称
     */
    @ApiModelProperty(value = "表/字段 名称")
    public String name;

    /**
     * 1：表 2：字段
     */
    public int type;

    /**
     * 校验规则
     */
    @ApiModelProperty(value = "校验规则")
    public List<String> checkRules;

    /**
     * 清洗规则，目前只针对表设置
     */
    @ApiModelProperty(value = "清洗规则，目前只针对表设置")
    public List<String> filterRules;

    /**
     * 生命周期，目前只针对表设置
     */
    @ApiModelProperty(value = "生命周期，目前只针对表设置")
    public List<String> lifecycleRules;

    /**
     * 告警设置
     */
    @ApiModelProperty(value = "告警设置")
    public List<String> noticeRules;

    /**
     * 表字段规则
     */
    public List<TableRuleInfoVO> fieldRules;
}
