package com.fisk.common.server.ocr.dto.businessmetadata;

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
public class TableRuleInfoDTO {
    /**
     * 利益相关方
     */
    @ApiModelProperty(value = "利益相关方")
    public List<String> stakeholders;

    /**
     * 表/字段 ID
     */
    @ApiModelProperty(value = "表/字段 ID")
    public String tableFieldUnique;

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
     * 业务名称
     */
    @ApiModelProperty(value = "接入应用名称/建模业务名称")
    public String businessName;

    /**
     * 更新规则
     */
    @ApiModelProperty(value = "更新规则")
    public List<String> updateRules;

    /**
     * 转换规则
     */
    @ApiModelProperty(value = "转换规则")
    public String transformationRules;

    /**
     * 已知数据问题
     */
    @ApiModelProperty(value = "已知数据问题")
    public String knownDataProblem;

    /**
     * 使用说明
     */
    @ApiModelProperty(value = "使用说明")
    public String directionsForUse;

    /**
     * 有效值约束
     */
    @ApiModelProperty(value = "有效值约束")
    public List<String> validValueConstraint;

    /**
     * 数据责任部门
     */
    @ApiModelProperty(value = "数据责任部门")
    public String dataResponsibilityDepartment;

    /**
     * 数据责任人
     */
    @ApiModelProperty(value = "数据责任人")
    public String dataResponsiblePerson;

    public TableRuleInfoDTO() {
        checkRules = new ArrayList<>();
        filterRules = new ArrayList<>();
        lifecycleRules = new ArrayList<>();
        noticeRules = new ArrayList<>();
        updateRules = new ArrayList<>();
        validValueConstraint = new ArrayList<>();
        stakeholders = new ArrayList<>();
    }

    /**
     * 表字段规则
     */
    public List<TableRuleInfoDTO> fieldRules;
}
